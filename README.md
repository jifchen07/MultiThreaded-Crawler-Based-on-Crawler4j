# Multithreaded Crawler and Crawler Results Analysis

## Task

* Configure and compile a crawler and have it crawl nytime.com. 
* Study the characteristics of the crawl, download web pages and collect web page metadata.
* Analyze the crawling results.

## Library Used

* [crawler4j with all its dependency](https://jar-download.com/artifacts/edu.uci.ics/crawler4j/4.4.0/source-code)

## Controller Configurations

The controller configurations are set in `Controller.java`. 7 crawlers/threads are working simultaneously.

```
String crawlStorageFolder = "/data/crawl";
int numberOfCrawlers = 7;       
int maxDepthOfCrawling = 16;    
int maxPagesToFetch = 20000;     
int politenessDelay = 300;      // by default 200 ms, available to tune
String userAgentString = "JF C (https://github.com/yasserg/crawler4j/)";
String rootURL = "https://www.nytimes.com/";
```

## Crawler Setup

The customized crawler is defined in `MyCrawler.java`. Most of the setup is done in the `shouldVisit()` and `visit()` functions.

* `shouldVisit()`: This function decides whether the given URL should be crawled or not. 
* `visit()`: This function is called after the content of a URL is downloaded successfully. You can easily get the URL, text, links, html, and unique id of the downloaded page and do any necessary work on them.  

A `CrawData` class is defined in `CrawData.java` to organize and save the data during the crawling. It also ensures thread-safety since multiple thread/crawler may try to write to the same file. It is achieved by using synchronization locks. For example:

```
	private static final Object lockV = new Object();
```

```
    private synchronized void dumpVisitedUrls(String fileName, ArrayList<VisitedUrl> visitedUrls) {
        synchronized (lockV) {
            try {
                File file = new File(fileName);
                boolean fileExists = file.exists();
                if (!fileExists) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                BufferedWriter bw = new BufferedWriter(fw);

                if (!fileExists) {
                    bw.write("URL,Size(kB),No of Outlinks,Content Type\n");
                }
                for (VisitedUrl url: visitedUrls) {
                    bw.write(url.url + "," + url.fileSize / 1024 + ","
                            + url.numOfOutlinks + "," + url.contentType + "\n");
                }
                bw.close();
            } catch (IOException e) {
                System.out.println("Error: writing to file " + fileName + " failed");
            }
        }
    }
```

## Data Analysis

The script `analysis/generate_crawl_report.py` analyze the crawl statistics and export a report:

```
Fetch Statistics:
=================
# fetches attempted: 19850
# fetches succeeded: 13994
# fetches failed or aborted: 5856

Outgoing URLs:
==============
Total URLs extracted: 706000
# unique URLs extracted: 305209
# unique URLs within News Site: 169387
# unique URLs outside News Site: 135822

Status Codes:
=============
200 OK: 13994
301 Moved Permanently: 1694
302 Found: 4125
400 Bad Request: 24
401 Unauthorized: 1
403 Forbidden: 2
404 Not Found: 5
410 Gone: 3
502 Bad Gateway: 1
503 Service Unavailable: 1

File Sizes:
===========
< 1KB: 0
1KB ~ <10KB: 25
10KB ~ <100KB: 2344
100KB ~ <1MB: 9883
>= 1MB: 1548

Content Types:
==============
text/html: 13776
image/png: 14
image/svg+xml: 6
image/jpeg: 2
image/vnd.microsoft.icon: 1
image/gif: 1
```



