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

import org.jivesoftware.smack.packet.IQ;


/** Extension for registering to push notifications. */
public class PushRegistration extends IQ {
    public static final String NAMESPACE = "http://kontalk.org/extensions/presence#push";
    public static final String ELEMENT_NAME = "register";

    private final String mProvider;
    private final String mRegId;

    public PushRegistration(String provider, String regId) {
        super(ELEMENT_NAME, NAMESPACE);
        setType(Type.set);
        mProvider = provider;
        mRegId = regId;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute("provider", mProvider)
            .rightAngleBracket()
            .append(mRegId);

        return xml;
    }

}
