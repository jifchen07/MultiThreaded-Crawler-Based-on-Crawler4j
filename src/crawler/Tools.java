package crawler;

public class Tools {
    public static String normalizeUrl(String url) {
        /*
        convert to lower case, replace ',' to '_', remove prefix and suffix '/'
         */
        url = url.toLowerCase();
        url = url.replace(',', '-');
        url = url.replaceFirst("^(https?://)?(www.)?", "");
        url = url.replaceAll("/$", "");
        return url;
    }

    public static String removeComma(String url) {
        url = url.replace(',', '-');
        return url;
    }
}
