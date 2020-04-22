/*
 * Kontalk client common library
 * Copyright (C) 2020 Kontalk Devteam <devteam@kontalk.org>

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kontalk.client;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.parsing.SmackParsingException;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;

import java.io.IOException;


/**
 * Requests upload info.
 * @author Daniele Ricci
 */
public class UploadInfo extends IQ {
    public static final String ELEMENT_NAME = "upload";
    public static final String NAMESPACE  = UploadExtension.NAMESPACE;

    private final String mNode;
    private final String mMime;
    private final String mUri;

    public UploadInfo(String node) {
        this(node, null);
    }

    public UploadInfo(String node, String mime) {
        this(node, mime, null);
    }

    public UploadInfo(String node, String mime, String uri) {
        super(ELEMENT_NAME, NAMESPACE);
        mNode = node;
        mMime = mime;
        mUri = uri;
    }

    public String getNode() {
        return mNode;
    }

    public String getMime() {
        return mMime;
    }

    public String getUri() {
        return mUri;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute("node", mNode);
        if (mMime != null) {
            xml.rightAngleBracket()
                .halfOpenElement("media")
                .attribute("type", mMime)
                .closeEmptyElement();
        }
        else {
            xml.setEmptyElement();
        }

        return xml;
    }

    public static final class Provider extends IQProvider<UploadInfo> {

        @Override
        public UploadInfo parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment)
                throws XmlPullParserException, IOException, SmackParsingException {
            boolean done = false, in_uri = false;
            int depth = parser.getDepth();
            String node, mime = null, uri = null;

            // node is in the <upload/> element
            node = parser.getAttributeValue(null, "node");

            while (!done) {
                XmlPullParser.Event eventType = parser.next();

                if (eventType == XmlPullParser.Event.START_ELEMENT) {
                    if ("media".equals(parser.getName()) && depth >= 0) {
                        if (parser.getDepth() == (depth + 1)) {
                            mime = parser.getAttributeValue(null, "type");
                        }
                    }
                    else if ("uri".equals(parser.getName()) && depth >= 0) {
                        if (parser.getDepth() == (depth + 1)) {
                            in_uri = true;
                        }
                    }
                }
                else if (eventType == XmlPullParser.Event.TEXT_CHARACTERS) {
                    if (in_uri) {
                        uri = parser.getText();
                        in_uri = false;
                    }
                }
                else if (eventType == XmlPullParser.Event.END_ELEMENT) {
                    if (ELEMENT_NAME.equals(parser.getName())) {
                        done = true;
                    }
                }
            }

            if (node != null)
                return new UploadInfo(node, mime, uri);
            else
                return null;
        }
    }

}
