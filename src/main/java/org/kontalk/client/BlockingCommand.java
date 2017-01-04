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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * XEP-0191: Blocking Command
 * http://xmpp.org/extensions/xep-0191.html
 * @author Daniele Ricci
 */
public class BlockingCommand extends IQ {
    public static final String NAMESPACE = "urn:xmpp:blocking";

    public static final String BLOCKLIST = "blocklist";
    public static final String BLOCK = "block";
    public static final String UNBLOCK = "unblock";
    public static final String UNALLOW = "unallow";

    // might be used often, better keeping a cache of it
    private static BlockingCommand sBlocklist;

    private List<String> mJidList;

    public BlockingCommand(String command) {
        super(command, NAMESPACE);
        setType(IQ.Type.set);
    }

    public BlockingCommand(String command, String jid) {
        this(command);

        mJidList = new LinkedList<String>();
        mJidList.add(jid);
    }

    private BlockingCommand(String command, List<String> jidList) {
        this(command);
        mJidList = jidList;
    }

    /* TODO
    public BlockingCommand(IQ iq) {
        super(iq);
    }
    */

    public List<String> getItems() {
        return mJidList;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if (mJidList != null && mJidList.size() > 0) {
            xml.rightAngleBracket();
            for (String jid: mJidList) {
                xml.halfOpenElement("item")
                    .attribute("jid", jid)
                    .closeEmptyElement();
            }
        }
        else {
            xml.setEmptyElement();
        }

        return xml;
    }

    public static BlockingCommand block(String jid) {
        return new BlockingCommand(BLOCK, jid);
    }

    public static BlockingCommand unblock(String jid) {
        return new BlockingCommand(UNBLOCK, jid);
    }

    public static BlockingCommand unallow(String jid) {
        return new BlockingCommand(UNALLOW, jid);
    }

    public static BlockingCommand blocklist() {
        if (sBlocklist == null) {
            sBlocklist = new BlockingCommand(BLOCKLIST);
            sBlocklist.setType(IQ.Type.get);
        }

        return sBlocklist;
    }

    public static final class Provider extends IQProvider<BlockingCommand> {

        @Override
        public BlockingCommand parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
            List<String> jidList = null;
            boolean done = false;

            while (!done) {
                int eventType = parser.next();

                if (eventType == XmlPullParser.START_TAG) {
                    if ("item".equals(parser.getName())) {
                        String jid = parser.getAttributeValue(null, "jid");
                        if (jid != null && jid.length() > 0) {
                            if (jidList == null)
                                jidList = new LinkedList<String>();

                            jidList.add(jid);
                        }
                    }

                }
                else if (eventType == XmlPullParser.END_TAG) {
                    if (BLOCKLIST.equals(parser.getName())) {
                        done = true;
                    }
                }
            }

            return new BlockingCommand(BLOCKLIST, jidList);
        }

    }

}
