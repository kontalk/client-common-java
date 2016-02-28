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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
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
    private final String mOwner;
    private String mSubject;
    private List<Member> mMembers;
    private boolean mPart;

    public GroupExtension(String id, String owner) {
        this(id, owner, null);
    }

    public GroupExtension(String id, String owner, String subject) {
        this(id, owner, subject, null);
    }

    public GroupExtension(String id, String owner, String subject, Collection<Member> members) {
        mId = id;
        mOwner = owner;
        mSubject = subject;
        mMembers = members != null ? new LinkedList<>(members) : null;
    }

    public String getID() {
        return mId;
    }

    public String getOwner() {
        return mOwner;
    }

    /** Returns a JID for this group. Only for internal use, it is not a real JID. */
    public String getJID() {
        return XmppStringUtils.completeJidFrom(mId, mOwner);
    }

    private void ensureMembers() {
        if (mMembers == null)
            mMembers = new LinkedList<>();
    }

    public void addMember(String jid) {
        ensureMembers();
        mMembers.add(new Member(jid, Member.Operation.ADD));
    }

    public void removeMember(String jid) {
        ensureMembers();
        mMembers.add(new Member(jid, Member.Operation.REMOVE));
    }

    public List<Member> getMembers() {
        return mMembers;
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        mSubject = subject;
    }

    public void part() {
        mPart = true;
    }

    public boolean hasPart() {
        return mPart;
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

        if (mSubject == null && (mMembers == null || mMembers.isEmpty())) {
            // nothing to append
            buf.closeEmptyElement();
        }
        else {
            buf.rightAngleBracket();

            if (mPart) {
                buf.emptyElement("part");
            }

            if (mSubject != null) {
                buf.element("subject", mSubject);
            }

            if (mMembers != null) {
                for (Member m : mMembers) {
                    buf.halfOpenElement(m.operation.toString())
                        .attribute("jid", m.jid)
                        .closeEmptyElement();
                }
            }

            buf.closeElement(ELEMENT_NAME);
        }

        return buf.toString();
    }

    public static class Provider extends ExtensionElementProvider<GroupExtension> {

        @Override
        public GroupExtension parse(XmlPullParser parser, int initialDepth)
                throws XmlPullParserException, IOException, SmackException {

            String id = parser.getAttributeValue(null, "id");
            String owner = parser.getAttributeValue(null, "owner");

            List<Member> members = new LinkedList<>();
            String subject = null;
            boolean part = false;

            boolean done = false, in_subject = false;
            while (!done) {
                int eventType = parser.next();

                if (eventType == XmlPullParser.START_TAG) {
                    String s = parser.getName();
                    switch (s) {
                        case "subject":
                            in_subject = true;
                            break;
                        case "part":
                            part = true;
                            break;
                        default:
                            Member.Operation op = Member.Operation.fromString(s);
                            if (op != null) {
                                String jid = parser.getAttributeValue(null, "jid");
                                if (jid == null)
                                    continue;

                                members.add(new Member(jid, op));
                            }
                            break;
                    }
                }
                else if (eventType == XmlPullParser.TEXT && in_subject) {
                    subject = parser.getText();
                }
                else if (eventType == XmlPullParser.END_TAG) {
                    switch (parser.getName()) {
                        case ELEMENT_NAME:
                            done = true;
                            break;
                        case "subject":
                            in_subject = false;
                            break;
                    }
                }
            }

            if (id != null && owner != null) {
                GroupExtension ext = new GroupExtension(id, owner, subject, members);
                if (part)
                    ext.part();
                return ext;
            }

            return null;
        }
    }

    public static class Member {

        public enum Operation {
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

        public final String jid;
        public final Operation operation;

        public Member(String jid) {
            this(jid, Operation.ADD);
        }

        public Member(String jid, Operation operation) {
            this.jid = jid;
            this.operation = operation;
        }
    }

}
