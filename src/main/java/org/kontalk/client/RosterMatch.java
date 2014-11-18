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
                builder.openElement("item")
                    .append(item)
                    .closeElement("item");
            }
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
