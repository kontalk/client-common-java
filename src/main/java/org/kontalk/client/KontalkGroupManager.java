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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.Manager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPConnectionRegistry;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.address.packet.MultipleAddresses;
import org.jxmpp.util.XmppStringUtils;


/**
 * A manager for the Kontalk group chat protocol.
 * @author Daniele Ricci
 */
public class KontalkGroupManager extends Manager {

    private static final Logger LOGGER = Logger.getLogger(KontalkGroupManager.class.getName());

    private static Map<XMPPConnection, KontalkGroupManager> INSTANCES = new WeakHashMap<XMPPConnection, KontalkGroupManager>();

    static {
        XMPPConnectionRegistry.addConnectionCreationListener(new ConnectionCreationListener() {
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
        private final String mGroupOwner;
        private String mSubject;
        private String[] mMembers;

        public KontalkGroup(XMPPConnection connection, String groupId, String groupOwner) {
            mConnection = connection;
            mGroupId = groupId;
            mGroupOwner = groupOwner;
        }

        public boolean isOwned() {
            return mConnection.getUser().equalsIgnoreCase(mGroupOwner);
        }

        public void create(String subject, String[] members, Stanza message) {
            mSubject = subject;
            mMembers = members;
            GroupExtension.addCreateGroup(message, mGroupId, mGroupOwner, mSubject, mMembers);
        }

        public void leave(Stanza message) {
            GroupExtension.addLeaveGroup(message, mGroupId, mGroupOwner);
        }

        // TODO public void addMembers(String subject, String[] currentMembers, String[] newMembers) {
        // TODO public void removeMembers(String subject, String[] currentMembers, String[] removedMembers) {

        public void setSubject(String subject, Stanza message) {
            mSubject = subject;
            GroupExtension.addSetSubject(message, mGroupId, mGroupOwner, mSubject);
        }

        public void groupInfo(Stanza message) {
            GroupExtension.addGroupInfo(message, mGroupId, mGroupOwner);
        }

        /** Process an outgoing message for routing. */
        public void addRouteExtension(String[] members, Stanza message) {
            mMembers = members;
            MultipleAddresses p = new MultipleAddresses();
            for (String rcpt : mMembers)
                p.addAddress(MultipleAddresses.Type.to, rcpt, null, null, false, null);
            message.addExtension(p);
        }

        public String getJID() {
            return XmppStringUtils.completeJidFrom(mGroupId, mGroupOwner);
        }

        public boolean checkRequest(Stanza packet) {
            GroupExtension group = GroupExtension.from(packet);
            // group modification commands are allowed only by the owner
            return group != null && group.getJID().equalsIgnoreCase(getJID()) &&
                !(!isOwned() && (group.getType() == GroupExtension.Type.CREATE || group.getType() == GroupExtension.Type.SET));
        }
    }

    private KontalkGroupManager(final XMPPConnection connection) {
        super(connection);
    }

    private Map<String, WeakReference<KontalkGroup>> mGroups = new HashMap<>();

    public synchronized KontalkGroup getGroup(Stanza packet) {
        if (packet instanceof Message) {
            ExtensionElement ext = packet.getExtension(GroupExtension.ELEMENT_NAME, GroupExtension.NAMESPACE);
            if (ext instanceof GroupExtension) {
                GroupExtension group = (GroupExtension) ext;
                return getGroup(group.getID(), group.getOwner());
            }
        }
        return null;
    }

    public synchronized KontalkGroup getGroup(String groupJid) {
        String id = XmppStringUtils.parseLocalpart(groupJid);
        String owner = XmppStringUtils.parseDomain(groupJid);
        return getGroup(id, owner);
    }

    public synchronized KontalkGroup getGroup(String groupId, String groupOwner) {
        String key = XmppStringUtils.completeJidFrom(groupId, groupOwner);
        WeakReference<KontalkGroup> groupRef = mGroups.get(key);

        if (groupRef == null || groupRef.get() == null) {
            groupRef = new WeakReference<>(new KontalkGroup(connection(), groupId, groupOwner));
            mGroups.put(key, groupRef);
        }

        return groupRef.get();
    }

}
