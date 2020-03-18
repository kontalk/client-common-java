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
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Kontalk custom roster packet.
 * @author Daniele Ricci
 */
public class RosterMatch extends IQ {
    public static final String NAMESPACE = "http://kontalk.org/extensions/roster";
    public static final String ELEMENT_NAME = IQ.QUERY_ELEMENT;

    private List<String> mItems;

    public RosterMatch() {
        super(ELEMENT_NAME, NAMESPACE);
    }

    private RosterMatch(List<String> items) {
        this();
        mItems = items;
    }

    public void addItem(String jid) {
        if (mItems == null)
            mItems = new LinkedList<>();
        mItems.add(jid);
    }

    public List<String> getItems() {
        return mItems;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if (mItems != null && mItems.size() > 0) {
            xml.rightAngleBracket();
            for (String item : mItems) {
                xml.halfOpenElement("item")
                    .attribute("jid", item)
                    .closeEmptyElement();
            }
        }
        else {
            xml.setEmptyElement();
        }

        return xml;
    }

    public static final class Provider extends IQProvider<RosterMatch> {

        @Override
        public RosterMatch parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
            boolean done = false;
            List<String> items = null;

            while (!done) {
                int eventType = parser.next();

                if (eventType == XmlPullParser.START_TAG) {
                    if ("item".equals(parser.getName())) {
                        if (items == null)
                            items = new LinkedList<>();
                        String item = parser.getAttributeValue(null, "jid");
                        if (item != null)
                            items.add(item);
                    }
                }
                else if (eventType == XmlPullParser.END_TAG) {
                    if (ELEMENT_NAME.equals(parser.getName())) {
                        done = true;
                    }
                }
            }

            return new RosterMatch(items);
        }

    }

}
