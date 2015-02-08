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

package edu.uci.ics.crawler4j.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.log4j.Logger;

/**
 * Originally created as edu.uci.ics.crawler4j.util.IO
 * <p>
 * Makes use of features included in java 7.
 * 
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 * @author Renan Freitas
 */
public class FileUtils
{
    public static String FILE_SEPARATOR = System.getProperty( "file.separator" );

    private static final Logger logger = Logger.getLogger( FileUtils.class );

    private static final FileVisitor<Path> deleteVisitor = new SimpleFileVisitor<Path>() {

	@Override
	public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
	    Files.delete( file );
	    return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
	    if ( e == null ) {
		Files.delete( dir );
		return FileVisitResult.CONTINUE;
	    } else {
		throw e;
	    }
	}
    };

    public static void deleteFolderContents(final File folder) throws IOException {
	final String absolutePath = folder.getAbsolutePath();
	logger.info( "Deleting content of: " + absolutePath );
	Files.walkFileTree( folder.toPath(), deleteVisitor );
	logger.info( "Deleted content of: " + absolutePath );
    }
}
