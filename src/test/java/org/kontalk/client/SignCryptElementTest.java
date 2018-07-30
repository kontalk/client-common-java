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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.StandardExtensionElement;
import org.jivesoftware.smack.provider.ProviderManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kontalk.client.OpenPGPExtension.BodyElement;
import org.kontalk.client.OpenPGPExtension.SignCryptElement;

/**
 * @author Alexander Bikadorov {@literal <bikaejkb@mail.tu-berlin.de>}
 */
public class SignCryptElementTest {

    private static final List<String> JIDS = Arrays.asList("alice@example.com", "bob@example.com");
    private static final Date TIMESTAMP = createTimestamp();
    private static final ExtensionElement STANDARD_EXTENSION =
            new StandardExtensionElement("test_name", "test_namespace");
    private static final BodyElement BODY_EXTENSION = new BodyElement("test_body");
    private static final List<ExtensionElement> PAYLOAD_EXTENSIONS = Arrays.asList(
            STANDARD_EXTENSION, BODY_EXTENSION);
    private static final String XML =
            "<signcrypt xmlns='urn:xmpp:openpgp:0'>" +
            "<to jid='alice@example.com'/>" +
            "<to jid='bob@example.com'/>" +
            "<time stamp='2014-07-10T15:06:00.000+00:00'/>" +
            "<payload>" +
            "<test_name xmlns='test_namespace'></test_name>" +
            "<body xmlns='jabber:client'>test_body</body>" +
            "</payload>" +
            "</signcrypt>";

    private static Date createTimestamp() {
        try {
            return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss Z").parse("2014-07-10 17:06:00 +0200");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @BeforeClass
    public static void setUpClass() {
        ProviderManager.addExtensionProvider(BodyElement.ELEMENT_NAME, BodyElement.NAMESPACE, new BodyElement.Provider());
    }

    @Test
    public void testToXML() throws Exception {
        SignCryptElement element = new SignCryptElement(JIDS, TIMESTAMP, 0, PAYLOAD_EXTENSIONS);
        String xml = element.toXML(null).toString();

        assertEquals(XML, xml);
    }

    @Test
    public void testParse() throws Exception {
        SignCryptElement parsed = SignCryptElement.parse(XML);

        assertEquals("signcrypt", parsed.getElementName());
        assertEquals("urn:xmpp:openpgp:0", parsed.getNamespace());
        assertEquals(JIDS, parsed.getJIDs());
        assertEquals(TIMESTAMP, parsed.getTimeStamp());
        assertEquals(PAYLOAD_EXTENSIONS.size(), parsed.getPayload().size());

        assertEquals(STANDARD_EXTENSION.getClass(), parsed.getPayload().get(0).getClass());
        assertEquals(STANDARD_EXTENSION.getNamespace(), parsed.getPayload().get(0).getNamespace());
        assertEquals(STANDARD_EXTENSION.getElementName(), parsed.getPayload().get(0).getElementName());

        assertEquals(BODY_EXTENSION.getClass(), parsed.getPayload().get(1).getClass());
        assertEquals(BODY_EXTENSION.getNamespace(), parsed.getPayload().get(1).getNamespace());
        assertEquals(BODY_EXTENSION.getElementName(), parsed.getPayload().get(1).getElementName());
        assertEquals(BODY_EXTENSION.getText(), ((BodyElement) parsed.getPayload().get(1)).getText());
    }

    @Test
    public void testParseToXML() throws Exception {
        SignCryptElement signCryptElement = new SignCryptElement(JIDS, TIMESTAMP, 42, PAYLOAD_EXTENSIONS);

        SignCryptElement parsed = SignCryptElement.parse(signCryptElement.toXML(null).toString());
        assertEquals(JIDS, parsed.getJIDs());
        assertEquals(TIMESTAMP, parsed.getTimeStamp());
        assertEquals(PAYLOAD_EXTENSIONS.size(), parsed.getPayload().size());

        assertEquals(STANDARD_EXTENSION.getClass(), parsed.getPayload().get(0).getClass());
        assertEquals(STANDARD_EXTENSION.getNamespace(), parsed.getPayload().get(0).getNamespace());
        assertEquals(STANDARD_EXTENSION.getElementName(), parsed.getPayload().get(0).getElementName());

        assertEquals(BODY_EXTENSION.getClass(), parsed.getPayload().get(1).getClass());
        assertEquals(BODY_EXTENSION.getNamespace(), parsed.getPayload().get(1).getNamespace());
        assertEquals(BODY_EXTENSION.getElementName(), parsed.getPayload().get(1).getElementName());
        assertEquals(BODY_EXTENSION.getText(), ((BodyElement) parsed.getPayload().get(1)).getText());
    }

}
