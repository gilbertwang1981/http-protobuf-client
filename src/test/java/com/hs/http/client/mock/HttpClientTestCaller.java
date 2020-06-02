package com.hs.http.client.mock;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hs.http.client.test.proto.Test.TestMessageRequest;
import com.hs.http.client.test.proto.Test.TestMessageResponse;

@Component
public class HttpClientTestCaller {
	private static Logger logger = LoggerFactory.getLogger(HttpClientTestCaller.class);
	
	@Autowired
	private HttpClientService httpClientService;
	
	@PostConstruct
	private void init() {
		new Timer().scheduleAtFixedRate(new TimerTask() {
			public void run() {
				call();
			}
		} , 5000 , 5000); 
	}
	
	private void call() {
		TestMessageRequest.Builder bd = TestMessageRequest.newBuilder();
		bd.setKey("test");
		
		HttpResponse response = null;
		try {
			response = httpClientService.get(bd.build());
			if (response != null) {
				TestMessageResponse res = TestMessageResponse.parseFrom(response.getEntity().getContent());
				
				logger.info("调用服务成功，{}" , res.getValue());
			} else {
				logger.error("调用返回为空.");
			}
		} catch (Exception e) {
			logger.error("调用发生异常, {}" , e.getMessage());
		} finally {
		    try {
		        if (response != null) {
		            EntityUtils.consume(response.getEntity());
		        }
		    } catch (IOException e) {
		    	logger.error("关闭链接发生异常, {}" , e.getMessage());
		    }
		}
	}
}
