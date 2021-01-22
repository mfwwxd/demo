package uyun.smc.sign;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;



/**
 * Please don't public this class from this package, since we provider signature
 * relative functions using a uniform Builder way for caller.
 * @author chenmx
 * @version 1.1
 */
class PublicSignature {
	private final static String CHARSET_UTF8 = "utf8";
	private final static String ALGORITHM = "UTF-8";
	private final static String SEPARATOR = "/";

	// 第一步
	public static Map<String, String> splitQueryString(String url)
			throws URISyntaxException, UnsupportedEncodingException {
		URI uri = new URI(url);
		String query = uri.getQuery();
		final String[] pairs = query.split("&");
		TreeMap<String, String> queryMap = new TreeMap<String, String>();
		for (String pair : pairs) {
			final int idx = pair.indexOf("=");
			final String key = idx > 0 ? pair.substring(0, idx) : pair;
			if (!queryMap.containsKey(key)) {
				queryMap.put(key, URLDecoder.decode(pair.substring(idx + 1), CHARSET_UTF8));
			}
		}
		return queryMap;
	}

	public static String generate(String method, Map<String, String> parameter, Map<String, String> header,
			String accessKeySecret, String postString) throws Exception {
		String signString = generateSignString(method, parameter, header, postString);
		
		byte[] signBytes = hmacSHA256Signature(accessKeySecret, signString);
		String signature = byte2hex(signBytes);
		if ("POST".equals(method))
			return signature;
		return URLEncoder.encode(signature, "UTF-8");

	}

	public static String getStrBeforeSign(String method, Map<String, String> parameter, Map<String, String> header,
								  String postString) {
		String signString = generateSignString(method, parameter, header, postString);
		return signString;
	}

	// 第二步 构造的规范化字符串按照规则构造成待签名的字符串
	public static String generateSignString(String httpMethod, Map<String, String> parameter,
			Map<String, String> header, String postString) {
		TreeMap<String, String> sortParameter = new TreeMap<String, String>();
		sortParameter.putAll(parameter);

		TreeMap<String, String> sortHeader = new TreeMap<String, String>();
		sortHeader.putAll(header);

        String headerString = UrlUtil.generateQueryString( sortHeader);

		String paramString = UrlUtil.generateQueryString(sortParameter);
		if (null == httpMethod) {
			throw new RuntimeException("httpMethod can not be empty");
		}
		// 构建待签名的字符串
		StringBuilder stringToSign = new StringBuilder();
		stringToSign.append(httpMethod).append("&").append(percentEncode(SEPARATOR)).append("&");
		stringToSign.append(percentEncode(headerString));
		if(StringUtils.isNotEmpty(paramString)){
            stringToSign.append("&").append(percentEncode(paramString));
        }
		if ((httpMethod.equals(Util.HTTP.POST.method())
				|| httpMethod.equals(Util.HTTP.PUT.method())
				|| httpMethod.equals(Util.HTTP.DELETE.method())
				|| httpMethod.equals(Util.HTTP.PATCH.method()))
				&& !StringUtils.isEmpty(postString)) {
			stringToSign.append("&");
			stringToSign.append(percentEncode(postString));
		}
		return stringToSign.toString().toLowerCase();
	}

	// 第三步计算代签名字符串的HMAC256

	public static byte[] hmacSHA256Signature(String secret, String baseString) throws Exception {
		if (isEmpty(secret)) {
			throw new IOException("secret can not be empty");
		}
		if (isEmpty(baseString)) {
			return null;
		}
		Mac mac = Mac.getInstance("HmacSHA256");
		SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(CHARSET_UTF8), ALGORITHM);
		mac.init(keySpec);
		return mac.doFinal(baseString.getBytes(CHARSET_UTF8));
	}

	private static boolean isEmpty(String str) {
		return (str == null || str.length() == 0);
	}

	// 第四步 按照 编码规则把上面的 HMAC 值编码成字符串，即得到签名值（Signature）
	public static String byte2hex(byte[] b) {
		StringBuilder hs = new StringBuilder();
		String stmp;
		for (int n = 0; b != null && n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0XFF);
			if (stmp.length() == 1)
				hs.append('0');
			hs.append(stmp);
		}
		return hs.toString();
	}

	public static String percentEncode(String value) {
		try {
			return value == null ? null
					: URLEncoder.encode(value, CHARSET_UTF8).replace("+", "%20").replace("_", "%5F").replace("*", "%2A").replace("%7E",
							"~");
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * get SignatureNonce
	 *
	 * @return
	 */
	public static String getUniqueNonce() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}

	/**
	 * get timestamp
	 *
	 * @return
	 */
	public static String getISO8601Time() {
		Date nowDate = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(new SimpleTimeZone(0, "GMT"));

		return df.format(nowDate);
	}

	// 第五步 将得到的签名值作�??Signature?参数添加到请求参数中，即完成对请求签名的过程�??
	public static String composeUrl(String endpoint, Map<String, String> queries) throws UnsupportedEncodingException {
		Map<String, String> mapQueries = queries;
		StringBuilder urlBuilder = new StringBuilder("");
		urlBuilder.append("http");
		urlBuilder.append("://").append(endpoint);
		if (-1 == urlBuilder.indexOf("?")) {
			urlBuilder.append("/?");
		}
		urlBuilder.append(concatQueryString(mapQueries));
		return urlBuilder.toString();
	}

	public static String concatQueryString(Map<String, String> parameters) throws UnsupportedEncodingException {
		if (null == parameters) {
			return null;
		}
		StringBuilder urlBuilder = new StringBuilder("");
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();
			urlBuilder.append(encode(key));
			if (val != null) {
				urlBuilder.append("=").append(encode(val));
			}
			urlBuilder.append("&");
		}

		int strIndex = urlBuilder.length();
		if (parameters.size() > 0) {
			urlBuilder.deleteCharAt(strIndex - 1);
		}
		return urlBuilder.toString();
	}

	public static String encode(String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF-8");
	}

}