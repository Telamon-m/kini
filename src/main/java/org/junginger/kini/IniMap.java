package org.junginger.kini;

import java.util.Map;
import java.util.Set;

public interface IniMap {

    /**
     * Returns all keys/values as a Map
     *
     * @return a read-only Map object containing all keys and values of the ini file.
     */
    Map<String, String> asMap();

    String getValue(String key);

    Set<String> getKeys();
}
