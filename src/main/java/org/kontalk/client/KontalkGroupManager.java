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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.Manager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPConnectionRegistry;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.address.packet.MultipleAddresses;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;


/**
 * A manager for the Kontalk group chat protocol.
 * @author Daniele Ricci
 */
public class KontalkGroupManager extends Manager {

    private static Map<XMPPConnection, KontalkGroupManager> INSTANCES = new WeakHashMap<>();

    static {
        XMPPConnectionRegistry.addConnectionCreationListener(new ConnectionCreationListener() {
            @Override
            public void connectionCreated(XMPPConnection connection) {
                getInstanceFor(connection);
            }
        });
    }

    public static synchronized KontalkGroupManager getInstanceFor(XMPPConnection connection) {
        KontalkGroupManager manager = INSTANCES.get(connection);

        if (manager == null) {
            manager = new KontalkGroupManager(connection);
            INSTANCES.put(connection, manager);
        }

        return manager;
    }

    /** Handles a single group. An instance is created for each group to be managed. */
    public static class KontalkGroup {
        private final XMPPConnection mConnection;

        private final String mGroupId;
        private final EntityBareJid mGroupOwner;
        private String mSubject;
        private String[] mMembers;

        public KontalkGroup(XMPPConnection connection, String groupId, String groupOwner) throws XmppStringprepException {
            mConnection = connection;
            mGroupId = groupId;
            mGroupOwner = JidCreate.entityBareFrom(groupOwner);
        }

        public boolean isOwned() {
            return isOwned(mConnection.getUser());
        }

        public boolean isOwned(Jid by) {
            return mGroupOwner.isParentOf(by);
        }

        public void create(String subject, String[] members, Stanza message) {
            mSubject = subject != null ? subject : "";
            mMembers = members;
            GroupExtension.addCreateGroup(message, mGroupId, mGroupOwner.toString(), mSubject, mMembers);
        }

        public void leave(Stanza message) {
            GroupExtension.addLeaveGroup(message, mGroupId, mGroupOwner.toString());
        }

        public void setSubject(String subject, Stanza message) {
            mSubject = subject != null ? subject : "";
            GroupExtension.addSetSubject(message, mGroupId, mGroupOwner.toString(), mSubject);
        }

        public void addRemoveMembers(String subject, String[] members, String[] added, String[] removed, Stanza message) {
            mSubject = subject != null ? subject : "";
            mMembers = members;
            GroupExtension.addEditMembers(message, mGroupId, mGroupOwner.toString(), mSubject,
                mMembers, added, removed);
        }

        public void groupInfo(Stanza message) {
            GroupExtension.addGroupInfo(message, mGroupId, mGroupOwner.toString());
        }

        /** Process an outgoing message for routing. */
        public void addRouteExtension(String[] members, Stanza message) throws XmppStringprepException {
            mMembers = members;
            MultipleAddresses p = new MultipleAddresses();
            for (String rcpt : mMembers)
                p.addAddress(MultipleAddresses.Type.to, JidCreate.from(rcpt), null, null, false, null);
            message.addExtension(p);
        }

        public String getJID() {
            return XmppStringUtils.completeJidFrom(mGroupId, mGroupOwner);
        }

        public boolean checkRequest(Stanza packet) {
            GroupExtension group = GroupExtension.from(packet);
            // group modification commands are allowed only by the owner
            return group != null && group.getJID().equalsIgnoreCase(getJID()) &&
                !(!isOwned(packet.getFrom()) && (group.getType() == GroupExtension.Type.CREATE || group.getType() == GroupExtension.Type.SET));
        }

        /** Checks whether the given group JID is owned by the given JID. */
        public static boolean checkOwnership(String groupJid, String checkJid) {
            String owner = XmppStringUtils.parseDomain(groupJid);
            return XmppStringUtils.parseBareJid(checkJid).equalsIgnoreCase(owner);
        }
    }

    private KontalkGroupManager(final XMPPConnection connection) {
        super(connection);
    }

    private Map<String, WeakReference<KontalkGroup>> mGroups = new HashMap<>();

    public synchronized KontalkGroup getGroup(Stanza packet) throws XmppStringprepException {
        if (packet instanceof Message) {
            ExtensionElement ext = packet.getExtension(GroupExtension.ELEMENT_NAME, GroupExtension.NAMESPACE);
            if (ext instanceof GroupExtension) {
                GroupExtension group = (GroupExtension) ext;
                return getGroup(group.getID(), group.getOwner());
            }
        }
        return null;
    }

    public synchronized KontalkGroup getGroup(String groupJid) throws XmppStringprepException {
        String id = XmppStringUtils.parseLocalpart(groupJid);
        String owner = XmppStringUtils.parseDomain(groupJid);
        return getGroup(id, owner);
    }

    public synchronized KontalkGroup getGroup(String groupId, String groupOwner) throws XmppStringprepException {
        String key = XmppStringUtils.completeJidFrom(groupId, groupOwner);
        WeakReference<KontalkGroup> groupRef = mGroups.get(key);

        KontalkGroup group = (groupRef != null) ? groupRef.get() : null;

        if (group == null) {
            group = new KontalkGroup(connection(), groupId, groupOwner);
            groupRef = new WeakReference<>(group);
            mGroups.put(key, groupRef);
        }

        return group;
    }

}
