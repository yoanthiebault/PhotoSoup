package com.photosoup;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.metadata.QuickTimeMetadataDirectory;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import java.io.*;
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

    static void updateExif(File file, File dst, LocalDateTime dateTime) {
        try {
            // note that metadata might be null if no metadata is found.
            final ImageMetadata metadata = Imaging.getMetadata(file);

            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            final TiffImageMetadata exif = jpegMetadata.getExif();
            final TiffOutputSet outputSet = exif.getOutputSet();

            final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
            exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
            exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
            exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, dateTime.format(FORMATTER));
            exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, dateTime.format(FORMATTER));

            try (FileOutputStream fos = new FileOutputStream(dst);
                 OutputStream os = new BufferedOutputStream(fos)) {
                new ExifRewriter().updateExifMetadataLossless(file, os, outputSet);
            }
        } catch (ImageReadException | IOException | ImageWriteException e) {
            throw new IllegalStateException(e);
        }
    }
}
