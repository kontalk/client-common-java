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
import org.junit.Test;

import static org.junit.Assert.*;


public class BitsOfBinaryTest {

    @Test
    public void testToXml() {
        final String TEST_DATA = "AAAABBBCCCDDDD";
        final String TEST_MIME = "text/plain";

        final BitsOfBinary actual = new BitsOfBinary(null, TEST_DATA);
        final String expected = new XmlStringBuilder()
            .prelude(BitsOfBinary.ELEMENT_NAME, BitsOfBinary.NAMESPACE)
            .rightAngleBracket()
            .append(TEST_DATA)
            .closeElement(BitsOfBinary.ELEMENT_NAME)
            .toString();
        assertEquals(expected, actual.toXML(null).toString());

        final BitsOfBinary actualWithType = new BitsOfBinary(TEST_MIME, TEST_DATA);
        final String expectedWithType = new XmlStringBuilder()
            .prelude(BitsOfBinary.ELEMENT_NAME, BitsOfBinary.NAMESPACE)
            .attribute("type", TEST_MIME)
            .rightAngleBracket()
            .append(TEST_DATA)
            .closeElement(BitsOfBinary.ELEMENT_NAME)
            .toString();
        assertEquals(expectedWithType, actualWithType.toXML(null).toString());
    }

}
