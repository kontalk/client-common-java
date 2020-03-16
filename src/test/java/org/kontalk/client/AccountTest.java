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

import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.iqregister.packet.Registration;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;

import org.kontalk.util.XMPPParserUtils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class AccountTest {

    @Test
    public void testToXml() {
        final String TEST_TOKEN = "AAAABBBCCCDDDD";

        final Account actual = new Account();
        actual.setPrivateKeyToken(TEST_TOKEN);

        final String expected = new XmlStringBuilder()
            .prelude(Account.ELEMENT_NAME, Account.NAMESPACE)
            .rightAngleBracket()
            .openElement("privatekey")
            .element("token", TEST_TOKEN)
            .closeElement("privatekey")
            .closeElement(Account.ELEMENT_NAME)
            .toString();
        assertEquals(expected, actual.toXML(null).toString());
    }

    @Test
    public void testFromXml() throws Exception {
        final String TEST_DATA = "<iq to=\"prime.kontalk.net\" type=\"result\">" +
            "<query xmlns=\"jabber:iq:register\">" +
            "<account xmlns=\"http://kontalk.org/protocol/register#account\">" +
            "<privatekey>" +
            "<private>Y2lhbw==</private>" +
            "<public>emlv</public>" +
            "</privatekey>" +
            "</account>" +
            "</query>" +
            "</iq>";
        final String TEST_PRIV_DATA = "ciao";
        final String TEST_PUB_DATA = "zio";

        ProviderManager.addExtensionProvider(Account.ELEMENT_NAME, Account.NAMESPACE, new Account.Provider());

        XmlPullParser parser = XMPPParserUtils.getPullParser(TEST_DATA);
        parser.next();
        Registration iq = (Registration) PacketParserUtils.parseIQ(parser);
        Account account = iq.getExtension(Account.ELEMENT_NAME, Account.NAMESPACE);
        assertArrayEquals(TEST_PRIV_DATA.getBytes(), account.getPrivateKeyData());
        assertArrayEquals(TEST_PUB_DATA.getBytes(), account.getPublicKeyData());
    }

}
