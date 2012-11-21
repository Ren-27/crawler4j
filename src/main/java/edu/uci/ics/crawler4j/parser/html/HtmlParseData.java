/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.parser.html;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.ExtractedUrlAnchorPair;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;

public class HtmlParseData implements ParseData {

	private String html = "";

	private String content = "";

	private String title = "";

	private List<WebURL> outgoingUrls;

	@Override
	public Object getContent() {
		return content;
	}

	public String getHtml() {
		return html;
	}

	@Override
	public String[] getMimeTypes() {
		return new String[] { "text/html" };
	}

	public List<WebURL> getOutgoingUrls() {
		return outgoingUrls;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public void parse(Page page, CrawlConfig config) {
		HtmlParser htmlParser = new HtmlParser();
		ParseContext parseContext = new ParseContext();

		Metadata metadata = new Metadata();
		HtmlContentHandler contentHandler = new HtmlContentHandler();
		InputStream inputStream = null;
		try {
			inputStream = new ByteArrayInputStream(page.getContentData());
			htmlParser.parse(inputStream, contentHandler, metadata, parseContext);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (page.getContentCharset() == null) {
			page.setContentCharset(metadata.get("Content-Encoding"));
		}

		content = contentHandler.getBodyText().trim();
		title = metadata.get(DublinCore.TITLE);

		outgoingUrls = new ArrayList<WebURL>();

		String baseURL = contentHandler.getBaseUrl();
		String contextURL;
		if (baseURL != null) {
			contextURL = baseURL;
		} else {
			contextURL = page.getWebURL().getURL();
		}

		int urlCount = 0;
		for (ExtractedUrlAnchorPair urlAnchorPair : contentHandler.getOutgoingUrls()) {
			String href = urlAnchorPair.getHref();
			href = href.trim();
			if (href.length() == 0) {
				continue;
			}
			String hrefWithoutProtocol = href.toLowerCase();
			if (href.startsWith("http://")) {
				hrefWithoutProtocol = href.substring(7);
			}
			if (!hrefWithoutProtocol.contains("javascript:") && !hrefWithoutProtocol.contains("@")) {
				String url = URLCanonicalizer.getCanonicalURL(href, contextURL);
				if (url != null) {
					WebURL webURL = new WebURL();
					webURL.setURL(url);
					webURL.setAnchor(urlAnchorPair.getAnchor());
					outgoingUrls.add(webURL);
					urlCount++;
					if (urlCount > config.getMaxOutgoingLinksToFollow()) {
						break;
					}
				}
			}
		}

		try {
			if (page.getContentCharset() == null) {
				html = new String(page.getContentData());
			} else {
				html = new String(page.getContentData(), page.getContentCharset());
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		page.setParseData(this);
	}

	@Override
	public String toString() {
		return content;
	}

}
