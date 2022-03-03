package st.old.isis.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import st.old.isis.exception.IsisException;

@Service
@RequiredArgsConstructor
public class ObjectUtils {

  private final ObjectMapper mapper;

  public String toString(@NonNull final Object obj) throws IsisException {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw IsisException.exception("Cannot convert object to string", "JsonProcessingException");
    }
  }
}
