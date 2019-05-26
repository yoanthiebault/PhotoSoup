package com.photosoup;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class Utils {

    private Utils() {
    }

    static String getExtension(File file) {
        return FilenameUtils.getExtension(file.getName());
    }
}
