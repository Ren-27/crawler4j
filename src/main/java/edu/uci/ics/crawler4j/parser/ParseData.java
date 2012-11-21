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

package edu.uci.ics.crawler4j.parser;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.html.HtmlParseData;

public interface ParseData {

	/**
	 * Return the main content object of this parsed data. For e.g. {@link HtmlParseData} this would be the tag-stripped
	 * content of the page.
	 * 
	 * @return
	 */
	public Object getContent();

	/**
	 * Return an array of mime types that can be handled by this {@link ParseData}.
	 * 
	 * @return
	 */
	public String[] getMimeTypes();

	/**
	 * This procedure actually parses the content based on the underlying implementation which can differ for e.g. HTML
	 * and PDF documents. At the end of this function, the implementing class <b>MUST</b> call to propagate the parsed
	 * data to the regarding page.
	 * 
	 * <pre>
	 * page.setParseData(this);
	 * </pre>
	 * 
	 * @param page
	 * @param config
	 */
	public void parse(Page page, CrawlConfig config);

	@Override
	public String toString();

}
