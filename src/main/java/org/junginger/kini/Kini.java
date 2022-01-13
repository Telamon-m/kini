package org.junginger.kini;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Kini {

    private final Pattern PATTERN_SECTION = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    private final Pattern PATTERN_COMMENT = Pattern.compile("^(;)(.*)");
    private final Pattern PATTERN_KEY_VALUE_PAIR = Pattern.compile("\\s*([^=]*)=(.*)");

    @Getter
    private final boolean trimCommentLines;

    @Getter
    private final boolean isCaseSensitive;

    public static Kini defaultKini() {
        return Kini.builder().trimCommentLines(true).isCaseSensitive(false).build();
    }

    public IniFile load(String iniFile) throws IOException {
        return load(new StringReader(iniFile));
    }

    public IniFile load(InputStream inputStream) throws IOException {
        return load(new InputStreamReader(inputStream));
    }

    public IniFile load(Reader reader) throws IOException {
        List<String> commentLines = new ArrayList<>();
        Map<String, Section> sections = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            String sectionName = null;
            Map<String, String> sectionMap = null;
            List<String> sectionCommentLines = null;
            while ((line = bufferedReader.readLine()) != null) {
                Matcher m = PATTERN_COMMENT.matcher(line);
                if (m.matches()) {
                    String comment = m.group(2);
                    if (trimCommentLines) {
                        comment = comment.trim();
                    }
                    commentLines.add(comment);
                    if (sectionCommentLines != null) {
                        sectionCommentLines.add(comment);
                    }
                } else {
                    m = PATTERN_SECTION.matcher(line);
                    if (m.matches()) {
                        // A new sectionName begins
                        if (sectionName != null) {
                            sections.put(sectionName, new SectionImpl(sectionMap, sectionName, sectionCommentLines, isCaseSensitive));
                        }
                        sectionName = m.group(1).trim();
                        sectionMap = new HashMap<>();
                        sectionCommentLines = new ArrayList<>();
                    } else if (sectionName != null) {
                        m = PATTERN_KEY_VALUE_PAIR.matcher(line);
                        if (m.matches()) {
                            String key = m.group(1).trim();
                            String value = m.group(2).trim();
                            sectionMap.put(key, value);
                        }
                    }
                }
            }
            if (sectionName != null) {
                sections.put(sectionName, new SectionImpl(sectionMap, sectionName, sectionCommentLines, isCaseSensitive));
            }
        }
        return new IniFile(sections.keySet(), commentLines, sections, isCaseSensitive);
    }
}
