package org.junginger.kini;

import java.util.*;

import static java.util.Locale.ROOT;

public class IniFile extends AbstractIniMap implements IniMap {

    private final Set<String> sectionNames;

    private final List<String> comments;

    private final Map<String, Section> sections;

    private final Map<String, Section> sectionsUpper;

    private final boolean isCaseSensitive;

    public IniFile(Set<String> sectionNames, List<String> comments, Map<String, Section> sections, boolean isCaseSensitive) {
        super(allKeyValuePairs(sections), isCaseSensitive);
        this.sectionNames = sectionNames;
        this.comments = comments;
        this.sections = sections;
        this.isCaseSensitive = isCaseSensitive;

        sectionsUpper = new HashMap<>();
        // Alternative: Use a map which encapsulates this internally
        sections.forEach((k, v) -> sectionsUpper.put(k.toUpperCase(ROOT), v));
    }

    private static Map<String, String> allKeyValuePairs(Map<String, Section> sections) {
        Map<String, String> all = new HashMap<>();
        sections.forEach((k, v) -> all.putAll(v.asMap()));
        return all;
    }

    public Set<String> getSectionNames() {
        return Collections.unmodifiableSet(sectionNames);
    }

    public Collection<String> getCommentLines() {
        return Collections.unmodifiableCollection(comments);
    }

    public Section getSection(String sectionName) {
        if (isCaseSensitive) {
            return sections.get(sectionName);
        }
        return sectionsUpper.get(sectionName.toUpperCase(ROOT));
    }
}
