package com.hs.http.client.cfg;

import java.util.Collections;
import java.util.List;

public class AppCfgCenter {
	public static Integer getKey(String service , String key , Integer defaultValue) {
		return defaultValue;
	}
	
	public static String getKey(String service , String key , String defaultValue) {
		return defaultValue;
	}
	
	public static List<String> getAvaliableServiceInstances(String service) {
		return Collections.emptyList();
	}
	
	public static String getServiceName() {
		return "";
	}
}
