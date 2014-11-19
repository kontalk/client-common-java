/*
 * Kontalk client common library
 * Copyright (C) 2014 Kontalk Devteam <devteam@kontalk.org>

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
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smack.util.stringencoder.Base64;


/**
 * XEP-0189: Public Key Publishing
 * http://xmpp.org/extensions/xep-0189.html#request
 */
public class PublicKeyPublish extends IQ {

    public static final String NAMESPACE = "urn:xmpp:pubkey:2";
    public static final String ELEMENT_NAME = "pubkey";

    private static XmlStringBuilder sChildElement;

    private byte[] mPublicKey;

    private PublicKeyPublish(IQ.Type type, byte[] publicKey) {
        setType(type);
        mPublicKey = publicKey;
    }

    public PublicKeyPublish() {
        // default IQ with type get
    }

    public byte[] getPublicKey() {
        return mPublicKey;
    }

    @Override
    public CharSequence getChildElementXML() {
        if (mPublicKey != null) {
            return new XmlStringBuilder()
                .halfOpenElement(ELEMENT_NAME)
                .xmlnsAttribute(NAMESPACE)
                .rightAngleBracket()
                .append(Base64.encodeToString(mPublicKey))
                .closeElement(ELEMENT_NAME);
        }
        else {
            if (sChildElement == null) {
                sChildElement = new XmlStringBuilder()
                    .halfOpenElement(ELEMENT_NAME)
                    .xmlnsAttribute(NAMESPACE)
                    .closeEmptyElement();
            }
            return sChildElement;
        }
    }

    // TODO IQProvider

}
