package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = {"message"})
@XmlRootElement(name = "FileDump")
public class BloombergEmail {

  @XmlElement(name = "Message")
  protected List<BloombergEmailMessage> message;

  public List<BloombergEmailMessage> getConversation() {
    if (message == null) {
      message = new ArrayList<>();
    }
    return this.message;
  }
}
