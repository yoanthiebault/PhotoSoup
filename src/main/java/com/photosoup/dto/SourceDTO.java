package com.photosoup.dto;

import java.io.File;
import java.time.temporal.TemporalAmount;

public class SourceDTO {
    private final File folder;
    private final TemporalAmount offset;

    public SourceDTO(File folder, TemporalAmount offset) {
        this.folder = folder;
        this.offset = offset;
    }

    public File getFolder() {
        return folder;
    }

    public TemporalAmount getOffset() {
        return offset;
    }
}
