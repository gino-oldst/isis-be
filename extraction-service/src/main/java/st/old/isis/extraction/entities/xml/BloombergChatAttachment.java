package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "BloombergChatAttachment",
    propOrder = {"user", "conversationID", "dateTimeUTC", "fileName", "fileID", "fileSize"})
public class BloombergChatAttachment {

  @XmlElement(name = "User")
  protected BloombergChatUser user;

  @XmlElement(name = "ConversationID")
  protected String conversationID;

  @XmlElement(name = "DateTimeUTC")
  protected long dateTimeUTC;

  @XmlElement(name = "FileName")
  protected String fileName;

  @XmlElement(name = "FileID")
  protected String fileID;

  @XmlElement(name = "FileSize")
  protected long fileSize;

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

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String value) {
    this.fileName = value;
  }

  public String getFileID() {
    return fileID;
  }

  public void setFileID(String value) {
    this.fileID = value;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long value) {
    this.fileSize = value;
  }
}
