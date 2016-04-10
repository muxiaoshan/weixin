package com.sam.crawler.http.sample;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * 多线程请求
 * @author HY02
 * 2016年3月25日
 */
public class MultipleThreadRequestHandler {
	public static void main(String[] args) throws InterruptedException {
		Thread.sleep(5000);
		for (int i = 0; i < 50; i++) {
			Thread t = new Thread(new RequestThread());
			t.start();
		}
	}
}

class RequestThread implements Runnable {

	public void run() {
		 try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			 try {
			        HttpGet httpget = new HttpGet("http://httpbin.org/");

			        System.out.println("Executing request " + httpget.getRequestLine());

			        // Create a custom response handler
			        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

			            //@Override
			            public String handleResponse(
			                    final HttpResponse response) throws ClientProtocolException, IOException {
			                int status = response.getStatusLine().getStatusCode();
			                if (status >= 200 && status < 300) {
			                    HttpEntity entity = response.getEntity();
			                    return entity != null ? EntityUtils.toString(entity) : null;
			                } else {
			                    throw new ClientProtocolException("Unexpected response status: " + status);
			                }
			            }

			            
			        };
			        String responseBody = httpclient.execute(httpget, responseHandler);
			        System.out.println("----------------------------------------");
			        System.out.println(responseBody);
			  } finally {
			        httpclient.close();
			  }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}