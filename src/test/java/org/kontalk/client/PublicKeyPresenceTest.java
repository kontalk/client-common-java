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

import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.jivesoftware.smack.util.stringencoder.java7.Java7Base64Encoder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Test case for {@link PublicKeyPresence}.
 * @author Daniele Ricci
 */
public class PublicKeyPresenceTest {

    private static final String KEYDATA_ENCODED = "VEVTVEtFWURBVEE=";
    private static final String KEYDATA = "TESTKEYDATA";
    private static final String FINGERPRINT = "AAAABBBBCCCCDDDDEEEEFFFF";

    @Before
    public void setUp() {
        Base64.setEncoder(Java7Base64Encoder.getInstance());
    }

    @Test
    public void testToXML() throws Exception {
        PublicKeyPresence p;
        XmlStringBuilder xml;

        p = new PublicKeyPresence(KEYDATA_ENCODED,
            FINGERPRINT);
        xml = new XmlStringBuilder()
            .halfOpenElement(PublicKeyPresence.ELEMENT_NAME)
            .xmlnsAttribute(PublicKeyPresence.NAMESPACE)
            .rightAngleBracket()
            .openElement("key")
            .append(KEYDATA_ENCODED)
            .closeElement("key")
            .openElement("print")
            .append(FINGERPRINT)
            .closeElement("print")
            .closeElement(PublicKeyPresence.ELEMENT_NAME);
        assertEquals(xml.toString(), p.toXML());
        assertArrayEquals(KEYDATA.getBytes(), p.getKey());
        assertEquals(FINGERPRINT, p.getFingerprint());

        p = new PublicKeyPresence(KEYDATA_ENCODED);
        xml = new XmlStringBuilder()
            .halfOpenElement(PublicKeyPresence.ELEMENT_NAME)
            .xmlnsAttribute(PublicKeyPresence.NAMESPACE)
            .rightAngleBracket()
            .openElement("key")
            .append(KEYDATA_ENCODED)
            .closeElement("key")
            .closeElement(PublicKeyPresence.ELEMENT_NAME);
        assertEquals(xml.toString(), p.toXML());
        assertArrayEquals(KEYDATA.getBytes(), p.getKey());
        assertNull(p.getFingerprint());

        p = new PublicKeyPresence((String) null, FINGERPRINT);
        xml = new XmlStringBuilder()
            .halfOpenElement(PublicKeyPresence.ELEMENT_NAME)
            .xmlnsAttribute(PublicKeyPresence.NAMESPACE)
            .rightAngleBracket()
            .openElement("print")
            .append(FINGERPRINT)
            .closeElement("print")
            .closeElement(PublicKeyPresence.ELEMENT_NAME);
        assertEquals(xml.toString(), p.toXML());
        assertNull(p.getKey());
        assertEquals(FINGERPRINT, p.getFingerprint());
    }
}
