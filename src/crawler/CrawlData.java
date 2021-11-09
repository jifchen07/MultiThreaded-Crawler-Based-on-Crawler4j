package crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

class ProcessedUrl {
    // all the urls that are processes in shouldVisit() method
    public String url;
    public Boolean inDomain;
}

class VisitedUrl {
    // successfully visited urls
    public String url;
    public int fileSize;    // in bytes
    public int numOfOutlinks;
    public String contentType;
}

class FetchedUrl {
    // urls that the crawler attempts to fetch, could return unsuccessful status code
    public String url;
    public int statusCode;
}


public class CrawlData {
    private ArrayList<ProcessedUrl> processedUrls;   // all urls found in the shouldVisit() method whether they are in domain
    private ArrayList<VisitedUrl> visitedUrls;
    private ArrayList<FetchedUrl> fetchedUrls;
    private static final int BATCH = 50;      // batch size for dumping to csv

    // initialize synchronization locks
    private static final Object lockP = new Object();
    private static final Object lockV = new Object();
    private static final Object lockF = new Object();

    private static final String processedFileName = "urls_" + Controller.newsSite + ".csv";
    private static final String visitedFileName = "visit_" + Controller.newsSite + ".csv";
    private static final String fetchedFileName = "fetch_" + Controller.newsSite + ".csv";

    public CrawlData() {
        processedUrls = new ArrayList<>();
        visitedUrls = new ArrayList<>();
        fetchedUrls = new ArrayList<>();
    }

    public void addProcessedUrl(String url, boolean inDomain) {
        ProcessedUrl processedUrl = new ProcessedUrl();
        processedUrl.url = Tools.removeComma(url);
        processedUrl.inDomain = inDomain;

        processedUrls.add(processedUrl);

        if (processedUrls.size() == BATCH) {
            dumpProcessedUrls(processedFileName, processedUrls);
            processedUrls = new ArrayList<>();
        }
    }

    public void addVisitedUrl(String url, int fileSize, int numOfOutlinks, String contentType) {
        VisitedUrl visitedUrl = new VisitedUrl();
        visitedUrl.url = Tools.removeComma(url);
        visitedUrl.fileSize = fileSize;
        visitedUrl.numOfOutlinks = numOfOutlinks;
        visitedUrl.contentType = contentType;

        visitedUrls.add(visitedUrl);

        if (visitedUrls.size() == BATCH) {
            dumpVisitedUrls(visitedFileName, visitedUrls);
            visitedUrls = new ArrayList<>();
        }
    }

    public void addFetchedUrl(String url, int statusCode) {
        FetchedUrl fetchedUrl = new FetchedUrl();
        fetchedUrl.url = Tools.removeComma(url);
        fetchedUrl.statusCode = statusCode;

        fetchedUrls.add(fetchedUrl);

        if (fetchedUrls.size() == BATCH) {
            dumpFetchedUrls(fetchedFileName, fetchedUrls);
            fetchedUrls = new ArrayList<>();
        }
    }

    public static void dumpProcessedUrls(String fileName, ArrayList<ProcessedUrl> processedUrls) {
        synchronized (lockP) {
            try {
                File file = new File(fileName);
                boolean fileExists = file.exists();
                if (!fileExists) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                BufferedWriter bw = new BufferedWriter(fw);

                if (!fileExists) {
                    bw.write("URL,Residence Code\n");
                }
                for (ProcessedUrl url: processedUrls) {
                    bw.write(url.url + "," + (url.inDomain ? "OK" : "N_OK") + "\n");
                }
                bw.close();
            } catch (IOException e) {
                System.out.println("Error: writing to file " + fileName + " failed");
            }
        }

    }

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

    private synchronized void dumpFetchedUrls(String fileName, ArrayList<FetchedUrl> fetchedUrls) {
        synchronized (lockF) {
            try {
                File file = new File(fileName);
                boolean fileExists = file.exists();
                if (!fileExists) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                BufferedWriter bw = new BufferedWriter(fw);

                if (!fileExists) {
                    bw.write("URL,Status Code\n");
                }
                for (FetchedUrl url: fetchedUrls) {
                    bw.write(url.url + "," + url.statusCode + "\n");
                }
                bw.close();
            } catch (IOException e) {
                System.out.println("Error: writing to file " + fileName + " failed");
            }
        }
    }
}
