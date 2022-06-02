package st.old.isis.dto;

import java.io.Serializable;
import java.util.List;

public interface CsvList extends Serializable {
    default List<String> toCsvStringList() {
        return List.of();
    }
}
