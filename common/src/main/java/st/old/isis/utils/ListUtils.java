package st.old.isis.utils;

import java.util.Collection;
import java.util.List;

public class ListUtils {

    public static boolean isNullOrEmpty( final Collection<?> c ) {
        return c == null || c.isEmpty();
    }

    public static boolean containsItemsFromList(final String input, final List<String> listOfString){
        return listOfString.stream().anyMatch(input::contains);
    }
}
