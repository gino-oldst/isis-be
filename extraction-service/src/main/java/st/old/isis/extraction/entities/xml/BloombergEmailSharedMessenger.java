package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BloombergEmailSharedMessenger", propOrder = {
    "userInfo"
})
public class BloombergEmailSharedMessenger {

    @XmlElement(name = "UserInfo")
    protected BloombergEmailUserInfo userInfo;
    public BloombergEmailUserInfo getUserInfo() {
        return userInfo;
    }
}
