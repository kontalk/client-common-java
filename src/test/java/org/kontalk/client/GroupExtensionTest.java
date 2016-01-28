/*
 * Kontalk client common library
 * Copyright (C) 2015 Kontalk Devteam <devteam@kontalk.org>

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

import java.io.IOException;
import java.io.StringReader;

import org.jivesoftware.smack.SmackException;
import org.junit.Before;
import org.junit.Test;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import org.kontalk.client.GroupExtension.Member;

import static org.junit.Assert.*;


/**
 *
 * @author Alexander Bikadorov {@literal <bikaejkb@mail.tu-berlin.de>}
 */
public class GroupExtensionTest {

    private GroupExtension.Provider provider;

    @Before
    public void setUp() {
        provider = new GroupExtension.Provider();
    }

    @Test
    public void testSimple() throws Exception {
        GroupExtension ext = new GroupExtension("mad-group", "david@localhost");

        GroupExtension parsed = parse(ext.toXML());
        assertNotNull(parsed);
        assertEquals("mad-group", parsed.getID());
        assertEquals("david@localhost", parsed.getOwner());
    }

    @Test
    public void testCreate() throws Exception {
        GroupExtension ext = new GroupExtension("mad-group", "david@localhost");
        ext.setSubject("Mad group");
        ext.addMember("alpha@localhost");
        ext.addMember("beta@localhost");

        GroupExtension parsed = parse(ext.toXML());
        assertNotNull(parsed);
        assertEquals("mad-group", parsed.getID());
        assertEquals("david@localhost", parsed.getOwner());
        assertEquals("Mad group", parsed.getSubject());
        assertNotNull(ext.getMembers());
        assertEquals(2, ext.getMembers().size());

        Member m;

        m = ext.getMembers().get(0);
        assertNotNull(m);
        assertEquals("alpha@localhost", m.jid);
        assertEquals(Member.Operation.ADD, m.operation);

        m = ext.getMembers().get(1);
        assertNotNull(m);
        assertEquals("beta@localhost", m.jid);
        assertEquals(Member.Operation.ADD, m.operation);
    }

    @Test
    public void testRemove() throws Exception {
        GroupExtension ext = new GroupExtension("mad-group", "david@localhost");
        ext.setSubject("Mad group");
        ext.removeMember("alpha@localhost");

        GroupExtension parsed = parse(ext.toXML());
        assertNotNull(parsed);
        assertEquals("mad-group", parsed.getID());
        assertEquals("david@localhost", parsed.getOwner());
        assertEquals("Mad group", parsed.getSubject());
        assertNotNull(ext.getMembers());
        assertEquals(1, ext.getMembers().size());

        Member m;

        m = ext.getMembers().get(0);
        assertNotNull(m);
        assertEquals("alpha@localhost", m.jid);
        assertEquals(Member.Operation.REMOVE, m.operation);
    }

    @Test
    public void testPart() throws Exception {
        GroupExtension ext = new GroupExtension("mad-group", "david@localhost");
        ext.part();

        GroupExtension parsed = parse(ext.toXML());
        assertNotNull(parsed);
        assertEquals("mad-group", parsed.getID());
        assertEquals("david@localhost", parsed.getOwner());
        assertNull(ext.getMembers());
        assertEquals(true, ext.hasPart());
    }

    private GroupExtension parse(CharSequence xml) throws IOException, XmlPullParserException, SmackException {
        XmlPullParser parser = new KXmlParser();
        parser.setInput(new StringReader(xml.toString()));
        parser.next();
        return provider.parse(parser, 0);
    }

}
