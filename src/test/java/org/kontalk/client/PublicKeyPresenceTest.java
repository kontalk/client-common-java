package org.kontalk.client;

import org.jivesoftware.smack.SmackInitialization;
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
