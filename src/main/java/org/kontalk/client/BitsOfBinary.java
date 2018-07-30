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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * XEP-0231: Bits of Binary
 * @author Daniele Ricci
 */
public class BitsOfBinary implements ExtensionElement {
    public static final String ELEMENT_NAME = "data";
    public static final String NAMESPACE = "urn:xmpp:bob";

    private final String mMime;
    private final File mFile;

    /** Cache of Base64-encoded data. */
    private String mCache;

    public BitsOfBinary(String mime, String contents) {
        this(mime, (File) null);
        mCache = contents;
    }

    public BitsOfBinary(String mime, File path) {
        mMime = mime;
        mFile = path;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    /**
     * Returns the decoded contents.
     * @return the Base64-decoded contents.
     */
    public byte[] getContents() {
        updateContents();
        if (mCache != null)
            return Base64.decode(mCache);

        return null;
    }

    /**
     * Returns the MIME type (if defined).
     * @return the MIME type (if any).
     */
    public String getType() {
        return mMime;
    }

    private void updateContents() {
        if (mCache == null && mFile != null) {
            FileInputStream source = null;
            try {
                source = new FileInputStream(mFile);
                ByteArrayOutputStream bos = new ByteArrayOutputStream((int) mFile.length());
                byte[] buffer = new byte[1024];
                int len;
                while ((len = source.read(buffer)) != -1)
                    bos.write(buffer, 0, len);
                mCache = Base64.encodeToString(bos.toByteArray());
                bos.close();
            }
            catch (IOException e) {
                // error! Invalidate cache
                mCache = null;
            }
            finally {
                try {
                    source.close();
                }
                catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Returns an XML representation of this object as per XEP-0231.
     * @return the XML representation.
     */
    @Override
    public XmlStringBuilder toXML(String enclosingNamespace) {
        updateContents();
        if (mCache == null) return null;

        XmlStringBuilder xml = new XmlStringBuilder()
            .prelude(ELEMENT_NAME, NAMESPACE);

        if (mMime != null) {
            xml.attribute("type", mMime);
        }

        xml.rightAngleBracket()
            .append(mCache)
            .closeElement(ELEMENT_NAME);
        return xml;
    }

    /** Provider class for parsing {@link BitsOfBinary}. */
    public static final class Provider extends ExtensionElementProvider<BitsOfBinary> {

        @Override
        public BitsOfBinary parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
            String contents = null, mime;
            boolean done = false;

            mime = parser.getAttributeValue(null, "type");

            while (!done) {
                int eventType = parser.next();

                if (eventType == XmlPullParser.END_TAG) {
                    if (ELEMENT_NAME.equals(parser.getName())) {
                        done = true;
                    }
                }
                else if (eventType == XmlPullParser.TEXT) {
                    contents = parser.getText();
                }
            }

            if (contents != null)
                return new BitsOfBinary(mime, contents);
            else
                return null;
        }

    }

}
