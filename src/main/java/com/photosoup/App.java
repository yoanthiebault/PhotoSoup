package com.photosoup;

import com.photosoup.dto.ConfigurationDTO;
import com.photosoup.model.SourceFolder;
import com.photosoup.model.SourcePhoto;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    private final PhotoDateHandler dateHandler = new PhotoDateHandler();

    public void run(ConfigurationDTO configuration) {
        if (!configuration.getOutput().exists()) {
            configuration.getOutput().mkdir();
        }
        if (configuration.getOutput().listFiles().length != 0) {
            throw new IllegalArgumentException("Output directory is not empty");
        }

        final List<SourceFolder> sourceFolders = indexesPhotos(configuration);

        final List<SourcePhoto> sortedPhotos = sourceFolders.stream()
                .flatMap(sourceFolder -> sourceFolder.getPhotos().stream())
                .sorted(Comparator.comparing(SourcePhoto::getDateTime))
                .collect(Collectors.toList());


        copyPhotos(configuration, sortedPhotos);
    }

    private void copyPhotos(ConfigurationDTO configuration, List<SourcePhoto> sortedPhotos) {
        try {
            final DateTimeFormatter fileNamePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
            for (int i = 0; i < sortedPhotos.size(); i++) {
                final SourcePhoto original = sortedPhotos.get(i);
                final File copied = Utils.findUniqueFileName(
                        configuration.getOutput(),
                        String.format("%s%s", configuration.getPrefix(), original.getDateTime().format(fileNamePattern)),
                        Utils.getExtension(original.getFile()));
                if (original.getOffset() != null) {
                    PhotoDateHandler.updateExif(original.getFile(), copied, original.getDateTime());
                } else {
                    FileUtils.copyFile(original.getFile(), copied);
                }

                if (i%100 == 0) {
                    System.out.println(String.format("Copied %s photos.", i+1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<SourceFolder> indexesPhotos(ConfigurationDTO configurationDTO) {
        final List<SourceFolder> result = new ArrayList<>();
        configurationDTO.getSources().forEach(sourceDTO -> {
            final SourceFolder sourceFolder = new SourceFolder();
            sourceFolder.setFolder(sourceDTO.getFolder());
            for (File photo : sourceDTO.getFolder().listFiles()) {
                if (!photo.getName().startsWith(".")) {
                    final SourcePhoto sourcePhoto = new SourcePhoto();
                    sourcePhoto.setFile(photo);
                    sourcePhoto.setOffset(sourceDTO.getOffset());
                    sourcePhoto.setDateTime(sourceDTO.getOffset() != null
                            ? PhotoDateHandler.extractDateTime(photo).plus(sourceDTO.getOffset())
                            : PhotoDateHandler.extractDateTime(photo));
                    sourceFolder.getPhotos().add(sourcePhoto);
                }
            }
            result.add(sourceFolder);
        });
        return result;
    }


}
