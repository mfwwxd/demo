package uyun.smc.sign;

import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Please don't public this class from this package, since we provider signature
 * relative functions using a uniform Builder way for caller.
 * @author chenmx
 * @version 1.1
 */
public class UrlUtil {

	private final static String CHARSET_UTF8 = "UTF-8";

	/**
	 * 生成规范化请求字符串
	 * 
	 * @param params

	 * @return
	 */
	public static String generateQueryString(Map<String, String> params) {
		StringBuilder canonicalizedQueryString = new StringBuilder();
        //参数
        for (Map.Entry<String, String> entry : params.entrySet()) {
            canonicalizedQueryString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        if (canonicalizedQueryString.length() > 1) {
			canonicalizedQueryString.setLength(canonicalizedQueryString.length() - 1);
		}
		return canonicalizedQueryString.toString();
	}

	/**
	 * 去除URL的query部分
	 *
	 * @param params

	 * @return
	 */
	public static String trimQueryString(String url) {
		return StringUtils.substringBefore(url, "?");
	}

	/**
	 * 获取URL的query部分
	 * @param url
	 * @return
	 */
	public static String getQueryString(String url) {
		return StringUtils.substringAfter(url, "?");
	}

	/**
	 * 生成规范化请求字符串
	 *
	 * @param params

	 * @return
	 */
	public static Map<String, String> getQueryParam(String url) {
		Map<String, String> paramMap = new HashMap<>();
		String queryString = getQueryString(url);
		if(StringUtils.isNotEmpty(queryString)) {
			String[] pairs = StringUtils.split(queryString, "&");
			for(String pair : pairs) {
				String[] param = pair.split("=");
				paramMap.put(param[0], param[1]);
			}
		}
		return paramMap;
	}

	/**
	 * 替换URL为缓存URL
	 * @param originUrl
	 * @param cacheUrl
	 * @return
	 */
	public static String changeRemoteUrl(String originUrl, String cacheUrl) {
		String queryString = getQueryString(originUrl);
		if(StringUtils.isNotEmpty(queryString)) {
			return cacheUrl + "?" + queryString;
		}
		return cacheUrl;
	}

	/**
	 * 参数编码
	 * 
	 * @param value
	 * @return
	 */
	public static String percentEncode(String value) {
		try {
			// 使用URLEncoder.encode编码
			return value == null ? null : URLEncoder.encode(value, CHARSET_UTF8);
		} catch (Exception e) {
			// 不可能发生的异常
		}
		return "";
	}

	/**
	 * 参数解码
	 *
	 * @param value
	 * @return
	 */
	public static String percentDecode(String value) {
		try {
			// 使用URLEncoder.decode
			return value == null ? null : URLDecoder.decode(value, CHARSET_UTF8);
		} catch (Exception e) {
			// 不可能发生的异常
		}
		return "";
	}

	/**
	 * URL请求地址拼接, 请求参数拼接请求URL的参数
	 * 拼接到URL中,所以需要encode处理
	 * 防止encode两次, 先做一次decode
	 * @param map
	 * @return
	 */
	public static String getRequestParamSplitJoin(Map<String, String> map) {
		if (map == null || map.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			sb.append(entry.getKey())
					.append("=")
					.append(percentEncode(percentDecode(entry.getValue())))
					.append("&");
		}
		String s = sb.toString();
		if (s.endsWith("&")) {
			s = StringUtils.substringBeforeLast(s, "&");
		}
		return s;
	}

	public static boolean hasContentType(Map<String, String> header) {
		if(header == null || header.isEmpty()) {
			return false;
		}
		for (String name : header.keySet()) {
			if(name.equalsIgnoreCase("Content-type")) {
				return true;
			}
		}
		return false;
	}

	public static String getContentType(Map<String, String> header) {
		if(header == null || header.isEmpty()) {
			return null;
		}
		for (String name : header.keySet()) {
			if(name.equalsIgnoreCase("Content-type")) {
				return header.get(name);
			}
		}
		return null;
	}
}