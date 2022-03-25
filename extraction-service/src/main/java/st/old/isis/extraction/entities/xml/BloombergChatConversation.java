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
      message = new ArrayList<BloombergChatMessage>();
    }
    return this.message;
  }

  public List<BloombergChatParticipantEntered> getParticipantEntered() {
    if (participantEntered == null) {
      participantEntered = new ArrayList<BloombergChatParticipantEntered>();
    }
    return this.participantEntered;
  }

  public List<BloombergChatParticipantLeft> getParticipantLeft() {
    if (participantLeft == null) {
      participantLeft = new ArrayList<BloombergChatParticipantLeft>();
    }
    return this.participantLeft;
  }
}
