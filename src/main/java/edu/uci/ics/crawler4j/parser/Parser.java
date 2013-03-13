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

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class Parser extends Configurable {

	protected static final Logger logger = Logger.getLogger(Parser.class.getName());

	public Parser(CrawlConfig config) {
		super(config);
	}

	public boolean parse(Page page, String contextURL) {
		Class<? extends ParseData> pdcls = ContentTypeRegistry.getHandler(page.getContentType());
		ParseData pd;
		if (pdcls == null) {
			pdcls = ContentTypeRegistry.getHandler("BINARY");
			if (!config.isIncludeBinaryContentInCrawling() || pdcls == null) {
				return false;
			}
		}
		try {
			pd = pdcls.newInstance();
			pd.parse(page, config);
			return true;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
