package org.vertx.mods.web.mapping;

import java.util.regex.Pattern;

/**
 * UrlMappingEntry holds pattern and filepath for each
 * url mapping defined in config.
 *
 * @author ArsenyYankovsky
 */
public class UrlMappingEntry {
    private Pattern pattern;
    private String filePath;

    public UrlMappingEntry(String pattern, String filePath) {
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        this.filePath = filePath;
    }

    public boolean matches(String url) {
        return pattern.matcher(url).matches();
    }

    public String getFilePath() {
        return filePath;
    }
}
