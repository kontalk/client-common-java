/*
 * Kontalk client common library
 * Copyright (C) 2016 Kontalk Devteam <devteam@kontalk.org>

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
    private static final String REGISTER = "register";
    private static final String UNREGISTER = "unregister";

    private final String mProvider;
    private final String mRegId;

    private PushRegistration(String element, String provider, String regId) {
        super(element, NAMESPACE);
        setType(Type.set);
        mProvider = provider;
        mRegId = regId;
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        xml.attribute("provider", mProvider);

        if (mRegId != null) {
            xml.rightAngleBracket()
                .append(mRegId);
        }
        else {
            xml.setEmptyElement();
        }

        return xml;
    }

    public static PushRegistration register(String provider, String regId) {
        return new PushRegistration(REGISTER, provider, regId);
    }

    public static PushRegistration unregister(String provider) {
        return new PushRegistration(UNREGISTER, provider, null);
    }

}
