package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "BloombergChatUser",
    propOrder = {
      "loginName",
      "firstName",
      "lastName",
      "uuid",
      "firmNumber",
      "accountNumber",
      "companyName",
      "emailAddress",
      "corporateEmailAddress"
    })
public class BloombergChatUser {

  @XmlElement(name = "LoginName", required = true)
  protected String loginName;

  @XmlElement(name = "FirstName", required = true)
  protected String firstName;

  @XmlElement(name = "LastName", required = true)
  protected String lastName;

  @XmlElement(name = "UUID", required = true)
  protected String uuid;

  @XmlElement(name = "FirmNumber", required = true)
  protected String firmNumber;

  @XmlElement(name = "AccountNumber", required = true)
  protected String accountNumber;

  @XmlElement(name = "CompanyName", required = true)
  protected String companyName;

  @XmlElement(name = "EmailAddress", required = true)
  protected String emailAddress;

  @XmlElement(name = "CorporateEmailAddress", required = true)
  protected String corporateEmailAddress;

  public String getLoginName() {
    return loginName;
  }

  public void setLoginName(String value) {
    this.loginName = value;
  }

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

  public String getUUID() {
    return uuid;
  }

  public void setUUID(String value) {
    this.uuid = value;
  }

  public String getFirmNumber() {
    return firmNumber;
  }

  public void setFirmNumber(String value) {
    this.firmNumber = value;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String value) {
    this.accountNumber = value;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String value) {
    this.companyName = value;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String value) {
    this.emailAddress = value;
  }

  public String getCorporateEmailAddress() {
    return corporateEmailAddress;
  }

  public void setCorporateEmailAddress(String value) {
    this.corporateEmailAddress = value;
  }
}
