package com.sam.crawler.http.sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 连接池管理多线程请求
 * @author HY02
 * 2016年3月25日
 */
public class PoolHttpRequestHandler {

	private static final Logger logger = LoggerFactory.getLogger(PoolHttpRequestHandler.class);
	/**
	 * 以请求medsci为例，聚合内部新闻，给新闻打上标签
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// Increase max total connection to 200  
		cm.setMaxTotal(200);  
		// Increase default max connection per route to 20  
		cm.setDefaultMaxPerRoute(20);  
		CloseableHttpClient httpClient = HttpClients.custom()  
		        .setConnectionManager(cm)  
		        .build();  
		String originalWebsite = "http://www.medsci.cn/";
		HttpGet origHttpget = new HttpGet(originalWebsite);
		CloseableHttpResponse response = null;
		List<String> newsHref = new ArrayList<String>();
		try {
			response = httpClient.execute(origHttpget);
			HttpEntity entity = response.getEntity();
			String resString = EntityUtils.toString(entity);
			//System.out.println("resString=" + resString);
			if (resString != null && !"".equals(resString)) {
				String regex = "<a\\s+[^<>]*\\s+href=\"([^<>\"]*)\"[^<>]*>";//"<a.+href=\"(^\")+\"";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(resString);
				while (matcher.find()) {
					String href = matcher.group(1);
					if (href.indexOf("javascript") == -1) {
						if (href.startsWith("/")) {//加上主网址前缀
							href = originalWebsite + href;
						}
						newsHref.add(href);
					}
					logger.info("\nmatch:{},\nhref:{}", matcher.group(), href);
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// URIs to perform GETs on  
		/*String[] urisToGet = {  
		    "http://www.baidu.com/",  
		    "http://www.qq.com/",  
		    "http://www.sohu.com/",  
		    "http://www.sina.com.cn/"  
		};*/  
		
		// create a thread for each URI  
		List<GetThread> threads = new ArrayList<GetThread>();  
		for (String href : newsHref) {  
		    try {
				HttpGet httpget = new HttpGet(href);  
				threads.add(new GetThread(httpClient, httpget));
			} catch (Exception e) {
				e.printStackTrace();
			}  
		}  
		  
		// start the threads  
		for (GetThread gt : threads) {  
			gt.start();  
		}  
		  
		// join the threads  
		for (GetThread gt : threads) {  
			gt.join();  
		}
	}
	static class GetThread extends Thread {  
		  
	    private final CloseableHttpClient httpClient;  
	    private final HttpContext context;  
	    private final HttpGet httpget;  
	  
	    public GetThread(CloseableHttpClient httpClient, HttpGet httpget) {  
	        this.httpClient = httpClient;  
	        this.context = HttpClientContext.create();  
	        this.httpget = httpget;  
	    }  
	  
	    @Override  
	    public void run() {  
	        try {  
	            CloseableHttpResponse response = httpClient.execute(  
	                    httpget, context);  
	            try {  
	                HttpEntity entity = response.getEntity();  
	                if (entity != null) {
	                	String newsContent = EntityUtils.toString(entity, "utf-8");
	                	if (newsContent != null) {
	                		int len = newsContent.length();
	                		if (len > 0) {
	                			if (len > 500) {
	                				newsContent = newsContent.substring(0, 500);
	                			}
	                			logger.info("news url:{}, news content:{}", httpget.getURI(), newsContent);
	                		}
	                	}
	                }
	            } finally {  
	                response.close();  
	            }  
	        } catch (Exception ex) {  
	            // Handle other errors  
	        	logger.error("news url:{}", httpget.getURI(), ex);
	        }  
	    }  
	  
	} 
}

