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

package org.kontalk.util;

import java.io.StringReader;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;
import org.jivesoftware.smack.xml.XmlPullParserFactory;
import org.jivesoftware.smack.xml.xpp3.Xpp3XmlPullParserFactory;


/**
 * XMPP related functions.
 * @author Daniele Ricci
 */
public class XMPPParserUtils {

    public static final String XML_XMPP_TYPE = "application/xmpp+xml";

    private XMPPParserUtils() {
        throw new AssertionError();
    }

    private static XmlPullParserFactory _xmlFactory;

    public static XmlPullParser getPullParser(String data) throws XmlPullParserException {
        if (_xmlFactory == null) {
            _xmlFactory = new Xpp3XmlPullParserFactory();
        }

        return _xmlFactory.newXmlPullParser(new StringReader(data));
    }

    /** Parses a &lt;xmpp&gt;-wrapped message stanza. */
    public static Message parseMessageStanza(String data) throws Exception {

        XmlPullParser parser = getPullParser(data);
        boolean done = false, in_xmpp = false;
        Message msg = null;

        while (!done) {
            XmlPullParser.Event eventType = parser.next();

            if (eventType == XmlPullParser.Event.START_ELEMENT) {

                if ("xmpp".equals(parser.getName()))
                    in_xmpp = true;

                else if ("message".equals(parser.getName()) && in_xmpp) {
                    msg = PacketParserUtils.parseMessage(parser);
                }
            }

            else if (eventType == XmlPullParser.Event.END_ELEMENT) {

                if ("xmpp".equals(parser.getName()))
                    done = true;
            }
        }

        return msg;
    }

}

