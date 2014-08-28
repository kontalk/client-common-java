package org.kontalk.util;

import java.util.Date;

import junit.framework.TestCase;

import static junit.framework.Assert.*;


/**
 * Test case for {@link CPIMMessage}.
 * @author Daniele Ricci
 */
public class CPIMMessageTest extends TestCase {

    private static final String FROM = "alice@example.com";
    private static final String TO = "bob@example.com";
    private static final Date DATE = new Date(1408912505000L);
    private static final String MIME = "text/plain";
    private static final String BODY = "TEST BODY";

    private static final String OUTPUT =
        "Content-type: Message/CPIM\n"+
        "\n" +
        "From: alice@example.com\n" +
        "To: bob@example.com\n" +
        "DateTime: 2014-08-24T20:35:05+00:00\n" +
        "\n" +
        "Content-type: text/plain; charset=utf-8\n" +
        "\n" +
        "TEST BODY";

    public void testToString() throws Exception {
        CPIMMessage m = new CPIMMessage(
            FROM,
            TO,
            DATE,
            MIME,
            BODY
        );
        assertEquals("generated CPIM data not matching.", OUTPUT, m.toString());
    }

    public void testParse() throws Exception {
        CPIMMessage m = CPIMMessage.parse(OUTPUT);
        assertEquals("From attribute not matching.", FROM, m.getFrom());
        assertEquals("To attribute not matching.", TO, m.getTo());
        assertEquals("Mime attribute not matching.", MIME, m.getMime());
        assertEquals("Date attribute not matching.", DATE, m.getDate());
        assertEquals("Body not matching.", BODY, m.getBody());
    }
}
