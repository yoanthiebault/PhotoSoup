package com.photosoup.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class SourceFolder {
    private File folder;
    private Collection<SourcePhoto> photos = new ArrayList<>();

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public Collection<SourcePhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(Collection<SourcePhoto> photos) {
        this.photos = photos;
    }
}
