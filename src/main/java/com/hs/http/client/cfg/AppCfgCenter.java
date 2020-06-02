package com.hs.http.client.cfg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hs.http.client.annotation.impl.ServiceMgr;
import com.hs.http.client.redis.RedisRepository;
import com.hs.http.client.utils.AddressConvertor;
import com.hs.http.consts.ServiceRpcHttpClientConsts;

public class AppCfgCenter {
	private static String service;
	
	private static final Integer FLUSH_TIMEOUT = 5;
	private static final Integer INIT_SIZE = 8;
	private static final Integer MAX_SIZE = 128;
	
	private static LoadingCache<String , String> configCache = CacheBuilder.newBuilder().initialCapacity(INIT_SIZE).maximumSize(MAX_SIZE)
			.refreshAfterWrite(FLUSH_TIMEOUT , TimeUnit.SECONDS)
			.recordStats().build(new CacheLoader<String , String>() {
	        @Override
	        public String load(String key) {
	        	String value = RedisRepository.getInstance().get(key);
	        	if (value == null) {
	        		return "";
	        	}
	        	
	        	return value;
	        }
    });
	
	public static Integer getKey(String service , String key , Integer defaultValue) {
		try {
			return Integer.parseInt(configCache.get(ServiceRpcHttpClientConsts.DEFAULT_REDIS_KEY_PREFIX + ":" + service + ":" + key));
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public static String getKey(String service , String key , String defaultValue) {
		try {
			return configCache.get(ServiceRpcHttpClientConsts.DEFAULT_REDIS_KEY_PREFIX + ":" + service + ":" + key);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public static List<String> getAvaliableServiceInstances(String service) {
		Set<String> hosts = RedisRepository.getInstance().smembers(ServiceRpcHttpClientConsts.DEFAULT_REDIS_KEY_PREFIX + ":" + service + ":hosts");
		if (hosts.isEmpty()) {
			return Collections.emptyList();
		} else {
			return new ArrayList<>(hosts);
		}
	}
	
	public static Boolean delServiceHost(String service , String host) {
		return RedisRepository.getInstance().srem(ServiceRpcHttpClientConsts.DEFAULT_REDIS_KEY_PREFIX + ":" + service + ":hosts" , host);
	}
	
	public static Set<String> getServiceNames() {
		return ServiceMgr.getInstance().getAllServices();
	}
	
	public static Boolean registerService(String service) throws IOException {
		return RedisRepository.getInstance().sadd(ServiceRpcHttpClientConsts.DEFAULT_REDIS_KEY_PREFIX + ":" + service + ":hosts" , 
				AddressConvertor.getLocalIPList().get(0) , -1);
	}
	
	public static Boolean pingService(String service) throws IOException {
		return RedisRepository.getInstance().set(ServiceRpcHttpClientConsts.DEFAULT_REDIS_KEY_PREFIX + ":" + service + ":host:" + AddressConvertor.getLocalIPList().get(0) , 
				 service , ServiceRpcHttpClientConsts.SERVICE_REG_DISCOVERY_EXP);
	}
	
	public static Boolean isExisted(String ip , String service) {
		return RedisRepository.getInstance().isExsited(ServiceRpcHttpClientConsts.DEFAULT_REDIS_KEY_PREFIX + ":" + service + ":host:" + ip);
	}
	
	public static void setServiceName(String service) {
		AppCfgCenter.service = service;
	}
	
	public static String getServiceName() {
		return service;
	}
}
