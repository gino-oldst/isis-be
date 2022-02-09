package st.old.isis.utils;

import java.util.Collection;

public class ListUtils {

    public static boolean isNullOrEmpty( final Collection<?> c ) {
        return c == null || c.isEmpty();
    }
}
