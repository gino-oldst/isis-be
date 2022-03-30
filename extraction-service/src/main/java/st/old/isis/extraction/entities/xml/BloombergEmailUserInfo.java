package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BloombergEmailUserInfo", propOrder = {
    "firstName",
    "lastName",
    "bloombergEmailAddress"
})
public class BloombergEmailUserInfo {

    @XmlElement(name = "FirstName", required = true)
    protected String firstName;
    @XmlElement(name = "LastName", required = true)
    protected String lastName;
    @XmlElement(name = "BloombergEmailAddress", required = true)
    protected String bloombergEmailAddress;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String value) {
        this.firstName = value;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String value) {
        this.lastName = value;
    }

    public String getBloombergEmailAddress() {
        return bloombergEmailAddress;
    }

    public void setBloombergEmailAddress(String value) {
        this.bloombergEmailAddress = value;
    }

}
