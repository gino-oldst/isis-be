package st.old.isis.dto.metadata;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BloombergEmailRecipientMetadata.class, name = "BLOOMBERG_EMAIL_RECIPIENT")
})
public abstract class Metadata implements Serializable {

    private static final long serialVersionUID = -6172184058559114767L;

}
