/*
 * Kontalk client common library
 * Copyright (C) 2018 Kontalk Devteam <devteam@kontalk.org>

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

import java.io.IOException;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * XEP-0363: HTTP File Upload
 * @author Daniele Ricci
 */
public class HTTPFileUpload {

    public static final String NAMESPACE = "urn:xmpp:http:upload";

    public static class Request extends IQ {
        public static final String ELEMENT_NAME = "request";

        private final String mFilename;
        private final long mSize;
        private final String mContentType;

        public Request(String filename, long size) {
            this(filename, size, null);
        }

        public Request(String filename, long size, String contentType) {
            super(ELEMENT_NAME, NAMESPACE);
            this.mFilename = filename;
            this.mSize = size;
            this.mContentType = contentType;
        }

        @Override
        protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
            xml.rightAngleBracket()
                .element("filename", mFilename)
                .element("size", String.valueOf(mSize));

            if (mContentType != null)
                xml.element("content-type", mContentType);

            return xml;
        }
    }

    public static class Slot extends IQ {
        public static final String ELEMENT_NAME = "slot";

        private final String mPutUrl;
        private final String mGetUrl;

        public Slot(String putUrl, String getUrl) {
            super(ELEMENT_NAME, NAMESPACE);
            this.mPutUrl = putUrl;
            this.mGetUrl = getUrl;
        }

        public String getPutUrl() {
            return mPutUrl;
        }

        public String getGetUrl() {
            return mGetUrl;
        }

        @Override
        protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
            xml.rightAngleBracket()
                .element("put", mPutUrl)
                .element("get", mGetUrl);
            return xml;
        }

        public static final class Provider extends IQProvider<Slot> {

            @Override
            public Slot parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {
                boolean done = false, in_get_uri = false, in_put_uri = false;
                String getUri = null, putUri = null;

                while (!done) {
                    int eventType = parser.next();

                    if (eventType == XmlPullParser.START_TAG) {
                        if ("put".equals(parser.getName())) {
                            in_put_uri = true;
                        }
                        else if ("get".equals(parser.getName())) {
                            in_get_uri = true;
                        }
                    }
                    else if (eventType == XmlPullParser.TEXT) {
                        if (in_put_uri) {
                            putUri = parser.getText();
                        }
                        else if (in_get_uri) {
                            getUri = parser.getText();
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG) {
                        if (ELEMENT_NAME.equals(parser.getName())) {
                            done = true;
                        }
                        else if ("put".equals(parser.getName())) {
                            in_put_uri = false;
                        }
                        else if ("get".equals(parser.getName())) {
                            in_get_uri = false;
                        }
                    }
                }

                if (getUri != null && putUri != null)
                    return new Slot(putUri, getUri);
                else
                    return null;
            }
        }

    }

}
