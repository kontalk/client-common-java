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
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Kontalk account managament extension to jabber:iq:register query.
 * @author Daniele Ricci
 */
public class Account implements ExtensionElement {
    public static final String NAMESPACE = "http://kontalk.org/protocol/register#account";
    // child of query
    public static final String ELEMENT_NAME = "account";

    private String mPrivateKeyToken;
    private byte[] mPrivateKeyData;
    private byte[] mPublicKeyData;

    public Account() {
    }

    public Account(String privateKeyData, String publicKeyData) {
        this();
        mPrivateKeyData = Base64.decode(privateKeyData);
        mPublicKeyData = Base64.decode(publicKeyData);
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
    public CharSequence toXML() {
        XmlStringBuilder xml = new XmlStringBuilder()
            .prelude(ELEMENT_NAME, NAMESPACE)
            .rightAngleBracket();

        if (mPrivateKeyToken != null) {
            xml.openElement("privatekey")
                .element("token", mPrivateKeyToken)
                .closeElement("privatekey");
        }

        // TODO private key data (do we need it? This is read-only)

        xml.closeElement(ELEMENT_NAME);

        return xml.toString();
    }

    public void setPrivateKeyToken(String privateKeyToken) {
        mPrivateKeyToken = privateKeyToken;
    }

    public byte[] getPrivateKeyData() {
        return mPrivateKeyData;
    }

    public byte[] getPublicKeyData() {
        return mPublicKeyData;
    }

    public static final class Provider extends ExtensionElementProvider<Account> {

        @Override
        public Account parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
            boolean done = false, in_privatekey = false;
            boolean in_priv_keydata = false, in_pub_keydata = false;
            String privKeyData = null, pubKeyData = null;

            while (!done) {
                int eventType = parser.next();

                if (eventType == XmlPullParser.START_TAG) {
                    String name = parser.getName();
                    if (in_privatekey) {
                        if ("private".equals(name)) {
                            in_priv_keydata = true;
                        }
                        else if ("public".equals(name)) {
                            in_pub_keydata = true;
                        }
                    }
                    else if ("privatekey".equals(name)) {
                        in_privatekey = true;
                    }
                }
                else if (eventType == XmlPullParser.END_TAG) {
                    String name = parser.getName();
                    if (ELEMENT_NAME.equals(name)) {
                        done = true;
                    }
                    else if ("privatekey".equals(name)) {
                        in_privatekey = false;
                    }
                    else if ("private".equals(name)) {
                        in_priv_keydata = false;
                    }
                    else if ("public".equals(name)) {
                        in_pub_keydata = false;
                    }
                }
                else if (eventType == XmlPullParser.TEXT) {
                    if (in_privatekey) {
                        if (in_priv_keydata) {
                            privKeyData = parser.getText();
                        }
                        else if (in_pub_keydata) {
                            pubKeyData = parser.getText();
                        }
                    }
                }
            }

            return new Account(privKeyData, pubKeyData);
        }

    }

}
