package com.hs.service.rpc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hs.http.client.ServiceRpcHttpClient;
import com.hs.http.client.cfg.AppCfgCenter;

public class ServiceRpc {
	private static Logger logger = LoggerFactory.getLogger(ServiceRpc.class);
	
	private ServiceRpcHttpClient client = new ServiceRpcHttpClient(AppCfgCenter.getServiceName());
	
	private AtomicInteger counter = new AtomicInteger(0);
	
	private static final Integer DEFAULT_DELTA = 1000000;
	
	private static final Integer FLUSH_TIMEOUT = 3;
	
	private static final Integer INIT_SIZE = 8;
	
	private static final Integer MAX_SIZE = 128;
	
	private static final Integer DEFAULT_PORT = 8080;
	
	private Map<String , List<String>> recoveryHosts = new ConcurrentHashMap<>();
	
	private LoadingCache<String , List<String>> hostsCache = CacheBuilder.newBuilder().initialCapacity(INIT_SIZE).maximumSize(MAX_SIZE)
			.refreshAfterWrite(FLUSH_TIMEOUT , TimeUnit.SECONDS)
			.recordStats().build(new CacheLoader<String , List<String>>() {
                @Override
                public List<String> load(String key) {
                	return AppCfgCenter.getAvaliableServiceInstances(key);
                }
            });
	
	private Integer getPort() {
		String port = System.getenv("CALLEE_SERVICE_PORT");
		if (port == null) {
			return DEFAULT_PORT;
		}
		
		return Integer.parseInt(port);
	}
	
	public HttpResponse serviceRpc(String service , String method , byte [] content) throws Exception {
		if (client == null) {
			logger.error("http client句柄为空 {} {} {}" , service , getPort() , method);
			
			return null;
		}
		
		String targetHost = client.getDefaultDomainName(service);
		if (targetHost != null && ! "".equals(targetHost)) {
			logger.info("使用域名路由 {}" , client.getDefaultDomainName(service));
			
			return client.post(targetHost , getPort() , method , content);
		}
		
		List<String> hosts = hostsCache.get(service);
		if (hosts == null || hosts.size() == 0) {
			hosts = recoveryHosts.get(service);
			if (hosts == null || hosts.size() == 0) {
				logger.error("获取到的服务地址列表为空 {} {} {}" , service , getPort() , method);
				
				return null;
			}
		} else {
			recoveryHosts.put(service , hosts);
		}
		
		counter.compareAndSet(Integer.MAX_VALUE - DEFAULT_DELTA , 0);
		
		return client.post(
				hosts.get(counter.addAndGet(1) % hosts.size()) , 
				getPort() , method , content);
	}
	
	public void returnResource(HttpResponse response) {
		client.returnResource(response);
	}
}
