package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BloombergEmailOriginalSender", propOrder = {
    "userInfo"
})
public class BloombergEmailOriginalSender {

    @XmlElement(name = "UserInfo")
    protected BloombergEmailUserInfo userInfo;
    public BloombergEmailUserInfo getUserInfo() {
        return userInfo;
    }
}
