package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BloombergEmailForwardedTo", propOrder = {
    "recipient"
})
public class BloombergEmailForwardedTo {

    @XmlElement(name = "Recipient", required = true)
    protected BloombergEmailRecipient recipient;

    public BloombergEmailRecipient getRecipient() {
        return recipient;
    }

    public void setRecipient(BloombergEmailRecipient value) {
        this.recipient = value;
    }

}
