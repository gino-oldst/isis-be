package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BloombergEmailSender", propOrder = {
    "userInfo"
})
public class BloombergEmailSender {

    @XmlElement(name = "UserInfo")
    protected BloombergEmailUserInfo userInfo;
    public BloombergEmailUserInfo getUserInfo() {
        return userInfo;
    }
}
