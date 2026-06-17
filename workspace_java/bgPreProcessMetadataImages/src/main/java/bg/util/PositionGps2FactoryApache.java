package bg.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;

public final class PositionGps2FactoryApache {

    private static final DateTimeFormatter EXIF_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    private PositionGps2FactoryApache() {
    }

    /**
     * Extrait latitude/longitude, altitude, date, orientation, GPS direction,
     * et yaw/pitch/roll depuis les métadonnées EXIF.
     *
     * @return null si aucune info GPS exploitable.
     */
    public static PositionGps2 extractPosition(File imageFile) {
        try {
            ImageMetadata metadata = Imaging.getMetadata(imageFile);
            if (!(metadata instanceof JpegImageMetadata jpegMetadata)) {
                return null;
            }

            TiffImageMetadata exif = jpegMetadata.getExif();
            if (exif == null) {
                return null;
            }

            double lat;
            double lon;
            Double altitude = null;
            LocalDateTime takenAt = null;

            String orientation = null;
            String gpsImgDirection = null;
            String gpsImgDirectionRef = null;
            Double yaw = null;
            Double pitch = null;
            Double roll = null;

            // GPS
            TiffImageMetadata.GpsInfo gpsInfo = exif.getGpsInfo();
            if (gpsInfo == null) {
                return null;
            }

            lat = gpsInfo.getLatitudeAsDegreesNorth();
            lon = gpsInfo.getLongitudeAsDegreesEast();

            if (Double.isNaN(lat) || Double.isNaN(lon)) {
                return null;
            }

            // Altitude
            TiffField altitudeField =
                    jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_ALTITUDE);
            TiffField altitudeRefField =
                    jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_ALTITUDE_REF);

            if (altitudeField != null) {
                altitude = extractDoubleValue(altitudeField);

                Integer altitudeRef = extractIntegerValue(altitudeRefField);
                if (altitude != null && altitudeRef != null && altitudeRef == 1) {
                    altitude = -altitude;
                }
            }

            // Date
            String exifDateString = null;

            TiffField dateOriginalField =
                    jpegMetadata.findExifValueWithExactMatch(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
            TiffField dateDigitizedField =
                    jpegMetadata.findExifValueWithExactMatch(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
            TiffField dateTimeField =
                    jpegMetadata.findExifValueWithExactMatch(TiffTagConstants.TIFF_TAG_DATE_TIME);

            if (dateOriginalField != null) {
                exifDateString = safeStringValue(dateOriginalField);
            }
            if (exifDateString == null && dateDigitizedField != null) {
                exifDateString = safeStringValue(dateDigitizedField);
            }
            if (exifDateString == null && dateTimeField != null) {
                exifDateString = safeStringValue(dateTimeField);
            }

            if (exifDateString != null) {
                takenAt = parseExifDate(exifDateString);
            }

            // Orientation
            TiffField orientationField =
                    jpegMetadata.findExifValueWithExactMatch(TiffTagConstants.TIFF_TAG_ORIENTATION);
            if (orientationField != null) {
                orientation = safeStringValue(orientationField);
                if (orientation == null) {
                    orientation = orientationField.getValueDescription();
                }
            }

            // GPS Img Direction
            TiffField gpsImgDirectionField =
                    jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION);
            if (gpsImgDirectionField != null) {
                Double dir = extractDoubleValue(gpsImgDirectionField);
                gpsImgDirection = dir != null ? String.valueOf(dir) : safeStringValue(gpsImgDirectionField);
            }

            // GPS Img Direction Ref
            TiffField gpsImgDirectionRefField =
                    jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_IMG_DIRECTION_REF);
            if (gpsImgDirectionRefField != null) {
                gpsImgDirectionRef = safeStringValue(gpsImgDirectionRefField);
            }

            // UserComment -> Yaw / Pitch / Roll
            TiffField userCommentField =
                    jpegMetadata.findExifValueWithExactMatch(ExifTagConstants.EXIF_TAG_USER_COMMENT);
            if (userCommentField != null) {
                byte[] bytes = getByteArray(userCommentField);
                String userComment = decodeExifUserComment(bytes);

                if (userComment != null) {
                    yaw = getAngleFromUserCommentAsDouble(userComment, "Yaw");
                    pitch = getAngleFromUserCommentAsDouble(userComment, "Pitch");
                    roll = getAngleFromUserCommentAsDouble(userComment, "Roll");
                }
            }

            return new PositionGps2(
                    lat,
                    lon,
                    altitude,
                    takenAt,
                    imageFile.getName(),
                    orientation,
                    gpsImgDirection,
                    gpsImgDirectionRef,
                    yaw,
                    pitch,
                    roll
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Double getAngleFromUserCommentAsDouble(String userComment, String tag) {
		String s = getAngleFromUserComment( userComment,  tag);
		Double v_r = null;
		if (s == null) {
			return null;
		}
		try {
			double v_d= Double.parseDouble(s);
			v_r = Math.toRadians(v_d);
		} catch (NumberFormatException e) {
			
		}
		return v_r;
	}

	private static Double extractDoubleValue(TiffField field) {
        if (field == null) {
            return null;
        }

        try {
            Object value = field.getValue();

            if (value instanceof Number number) {
                return number.doubleValue();
            }

            if (value instanceof Number[] numbers && numbers.length > 0 && numbers[0] != null) {
                return numbers[0].doubleValue();
            }

            if (value instanceof String s) {
                return tryParseDouble(s);
            }

            if (value instanceof String[] arr && arr.length > 0 && arr[0] != null) {
                return tryParseDouble(arr[0]);
            }
        } catch (Exception e) {
            // ignore
        }

        try {
            return tryParseDouble(field.getValueDescription());
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer extractIntegerValue(TiffField field) {
        if (field == null) {
            return null;
        }

        try {
            Object value = field.getValue();

            if (value instanceof Number number) {
                return number.intValue();
            }

            if (value instanceof Number[] numbers && numbers.length > 0 && numbers[0] != null) {
                return numbers[0].intValue();
            }

            if (value instanceof String s) {
                return tryParseInteger(s);
            }

            if (value instanceof String[] arr && arr.length > 0 && arr[0] != null) {
                return tryParseInteger(arr[0]);
            }
        } catch (Exception e) {
            // ignore
        }

        try {
            return tryParseInteger(field.getValueDescription());
        } catch (Exception e) {
            return null;
        }
    }

    public static String safeStringValue(TiffField field) {
        if (field == null) {
            return null;
        }

        try {
            Object value = field.getValue();

            if (value instanceof String s) {
                return unquote(s.trim());
            }

            if (value instanceof String[] arr && arr.length > 0 && arr[0] != null) {
                return unquote(arr[0].trim());
            }

            if (value != null) {
                return unquote(value.toString().trim());
            }
        } catch (Exception e) {
            // ignore
        }

        try {
            String desc = field.getValueDescription();
            if (desc != null) {
                return unquote(desc.trim());
            }
        } catch (Exception e) {
            // ignore
        }

        return null;
    }

    private static byte[] getByteArray(TiffField field) {
        if (field == null) {
            return null;
        }

        try {
            return field.getByteArrayValue();
        } catch (Exception e) {
            // ignore
        }

        try {
            Object value = field.getValue();
            if (value instanceof byte[] bytes) {
                return bytes;
            }
        } catch (Exception e) {
            // ignore
        }

        return null;
    }

    private static String decodeExifUserComment(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        if (bytes.length >= 8) {
            String prefix = new String(bytes, 0, 8, StandardCharsets.US_ASCII);
            byte[] content = Arrays.copyOfRange(bytes, 8, bytes.length);

            if (prefix.startsWith("ASCII")) {
                return new String(content, StandardCharsets.UTF_8)
                        .replace("\0", "")
                        .trim();
            }
        }

        return new String(bytes, StandardCharsets.UTF_8)
                .replace("\0", "")
                .trim();
    }

    private static String getAngleFromUserComment(String userComment, String key) {
        if (userComment == null || key == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(key + "\\s*:\\s*([-+]?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(userComment);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static LocalDateTime parseExifDate(String exifDate) {
        if (exifDate == null || exifDate.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(exifDate.trim(), EXIF_DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private static Double tryParseDouble(String s) {
        if (s == null) {
            return null;
        }

        String cleaned = unquote(s).trim();

        Matcher matcher = Pattern.compile("[-+]?\\d+(?:[.,]\\d+)?").matcher(cleaned);
        if (matcher.find()) {
            String number = matcher.group().replace(',', '.');
            try {
                return Double.parseDouble(number);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    private static Integer tryParseInteger(String s) {
        Double d = tryParseDouble(s);
        return d != null ? d.intValue() : null;
    }

    private static String unquote(String s) {
        if (s == null) {
            return null;
        }

        String out = s.trim();
        if (out.length() >= 2 && out.startsWith("'") && out.endsWith("'")) {
            out = out.substring(1, out.length() - 1);
        }
        return out;
    }
}