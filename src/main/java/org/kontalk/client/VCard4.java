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

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


/**
 * Very basic vCard4 (XEP-0292) implementation.
 * http://xmpp.org/extensions/xep-0292.html
 * @author Daniele Ricci
 */
public class VCard4 extends IQ {

    public static final String ELEMENT_NAME = "vcard";
    public static final String NAMESPACE = "urn:ietf:params:xml:ns:vcard-4.0";

    private static final String KEY_PREFIX = "data:application/pgp-keys;base64,";

    private byte[] mPGPKey;

    public VCard4() {
        super();
    }

    public void setPGPKey(byte[] keydata) {
        mPGPKey = keydata;
    }

    public byte[] getPGPKey() {
        return mPGPKey;
    }

    @Override
    public String getChildElementXML() {
        StringBuilder b = new StringBuilder()
        	.append('<')
            .append(ELEMENT_NAME)
            .append(" xmlns='")
            .append(NAMESPACE)
            .append('\'');

        if (mPGPKey != null) b
            .append("><key><uri>")
            .append(KEY_PREFIX)
            .append(Base64.encodeToString(mPGPKey))
            .append("</uri></key></")
            .append(ELEMENT_NAME)
            .append('>');
        else b
            .append("/>");

        return b.toString();
    }

    public static final class Provider extends IQProvider<VCard4> {

        @Override
        public VCard4 parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
            boolean done = false, in_key = false, in_uri = false;
            String uri = null;

            while (!done) {
                int eventType = parser.next();

                if (eventType == XmlPullParser.START_TAG) {
                    if ("key".equals(parser.getName())) {
                        in_key = true;
                    }
                    else if ("uri".equals(parser.getName()) && in_key) {
                        in_uri = true;
                    }
                }
                else if (eventType == XmlPullParser.TEXT) {
                    if (in_key && in_uri) {
                        uri = parser.getText();
                        in_uri = false;
                    }
                }
                else if (eventType == XmlPullParser.END_TAG) {
                    if (ELEMENT_NAME.equals(parser.getName())) {
                        done = true;
                    }
                    else if ("key".equals(parser.getName())) {
                        in_key = false;
                    }

                }
            }

            VCard4 iq = new VCard4();
            if (uri != null && uri.startsWith(KEY_PREFIX)) {
                byte[] keydata = Base64.decode(uri.substring(KEY_PREFIX.length()));
                iq.setPGPKey(keydata);
            }

            return iq;
        }

    }

}
