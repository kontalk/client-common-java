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

import java.util.Collections;

import org.jivesoftware.smack.packet.Message;
import org.junit.Test;
import org.jxmpp.jid.impl.JidCreate;

import static org.junit.Assert.*;


public class KontalkGroupTest {

    @Test
    public void testCheckRequest() throws Exception {
        GroupExtension ext;
        KontalkGroupManager.KontalkGroup group;
        Message cmd;

        ext = new GroupExtension("mad-group", "david@localhost", GroupExtension.Type.CREATE,
            "Mad group", Collections.<GroupExtension.Member>emptyList());
        ext.addMember("alpha@localhost");
        ext.addMember("beta@localhost");

        cmd = new Message(JidCreate.from("golia@localhost"));
        cmd.setFrom(JidCreate.from("charie@localhost"));
        cmd.addExtension(ext);

        group = new KontalkGroupManager.KontalkGroup(null, "mad-group", "david@localhost");
        assertFalse(group.checkRequest(cmd));

        ext = new GroupExtension("mad-group", "david@localhost", GroupExtension.Type.SET,
            "Mad group", Collections.<GroupExtension.Member>emptyList());
        ext.addMember("alpha@localhost");
        ext.addMember("beta@localhost");

        cmd = new Message(JidCreate.from("golia@localhost"));
        cmd.setFrom(JidCreate.from("charie@localhost"));
        cmd.addExtension(ext);

        group = new KontalkGroupManager.KontalkGroup(null, "mad-group", "david@localhost");
        assertFalse(group.checkRequest(cmd));
    }

}
