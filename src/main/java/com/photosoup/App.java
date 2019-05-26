package com.photosoup;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory;
import com.photosoup.dto.ConfigurationDTO;
import com.photosoup.dto.SourceDTO;
import com.photosoup.model.SourceFolder;
import com.photosoup.model.SourcePhoto;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class App {
    private final PhotoDateHandler dateHandler = new PhotoDateHandler();

    private static void printTagValue(final JpegImageMetadata jpegMetadata,
                                      final TagInfo tagInfo) {
        final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(tagInfo);
        if (field == null) {
            System.out.println(tagInfo.name + ": " + "Not Found.");
        } else {
            System.out.println(tagInfo.name + ": "
                    + field.getValueDescription());
        }
    }


    public void run(ConfigurationDTO configuration) {
//        sourceFolders.forEach(sourceFolder -> {
//            System.out.println("Source folder");
//            Stream.of(sourceFolder.getFolder().listFiles()).forEach(file -> {
//                try {
//                    final ImageMetadata metadata = Imaging.getMetadata(file);
//                    if (metadata instanceof JpegImageMetadata) {
//                        final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
//                        printTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME);
//                        printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
//                        printTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
//                    }
//                } catch (ImageReadException | IOException e) {
//                    e.printStackTrace();
//                }
//            });
//        });
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
            for (int i = 0; i < sortedPhotos.size(); i++) {
                final SourcePhoto original = sortedPhotos.get(i);
                final String copiedName = String.format("%s%05d.%s", configuration.getPrefix(), i, Utils.getExtension(original.getFile()));
                final File copied = new File(configuration.getOutput(), copiedName);
                FileUtils.copyFile(original.getFile(), copied);
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
                    sourcePhoto.setDateTime(dateHandler.extractDateTime(photo).plus(sourceDTO.getOffset()));
                    sourceFolder.getPhotos().add(sourcePhoto);
                }
            }
            result.add(sourceFolder);
        });
        return result;
    }


}
