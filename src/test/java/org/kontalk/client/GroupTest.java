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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.junit.Assert;
import org.junit.Test;
import org.kontalk.client.GroupExtension.Member;
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
        List<Member> members = Arrays.asList(
            new Member("jid1"),
            new Member("jid2", Member.Operation.ADD),
            new Member("jid3", Member.Operation.REMOVE));

        this.testGroupXML("testid", "testowner", GroupExtension.Type.NONE, Collections.emptyList(), "");
        this.testGroupXML("testid", "testowner", GroupExtension.Type.CREATE, members, "_subj_");
        this.testGroupXML("testid", "testowner", GroupExtension.Type.GET, Collections.emptyList(), "");
        this.testGroupXML("testid", "testowner", GroupExtension.Type.RESULT, members, "_subj_");
        this.testGroupXML("testid", "testowner", GroupExtension.Type.SET, members, "_subj_");
        this.testGroupXML("testid", "testowner", GroupExtension.Type.PART, Collections.emptyList(), "");
    }

    private void testGroupXML(String id,
            String owner,
            GroupExtension.Type type,
            List<Member> members,
            String subject)
            throws XmlPullParserException, IOException, SmackException {

        GroupExtension group = new GroupExtension(id, owner, type, subject, members);

        ExtensionElementProvider<GroupExtension> provider = new GroupExtension.Provider();
        XmlPullParser parser = new KXmlParser();
        parser.setInput(new StringReader(group.toXML().toString()));
        parser.next();
        GroupExtension parsedGroup = provider.parse(parser, 0);

        Assert.assertEquals(parsedGroup.getID(), id);
        Assert.assertEquals(parsedGroup.getOwner(), owner);
        Assert.assertEquals(parsedGroup.getType(), type);
        Assert.assertEquals(parsedGroup.getMembers().size(), members.size());
        Assert.assertEquals(parsedGroup.getSubject(), subject);
    }
}
