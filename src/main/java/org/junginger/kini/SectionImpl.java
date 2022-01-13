package org.junginger.kini;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SectionImpl extends AbstractIniMap implements Section {

    private final String sectionName;

    private final List<String> commentLines;

    public SectionImpl(Map<String, String> entries, String sectionName, List<String> commentLines, boolean isCaseSensitive) {
        super(entries, isCaseSensitive);
        this.sectionName = sectionName;
        this.commentLines = commentLines;
    }

    @Override
    public String getSectionName() {
        return sectionName;
    }

    @Override
    public Collection<String> getCommentLines() {
        return Collections.unmodifiableCollection(commentLines);
    }

}