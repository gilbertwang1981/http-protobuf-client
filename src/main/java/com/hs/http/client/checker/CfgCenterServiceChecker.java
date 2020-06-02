package com.hs.http.client.checker;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.hs.http.client.cfg.AppCfgCenter;

@Component
public class CfgCenterServiceChecker {
	private static Logger logger = LoggerFactory.getLogger(CfgCenterServiceChecker.class);
	
	private static final Integer INITIAL_DELAY = 5000;
	private static final Integer FIXED_INTERVAL = 5000;
	
	@PostConstruct
	private void initialize() {
		new Timer().schedule(new TimerTask() {  
            @Override  
            public void run() {
            	Set<String> services = AppCfgCenter.getServiceNames();
            	Iterator<String> serviceIterator = services.iterator();  
            	while (serviceIterator.hasNext()) {  
            		String service = serviceIterator.next();
            		List<String> hosts = AppCfgCenter.getAvaliableServiceInstances(service);
            		Iterator<String> hostIterator = hosts.iterator();
            		while (hostIterator.hasNext()) {
            			String host = hostIterator.next();
            			try {
	            			if (!ping(host , service)) {
	            				logger.info("删除节点 {}-{}" , service , host);
	            				
	            				AppCfgCenter.delServiceHost(service, host);
	            			} else {
	            				logger.info("节点健康 {}/{}" , service , host);
	            			}
            			} catch (Exception e) {
            				logger.info("发生异常，删除节点 {}/{}/{}" , service , host , e.getMessage());
            				
            				AppCfgCenter.delServiceHost(service, host);
            			}
            		}
            	}
            }  
        } , INITIAL_DELAY , FIXED_INTERVAL);
	}
	
	private Boolean ping(String host , String service) {
		return AppCfgCenter.isExisted(host , service);
	}
}
