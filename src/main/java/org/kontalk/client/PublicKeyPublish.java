package org.kontalk.client;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smack.util.stringencoder.Base64;


/**
 * XEP-0189: Public Key Publishing
 * http://xmpp.org/extensions/xep-0189.html#request
 */
public class PublicKeyPublish extends IQ {

    public static final String NAMESPACE = "urn:xmpp:pubkey:2";
    public static final String ELEMENT_NAME = "pubkey";

    private static XmlStringBuilder sChildElement;

    private byte[] mPublicKey;

    private PublicKeyPublish(IQ.Type type, byte[] publicKey) {
        setType(type);
        mPublicKey = publicKey;
    }

    public PublicKeyPublish() {
        // default IQ with type get
    }

    public byte[] getPublicKey() {
        return mPublicKey;
    }

    @Override
    public CharSequence getChildElementXML() {
        if (mPublicKey != null) {
            return new XmlStringBuilder()
                .halfOpenElement(ELEMENT_NAME)
                .xmlnsAttribute(NAMESPACE)
                .rightAngleBracket()
                .append(Base64.encodeToString(mPublicKey))
                .closeElement(ELEMENT_NAME);
        }
        else {
            if (sChildElement == null) {
                sChildElement = new XmlStringBuilder()
                    .halfOpenElement(ELEMENT_NAME)
                    .xmlnsAttribute(NAMESPACE)
                    .closeEmptyElement();
            }
            return sChildElement;
        }
    }

    // TODO IQProvider

}
