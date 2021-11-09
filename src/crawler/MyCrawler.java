package crawler;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class MyCrawler extends WebCrawler {
    private final static Pattern FILTERS = Pattern.compile
            (".*(\\.(css|json|js|xml|wav|avi|mov|m4v|mp3|mp4|zip|gz))$");
    private String siteDomain = Controller.siteDomain;

    private static volatile AtomicInteger numOfCrawlers = new AtomicInteger(0);
    private int crawlerIndex;
    private CrawlData crawlData;

    public MyCrawler() {
        crawlerIndex = numOfCrawlers.getAndIncrement();
        crawlData = new CrawlData();        // temporarily holds crawl results
        System.out.printf("Crawler num %d created", crawlerIndex);
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = Tools.normalizeUrl(url.getURL());
        boolean inDomain = false;
        boolean typeValid = false;
        if (href.startsWith(siteDomain)){
            inDomain = true;
        }
        if (!FILTERS.matcher(href).matches()) {
            typeValid = true;
        }
        crawlData.addProcessedUrl(url.getURL(), inDomain);
        return inDomain && typeValid;
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        String contentType = page.getContentType().toLowerCase().split(";")[0];

        if (contentType.equals("text/html")) {
            if (page.getParseData() instanceof HtmlParseData) {
                HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                Set<WebURL> links = htmlParseData.getOutgoingUrls();
                crawlData.addVisitedUrl(url, page.getContentData().length, links.size(), "text/html");
            }
        }
        else {
            // possible MIME types for pdf and word documents, plus any image type
            Set<String> validDocTypes = new HashSet<>(Arrays.asList("application/pdf",
                    "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
            if (validDocTypes.contains(contentType) || contentType.startsWith("image")) {
                crawlData.addVisitedUrl(url, page.getContentData().length, 0, contentType);
            }
        }
    }

    @Override
    public void handlePageStatusCode(WebURL url, int statusCode, String statusDescription) {
        crawlData.addFetchedUrl(url.getURL(), statusCode);
    }

}
