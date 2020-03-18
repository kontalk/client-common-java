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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.util.XmppStringUtils;
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
    private final Jid mOwner;
    private final Type mType;
    private final String mSubject;
    private final List<Member> mMembers;

    public enum Type {
        NONE(""),
        CREATE("create"),
        PART("part"),
        GET("get"),
        RESULT("result"),
        SET("set");

        private final String element;

        Type(String element) {
            this.element = element;
        }

        @Override
        public String toString() {
            return element;
        }

        public static Type fromString(String element) {
            for (Type c : Type.values()) {
              if (c.element.equals(element)) {
                return c;
              }
            }

            return null;
        }
    }

    /** A new group extension with default type. */
    public GroupExtension(String id, Jid ownerJid) {
        this(id, ownerJid, Type.NONE, null, Collections.<Member>emptyList());
    }

    /** A new group extension with type 'leave' or 'get'. */
    public GroupExtension(String id, Jid ownerJid, Type type) {
        this(id, ownerJid, type, null, Collections.<Member>emptyList());
    }

    /** A new group extension with type 'set'. */
    public GroupExtension(String id, Jid ownerJid, Type type, String subject) {
        this(id, ownerJid, type, subject, Collections.<Member>emptyList());
    }

    /** A new group extension with type 'create', 'set' or 'result'. */
    public GroupExtension(String id, Jid ownerJid, Type type, String subject, Collection<Member> member) {
        mId = id;
        mOwner = ownerJid;
        mType = type;
        mMembers = new ArrayList<>(member);
        mSubject = subject;
    }

    public String getID() {
        return mId;
    }

    public Jid getOwner() {
        return mOwner;
    }

    /** Returns a JID for this group. Only for internal use, it is not a real JID. */
    public Jid getJid() {
        return JidCreate.fromOrThrowUnchecked(XmppStringUtils
            .completeJidFrom(mId, mOwner));
    }

    public Type getType() {
        return mType;
    }

    public void addMember(Jid jid) {
        mMembers.add(new Member(jid, Member.Operation.ADD));
    }

    public void removeMember(Jid jid) {
        mMembers.add(new Member(jid, Member.Operation.REMOVE));
    }

    public List<Member> getMembers() {
        return mMembers;
    }

    public String getSubject() {
        return mSubject;
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
    public XmlStringBuilder toXML(String enclosingNamespace) {
        XmlStringBuilder buf = new XmlStringBuilder()
                .halfOpenElement(ELEMENT_NAME)
                .xmlnsAttribute(NAMESPACE)
                .attribute("id", mId)
                .attribute("owner", mOwner);
        if (mType != Type.NONE) {
            buf.attribute("type", mType.toString());
        }
        if (mMembers.isEmpty() && StringUtils.isNullOrEmpty(mSubject)) {
            // nothing to append
            buf.closeEmptyElement();
        } else {
            buf.rightAngleBracket();

            if (!StringUtils.isNullOrEmpty(mSubject)) {
                buf.element("subject", mSubject);
            }
            for (Member m: mMembers){
                buf.halfOpenElement(m.operation.toString())
                        .attribute("jid", m.jid)
                        .closeEmptyElement();
            }

            buf.closeElement(ELEMENT_NAME);
        }

        return buf;
    }

    public static GroupExtension addCreateGroup(Stanza message, String groupId, Jid groupOwner, String subject, Jid[] members) {
        List<Member> membersList = new ArrayList<>(members.length);
        for (Jid m : members)
            membersList.add(new Member(m, Member.Operation.NONE));
        GroupExtension ext = new GroupExtension(groupId, groupOwner, Type.CREATE, subject, membersList);
        message.addExtension(ext);
        return ext;
    }

    public static GroupExtension addLeaveGroup(Stanza message, String groupId, Jid groupOwner) {
        GroupExtension ext = new GroupExtension(groupId, groupOwner, Type.PART);
        message.addExtension(ext);
        return ext;
    }

    public static GroupExtension addEditMembers(Stanza message, String groupId, Jid groupOwner, String subject, Jid[] members, Jid[] addMembers, Jid[] removeMembers) {
        if (addMembers == null && removeMembers == null)
            throw new IllegalArgumentException("At least one of add or remove members must not be null");

        List<Member> membersList = new ArrayList<>(members.length +
            (addMembers != null ? addMembers.length : 0) +
            (removeMembers != null ? removeMembers.length : 0));

        for (Jid m : members)
            membersList.add(new Member(m, Member.Operation.NONE));

        if (addMembers != null) {
            for (Jid m : addMembers)
                membersList.add(new Member(m, Member.Operation.ADD));
        }

        if (removeMembers != null) {
            for (Jid m : removeMembers)
                membersList.add(new Member(m, Member.Operation.REMOVE));
        }

        GroupExtension ext = new GroupExtension(groupId, groupOwner, Type.SET, subject, membersList);
        message.addExtension(ext);
        return ext;
    }

    public static GroupExtension addSetSubject(Stanza message, String groupId, Jid groupOwner, String subject) {
        GroupExtension ext = new GroupExtension(groupId, groupOwner, Type.SET, subject);
        message.addExtension(ext);
        return ext;
    }

    public static GroupExtension addGroupInfo(Stanza message, String groupId, Jid groupOwner) {
        GroupExtension ext = new GroupExtension(groupId, groupOwner);
        message.addExtension(ext);
        return ext;
    }

    public static GroupExtension from(Stanza message) {
        return message.getExtension(GroupExtension.ELEMENT_NAME, GroupExtension.NAMESPACE);
    }

    public static class Provider extends ExtensionElementProvider<GroupExtension> {

        @Override
        public GroupExtension parse(XmlPullParser parser, int initialDepth)
                throws XmlPullParserException, IOException, SmackException {

            String id = parser.getAttributeValue(null, "id");
            String owner = parser.getAttributeValue(null, "owner");
            String c = parser.getAttributeValue(null, "type");
            Type type = c == null ? Type.NONE : Type.fromString(c);

            List<Member> members = new ArrayList<>();
            String subj = "";

            boolean done = false, in_subject = false;
            while (!done) {
                int eventType = parser.next();

                if(eventType == XmlPullParser.END_DOCUMENT)
                    throw new SmackException("invalid XML schema");

                if (eventType == XmlPullParser.START_TAG) {
                    String s = parser.getName();
                    switch(s) {
                        case "subject":
                            in_subject = true;
                            break;
                        default:
                            Member.Operation op = Member.Operation.fromString(s);
                            if (op != null) {
                                String jid = parser.getAttributeValue(null, "jid");
                                if (jid == null)
                                    continue;

                                members.add(new Member(JidCreate.from(jid), op));
                            }
                            break;
                    }
                } else if (eventType == XmlPullParser.TEXT && in_subject) {
                    subj = parser.getText();
                } else if (eventType == XmlPullParser.END_TAG)
                    switch (parser.getName()) {
                        case ELEMENT_NAME:
                            done = true;
                            break;
                        case "subject":
                            in_subject = false;
                            break;
                    }
            }

            if (id == null || owner == null || type == null || subj == null) {
                return null;
            }

            return new GroupExtension(id, JidCreate.from(owner), type, subj, members);
        }
    }

        public static class Member {

        public enum Operation {
            NONE("member"),
            ADD("add"),
            REMOVE("remove");

            private final String element;

            Operation(String element) {
                this.element = element;
            }

            @Override
            public String toString() {
                return element;
            }

            public static Operation fromString(String element) {
                for (Operation c : Operation.values()) {
                  if (c.element.equals(element)) {
                    return c;
                  }
                }

                return null;
            }
        }

        public final Jid jid;
        public final Operation operation;

        public Member(Jid jid) {
            this(jid, Operation.NONE);
        }

        public Member(Jid jid, Operation operation) {
            this.jid = jid;
            this.operation = operation;
        }
    }
}
