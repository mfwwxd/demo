package uyun.smc.sign;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Global constant variables and some utility functions.
 * @author chenmx
 * @version 1.1
 */
public final class Util {
	private Util() {
	}

	private final static String UTF_8 = "utf-8";
	private final static String DELIMITER = "&";

	public static String utf_8() {
		return UTF_8;
	}

	public static String delimiter() {
		return DELIMITER;
	}

	public static String newIOSTimeStamp() {
		String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";
		return DateFormatUtils.format(new Date(), pattern);
	}

	public enum HTTP {
		GET("GET"), POST("POST"), PUT("PUT"), DELETE("DELETE"), HEAD("HEAD"),PATCH("PATCH"), OPTIONS("OPTIONS"), TRACE("TRACE");

		private final String method;

		private HTTP(String method) {
			this.method = method;
		}

		public static HTTP validate(String method) {
			for (HTTP m : HTTP.values()) {
				if (m.method().equals(method)) {
					return m;
				}
			}
			throw new IllegalArgumentException("invalid http method - " + method);
		}

		public String method() {
			return method;
		}
	}

	/**
	 * @param reference
	 * @return reference
	 * Taken from guava's @{@code Preconditions}
	 */
	public static <T> T checkNotNull(T reference) {
		if (reference == null) {
			throw new NullPointerException();
		}
		return reference;
	}

	public final static class Maps {
		public static <K, V> Map<K, V> immutableCopyOf(Map<? extends K, ? extends V> from) {
			checkNotNull(from);
			Map<K, V> map = new HashMap<K, V>(from);
			return Collections.unmodifiableMap(map);
		}
	}

	private final static Logger LOG = Logger.getLogger(Util.class.getName());

	/**
	 * Translates a string into {@code application/x-www-form-urlencoded} format
	 * using a specific encoding scheme. This method uses the supplied encoding
	 * scheme to obtain the bytes for unsafe characters. Moreover, this method
	 * would be full back to original url if the
	 * {@link UnsupportedEncodingException} raised from the underlying encode
	 * function.
	 *
	 * @param url
	 *            the url need to be translated
	 * @param encoding
	 *            the encoding scheme
	 * @return the translated url.
	 */
	public static String urlEncodeWithFullback(String url, String encoding) {
		checkNotNull(url);
		checkNotNull(encoding);

		try {
			url = URLEncoder.encode(url, encoding);
		} catch (UnsupportedEncodingException fullback) {
			LOG.log(Level.WARNING, "url encode failed and full back to " + url + ", using encoding " + encoding,
					fullback);
		}

		return url;
	}
	
	  public static String inputStreamToString(InputStream inputStream) throws IOException {
	        final int bufferSize = 1024;
	        final char[] buffer = new char[bufferSize];
	        final StringBuilder out = new StringBuilder();
	        Reader in = new InputStreamReader(inputStream, "UTF-8");
	        for (; ; ) {
	            int rsz = in.read(buffer, 0, buffer.length);
	            if (rsz < 0)
	                break;
	            out.append(buffer, 0, rsz);
	        }
	        return out.toString();
	    }

}
