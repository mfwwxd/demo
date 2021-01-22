package uyun.smc.sign;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * It used to to build an immutable signature for api caller through the
 * parameters of specified url; Besides it provide a Builder class to load the
 * relative parameters and compose the final request url easily and friendly.
 * @author chenmx
 * @version 1.1
 */
public final class Signature {
	/**
	 * The HTTP method.
	 */
	private final String method;

	/**
	 * The relative parameters which generally parsed from a URL.
	 */
	private final Map<String, String> headers;

	/**
	 * The relative parameters which generally parsed from a URL.
	 */
	private final Map<String, String> parameters;

	/**
	 * The application secret key.
	 */
	private final String secret;

	/**
	 * The cached signature, this value is immutable but would be backed up by
	 * every thread.
	 */
	private String signature;

	/**
	 * The requested api url.
	 */
	private String url;

	/**
	 * The http body
	 */
	private String postString;

	private final static Logger LOG = Logger.getLogger(Signature.class.getName());

	/**
	 * Constructs a instance from a Builder class.
	 *
	 * @param builder
	 *            the specified builder class
	 */
	private Signature(Builder builder) {
		this.method = builder.method;
		this.secret = builder.secret;
		this.url = builder.url;
		this.postString = builder.postString;
		this.parameters = Util.Maps.immutableCopyOf(builder.parameters);
		this.headers = Util.Maps.immutableCopyOf(builder.headers);
		Util.HTTP.validate(this.method);
		Util.checkNotNull(this.secret);
	}

	/**
	 * Returns the string style signature.
	 * 
	 * @return the string style signature.
	 */
	public String sign() {
		try {
			if (signature == null) {
				signature = PublicSignature.generate(this.method, this.parameters, this.headers, this.secret,
						this.postString);
			}
			return signature;
		} catch (Exception e) {
			LOG.log(Level.WARNING, "create signature failed - " + this, e);
			throw new IllegalStateException(e); // fail-fast
		}
	}

	public String getStrBeforeSign() {
		return PublicSignature.getStrBeforeSign(this.method, this.parameters, this.headers, this.postString);
	}

	/**
	 * Returns a new <tt>Builder</tt> instance.
	 *
	 * @return a new <tt>Builder</tt> instance.
	 */
	public static Builder newBuilder() {
		return new Builder();
	}

	/**
	 * Returns a new composed api url used to request the resource directly.
	 * 
	 * @return a new composed api url
	 */
	public String compose() {
		return url + "&nonce=" + sign();
	}

	/**
	 * A <tt>Builder</tt> class is used to load the relative parameters build
	 * the {@link Signature} instance finally.
	 */
	public static class Builder {
		private String method;
		private Map<String, String> parameters = new HashMap<String, String>();
		private Map<String, String> headers = new HashMap<String, String>();
		private String secret;
		private String url;
		private String postString;

		/**
		 * Returns an immutable {@link Signature} instance.
		 * 
		 * @return an immutable {@link Signature} instance.
		 */
		public Signature build() {
			return new Signature(this);
		}

		/**
		 * Loads the HTTP method.
		 * 
		 * @param method
		 *            the HTTP method.
		 * @return the Builder self instance.
		 */
		public Builder method(String method) {
			this.method = method;
			return this;
		}

		/**
		 * Loads the application secret key.
		 * 
		 * @param secret
		 *            the application secret key
		 * @return the Builder self instance.
		 */
		public Builder secret(String secret) {
			this.secret = secret;
			return this;
		}

		/**
		 * Build Map Param
		 * @param map
		 * @return the Builder self instance
		 */
		public Builder parameter(Map<String, String> map) {
			this.parameters.putAll(map);
			return this;
		}

		public Builder parameter(String key, String value) {
			this.parameters.put(key, value);
			return this;
		}

		public Builder header(String key, String value) {
			if(StringUtils.isNotEmpty(value)) {
				this.headers.put(key, value);
			}
			return this;
		}

		/**
		 * Loads the application post body postString.
		 * 
		 * @param postString the application post body
		 * @return the Builder self instance.
		 */
		public Builder postString(String postString) {
			this.postString = postString;
			return this;
		}

		/**
		 * Loads the api URL.
		 * 
		 * @param url
		 *            the api URL.
		 * @return the Builder self instance.
		 */
		public Builder url(String url) {
			this.url = url;
			return this;
		}
	}

}
