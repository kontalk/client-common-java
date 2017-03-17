/*
 * Kontalk client common library
 * Copyright (C) 2017 Kontalk Devteam <devteam@kontalk.org>

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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.NamedElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jxmpp.util.XmppDateTime;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * Implementation of the (new) OpenPGP extension element (XEP-0373: OpenPGP for XMPP).
 *
 * Only <signcrypt/> content elements are suppported.
 *
 * @author Alexander Bikadorov {@literal <bikaejkb@mail.tu-berlin.de>}
 */
public class OpenPGPExtension implements ExtensionElement {
    public static final String ELEMENT_NAME = "openpgp";
    public static final String NAMESPACE = "urn:xmpp:openpgp:0";

    private final String mBase64Data;

    public OpenPGPExtension(String base64Data) {
        mBase64Data = base64Data;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    public String getData() {
        return mBase64Data;
    }

    @Override
    public String toXML() {
        return new XmlStringBuilder()
                .halfOpenElement(ELEMENT_NAME)
                .xmlnsAttribute(NAMESPACE)
                .rightAngleBracket()
                .escape(mBase64Data)
                .closeElement(ELEMENT_NAME).toString();
    }

    public static class Provider extends ExtensionElementProvider<OpenPGPExtension> {

        @Override
        public OpenPGPExtension parse(XmlPullParser parser, int initialDepth)
                throws XmlPullParserException, IOException, SmackException {
            String base64Data = parser.nextText();
            return new OpenPGPExtension(base64Data);
        }

    }

    /**
     * SignCrypt element.
     * This is the signed and encrypted content element of an {@link OpenPGPExtension} element.
     * {@link OpenPGPExtension} element.
     */
    public static class SignCryptElement implements ExtensionElement {
        public static final String ELEMENT_NAME = "signcrypt";
        public static final String NAMESPACE = "urn:xmpp:openpgp:0";

        private final List<String> mJIDs;
        private final Date mStamp;
        private final int mRPadLength;
        private final List<ExtensionElement> mPayloadExtensions;

        public SignCryptElement(List<String> jids, Date timeStamp, int rpadLength,
                                List<ExtensionElement> payloadExtensions) {
            if (jids.isEmpty())
                throw new IllegalArgumentException("Need at least one 'to' jid");

            mJIDs = jids;
            mStamp = timeStamp;
            mRPadLength = rpadLength;
            mPayloadExtensions = payloadExtensions;
        }

        @Override
        public String getElementName() {
            return ELEMENT_NAME;
        }

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }

        public List<String> getJIDs() {
            return mJIDs;
        }

        public Date getTimeStamp() {
            return mStamp;
        }

        public List<ExtensionElement> getPayload() {
            return mPayloadExtensions;
        }

        @Override
        public CharSequence toXML() {
            XmlStringBuilder buf = new XmlStringBuilder()
                    .halfOpenElement(ELEMENT_NAME).xmlnsAttribute(NAMESPACE).rightAngleBracket();
            for (String jid : mJIDs)
                buf.halfOpenElement("to").attribute("jid", jid).closeEmptyElement();
            buf.halfOpenElement("time")
                    .attribute("stamp", XmppDateTime.formatXEP0082Date(mStamp))
                    .closeEmptyElement();
            if (mRPadLength > 0)
                buf.openElement("rpad")
                        .append(StringUtils.randomString(mRPadLength))
                        .closeElement("rpad");
            buf.openElement("payload");
            for (NamedElement payloadElements : mPayloadExtensions)
                buf.append(payloadElements.toXML());
            return buf.closeElement("payload")
                    .closeElement(ELEMENT_NAME).toString();
        }

        public static SignCryptElement parse(String decryptedPayload)
                throws XmlPullParserException, IOException, SmackException, ParseException {

            List<String> jids = new ArrayList<>();
            Date date = null;
            List<ExtensionElement> content = new ArrayList<>();

            XmlPullParser parser = PacketParserUtils.getParserFor(decryptedPayload);
            if (!ELEMENT_NAME.equals(parser.getName()) || !NAMESPACE.equals(parser.getNamespace()))
                throw new ParseException("not a signcrypt element", 0);

            boolean done = false;
            final int initialDepth = parser.getDepth();
            while (!done) {
                int eventType = parser.next();

                if(eventType == XmlPullParser.END_DOCUMENT)
                    throw new SmackException("invalid XML schema");

                if (eventType == XmlPullParser.START_TAG) {
                    String s = parser.getName();
                    switch(s) {
                        case "to":
                            String jid = parser.getAttributeValue(null, "jid");
                            if (jid != null)
                                jids.add(jid);
                            break;
                        case "time":
                            String stamp = parser.getAttributeValue(null, "stamp");
                            date = XmppDateTime.parseXEP0082Date(stamp);
                            break;
                        case "payload":
                            int payloadDepth = parser.getDepth();
                            boolean in_payload = true;
                            while (in_payload) {
                                eventType = parser.next();
                                switch (eventType) {
                                    case XmlPullParser.START_TAG:
                                        String elementName = parser.getName();
                                        String namespace = parser.getNamespace();

                                        content.add(PacketParserUtils.parseExtensionElement(
                                                elementName, namespace, parser));
                                        break;
                                    case XmlPullParser.END_TAG:
                                        if (parser.getDepth() == payloadDepth) {
                                            in_payload = false;
                                        }
                                        break;
                                }
                            }
                            break;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getDepth() == initialDepth) {
                        done = true;
                    }
                }
            }

            if (jids.isEmpty() || date == null) {
                throw new ParseException("invalid signcrypt elment", 0);
            }

            return new SignCryptElement(jids, date, -1, content);
        }
    }

    /** Used as payload element. Smack has only the unusable class Message.Body. */
    public static class BodyElement implements ExtensionElement {
        public static final String ELEMENT_NAME = "body";
        public static final String NAMESPACE = "jabber:client";

        private final String mText;

        public BodyElement(String text) {
            mText = text;
        }

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public String getElementName() {
            return ELEMENT_NAME;
        }

        public String getText() {
            return mText;
        }

        @Override
        public CharSequence toXML() {
            return new XmlStringBuilder()
                    .halfOpenElement(ELEMENT_NAME)
                    .xmlnsAttribute(NAMESPACE)
                    .rightAngleBracket()
                    .escape(mText)
                    .closeElement(ELEMENT_NAME).toString();
        }

        public static class Provider extends ExtensionElementProvider<BodyElement> {
            @Override
            public BodyElement parse(XmlPullParser parser, int initialDepth)
                    throws XmlPullParserException, IOException, SmackException {
                String text = parser.nextText();
                return new BodyElement(text);
            }
        }

    }
}
