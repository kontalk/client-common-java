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

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * XEP-0189: Public Key Publishing
 * http://xmpp.org/extensions/xep-0189.html#request
 */
public class PublicKeyPublish extends IQ {

    public static final String NAMESPACE = "urn:xmpp:pubkey:2";
    public static final String ELEMENT_NAME = "pubkey";

    private byte[] mPublicKey;

    private PublicKeyPublish(IQ.Type type, byte[] publicKey) {
        super(ELEMENT_NAME, NAMESPACE);
        setType(type);
        mPublicKey = publicKey;
    }

    public PublicKeyPublish() {
        // default IQ with type get
        this(IQ.Type.get, null);
    }

    public byte[] getPublicKey() {
        return mPublicKey;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if (mPublicKey != null) {
            xml.rightAngleBracket()
                .append(Base64.encodeToString(mPublicKey));
        }
        else {
            xml.setEmptyElement();
        }

        return xml;
    }

    public static final class Provider extends IQProvider<PublicKeyPublish> {

        @Override
        public PublicKeyPublish parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
            boolean done = false;
            String key = null;

            while (!done) {
                int eventType = parser.next();

                if (eventType == XmlPullParser.TEXT) {
                    key = parser.getText();
                }
                else if (eventType == XmlPullParser.END_TAG) {
                    if (ELEMENT_NAME.equals(parser.getName())) {
                        done = true;
                    }
                }
            }

            if (key != null)
                return new PublicKeyPublish(IQ.Type.result, Base64.decode(key));
            else
                return null;
        }

    }

}
