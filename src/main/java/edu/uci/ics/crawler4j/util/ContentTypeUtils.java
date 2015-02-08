package edu.uci.ics.crawler4j.util;

/**
 * Utility class for Strings that represents contentTypes.
 */
// TODO shouldn't ContentType be a class??
public final class ContentTypeUtils
{
    public ContentTypeUtils() {
	throw new UnsupportedOperationException();
    }

    public static boolean hasBinaryContent(final String contentType) {
	if ( contentType != null ) {
	    final String typeStr = contentType.toLowerCase();
	    if ( typeStr.contains( "image" ) || typeStr.contains( "audio" ) || typeStr.contains( "video" ) || typeStr.contains( "application" ) ) {
		return true;
	    }
	}
	return false;
    }

    public static boolean hasPlainTextContent(final String contentType) {
	if ( contentType != null ) {
	    final String typeStr = contentType.toLowerCase();
	    if ( typeStr.contains( "text/plain" ) ) {
		return true;
	    }
	}
	return false;
    }
}
