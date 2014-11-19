/*
 * Kontalk client common library
 * Copyright (C) 2014 Kontalk Devteam <devteam@kontalk.org>

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
import org.jivesoftware.smack.util.XmlStringBuilder;
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
    }

    private RosterMatch(List<String> items) {
        mItems = items;
    }

    public void addItem(String jid) {
        if (mItems == null)
            mItems = new LinkedList<String>();
        mItems.add(jid);
    }

    public List<String> getItems() {
        return mItems;
    }

    @Override
    public CharSequence getChildElementXML() {
        XmlStringBuilder builder = new XmlStringBuilder()
            .halfOpenElement(ELEMENT_NAME)
            .xmlnsAttribute(NAMESPACE);

        if (mItems != null && mItems.size() > 0) {
            builder.rightAngleBracket();
            for (String item : mItems) {
                builder.halfOpenElement("item")
                    .attribute("jid", item)
                    .closeEmptyElement();
            }

            builder.closeElement(ELEMENT_NAME);
        }
        else {
            builder.closeEmptyElement();
        }

        return builder;
    }

    public static final class Provider extends IQProvider<RosterMatch> {

        @Override
        public RosterMatch parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
            boolean done = false, in_item = false;
            List<String> items = null;

            while (!done) {
                int eventType = parser.next();

                if (eventType == XmlPullParser.START_TAG) {
                    if ("item".equals(parser.getName())) {
                        in_item = true;
                    }
                }
                else if (eventType == XmlPullParser.TEXT) {
                    if (in_item) {
                        if (items == null)
                            items = new LinkedList<String>();
                        items.add(parser.getText());
                    }
                }
                else if (eventType == XmlPullParser.END_TAG) {
                    if (ELEMENT_NAME.equals(parser.getName())) {
                        done = true;
                    }
                    else if ("item".equals(parser.getName())) {
                        in_item = false;
                    }

                }
            }

            return new RosterMatch(items);
        }

    }

}
