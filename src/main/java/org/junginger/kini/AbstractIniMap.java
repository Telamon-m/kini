package org.junginger.kini;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractIniMap implements IniMap {

    private final Map<String, String> entries;

    private final boolean isCaseSensitive;

    public AbstractIniMap(Map<String, String> entries, boolean isCaseSensitive) {
        this.entries = entries;
        this.isCaseSensitive = isCaseSensitive;
    }

    @Override
    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(entries);
    }

    @Override
    public String getValue(String key) {
        if (isCaseSensitive) {
            return entries.get(key);
        } else {
            Optional<Map.Entry<String, String>> entry = entries.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(key)).findFirst();
            return entry.map(Map.Entry::getValue).orElse(null);
        }
    }

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(entries.keySet());
    }
}
