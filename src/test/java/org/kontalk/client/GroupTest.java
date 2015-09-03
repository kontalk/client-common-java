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
import java.util.HashSet;
import java.util.Set;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.junit.Assert;
import org.junit.Test;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author Alexander Bikadorov {@literal <bikaejkb@mail.tu-berlin.de>}
 */
public class GroupTest {

    public GroupTest() {
    }

    @Test
    public void testXML() throws XmlPullParserException, IOException, SmackException {
        Set<String> member = new HashSet();
        member.add("jid1");
        member.add("jid2");
        member.add("jid3");

        this.testGroupXML("testid", "testowner", GroupExtension.Command.NONE, new HashSet());
        this.testGroupXML("testid", "testowner", GroupExtension.Command.CREATE, member);
        this.testGroupXML("testid", "testowner", GroupExtension.Command.LIST, new HashSet());
        this.testGroupXML("testid", "testowner", GroupExtension.Command.LIST, member);
        this.testGroupXML("testid", "testowner", GroupExtension.Command.LEAVE, new HashSet());
    }

    private void testGroupXML(String id,
            String owner,
            GroupExtension.Command command,
            Set<String> member)
            throws XmlPullParserException, IOException, SmackException {

        GroupExtension group = new GroupExtension(id, owner, command, member);

        //System.out.println("XML: "+group.toXML());

        ExtensionElementProvider<GroupExtension> provider = new GroupExtension.Provider();
        XmlPullParser parser = new KXmlParser();
        parser.setInput(new StringReader(group.toXML().toString()));
        GroupExtension parsedGroup = provider.parse(parser, 0);

        Assert.assertEquals(parsedGroup.getID(), id);
        Assert.assertEquals(parsedGroup.getOwner(), owner);
        Assert.assertEquals(parsedGroup.getCommand(), command);
        Assert.assertEquals(parsedGroup.getMember().size(), member.size());
    }
}
