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

package org.kontalk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;

import org.jxmpp.util.XmppDateTime;


/**
 * <p>Message/CPIM generator and parser.</p>
 * <p>
 * To create a Message/CPIM message body, first construct a new object with the
 * necessary data, then use {@link #toString()} to retrieve the generated
 * Message/CPIM body. Note that all constructor fields are mandatory.<br>
 * </p>
 * <p>
 * You can use {@link #parse(String)} for a very basic Message/CPIM parser.
 * </p>
 * @author Daniele Ricci
 */
public class CPIMMessage {

    /** Charset used for byte encoding. */
    public static final String CHARSET = "utf-8";

    private static final String MIME_TYPE = "text/plain";
    private static final String TYPE = "Message/CPIM";

    private final String mFrom;
    private final String[] mTo;
    private final Date mDate;
    private final String mMime;
    private final CharSequence mBody;

    private StringBuilder mBuf;

    /** Constructs a new plain text message. */
    public CPIMMessage(String from, String[] to, Date date, String body) {
        this(from, to, date, MIME_TYPE, body);
    }

    public CPIMMessage(String from, String[] to, Date date, String mime, CharSequence body) {
        mFrom = from;
        mTo = to;
        mDate = date;
        mMime = mime;
        mBody = body;
    }

    public String getFrom() {
        return mFrom;
    }

    public String[] getTo() {
        return mTo;
    }

    public Date getDate() {
        return mDate;
    }

    public String getMime() {
        return mMime;
    }

    public CharSequence getBody() {
        return mBody;
    }

    public String toString() {
        if (mBuf == null) {
            String date = XmppDateTime.DateFormatType
                .XEP_0082_DATETIME_PROFILE.format(mDate);

            StringBuilder to = new StringBuilder();
            for(String item : mTo){
                if (to.length() > 0)
                    to.append("; ");
                to.append(item);
            }

            mBuf = new StringBuilder("Content-type: ")
                .append(TYPE)
                .append("\n\nFrom: ")
                .append(mFrom)
                .append("\nTo: ")
                .append(to.toString())
                .append("\nDateTime: ")
                .append(date)
                .append("\n\nContent-type: ")
                .append(mMime)
                .append("; charset=")
                .append(CHARSET)
                .append("\n\n")
                .append(mBody);
        }

        return mBuf.toString();
    }

    public byte[] toByteArray() throws UnsupportedEncodingException {
        return toString().getBytes(CHARSET);
    }

    /** A very bad CPIM parser. */
    public static CPIMMessage parse(String data) throws ParseException {
        CPIMParser p = new CPIMParser(data);

        String from = null,
            date = null,
            type = null,
            contents;
        String[] to = null;

        // first pass: CPIM content type
        CPIMParser.CPIMHeader h;
        boolean typeOk = false;
        while ((h = p.nextHeader()) != null && !typeOk) {
            if ("Content-type".equalsIgnoreCase(h.name) && TYPE.equalsIgnoreCase(h.value))
                typeOk = true;
        }

        if (!typeOk)
            throw new ParseException("Invalid content type", 0);

        // second pass: message headers
        while ((h = p.nextHeader()) != null) {
            if ("From".equalsIgnoreCase(h.name)) {
                from = h.value;
            }

            else if ("To".equalsIgnoreCase(h.name)) {
                to = h.value.split(";");
                for (int i = 0; i < to.length; i++) {
                    to[i] = to[i].trim();
                }
            }

            else if ("DateTime".equalsIgnoreCase(h.name)) {
                date = h.value;
            }
        }

        // third pass: message content type
        while ((h = p.nextHeader()) != null) {
            if ("Content-type".equalsIgnoreCase(h.name)) {
                type = h.value;

                int pos = type.indexOf(';');
                if (pos >= 0)
                    type = type.substring(0, pos).trim();
            }
        }

        // fourth pass: message content
        contents = p.getData();

        Date parsedDate = null;
        try {
            if (date != null) {
                parsedDate = XmppDateTime.DateFormatType
                    .XEP_0082_DATETIME_PROFILE.parse(date);
            }
        }
        catch (ParseException ignored) {
        }

        return new CPIMMessage(from, to, parsedDate, type, contents);
    }

    private static class CPIMParser {

        public static final class CPIMHeader {
            public final String name;
            public final String value;

            public CPIMHeader(String name, String value) {
                this.name = name;
                this.value = value;
            }

            public String toString() {
                return this.name + "=" + this.value;
            }
        }

        private BufferedReader mData;

        public CPIMParser(String data) {
            internalSetData(data);
        }

        public void internalSetData(String data) {
            mData = new BufferedReader(new StringReader(data));
        }

        public CPIMHeader nextHeader() {
            try {
                String line = mData.readLine();

                if (line != null && line.trim().length() > 0) {
                    int sep = line.indexOf(':');
                    if (sep >= 0) {
                        String name = line.substring(0, sep).trim();
                        String value = line.substring(sep + 1).trim();
                        return new CPIMHeader(name, value);
                    }
                }

            }
            catch (IOException e) {
                // not going to happen.
            }

            return null;
        }

        public String getData() {
            // read up all data
            StringBuilder buf = new StringBuilder();
            int c;
            try {
                while ((c = mData.read()) >= 0)
                    buf.append((char) c);

                // reader is no more needed
                mData.close();
            }
            catch (IOException e) {
                // not going to happen.
            }

            return buf.toString();
        }
    }

}
