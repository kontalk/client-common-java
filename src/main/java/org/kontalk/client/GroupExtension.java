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
import java.util.HashSet;
import java.util.Set;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Group extension.
 *
 * @author Alexander Bikadorov {@literal <bikaejkb@mail.tu-berlin.de>}
 */
public class GroupExtension implements ExtensionElement {
    public static final String ELEMENT_NAME = "group";
    public static final String NAMESPACE  = "http://kontalk.org/extensions/message#group";

    private final String mId;
    private final String mOwner;
    private final Command mCommand;
    private final Set<String> mMember;

    public enum Command {
        NONE(""),
        CREATE("create"),
        LEAVE("part"),
        LIST("list");

        private final String element;

        private Command(String element) {
            this.element = element;
        }

        @Override
        public String toString() {
            return element;
        }

        public static Command fromString(String element) {
            for (Command c : Command.values()) {
              if (c.element.equals(element)) {
                return c;
              }
            }

            return null;
        }
    }

    public GroupExtension(String id, String ownerJid) {
        this(id, ownerJid, Command.NONE, new HashSet<>());
    }

    public GroupExtension(String id, String ownerJid, Set<String> member, boolean created) {
        this(id, ownerJid, created ? Command.CREATE : Command.LIST, member);
    }

    public GroupExtension(String id, String ownerJid, Command command) {
        this(id, ownerJid, command, new HashSet<>());
    }

    public GroupExtension(String id, String ownerJid, Command command, Set<String> member) {
        mId = id;
        mOwner = ownerJid;
        mCommand = command;
        mMember = member;
    }

    public String getID() {
        return mId;
    }

    public String getOwner() {
        return mOwner;
    }

    public Command getCommand() {
        return mCommand;
    }

    public Set<String> getMember() {
        return mMember;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public CharSequence toXML() {
        XmlStringBuilder buf = new XmlStringBuilder()
                .halfOpenElement(ELEMENT_NAME)
                .xmlnsAttribute(NAMESPACE)
                .attribute("id", mId)
                .attribute("owner", mOwner);
        if (mCommand != Command.NONE) {
            buf.attribute("command", mCommand.toString());
        }
        if (mMember.isEmpty()) {
            //buf.emptyElement(mCommand.toString());
            buf.closeEmptyElement();
        } else {
            buf.rightAngleBracket();
            //buf.openElement(mCommand.toString());
            for (String jid: mMember){
                buf.halfOpenElement("member")
                        .attribute("jid", jid)
                        .closeEmptyElement();
            }
            //buf.closeElement(mCommand.toString());
            buf.closeElement(ELEMENT_NAME);
        }
        return buf.toString();
    }

    public static class Provider extends ExtensionElementProvider<GroupExtension> {

        @Override
        public GroupExtension parse(XmlPullParser parser, int initialDepth)
                throws XmlPullParserException, IOException, SmackException {
            boolean done = false;

            String id = null, owner = null;
            Command command = Command.NONE;
            Set<String> member = new HashSet<>();

            while (!done) {
                int eventType = parser.next();

                if (eventType == XmlPullParser.START_TAG) {
                    switch (parser.getName()) {
                        case "group":
                            id = parser.getAttributeValue(null, "id");
                            owner = parser.getAttributeValue(null, "owner");
                            String com = parser.getAttributeValue(null, "command");
                            if (com != null)
                                command = Command.fromString(com);
                            break;
                        case "member":
                            member.add(parser.getAttributeValue(null, "jid"));
                            break;
                    }
                } else if (eventType == XmlPullParser.END_TAG &&
                        ELEMENT_NAME.equals(parser.getName())) {
                    done = true;
                }
            }

            if (id == null || owner == null || command == null) {
                System.out.println(id+" "+owner+" "+command);
                return null;
            }

            return new GroupExtension(id, owner, command, member);
        }
    }

}
