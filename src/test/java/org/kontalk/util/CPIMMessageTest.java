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

package org.kontalk.util;

import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Test case for {@link CPIMMessage}.
 * @author Daniele Ricci
 */
public class CPIMMessageTest {

    private static final String FROM = "alice@example.com";
    private static final String[] TO = { "alice@example.com", "bob@example.com" };
    private static final Date DATE = new Date(1408912505000L);
    private static final String MIME = "text/plain";
    private static final String BODY = "TEST BODY";

    private static final String OUTPUT =
        "Content-type: Message/CPIM\n"+
        "\n" +
        "From: alice@example.com\n" +
        "To: alice@example.com; bob@example.com\n" +
        "DateTime: 2014-08-24T20:35:05+00:00\n" +
        "\n" +
        "Content-type: text/plain; charset=utf-8\n" +
        "\n" +
        "TEST BODY";

    @Test
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

    @Test
    public void testParse() throws Exception {
        CPIMMessage m = CPIMMessage.parse(OUTPUT);
        assertEquals("From attribute not matching.", FROM, m.getFrom());
        assertArrayEquals("To attribute not matching.", TO, m.getTo());
        assertEquals("Mime attribute not matching.", MIME, m.getMime());
        assertEquals("Date attribute not matching.", DATE, m.getDate());
        assertEquals("Body not matching.", BODY, m.getBody());
    }
}
