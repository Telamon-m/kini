package org.junginger.kini;

import java.util.Collection;

public interface Section extends IniMap {

    String getSectionName();

    Collection<String> getCommentLines();

}
