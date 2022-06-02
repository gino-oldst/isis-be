package st.old.isis.dto;

import java.io.Serializable;

public interface Dto extends Serializable {
    default String toCsvString() {
        return this.toString();
    }
}
