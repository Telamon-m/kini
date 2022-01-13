package org.junginger.kini;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Locale.ROOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KiniTests {

    private static final String SECTION_1 = "owner";
    private static final String SECTION_2 = "database";

    private static final String COMMENT_1 = "last modified 1 April 2001 by John Doe";
    private static final String COMMENT_3 = "Foo Bar foofoo";
    private static final String COMMENT_2 = "use IP address in case network name resolution is not working";

    private static final String SECTION_1_K1 = "name";
    private static final String SECTION_1_K1_V = "John Doe";
    private static final String SECTION_1_K2 = "organization";
    private static final String SECTION_1_K2_V = "Acme Widgets Inc.";

    private static final String SECTION_2_K1 = "server";
    private static final String SECTION_2_K1_V = "192.0.2.62";
    private static final String SECTION_2_K2 = "port";
    private static final String SECTION_2_K2_V = "143";
    private static final String SECTION_2_K3 = "file";
    private static final String SECTION_2_K3_V = "\"payroll.dat\"";

    private static final Map<String, String> SECTION_MAP_1 = Map.of(SECTION_1_K1, SECTION_1_K1_V, SECTION_1_K2, SECTION_1_K2_V);

    private static final Map<String, String> SECTION_MAP_2 = Map.of(SECTION_2_K1, SECTION_2_K1_V, SECTION_2_K2, SECTION_2_K2_V, SECTION_2_K3, SECTION_2_K3_V);

    private static void assertSection(Section section, String sectionName, Collection<String> comments, Map<String, String> sectionMap) {
        assertSection(section, sectionName, comments, sectionMap, true);
    }

    private static void assertSection(Section section, String sectionName, Collection<String> comments, Map<String, String> sectionMap, boolean isCaseSensitive) {
        assertThat(section).isNotNull();
        if (isCaseSensitive) {
            assertThat(section.getSectionName()).isEqualTo(sectionName);
        } else {
            assertThat(section.getSectionName()).isEqualToIgnoringCase(sectionName);
        }
        assertThat(section.getCommentLines().size()).isEqualTo(comments.size());
        assertThat(section.getCommentLines().containsAll(comments)).isTrue();

        assertThat(section.getKeys().size()).isEqualTo(sectionMap.size());
        section.getKeys().forEach(key1 -> sectionMap.keySet().stream().filter(k -> k.equalsIgnoreCase(key1)).findFirst().orElseThrow(() -> new IllegalStateException("Key " + key1 + " not found")));
        sectionMap.forEach((k, v) -> assertThat(section.getValue(k)).isEqualTo(v));

        Map<String, String> map = section.asMap();
        assertThrows(UnsupportedOperationException.class, () -> map.put("Foo Bar" + System.currentTimeMillis(), "asdf"));
        if (isCaseSensitive) {
            assertThat(map.equals(sectionMap)).isTrue();
        } else {
            map.forEach((k1, v1) -> assertThat(sectionMap.entrySet().stream().filter(k2 -> k2.getKey().equalsIgnoreCase(k1)).findFirst().orElseThrow(() -> new IllegalStateException("Could not find key ")).getValue()).isEqualTo(v1));
        }
    }

    private void assertIniFile(IniFile iniFile, boolean isCaseSensitive) {
        assertThat(iniFile.getValue(SECTION_1_K1)).isEqualTo(SECTION_1_K1_V);
        assertThat(iniFile.getValue(SECTION_1_K2)).isEqualTo(SECTION_1_K2_V);
        assertThat(iniFile.getValue(SECTION_2_K1)).isEqualTo(SECTION_2_K1_V);
        assertThat(iniFile.getValue(SECTION_2_K2)).isEqualTo(SECTION_2_K2_V);
        assertThat(iniFile.getValue(SECTION_2_K3)).isEqualTo(SECTION_2_K3_V);
        if (!isCaseSensitive) {
            assertThat(iniFile.getValue(SECTION_1_K2.toUpperCase(ROOT))).isEqualTo(SECTION_1_K2_V);
        }
    }

    @Test
    void testBuilder() {
        boolean isTrimComments = false;
        boolean isCaseSensitive = false;

        Kini kini = Kini.builder().trimCommentLines(isTrimComments).build();
        assertThat(kini.isTrimCommentLines()).isEqualTo(isTrimComments);
        assertThat(kini.isCaseSensitive()).isEqualTo(isCaseSensitive);

        kini = Kini.builder().trimCommentLines(isTrimComments).isCaseSensitive(isCaseSensitive).build();
        assertThat(kini.isTrimCommentLines()).isEqualTo(isTrimComments);
        assertThat(kini.isCaseSensitive()).isEqualTo(isCaseSensitive);

        isTrimComments = true;
        kini = Kini.builder().trimCommentLines(isTrimComments).isCaseSensitive(isCaseSensitive).build();
        assertThat(kini.isTrimCommentLines()).isEqualTo(isTrimComments);
        assertThat(kini.isCaseSensitive()).isEqualTo(isCaseSensitive);

        isCaseSensitive = true;
        kini = Kini.builder().trimCommentLines(isTrimComments).isCaseSensitive(isCaseSensitive).build();
        assertThat(kini.isTrimCommentLines()).isEqualTo(isTrimComments);
        assertThat(kini.isCaseSensitive()).isEqualTo(isCaseSensitive);

        // default builders
        kini = Kini.builder().build();
        assertThat(kini.isTrimCommentLines()).isEqualTo(false);
        assertThat(kini.isCaseSensitive()).isEqualTo(false);

        kini = Kini.defaultKini();
        assertThat(kini.isTrimCommentLines()).isEqualTo(true);
        assertThat(kini.isCaseSensitive()).isEqualTo(false);
    }


    @Test
    void testParsing() throws IOException {
        InputStream in = getClass().getResourceAsStream("/file1.ini");
        assertThat(in).isNotNull();
        String s = IOUtils.toString(in, StandardCharsets.UTF_8);
        Kini kini = Kini.builder().trimCommentLines(true).build();
        var iniFile = kini.load(s);
        assertThat(iniFile).isNotNull();
        assertThat(iniFile.getSectionNames().size()).isEqualTo(2);
        assertThat(iniFile.getSectionNames().containsAll(List.of(SECTION_1, SECTION_2))).isTrue();

        assertThat(iniFile.getCommentLines().size()).isEqualTo(3);
        iniFile.getCommentLines().forEach(System.out::println);
        System.out.println(iniFile.getCommentLines());
        assertThat(iniFile.getCommentLines().containsAll(List.of(COMMENT_1, COMMENT_3, COMMENT_2))).isTrue();

        assertSection(iniFile.getSection(SECTION_1), SECTION_1, List.of(), SECTION_MAP_1);
        assertSection(iniFile.getSection(SECTION_2), SECTION_2, List.of(COMMENT_2), SECTION_MAP_2);

        Map<String, String> map = iniFile.asMap();
        assertThrows(UnsupportedOperationException.class, () -> map.put("Foo Bar" + System.currentTimeMillis(), "asdf"));
        assertThat(map.size()).isEqualTo(5);

        assertIniFile(iniFile, false);
    }

    @Test
    void testParsingWithCaseInsensitive() throws IOException {
        InputStream in = getClass().getResourceAsStream("/file1.ini");
        assertThat(in).isNotNull();
        String s = IOUtils.toString(in, StandardCharsets.UTF_8);
        Kini kini = Kini.builder().trimCommentLines(true).isCaseSensitive(false).build();
        IniFile iniFile = kini.load(s);
        assertThat(iniFile).isNotNull();
        assertThat(iniFile.getSectionNames().size()).isEqualTo(2);

        Map<String, String> map = new HashMap<>(SECTION_MAP_1);
        map.put(SECTION_1_K1.toLowerCase(ROOT), map.remove(SECTION_1_K1));
        map.put(SECTION_1_K2.toUpperCase(ROOT), map.remove(SECTION_1_K2));
        String sectionNameUpper = SECTION_1.toUpperCase(ROOT);
        assertSection(iniFile.getSection(sectionNameUpper), sectionNameUpper, List.of(), map, false);
    }

    @Test
    void testParsingWithCaseSensitive() throws IOException {
        InputStream in = getClass().getResourceAsStream("/file1.ini");
        assertThat(in).isNotNull();
        String s = IOUtils.toString(in, StandardCharsets.UTF_8);
        Kini kini = Kini.builder().trimCommentLines(true).isCaseSensitive(true).build();
        IniFile iniFile = kini.load(s);
        assertThat(iniFile).isNotNull();
        assertThat(iniFile.getSectionNames().size()).isEqualTo(2);

        String sectionNameUpper = SECTION_1.toUpperCase(ROOT);
        Section section = iniFile.getSection(sectionNameUpper);
        assertThat(section).isNull();
        section = iniFile.getSection(SECTION_1);

        assertSection(section, SECTION_1, List.of(), SECTION_MAP_1, true);

        assertThat(section.getValue(SECTION_1_K2)).isEqualTo("Acme Widgets Inc.");
        assertThat(section.getValue(SECTION_1_K2.toUpperCase(ROOT))).isNull();
    }

    @Test
    void testParsingStream() throws IOException {
        InputStream in = getClass().getResourceAsStream("/file1.ini");
        assertThat(in).isNotNull();
        Kini kini = Kini.builder().trimCommentLines(true).isCaseSensitive(true).build();
        IniFile iniFile = kini.load(in);
        assertThat(iniFile).isNotNull();
        assertThat(iniFile.getSectionNames().size()).isEqualTo(2);
    }
}