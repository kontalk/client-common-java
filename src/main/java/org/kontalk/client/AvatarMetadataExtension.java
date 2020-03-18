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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Avatar metadata extension (XEP-0084).
 * To be used as payload of a PubSub item.
 *
 * @author Alexander Bikadorov {@literal <bikaejkb@mail.tu-berlin.de>}
 */
public class AvatarMetadataExtension implements ExtensionElement {
    public static final String ELEMENT_NAME = "metadata";
    public static final String NAMESPACE = "urn:xmpp:avatar:metadata";

    private final List<Info> mInfos;

    public AvatarMetadataExtension(List<Info> infos) {
        mInfos = infos;
    }

    public List<Info> getInfos() {
        return mInfos;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public XmlStringBuilder toXML(String enclosingNamespace) {
        XmlStringBuilder builder = new XmlStringBuilder()
                .halfOpenElement(ELEMENT_NAME)
                .xmlnsAttribute(NAMESPACE)
                .rightAngleBracket();
        for (Info info : mInfos) {
            builder.append(info.toXML());
        }
        builder.closeElement(ELEMENT_NAME);
        return builder;
    }

    /** A metadata info element. */
    public static class Info {
        private final String mId; // REQUIRED
        private final int mBytes; // REQUIRED
        private final String mType; // REQUIRED
        private final int mWidth; // RECOMMENDED
        private final int mHeight; // RECOMMENDED
        private final String mUrl; // OPTIONAL

        public Info(String id, int bytes, String type, int width, int height) {
            this(id, bytes, type, width, height, "");
        }

        private Info(String id, int bytes, String type, int width, int height, String url) {
            mId = id;
            mBytes = bytes;
            mType = type;
            mWidth = width;
            mHeight = height;
            mUrl = url;
        }

        public String getId() {
            return mId;
        }

        public int getBytes() {
            return mBytes;
        }

        public String getType() {
            return mType;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        public String getUrl() {
            return mUrl;
        }

        public String toXML() {
            XmlStringBuilder buf = new XmlStringBuilder()
                .halfOpenElement("info")
                .attribute("id", mId)
                .attribute("bytes", mBytes)
                .attribute("type", mType);
            if (mWidth > -1)
                buf.attribute("width", mWidth);
            if (mHeight > -1)
                buf.attribute("height", mHeight);
            if (!mType.isEmpty())
                buf.attribute("type", mType);
            if (!mUrl.isEmpty())
                buf.attribute("url", mType);
            return buf.closeEmptyElement().toString();
        }

        private static Info parse(XmlPullParser parser) {
            String id = parser.getAttributeValue(null, "id");
            int bytes = parseInt(parser, "bytes");
            String type = parser.getAttributeValue(null, "type");
            int width = parseInt(parser, "width");
            int height = parseInt(parser, "height");
            String url = parser.getAttributeValue(null, "url");
            if (url == null)
                url = "";

            if (id != null && bytes > -1 && type != null)
                return new Info(id, bytes, type, width, height, url);

            return null;
        }

        private static int parseInt(XmlPullParser parser, String att) {
            String value = parser.getAttributeValue(null, att);
            try {
                return Integer.parseInt(value);
            }
            catch (NumberFormatException ignored) {
            }

            return -1;
        }
    }

    public static final class Provider extends ExtensionElementProvider<AvatarMetadataExtension> {

        @Override
        public AvatarMetadataExtension parse(XmlPullParser parser, int initialDepth)
                throws XmlPullParserException, IOException, SmackException {
            boolean done = false;

            List<Info> infos = new LinkedList<>();

            while (!done) {
                int eventType = parser.next();

                if(eventType == XmlPullParser.END_DOCUMENT)
                    throw new SmackException("invalid XML schema");

                if (eventType == XmlPullParser.START_TAG &&
                        "info".equals(parser.getName())) {
                    Info info = Info.parse(parser);
                    if (info != null)
                        infos.add(info);
                }

                else if (eventType == XmlPullParser.END_TAG &&
                        ELEMENT_NAME.equals(parser.getName())) {
                    done = true;
                }
            }

            return new AvatarMetadataExtension(infos);
        }
    }

}
