package edu.uci.ics.crawler4j.crawler;

import edu.uci.ics.crawler4j.url.WebURL;

// FIXME should really extends Runnable?
public interface Crawler
    extends
    Runnable
{

    /**
     * Initializes the current instance of the crawler
     * 
     * @param id the id of this crawler instance
     * @param crawlController the controller that manages this crawling session
     */
    void init(int id, CrawlController crawlController);

    /**
     * Get the id of the current crawler instance
     * 
     * @return the id of the current crawler instance
     */
    int getMyId();

    CrawlController getMyController();

    /**
     * This function is called just before starting the crawl by this crawler
     * instance. It can be used for setting up the data structures or
     * initializations needed by this crawler instance.
     */
    void onStart();

    /**
     * This function is called just before the termination of the current
     * crawler instance. It can be used for persisting in-memory data or other
     * finalization tasks.
     */
    void onBeforeExit();

    /**
     * The CrawlController instance that has created this crawler instance will
     * call this function just before terminating this crawler thread. Classes
     * that extend WebCrawler can override this function to pass their local
     * data to their controller. The controller then puts these local data in a
     * List that can then be used for processing the local data of crawlers (if
     * needed).
     */
    Object getMyLocalData();

    @Override
    void run();

    /**
     * Classes that extends WebCrawler can overwrite this function to tell the
     * crawler whether the given url should be crawled or not. The following
     * implementation indicates that all urls should be included in the crawl.
     * 
     * @param url the url which we are interested to know whether it should be
     *        included in the crawl or not.
     * @return if the url should be included in the crawl it returns true,
     *         otherwise false is returned.
     */
    boolean shouldVisit(WebURL url);

    /**
     * Classes that extends WebCrawler can overwrite this function to process
     * the content of the fetched and parsed page.
     * 
     * @param page the page object that is just fetched and parsed.
     */
    void visit(Page page);

    Thread getThread();

    void setThread(Thread myThread);

    boolean isNotWaitingForNewURLs();

}