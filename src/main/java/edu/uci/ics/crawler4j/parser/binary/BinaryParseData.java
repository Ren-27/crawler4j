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

package edu.uci.ics.crawler4j.parser.binary;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.ParseData;

public class BinaryParseData implements ParseData {

	private byte[] content;

	@Override
	public Object getContent() {
		return content;
	}

	@Override
	public String[] getMimeTypes() {
		return new String[] { "BINARY" };
	}

	@Override
	public void parse(Page page, CrawlConfig config) {
		content = page.getContentData();
		page.setParseData(this);
	}

	@Override
	public String toString() {
		return "[Binary parse data can not be dumped as string]";
	}

}
