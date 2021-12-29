package hexlet.code.service;

import java.util.Locale;

public class UrlStandardizer {
    public static String standardize(String inputUrl) {
        inputUrl = inputUrl.toLowerCase(Locale.ROOT);

        if (!(inputUrl.startsWith("https://www."))) {
            inputUrl = "https://www." + inputUrl;
        }
        if (inputUrl.startsWith("www.")) {
            inputUrl = "https://" + inputUrl;
        }

        return inputUrl;
    }
}
