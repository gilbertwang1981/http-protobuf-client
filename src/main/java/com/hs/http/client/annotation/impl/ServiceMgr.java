package com.hs.http.client.annotation.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceMgr {
	
	private static ServiceMgr instance = null;
	
	public static ServiceMgr getInstance() {
		if (instance == null) {
			synchronized (ServiceMgr.class) {
				if (instance == null) {
					instance = new ServiceMgr();
				}
			}
		}
		
		return instance;
	}
	
	private Map<String , String> servicesMapper = new ConcurrentHashMap<>(); 
	
	public String getService(String clazz) {
		return servicesMapper.get(clazz);
	}
	
	public void setService(String clazz , String service) {
		servicesMapper.put(clazz , service);
	}
}
