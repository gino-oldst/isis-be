package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BloombergEmailMessage", propOrder = {
    "msgID",
    "msgTimeUTC",
    "sender",
    "recipient",
    "attachment",
    "msgBody",
    "subject"
})
public class BloombergEmailMessage {

    @XmlElement(name = "MsgID", required = true)
    protected String msgID;
    @XmlElement(name = "MsgTimeUTC")
    protected long msgTimeUTC;
    @XmlElement(name = "Sender", required = true)
    protected BloombergEmailSender sender;
    @XmlElement(name = "Recipient")
    protected List<BloombergEmailRecipient> recipient;
    @XmlElement(name = "Attachment")
    protected List<BloombergEmailAttachment> attachment;
    @XmlElement(name = "MsgBody", required = true)
    protected String msgBody;
    @XmlElement(name = "Subject", required = true)
    protected String subject;

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String value) {
        this.msgID = value;
    }

    public long getMsgTimeUTC() {
        return msgTimeUTC;
    }

    public void setMsgTimeUTC(long value) {
        this.msgTimeUTC = value;
    }

    public BloombergEmailSender getSender() {
        return sender;
    }

    public void setSender(BloombergEmailSender value) {
        this.sender = value;
    }

    public List<BloombergEmailRecipient> getRecipient() {
        if (recipient == null) {
            recipient = new ArrayList<BloombergEmailRecipient>();
        }
        return this.recipient;
    }

    public List<BloombergEmailAttachment> getAttachment() {
        if (attachment == null) {
            attachment = new ArrayList<BloombergEmailAttachment>();
        }
        return this.attachment;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String value) {
        this.msgBody = value;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String value) {
        this.subject = value;
    }

}
