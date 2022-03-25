package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = {"conversation"})
@XmlRootElement(name = "FileDump")
public class BloombergChat {

  @XmlElement(name = "Conversation")
  protected List<BloombergChatConversation> conversation;

  public List<BloombergChatConversation> getConversation() {
    if (conversation == null) {
      conversation = new ArrayList<>();
    }
    return this.conversation;
  }
}
