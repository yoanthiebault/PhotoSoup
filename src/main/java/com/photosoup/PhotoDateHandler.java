package com.photosoup;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoDateHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
    private static final DateTimeFormatter TZ_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final Pattern DATETIME_FILE_PATTERN = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})_(\\d{2})(\\d{2})(\\d{2})\\.\\w+");

    public static LocalDateTime extractDateTime(File photo) {
        try {
//            metadata.addDirectory();

            final Matcher matcher = DATETIME_FILE_PATTERN.matcher(photo.getName());
            if (matcher.matches()) {
                return LocalDateTime.of(
                        Integer.valueOf(matcher.group(1)),
                        Integer.valueOf(matcher.group(2)),
                        Integer.valueOf(matcher.group(3)),
                        Integer.valueOf(matcher.group(4)),
                        Integer.valueOf(matcher.group(5)),
                        Integer.valueOf(matcher.group(6))
                );
            }

            final Metadata metadata = ImageMetadataReader.readMetadata(photo);

            final Collection<ExifSubIFDDirectory> exifDirectories = metadata.getDirectoriesOfType(ExifSubIFDDirectory.class);
            if (exifDirectories.size() > 0) {
                final String value = exifDirectories
                        .stream()
                        .map(exifDirectory -> exifDirectory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElseThrow(IllegalStateException::new);
                return LocalDateTime.parse(value, FORMATTER);
            }

            final QuickTimeMetadataDirectory quickTimeDirectory = metadata.getFirstDirectoryOfType(QuickTimeMetadataDirectory.class);
            if (quickTimeDirectory != null) {
                final String value = quickTimeDirectory.getString(1286);
                return LocalDateTime.parse(value, TZ_FORMATTER);
            }
            throw new IllegalStateException(String.format("Unsuported file: %s", Utils.getExtension(photo)));
        } catch (IOException | ImageProcessingException e) {
            throw new RuntimeException(e);
        }

//        try {
//            final ImageMetadata metadata = Imaging.getMetadata(photo);
//            if (metadata instanceof JpegImageMetadata) {
//                final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
//                final TiffField field = jpegMetadata.findEXIFValueWithExactMatch(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
//                return LocalDateTime.parse(field.getValueDescription(), FORMATTER);
//            } else {
//                throw new IllegalStateException("Not supported");
//            }
//        } catch (ImageReadException | IOException e) {
//            throw new RuntimeException(e);
//        }

    }

}
