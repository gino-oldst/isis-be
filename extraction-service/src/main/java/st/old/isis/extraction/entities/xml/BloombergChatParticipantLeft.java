package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "BloombergChatParticipantLeft",
    propOrder = {"conversationID", "dateTimeUTC", "user"})
public class BloombergChatParticipantLeft {

  @XmlElement(name = "ConversationID", required = true)
  protected String conversationID;

  @XmlElement(name = "DateTimeUTC")
  protected long dateTimeUTC;

  @XmlElement(name = "User", required = true)
  protected BloombergChatUser user;

  public String getConversationID() {
    return conversationID;
  }

  public void setConversationID(String value) {
    this.conversationID = value;
  }

  public long getDateTimeUTC() {
    return dateTimeUTC;
  }

  public void setDateTimeUTC(long value) {
    this.dateTimeUTC = value;
  }

  public BloombergChatUser getUser() {
    return user;
  }

  public void setUser(BloombergChatUser value) {
    this.user = value;
  }
}
