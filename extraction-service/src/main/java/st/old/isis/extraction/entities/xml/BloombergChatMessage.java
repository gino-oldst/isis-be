package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "BloombergChatMessage",
    propOrder = {"conversationID", "content", "dateTimeUTC", "user"})
public class BloombergChatMessage {

  @XmlElement(name = "ConversationID", required = true)
  protected String conversationID;

  @XmlElement(name = "Content", required = true)
  protected String content;

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

  public String getContent() {
    return content;
  }

  public void setContent(String value) {
    this.content = value;
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
