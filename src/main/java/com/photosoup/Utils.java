package com.photosoup;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class Utils {

    private Utils() {
    }

    static String getExtension(File file) {
        return FilenameUtils.getExtension(file.getName());
    }

    static File findUniqueFileName(File directory, String name, String extension) {
        int index = 1;
        File file = new File(directory, String.format("%s.%s", name, extension));
        while (file.exists()) {
            file = new File(directory, String.format("%s - %s.%s", name, index, extension));
            index++;
        }
        return file;
    }
}
