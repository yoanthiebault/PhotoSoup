package com.photosoup.model;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;

public class SourcePhoto {
    private File file;
    private LocalDateTime dateTime;
    private TemporalAmount offset;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public TemporalAmount getOffset() {
        return offset;
    }

    public void setOffset(TemporalAmount offset) {
        this.offset = offset;
    }
}
