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
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smackx.commands.AdHocCommand;
import org.jivesoftware.smackx.commands.packet.AdHocCommandData;
import org.jivesoftware.smackx.commands.provider.AdHocCommandDataProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * Server list ad-hoc command.
 * @author Daniele Ricci
 */
public class ServerlistCommand extends IQ {
    public static final String ELEMENT_NAME = "command";
    public static final String NAMESPACE = "http://jabber.org/protocol/commands";

    private static final String COMMAND_NAME = "serverlist";

    public ServerlistCommand() {
        super(ELEMENT_NAME, NAMESPACE);
        setType(Type.set);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute("xmlns", NAMESPACE)
            .attribute("node", COMMAND_NAME)
            .attribute("action", "execute");
        xml.setEmptyElement();
        return xml;
    }

    public static class ServerlistCommandData extends AdHocCommandData {
        public static final String ELEMENT_NAME = "serverlist";
        public static final String NAMESPACE = "http://kontalk.org/extensions/serverlist";

        private List<String> mItems;

        public void addItem(String item) {
            if (mItems == null)
                mItems = new LinkedList<String>();
            mItems.add(item);
        }

        public List<String> getItems() {
            return mItems;
        }

        // TODO implement toXml
    }

    /** Inspired by {@link AdHocCommandDataProvider}. */
    public static class ResultProvider extends IQProvider<ServerlistCommandData> {

        /**
         * <iq from='kontalk.net' type='result' id='4H1Iu-205' to='alice@kontalk.net/8EL3UAOP'>
         *   <command xmlns='http://jabber.org/protocol/commands' node='serverlist' status='completed'>
         *     <serverlist xmlns='http://kontalk.org/extensions/serverlist'>
         *       <item node='prime.kontalk.net'/>
         *     </serverlist>
         *   </command>
         * </iq>
         */
        @Override
        public ServerlistCommandData parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
            boolean done = false;
            ServerlistCommandData adHocCommandData = new ServerlistCommandData();

            int eventType;
            adHocCommandData.setSessionID(parser.getAttributeValue("", "sessionid"));
            adHocCommandData.setNode(parser.getAttributeValue("", "node"));

            // Status
            String status = parser.getAttributeValue("", "status");
            if (AdHocCommand.Status.executing.toString().equalsIgnoreCase(status)) {
                adHocCommandData.setStatus(AdHocCommand.Status.executing);
            }
            else if (AdHocCommand.Status.completed.toString().equalsIgnoreCase(status)) {
                adHocCommandData.setStatus(AdHocCommand.Status.completed);
            }
            else if (AdHocCommand.Status.canceled.toString().equalsIgnoreCase(status)) {
                adHocCommandData.setStatus(AdHocCommand.Status.canceled);
            }

            // Action
            String action = parser.getAttributeValue("", "action");
            if (action != null) {
                AdHocCommand.Action realAction = AdHocCommand.Action.valueOf(action);
                if (realAction == null || realAction.equals(AdHocCommand.Action.unknown)) {
                    adHocCommandData.setAction(AdHocCommand.Action.unknown);
                }
                else {
                    adHocCommandData.setAction(realAction);
                }
            }

            boolean in_serverlist = false;
            while (!done) {
                eventType = parser.next();
                if (eventType == XmlPullParser.START_TAG) {

                    if (!in_serverlist) {
                        if (parser.getName().equals(ServerlistCommandData.ELEMENT_NAME)) {
                            String namespace = parser.getNamespace();
                            if (ServerlistCommandData.NAMESPACE.equals(namespace)) {
                                in_serverlist = true;
                            }
                        } else if (parser.getName().equals("error")) {
                            XMPPError error = PacketParserUtils.parseError(parser);
                            adHocCommandData.setError(error);
                        }
                    }
                    else {
                        if (parser.getName().equals("item")) {
                            String node = parser.getAttributeValue(null, "node");
                            if (node != null && node.length() > 0) {
                                adHocCommandData.addItem(node);
                            }
                        }
                    }
                }
                else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().equals("command")) {
                        done = true;
                    }
                    else if (in_serverlist && parser.getName().equals(ServerlistCommandData.ELEMENT_NAME)) {
                        in_serverlist = false;
                    }
                }
            }
            return adHocCommandData;
        }
    }
}
