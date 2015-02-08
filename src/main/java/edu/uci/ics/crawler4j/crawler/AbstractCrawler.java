/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.uci.ics.crawler4j.crawler;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.fetcher.CustomFetchStatus;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.DocIDServer;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.parser.html.HtmlParseData;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * WebCrawler class in the Runnable class that is executed by each crawler
 * thread.
 * <p>
 * It's the default implementation of {@link Crawler}.
 * 
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public abstract class AbstractCrawler
    implements
    Crawler
{

    protected final Logger logger = Logger.getLogger( getClass() );

    /**
     * The id associated to the crawler thread running this instance
     */
    private final int myId;

    /**
     * The controller instance that has created this crawler thread. This
     * reference to the controller can be used for getting configurations of the
     * current crawl or adding new seeds during runtime.
     */
    protected CrawlController myController;

    /**
     * The thread within which this crawler instance is running.
     */
    private Thread myThread;

    /**
     * The parser that is used by this crawler instance to parse the content of
     * the fetched pages.
     */
    private final Parser parser;

    /**
     * The fetcher that is used by this crawler instance to fetch the content of
     * pages from the web.
     */
    private final PageFetcher pageFetcher;

    /**
     * The RobotstxtServer instance that is used by this crawler instance to
     * determine whether the crawler is allowed to crawl the content of each
     * page.
     */
    private final RobotstxtServer robotstxtServer;

    /**
     * The DocIDServer that is used by this crawler instance to map each URL to
     * a unique docid.
     */
    private final DocIDServer docIdServer;

    /**
     * The Frontier object that manages the crawl queue.
     */
    private final Frontier frontier;

    /**
     * Is the current crawler instance waiting for new URLs? This field is
     * mainly used by the controller to detect whether all of the crawler
     * instances are waiting for new URLs and therefore there is no more work
     * and crawling can be stopped.
     */
    private boolean isWaitingForNewURLs;

    /**
     * Constructor.
     * 
     * @param id the id of this crawler instance
     * @param crawlController the controller that manages this crawling session
     */
    public AbstractCrawler(final int id, final CrawlController crawlController) {
	this.myId = id;
	this.pageFetcher = crawlController.getPageFetcher();
	this.robotstxtServer = crawlController.getRobotstxtServer();
	this.docIdServer = crawlController.getDocIdServer();
	this.frontier = crawlController.getFrontier();
	this.parser = new Parser( crawlController.getConfig() );
	this.myController = crawlController;
    }

    /**
     * Get the id of the current crawler instance
     * 
     * @return the id of the current crawler instance
     */
    @Override
    public int getMyId() {
	return myId;
    }

    @Override
    public CrawlController getMyController() {
	return myController;
    }

    /**
     * This function is called just before starting the crawl by this crawler
     * instance. It can be used for setting up the data structures or
     * initializations needed by this crawler instance.
     */
    @Override
    public void onStart() {
	// Do nothing by default
	// Sub-classed can override this to add their custom functionality
    }

    /**
     * This function is called just before the termination of the current
     * crawler instance. It can be used for persisting in-memory data or other
     * finalization tasks.
     */
    @Override
    public void onBeforeExit() {
	// Do nothing by default
	// Sub-classed can override this to add their custom functionality
    }

    /**
     * This function is called once the header of a page is fetched. It can be
     * overwritten by sub-classes to perform custom logic for different status
     * codes. For example, 404 pages can be logged, etc.
     * 
     * @param webUrl
     * @param statusCode
     * @param statusDescription
     */
    protected void handlePageStatusCode(final WebURL webUrl, final int statusCode, final String statusDescription) {
	// Do nothing by default
	// Sub-classed can override this to add their custom functionality
    }

    /**
     * This function is called if the content of a url could not be fetched.
     * 
     * @param webUrl
     */
    protected void onContentFetchError(final WebURL webUrl) {
	// Do nothing by default
	// Sub-classed can override this to add their custom functionality
    }

    /**
     * This function is called if there has been an error in parsing the
     * content.
     * 
     * @param webUrl
     */
    protected void onParseError(final WebURL webUrl) {
	// Do nothing by default
	// Sub-classed can override this to add their custom functionality
    }

    /**
     * The CrawlController instance that has created this crawler instance will
     * call this function just before terminating this crawler thread. Classes
     * that extend WebCrawler can override this function to pass their local
     * data to their controller. The controller then puts these local data in a
     * List that can then be used for processing the local data of crawlers (if
     * needed).
     */
    @Override
    public Object getMyLocalData() {
	return null;
    }

    @Override
    public void run() {
	onStart();
	while ( true ) {
	    final List<WebURL> assignedURLs = new ArrayList<>( 50 );
	    isWaitingForNewURLs = true;
	    frontier.getNextURLs( 50, assignedURLs );
	    isWaitingForNewURLs = false;
	    if ( assignedURLs.size() == 0 ) {
		if ( frontier.isFinished() ) {
		    return;
		}
		try {
		    Thread.sleep( 3000 );
		} catch ( final InterruptedException e ) {
		    e.printStackTrace();
		}
	    } else {
		for ( final WebURL curURL : assignedURLs ) {
		    if ( curURL != null ) {
			processPage( curURL );
			frontier.setProcessed( curURL );
		    }
		    if ( myController.isShuttingDown() ) {
			logger.info( "Exiting because of controller shutdown." );
			return;
		    }
		}
	    }
	}
    }

    /**
     * Classes that extends WebCrawler can overwrite this function to tell the
     * crawler whether the given url should be crawled or not. The following
     * implementation indicates that all urls should be included in the crawl.
     * 
     * @param url the url which we are interested to know whether it should be
     *        included in the crawl or not.
     * @return <code>true</code>, if the url should be included in the crawl it
     *         returns true, otherwise false is returned.
     */
    @Override
    public boolean shouldVisit(final WebURL url) {
	return true;
    }

    /**
     * Classes that extends WebCrawler can overwrite this function to process
     * the content of the fetched and parsed page.
     * 
     * @param page the page object that is just fetched and parsed.
     */
    @Override
    public void visit(final Page page) {
	// Do nothing by default
	// Sub-classed can override this to add their custom functionality
    }

    private void processPage(final WebURL url) {
	if ( url == null ) {
	    return;
	}
	PageFetchResult fetchResult = null;
	try {
	    fetchResult = pageFetcher.fetchHeader( url );
	    final int statusCode = fetchResult.getStatusCode();
	    handlePageStatusCode( url, statusCode, CustomFetchStatus.getStatusDescription( statusCode ) );
	    if ( statusCode != HttpStatus.SC_OK ) {
		if ( statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY ) {
		    if ( myController.getConfig().isFollowRedirects() ) {
			final String movedToUrl = fetchResult.getMovedToUrl();
			if ( movedToUrl == null ) {
			    return;
			}
			final int newDocId = docIdServer.getDocId( movedToUrl );
			if ( newDocId > 0 ) {
			    // Redirect page is already seen
			    return;
			}

			final WebURL webURL = new WebURL();
			webURL.setURL( movedToUrl );
			webURL.setParentDocid( url.getParentDocid() );
			webURL.setParentUrl( url.getParentUrl() );
			webURL.setDepth( url.getDepth() );
			webURL.setDocid( -1 );
			webURL.setAnchor( url.getAnchor() );
			if ( internalShouldVisit( webURL ) ) {
			    webURL.setDocid( docIdServer.getNewDocID( movedToUrl ) );
			    frontier.schedule( webURL );
			}
		    }
		} else if ( fetchResult.getStatusCode() == CustomFetchStatus.PageTooBig ) {
		    logger.info( "Skipping a page which was bigger than max allowed size: " + url.getURL() );
		}
		return;
	    }

	    if ( !url.getURL().equals( fetchResult.getFetchedUrl() ) ) {
		if ( docIdServer.isSeenBefore( fetchResult.getFetchedUrl() ) ) {
		    // Redirect page is already seen
		    return;
		}
		url.setURL( fetchResult.getFetchedUrl() );
		url.setDocid( docIdServer.getNewDocID( fetchResult.getFetchedUrl() ) );
	    }

	    final Page page = new Page( url );
	    final int docid = url.getDocid();

	    if ( !fetchResult.fetchContent( page ) ) {
		onContentFetchError( url );
		return;
	    }

	    if ( !parser.parse( page, url.getURL() ) ) {
		onParseError( url );
		return;
	    }

	    final ParseData parseData = page.getParseData();
	    if ( parseData instanceof HtmlParseData ) {
		final HtmlParseData htmlParseData = (HtmlParseData) parseData;

		final List<WebURL> toSchedule = new ArrayList<>();
		final int maxCrawlDepth = myController.getConfig().getMaxDepthOfCrawling();
		for ( final WebURL webURL : htmlParseData.getOutgoingUrls() ) {
		    webURL.setParentDocid( docid );
		    webURL.setParentUrl( url.getURL() );
		    final int newdocid = docIdServer.getDocId( webURL.getURL() );
		    if ( newdocid > 0 ) {
			// This is not the first time that this Url is
			// visited. So, we set the depth to a negative
			// number.
			webURL.setDepth( (short) -1 );
			webURL.setDocid( newdocid );
		    } else {
			webURL.setDocid( -1 );
			webURL.setDepth( (short) ( url.getDepth() + 1 ) );
			if ( maxCrawlDepth == -1 || url.getDepth() < maxCrawlDepth ) {
			    if ( internalShouldVisit( webURL ) ) {
				webURL.setDocid( docIdServer.getNewDocID( webURL.getURL() ) );
				toSchedule.add( webURL );
			    }
			}
		    }
		}
		frontier.scheduleAll( toSchedule );
	    }
	    try {
		visit( page );
	    } catch ( final Exception e ) {
		logger.error( "Exception while running the visit method. Message: '" + e.getMessage() + "' at " + e.getStackTrace()[0] );
	    }

	} catch ( final Exception e ) {
	    logger.error( e.getMessage() + ", while processing: " + url.getURL() );
	} finally {
	    if ( fetchResult != null ) {
		fetchResult.discardContentIfNotConsumed();
	    }
	}
    }

    private boolean internalShouldVisit(final WebURL webURL) {
	return shouldVisit( webURL ) && robotstxtServer.allows( webURL );
    }

    @Override
    public Thread getThread() {
	return myThread;
    }

    @Override
    public void setThread(final Thread myThread) {
	this.myThread = myThread;
    }

    @Override
    public boolean isNotWaitingForNewURLs() {
	return !isWaitingForNewURLs;
    }

}
