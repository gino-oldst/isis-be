package st.old.isis.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import st.old.isis.dto.metadata.Metadata;

import javax.persistence.AttributeConverter;

@Slf4j
@RequiredArgsConstructor
public class MetadataConverter implements AttributeConverter<Metadata, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(Metadata surveyResponse) {

        var objectString = "";

        try {
            objectString = objectMapper.writeValueAsString(surveyResponse);
        } catch (JsonProcessingException e) {
            log.error("Metadata conversion exception: " + e.getMessage(), e);
        }

        return objectString;
    }

    @Override
    public Metadata convertToEntityAttribute(String s) {
        Metadata object = null;

        try {
            object = objectMapper.readValue(s, Metadata.class);
        } catch (JsonProcessingException e) {
            log.error("Metadata conversion exception: " + e.getMessage(), e);
        }

        return object;
    }
}
