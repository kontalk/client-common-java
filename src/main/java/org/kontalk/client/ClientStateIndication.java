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

import org.jivesoftware.smack.packet.Stanza;


/**
 * XEP-0352: Client State Indication
 * @author Daniele Ricci
 */
public final class ClientStateIndication extends Stanza {
    public static final String NAMESPACE = "urn:xmpp:csi:0";

    private static final String ELEMENT_ACTIVE = "active";
    private static final String ELEMENT_INACTIVE = "inactive";

    public static final ClientStateIndication ACTIVE =
        new ClientStateIndication("<" + ELEMENT_ACTIVE + " xmlns='" + NAMESPACE + "'/>");
    public static final ClientStateIndication INACTIVE =
        new ClientStateIndication("<" + ELEMENT_INACTIVE + " xmlns='" + NAMESPACE + "'/>");

    private final String _xml;

    private ClientStateIndication(String xml) {
        _xml = xml;
    }

    @Override
    public String toXML() {
        return _xml;
    }

}
