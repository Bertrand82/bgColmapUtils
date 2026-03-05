package bg.util;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ElevationClient {

    private ElevationClient() {}

    // Ex: {"results":[{"latitude":48.85837,"longitude":2.294481,"elevation":35.0}],"status":"OK"}
    private static final Pattern ELEVATION_PATTERN =
            Pattern.compile("\"elevation\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");

    /** @return altitude en mètres (Double), ou null si non trouvé */
    public static Double getElevationOpenElevation(double lat, double lon) throws IOException {
        String url = String.format(Locale.US,
                "https://api.open-elevation.com/api/v1/lookup?locations=%.8f,%.8f",
                lat, lon);

        String json = httpGet(url);
        Matcher m = ELEVATION_PATTERN.matcher(json);
        if (m.find()) {
            return Double.valueOf(m.group(1));
        }
        return null;
    }

    private static String httpGet(String urlStr) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(10_000);
        conn.setRequestProperty("Accept", "application/json");

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            if (code < 200 || code >= 300) {
                throw new IOException("HTTP " + code + " response: " + sb);
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    // Petit test rapide
    public static void main(String[] args) throws Exception {
        double lat = 48.858370, lon = 2.294481; // Tour Eiffel
        Double elev = getElevationOpenElevation(lat, lon);
        System.out.println("elevation=" + elev);
    }
}