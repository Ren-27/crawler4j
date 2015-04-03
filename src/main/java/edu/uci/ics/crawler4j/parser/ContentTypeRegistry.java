/**
 * 
 */
package edu.uci.ics.crawler4j.parser;

import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * @author Markus Holtermann
 * 
 */
public class ContentTypeRegistry
{

    /**
     * An instance of a {@link org.apache.log4j.Logger}.
     */
    private static final Logger LOG = Logger.getLogger( ContentTypeRegistry.class.getName() );

    private static ContentTypeRegistry registry = null;

    private static HashMap<String, Class<? extends ParseData>> mapping;

    /**
     * Return the class for a certain MIME type. The basic construct looks like:
     * 
     * <pre>
     * Class&lt;? extends ParseData&gt; pdcls = ContentTypeRegistry.getHandler( page.getContentType() );
     * if ( pdcls == null ) {
     *     throw new Exception( &quot;Cannot find handler for content type &quot; + page.getContentType() );
     * }
     * ParseData pd = pdcls.newInstance();
     * pd.setPage( page, config );
     * </pre>
     * 
     * @param mimetype
     * @return
     */
    public static Class<? extends ParseData> getHandler(final String mimetype) {
	ContentTypeRegistry.getInstance();
	return mapping.get( mimetype );
    }

    private static void getInstance() {
	if ( registry == null ) {
	    registry = new ContentTypeRegistry();
	    mapping = new HashMap<String, Class<? extends ParseData>>();
	}
    }

    /**
     * Register the class cls as a new {@link ParseData} handler.
     * 
     * @param cls
     * @return
     */
    public static boolean register(final Class<? extends ParseData> cls) {
	ContentTypeRegistry.getInstance();
	try {
	    final String[] mimetypes = cls.newInstance().getMimeTypes();
	    for ( int i = 0; i < mimetypes.length; ++i ) {
		mapping.put( mimetypes[i], cls );
	    }
	    return true;
	} catch ( final InstantiationException e ) {
	    LOG.error( "Error registering " + cls.getName() );
	    LOG.debug( "Error registering " + cls.getName(), e );
	} catch ( final IllegalAccessException e ) {
	    LOG.error( "Error registering " + cls.getName() );
	    LOG.debug( "Error registering " + cls.getName(), e );
	}
	return false;
    }

    /**
     * Unregister all mime types defined by the class cls.
     * 
     * @param cls
     * @return
     */
    public static boolean unregister(final Class<? extends ParseData> cls) {
	// No need for ``getInstance()`` here. We call it in ``unregister()``
	try {
	    final String[] mimetypes = cls.newInstance().getMimeTypes();
	    for ( int i = 0; i < mimetypes.length; ++i ) {
		ContentTypeRegistry.unregister( mimetypes[i] );
	    }
	    return true;
	} catch ( final InstantiationException e ) {
	    LOG.error( "Error registering " + cls.getName() );
	    LOG.debug( "Error registering " + cls.getName(), e );
	} catch ( final IllegalAccessException e ) {
	    LOG.error( "Error registering " + cls.getName() );
	    LOG.debug( "Error registering " + cls.getName(), e );
	}
	return false;
    }

    /**
     * Unregister the {@link ParseData} class that is defined by the given
     * mimetype.
     * 
     * @param mimetype
     * @return
     */
    public static boolean unregister(final String mimetype) {
	ContentTypeRegistry.getInstance();
	if ( mapping.containsKey( mimetype ) ) {
	    mapping.remove( mimetype );
	}
	return true;
    }

}
