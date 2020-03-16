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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.jivesoftware.smack.SmackException;
import org.junit.Test;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author Alexander Bikadorov {@literal <bikaejkb@mail.tu-berlin.de>}
 */
public class OpenPGPExtensionTest {

    private static final String BASE64_DATA = "base64_test_data";
    private static final String XML =
            "<openpgp xmlns='urn:xmpp:openpgp:0'>" + BASE64_DATA + "</openpgp>";

    @Test
    public void testToXML() throws Exception {
        OpenPGPExtension element = new OpenPGPExtension(BASE64_DATA);
        String xml = element.toXML(null).toString();

        assertEquals(XML, xml);
    }

    @Test
    public void testParse() throws Exception {
        OpenPGPExtension parsed = parse(XML);

        assertEquals("openpgp", parsed.getElementName());
        assertEquals("urn:xmpp:openpgp:0", parsed.getNamespace());
        assertEquals(BASE64_DATA, parsed.getData());
    }

    @Test
    public void testParseToXML() throws Exception {
        OpenPGPExtension element = new OpenPGPExtension(BASE64_DATA);
        OpenPGPExtension parsed = parse(element.toXML(null));

        assertEquals(element.getClass(), parsed.getClass());
        assertEquals("openpgp", parsed.getElementName());
        assertEquals("urn:xmpp:openpgp:0", parsed.getNamespace());
        assertEquals(BASE64_DATA, parsed.getData());
    }

    private OpenPGPExtension parse(CharSequence xml) throws IOException, XmlPullParserException, SmackException {
        XmlPullParser parser = new KXmlParser();
        parser.setInput(new StringReader(xml.toString()));
        parser.next();
        return new OpenPGPExtension.Provider().parse(parser, 0);
    }
}
