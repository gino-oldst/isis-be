package st.old.isis.extraction.entities.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BloombergEmailAttachment", propOrder = {
    "fileName",
    "fileID",
    "fileSize"
})
public class BloombergEmailAttachment {

    @XmlElement(name = "FileName", required = true)
    protected String fileName;
    @XmlElement(name = "FileID", required = true)
    protected String fileID;
    @XmlElement(name = "FileSize")
    protected long fileSize;

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
