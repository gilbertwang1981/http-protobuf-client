package com.hs.http.client.cfg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.hs.http.client.annotation.impl.ServiceMgr;
import com.hs.http.client.redis.RedisRepository;
import com.hs.http.client.utils.AddressConvertor;
import com.hs.http.consts.ServiceRpcHttpClientConsts;

public class AppCfgCenter {
	private static String service;
	
	public static Integer getKey(String service , String key , Integer defaultValue) {
		String value = RedisRepository.getInstance().get(ServiceRpcHttpClientConsts.DEFAULT_REDIS_KEY_PREFIX + ":" + service + ":" + key);
		if (value == null) {
			return defaultValue;
		} else {
			return Integer.parseInt(value);
		}
	}
	
	public static String getKey(String service , String key , String defaultValue) {
		String value = RedisRepository.getInstance().get(ServiceRpcHttpClientConsts.DEFAULT_REDIS_KEY_PREFIX + ":" + service + ":" + key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
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
