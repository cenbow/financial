package com.mobanker.financial.common.message;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.mobanker.common.utils.HttpClientUtils;

@Component
public class MessageCenter {

	public static final Logger logger = LoggerFactory.getLogger(MessageCenter.class);

	/**
	 * 发送营销类信息
	 * 
	 * @return 是否发送成功
	 */
	public boolean sendMarketingMessage(String url, String nid, String userId, Map<String, Object> replaceParam) {

		return sendRemind(url, nid, userId, replaceParam, "2");
	}

	/**
	 * 发送通知类消息
	 * 
	 * @return
	 */
	public boolean sendRemind(String url, String nid, String userId, Map<String, Object> replaceParam) {

		return sendRemind(url, nid, userId, replaceParam, "1");
	}

	/**
	 * 
	 * @param useType
	 *            1、通知类短信 2、营销类短信
	 * @return 是否发送成功
	 */
	public boolean sendRemind(String url, String nid, String userId, Map<String, Object> replaceParam, String useType) {

		// 是否发送成功
		boolean sendSuccess = false;

		HttpClient client = HttpClientUtils.buildHttpClient();
		HttpPost method = new HttpPost(url);
		try {
			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			paramList.add(new BasicNameValuePair("type", "user"));
			paramList.add(new BasicNameValuePair("messageType", "licai"));
			paramList.add(new BasicNameValuePair("nid", nid));
			paramList.add(new BasicNameValuePair("userId", userId));
			paramList.add(new BasicNameValuePair("product", "shoujidai"));
			paramList.add(new BasicNameValuePair("systemId", "理财"));
			paramList.add(new BasicNameValuePair("useType", useType));
			if (replaceParam != null) {
				StringBuilder sb = new StringBuilder();
				Set<Entry<String, Object>> set = replaceParam.entrySet();
				Iterator<Entry<String, Object>> it = set.iterator();
				while (it.hasNext()) {
					Entry<String, Object> e = it.next();
					sb.append(e.getKey() + ":" + e.getValue() + ",");
				}
				String replace = sb.substring(0, sb.length() - 1);
				paramList.add(new BasicNameValuePair("replaceParam", replace));
			}
			method.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));

			HttpResponse resp = client.execute(method);
			if (200 == resp.getStatusLine().getStatusCode()) {
				String str = EntityUtils.toString(resp.getEntity());
				logger.debug("发送消息返回:{}", str);
				JSONObject jsonObj = JSONObject.parseObject(str);
				if ("1".equals(jsonObj.get("status"))) {
					sendSuccess = true;
				}
			}
		} catch (Exception e) {
			logger.error("发送消息出错:{}", e.getMessage());
		}
		return sendSuccess;
	}
	
	/**
	 * 发送通知类消息
	 * 
	 * @param url
	 * @param userId
	 * @param contents
	 */
	public void sendSysNoCode(String url, String userId, String contents) {
		HttpClient client = HttpClientUtils.buildHttpClient();
		HttpPost method = new HttpPost(url);
		try {
			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			paramList.add(new BasicNameValuePair("type", "user"));
			paramList.add(new BasicNameValuePair("messageType", "licai"));
			paramList.add(new BasicNameValuePair("contents", contents));
			paramList.add(new BasicNameValuePair("name", "活动"));
			paramList.add(new BasicNameValuePair("userId", userId));
			paramList.add(new BasicNameValuePair("product", "shoujidai"));
			paramList.add(new BasicNameValuePair("systemId", "理财"));
			method.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));

			HttpResponse resp = client.execute(method);
			if (200 == resp.getStatusLine().getStatusCode()) {
				String str = EntityUtils.toString(resp.getEntity());
				logger.debug("sendSysNoCode return:" + str);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("发送消息出错:{}", e.getMessage());
		}
	}
	
	
	public void sendNoCode(String url, String userId, String phone, String nid, Map<String, Object> replaceParam, String contents) {

		HttpClient client = HttpClientUtils.buildHttpClient();
		HttpPost method = new HttpPost(url);
		try {
			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			paramList.add(new BasicNameValuePair("type", "user"));
			paramList.add(new BasicNameValuePair("messageType", "licai"));

			if (!StringUtils.isEmpty(userId)) {
				paramList.add(new BasicNameValuePair("userId", userId));
			}
			if (!StringUtils.isEmpty(phone)) {
				paramList.add(new BasicNameValuePair("phone", phone));
			}
			if (!StringUtils.isEmpty(nid)) {
				paramList.add(new BasicNameValuePair("nid", nid));
				if (replaceParam != null) {
					StringBuilder sb = new StringBuilder();
					Set<Entry<String, Object>> set = replaceParam.entrySet();
					Iterator<Entry<String, Object>> it = set.iterator();
					while (it.hasNext()) {
						Entry<String, Object> e = it.next();
						sb.append(e.getKey() + ":" + e.getValue() + ",");
					}
					String replace = sb.substring(0, sb.length() - 1);
					paramList.add(new BasicNameValuePair("replaceParam", replace));
				}
			}
			if (!StringUtils.isEmpty(contents)) {
				paramList.add(new BasicNameValuePair("contents", contents));
			}
			paramList.add(new BasicNameValuePair("name", "活动"));
			paramList.add(new BasicNameValuePair("product", "shoujidai"));
			paramList.add(new BasicNameValuePair("systemId", "理财"));
			paramList.add(new BasicNameValuePair("channel", "sms"));
			method.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));

			HttpResponse resp = client.execute(method);
			if (200 == resp.getStatusLine().getStatusCode()) {
				String str = EntityUtils.toString(resp.getEntity());
				logger.debug("sendNoCode return:" + str);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("sendNoCode error:" + e.getMessage());
		}
	}
}
