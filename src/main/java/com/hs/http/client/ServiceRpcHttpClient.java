package com.hs.http.client;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hs.cfg.center.sdk.AppCfgCenter;
import com.hs.http.consts.ServiceRpcHttpClientConsts;

public class ServiceRpcHttpClient {
	private static Logger logger = LoggerFactory.getLogger(ServiceRpcHttpClient.class);
	
	private CloseableHttpClient httpClient = null;
	
	private static final Integer HTTP_CODE_SUCCESS = 200;
	private static final Integer CONNECT_TIMEOUT = 250;
	private static final Integer READ_TIMEOUT = 100;
	private static final Integer MAX_TOTAL_CONN = 100;
	private static final Integer MAX_PER_ROUTE = 25;
	private static final Integer MAX_RETRY_TIMES = 2;
	private static final Integer DEFAULT_KA_TIME = 60000;
	
	private String serviceName;
	
	public String getDefaultDomainName(String service) {
		return AppCfgCenter.getKey(serviceName , "/" + service + "/" + ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_DEFAULT_DOMAIN , "");
	}
	
	private Integer getMaxRetryTimes(String service) {
		return AppCfgCenter.getKey(service , ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_RETRY_TIMES , MAX_RETRY_TIMES);
	}
	
	private Integer getConnectionTimeout(String service , String method) {
		return AppCfgCenter.getKey(service , method  + "/" + ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_CONNECT_TIMEOUT , CONNECT_TIMEOUT);
	}
	
	private Integer getReadTimeout(String service , String method) {
		return AppCfgCenter.getKey(service , method  + "/" + ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_READ_TIMEOUT , READ_TIMEOUT);
	}
	
	private Integer getMaxConnection(String service) {
		return AppCfgCenter.getKey(service, ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_MAX_CONNS , MAX_TOTAL_CONN);
	}
	
	private Integer getMaxConnectionPerRoute(String service) {
		return AppCfgCenter.getKey(service, ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_MAX_CONNS_PER_ROUTE , MAX_PER_ROUTE);
	}
	
	private Integer getKeepaliveTime(String service) {
		return AppCfgCenter.getKey(service, ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_KEEPALIVE_TIME , DEFAULT_KA_TIME);
	}
	
	private DefaultConnectionKeepAliveStrategy keepAliveStrategy = new DefaultConnectionKeepAliveStrategy() {
		@Override
		public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
			long keepAlive = super.getKeepAliveDuration(response, context);
			if (keepAlive == -1) {
				return getKeepaliveTime(serviceName);
			}
			
			return keepAlive;
		}
	};
	
	public ServiceRpcHttpClient(String serviceName) {
		this.serviceName = serviceName;
		
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();  
	    cm.setMaxTotal(getMaxConnection(serviceName));  
	    cm.setDefaultMaxPerRoute(getMaxConnectionPerRoute(serviceName));
	    SocketConfig socketConfig = SocketConfig.custom()
                .setSoKeepAlive(true)
                .build();

		cm.setDefaultSocketConfig(socketConfig);
				    
		httpClient = HttpClients.custom()  
		        .setConnectionManager(cm)
		        .setRetryHandler(new DefaultHttpRequestRetryHandler(getMaxRetryTimes(serviceName) , true)).setKeepAliveStrategy(keepAliveStrategy)  
		        .build();  
	}

	public HttpResponse post(String host , Integer port , String url , byte[] content) throws Exception {
		logger.info("选择服务器地址 {}:{}" , host , port);
		
		try {
			URI uri = new URI("http", null, host , port , url, "", null);
			HttpPost post = new HttpPost(uri);
		
		    post.setEntity(new ByteArrayEntity(content));
		    post.addHeader("Content-Type", "application/x-protobuf;charset=UTF-8");
		    post.addHeader("Connection", "keep-alive");
		    
		    RequestConfig rconfig = RequestConfig.custom().
		    		setSocketTimeout(getReadTimeout(serviceName , url)).
		    		setConnectTimeout(getConnectionTimeout(serviceName , url)).build();
		    post.setConfig(rconfig);
		    
		    HttpResponse response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() == HTTP_CODE_SUCCESS) {				
				return response;
			} else {
				logger.error("HTTP调用失败 {}" , response.getStatusLine().getStatusCode());
				
				throw new IOException("HTTP调用失败,状态码 " + response.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void returnResource(HttpResponse response) {
		try {
			EntityUtils.consume(response.getEntity());
		} catch (IOException e) {
			logger.error("归还HTTP连接资源失败 {}" , e.getMessage());
		}
	}
}
