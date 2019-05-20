package com.photosoup.dto;

import java.io.File;
import java.time.Duration;

public class SourceDTO {
    private final File folder;
    private final Duration offset;

    public SourceDTO(File folder, Duration offset) {
        this.folder = folder;
        this.offset = offset;
    }

    public File getFolder() {
        return folder;
    }

    public Duration getOffset() {
        return offset;
    }
}
