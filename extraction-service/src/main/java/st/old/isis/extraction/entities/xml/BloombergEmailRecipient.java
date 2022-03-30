package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BloombergEmailRecipient", propOrder = {
    "userInfo",
    "forwardedBy",
    "forwardedTo"
})
public class BloombergEmailRecipient {

    @XmlElement(name = "UserInfo", required = true)
    protected BloombergEmailUserInfo userInfo;
    @XmlElement(name = "ForwardedBy", required = true)
    protected String forwardedBy;
    @XmlElement(name = "ForwardedTo", required = true)
    protected BloombergEmailForwardedTo forwardedTo;

    public BloombergEmailUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(BloombergEmailUserInfo value) {
        this.userInfo = value;
    }

    public String getForwardedBy() {
        return forwardedBy;
    }

    public void setForwardedBy(String value) {
        this.forwardedBy = value;
    }

    public BloombergEmailForwardedTo getForwardedTo() {
        return forwardedTo;
    }

    public void setForwardedTo(BloombergEmailForwardedTo value) {
        this.forwardedTo = value;
    }

}
