package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "BloombergConversation",
    propOrder = {
      "roomID",
      "startTimeUTC",
      "endTimeUTC",
      "message",
      "attachment",
      "participantEntered",
      "participantLeft"
    })
public class BloombergChatConversation {

  @XmlElement(name = "RoomID", required = true)
  protected String roomID;

  @XmlElement(name = "StartTimeUTC")
  protected long startTimeUTC;

  @XmlElement(name = "EndTimeUTC")
  protected long endTimeUTC;

  @XmlElement(name = "Message")
  protected List<BloombergChatMessage> message;

  @XmlElement(name = "Attachment")
  protected List<BloombergChatAttachment> attachment;

  @XmlElement(name = "ParticipantEntered")
  protected List<BloombergChatParticipantEntered> participantEntered;

  @XmlElement(name = "ParticipantLeft")
  protected List<BloombergChatParticipantLeft> participantLeft;

  public String getRoomID() {
    return roomID;
  }

  public void setRoomID(String value) {
    this.roomID = value;
  }

  public long getStartTimeUTC() {
    return startTimeUTC;
  }

  public void setStartTimeUTC(long value) {
    this.startTimeUTC = value;
  }

  public long getEndTimeUTC() {
    return endTimeUTC;
  }

  public void setEndTimeUTC(long value) {
    this.endTimeUTC = value;
  }

  public List<BloombergChatMessage> getMessage() {
    if (message == null) {
      message = new ArrayList<>();
    }
    return this.message;
  }

  public List<BloombergChatAttachment> getAttachment() {
    if (attachment == null) {
      attachment = new ArrayList<>();
    }
    return this.attachment;
  }

  public List<BloombergChatParticipantEntered> getParticipantEntered() {
    if (participantEntered == null) {
      participantEntered = new ArrayList<>();
    }
    return this.participantEntered;
  }

  public List<BloombergChatParticipantLeft> getParticipantLeft() {
    if (participantLeft == null) {
      participantLeft = new ArrayList<>();
    }
    return this.participantLeft;
  }
}
