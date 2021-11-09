package crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
    public final static String newsSite = "nytimes";
    public final static String siteDomain = "nytimes.com";

    public static void main(String[] args) throws Exception {
        String crawlStorageFolder = "/data/crawl";
        int numberOfCrawlers = 7;       // requirement default: 7
        int maxDepthOfCrawling = 16;    // requirement default: 16
        int maxPagesToFetch = 20000;     // requirement default: 20000
        int politenessDelay = 300;      // by default 200 ms, available to tune
        String userAgentString = "JF C (https://github.com/yasserg/crawler4j/)";
        String rootURL = "https://www.nytimes.com/";

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(maxDepthOfCrawling);
        config.setMaxPagesToFetch(maxPagesToFetch);
        config.setPolitenessDelay(politenessDelay);
        config.setUserAgentString(userAgentString);
        config.setIncludeBinaryContentInCrawling(true);     // enable binary content crawling (images etc.)

        // Instantiate the controller for this crawl
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController crawlController = new CrawlController(config, pageFetcher, robotstxtServer);

        crawlController.addSeed(rootURL);

        CrawlController.WebCrawlerFactory<MyCrawler> factory = MyCrawler::new;
        crawlController.start(factory, numberOfCrawlers);
    }

}