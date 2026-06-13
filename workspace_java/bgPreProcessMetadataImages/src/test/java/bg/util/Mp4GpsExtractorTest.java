package bg.util;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

/**
 * Tests unitaires pour {@link Mp4GpsExtractor}.
 * Les méthodes de parsing pure sont testables sans fichier réel.
 * Les tests d'intégration (avec un vrai MP4) sont commentés.
 */
public class Mp4GpsExtractorTest {

    // -------------------------------------------------------------------------
    // parseISO6709
    // -------------------------------------------------------------------------

    @Test
    public void testParseISO6709_decimal_with_altitude() {
        PositionGps2 pos = Mp4GpsExtractor.parseISO6709("+44.2374+001.4766+100.000/", "test.mp4");
        assertNotNull(pos);
        assertEquals(44.2374, pos.getLatitude(), 1e-4);
        assertEquals(1.4766, pos.getLongitude(), 1e-4);
        assertEquals(100.0, pos.getAltitudeMeters(), 1e-3);
    }

    @Test
    public void testParseISO6709_decimal_without_altitude() {
        PositionGps2 pos = Mp4GpsExtractor.parseISO6709("+44.2374+001.4766/", "test.mp4");
        assertNotNull(pos);
        assertEquals(44.2374, pos.getLatitude(), 1e-4);
        assertEquals(1.4766, pos.getLongitude(), 1e-4);
        assertNull(pos.getAltitudeMeters());
    }

    @Test
    public void testParseISO6709_negative_coords() {
        PositionGps2 pos = Mp4GpsExtractor.parseISO6709("-33.8688+151.2093+50.0/", "test.mp4");
        assertNotNull(pos);
        assertEquals(-33.8688, pos.getLatitude(), 1e-4);
        assertEquals(151.2093, pos.getLongitude(), 1e-4);
    }

    @Test
    public void testParseISO6709_null_returns_null() {
        assertNull(Mp4GpsExtractor.parseISO6709(null, "test.mp4"));
    }

    @Test
    public void testParseISO6709_invalid_string_returns_null() {
        assertNull(Mp4GpsExtractor.parseISO6709("not-a-gps-string", "test.mp4"));
    }

    @Test
    public void testParseISO6709_zero_coords_returns_null() {
        // (0, 0) is treated as invalid
        assertNull(Mp4GpsExtractor.parseISO6709("+00.0000+000.0000/", "test.mp4"));
    }

    // -------------------------------------------------------------------------
    // parseExiftoolDms
    // -------------------------------------------------------------------------

    @Test
    public void testParseExiftoolDms_north() {
        // 44 deg 14' 14.64" N => 44 + 14/60 + 14.64/3600 = 44.23740
        Double val = Mp4GpsExtractor.parseExiftoolDms("44 deg 14' 14.64\" N");
        assertNotNull(val);
        assertEquals(44.23740, val, 1e-4);
    }

    @Test
    public void testParseExiftoolDms_east() {
        // 1 deg 28' 35.76" E => 1 + 28/60 + 35.76/3600 = 1.47660
        Double val = Mp4GpsExtractor.parseExiftoolDms("1 deg 28' 35.76\" E");
        assertNotNull(val);
        assertEquals(1.47660, val, 1e-4);
    }

    @Test
    public void testParseExiftoolDms_south_is_negative() {
        Double val = Mp4GpsExtractor.parseExiftoolDms("33 deg 52' 7.68\" S");
        assertNotNull(val);
        assertTrue(val < 0);
        assertEquals(-33.8688, val, 1e-4);
    }

    @Test
    public void testParseExiftoolDms_west_is_negative() {
        Double val = Mp4GpsExtractor.parseExiftoolDms("74 deg 0' 21.6\" W");
        assertNotNull(val);
        assertTrue(val < 0);
    }

    @Test
    public void testParseExiftoolDms_null_returns_null() {
        assertNull(Mp4GpsExtractor.parseExiftoolDms(null));
    }

    @Test
    public void testParseExiftoolDms_invalid_returns_null() {
        assertNull(Mp4GpsExtractor.parseExiftoolDms("not-a-dms"));
    }

    // -------------------------------------------------------------------------
    // parseXmpDm
    // -------------------------------------------------------------------------

    @Test
    public void testParseXmpDm_north() {
        Double val = Mp4GpsExtractor.parseXmpDm("44,14.244N");
        assertNotNull(val);
        assertEquals(44.2374, val, 1e-4);
    }

    @Test
    public void testParseXmpDm_east() {
        Double val = Mp4GpsExtractor.parseXmpDm("1,28.596E");
        assertNotNull(val);
        assertEquals(1.4766, val, 1e-4);
    }

    @Test
    public void testParseXmpDm_south_is_negative() {
        Double val = Mp4GpsExtractor.parseXmpDm("33,52.128S");
        assertNotNull(val);
        assertTrue(val < 0);
    }

    @Test
    public void testParseXmpDm_decimal_fallback() {
        Double val = Mp4GpsExtractor.parseXmpDm("44.2374");
        assertNotNull(val);
        assertEquals(44.2374, val, 1e-4);
    }

    @Test
    public void testParseXmpDm_null_returns_null() {
        assertNull(Mp4GpsExtractor.parseXmpDm(null));
    }

    // -------------------------------------------------------------------------
    // parseJsonNumber
    // -------------------------------------------------------------------------

    @Test
    public void testParseJsonNumber_latitude() {
        String json = "[{\"SourceFile\":\"test.mp4\",\"GPSLatitude\":44.2374,\"GPSLongitude\":1.4766,\"GPSAltitude\":100.0}]";
        Double lat = Mp4GpsExtractor.parseJsonNumber(json, "GPSLatitude");
        assertNotNull(lat);
        assertEquals(44.2374, lat, 1e-4);
    }

    @Test
    public void testParseJsonNumber_negative() {
        String json = "[{\"GPSLatitude\":-33.8688}]";
        Double lat = Mp4GpsExtractor.parseJsonNumber(json, "GPSLatitude");
        assertNotNull(lat);
        assertEquals(-33.8688, lat, 1e-4);
    }

    @Test
    public void testParseJsonNumber_missing_field_returns_null() {
        String json = "[{\"GPSLongitude\":1.4766}]";
        assertNull(Mp4GpsExtractor.parseJsonNumber(json, "GPSLatitude"));
    }

    // -------------------------------------------------------------------------
    // isValidGps
    // -------------------------------------------------------------------------

    @Test
    public void testIsValidGps_valid() {
        assertTrue(Mp4GpsExtractor.isValidGps(44.2374, 1.4766));
    }

    @Test
    public void testIsValidGps_zero_zero_is_invalid() {
        assertFalse(Mp4GpsExtractor.isValidGps(0.0, 0.0));
    }

    @Test
    public void testIsValidGps_out_of_range_lat() {
        assertFalse(Mp4GpsExtractor.isValidGps(91.0, 1.0));
    }

    @Test
    public void testIsValidGps_out_of_range_lon() {
        assertFalse(Mp4GpsExtractor.isValidGps(44.0, 181.0));
    }

    // -------------------------------------------------------------------------
    // Integration test (requires a real MP4 file — disabled by default)
    // To enable: place a GPS-tagged MP4 at src/test/resources/test_gps.mp4
    // and remove the leading // from @Test below.
    // -------------------------------------------------------------------------

    //@Test
    public void integrationTest_mp4WithGps() {
        File mp4 = new File("src/test/resources/test_gps.mp4");
        if (!mp4.exists()) {
            System.out.println("[SKIP] integrationTest_mp4WithGps: " + mp4.getAbsolutePath() + " not found");
            return;
        }
        PositionGps2 pos = Mp4GpsExtractor.extractFromVideoFile(mp4);
        assertNotNull("GPS should be found in " + mp4.getName(), pos);
        System.out.println("integrationTest_mp4WithGps result: " + pos);
    }

    //@Test
    public void integrationTest_mp4WithoutGps() {
        File mp4 = new File("src/test/resources/test_no_gps.mp4");
        if (!mp4.exists()) {
            System.out.println("[SKIP] integrationTest_mp4WithoutGps: " + mp4.getAbsolutePath() + " not found");
            return;
        }
        PositionGps2 pos = Mp4GpsExtractor.extractFromVideoFile(mp4);
        assertNull("No GPS expected in " + mp4.getName(), pos);
    }
}
