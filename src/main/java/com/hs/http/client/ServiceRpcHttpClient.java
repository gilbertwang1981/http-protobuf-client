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

import com.hs.http.client.cfg.AppCfgCenter;
import com.hs.http.consts.ServiceRpcHttpClientConsts;

public class ServiceRpcHttpClient {
	private static Logger logger = LoggerFactory.getLogger(ServiceRpcHttpClient.class);
	
	private CloseableHttpClient httpClient = null;
	
	private static final Integer HTTP_CODE_SUCCESS = 200;
	private static final Integer CONNECT_TIMEOUT = 1000;
	private static final Integer READ_TIMEOUT = 500;
	private static final Integer MAX_TOTAL_CONN = 100;
	private static final Integer MAX_PER_ROUTE = 25;
	private static final Integer MAX_RETRY_TIMES = 2;
	private static final Integer DEFAULT_KA_TIME = 60000;
	
	private String serviceName;
	
	// 局部配置，根据具体被调用的服务配置
	// 键值：/本服务名/被调用的服务名/service-rpc-default-doamin-name
	public String getDefaultDomainName(String service) {
		return AppCfgCenter.getKey(serviceName , "/" + service + "/" + ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_DEFAULT_DOMAIN , "");
	}
	
	// 全局配置，当前服务共享配置
	// 键值：/本服务名/service-rpc-retry-times
	private Integer getMaxRetryTimes(String service) {
		return AppCfgCenter.getKey(service , ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_RETRY_TIMES , MAX_RETRY_TIMES);
	}
	
	// 局部配置，根据具体被调用的服务配置
	// 键值：/本服务名/被调用的服务名/method/service-rpc-connect-timeout
	private Integer getConnectionTimeout(String service , String calledService , String method) {
		return AppCfgCenter.getKey(service , "/" + calledService + "/" + method  + "/" + ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_CONNECT_TIMEOUT , CONNECT_TIMEOUT);
	}
	
	// 局部配置，根据具体被调用的服务配置
	// 键值：/本服务名/被调用的服务名/method/service-rpc-read-timeout
	private Integer getReadTimeout(String service , String calledService , String method) {
		return AppCfgCenter.getKey(service , "/" + calledService + "/" + method  + "/" + ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_READ_TIMEOUT , READ_TIMEOUT);
	}
	
	// 全局配置，当前服务共享配置
	// 键值：/本服务名/service-rpc-max-conn-num
	private Integer getMaxConnection(String service) {
		return AppCfgCenter.getKey(service, ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_MAX_CONNS , MAX_TOTAL_CONN);
	}
	
	// 全局配置，当前服务共享配置
	// 键值：/本服务名/service-rpc-max-conn-per-route
	private Integer getMaxConnectionPerRoute(String service) {
		return AppCfgCenter.getKey(service, ServiceRpcHttpClientConsts.SERVICE_RPC_HTTP_MAX_CONNS_PER_ROUTE , MAX_PER_ROUTE);
	}
	
	// 全局配置，当前服务共享配置
	// 键值：/本服务名/service-rpc-keep-alive-time
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

	public HttpResponse post(String service , String host , Integer port , String url , byte[] content) throws Exception {
		logger.info("选择服务器地址 {}:{}" , host , port);
		
		try {
			URI uri = new URI("http", null, host , port , url, "", null);
			HttpPost post = new HttpPost(uri);
		
		    post.setEntity(new ByteArrayEntity(content));
		    post.addHeader("Content-Type", "application/x-protobuf;charset=UTF-8");
		    post.addHeader("Connection", "keep-alive");
		    
		    RequestConfig rconfig = RequestConfig.custom().
		    		setSocketTimeout(getReadTimeout(serviceName , service , url)).
		    		setConnectTimeout(getConnectionTimeout(serviceName , service , url)).build();
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
