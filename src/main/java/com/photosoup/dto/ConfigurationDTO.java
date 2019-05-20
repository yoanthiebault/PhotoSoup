package com.photosoup.dto;

import java.io.File;
import java.util.List;

public class ConfigurationDTO {
    private final List<SourceDTO> sources;
    private final File output;
    private final String prefix;

    public ConfigurationDTO(List<SourceDTO> sources, File output, String prefix) {
        this.sources = sources;
        this.output = output;
        this.prefix = prefix;
    }

    public List<SourceDTO> getSources() {
        return sources;
    }

    public File getOutput() {
        return output;
    }

    public String getPrefix() {
        return prefix;
    }
}
