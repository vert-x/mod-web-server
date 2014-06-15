package org.vertx.mods.web.mapping;

import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A UrlMapper implementation that implements regex url mapping.
 *
 * @author ArsenyYankovsky
 */
public class UrlMapper {
    private List<UrlMappingEntry> urlMappingEntries = new ArrayList<>();
    private String webRootPrefix;

    public UrlMapper(JsonObject urlMappings, String webRootPrefix) {
        this.webRootPrefix = webRootPrefix;

        for (String pattern : urlMappings.getFieldNames()) {
            urlMappingEntries.add(new UrlMappingEntry(pattern, urlMappings.getString(pattern)));
        }
    }

    public String getFilePath(String requestUrl) {
        for (UrlMappingEntry urlMappingEntry : urlMappingEntries) {
            if (urlMappingEntry.matches(requestUrl)) {
                return webRootPrefix + urlMappingEntry.getFilePath();
            }
        }

        return webRootPrefix + requestUrl;
    }
}
