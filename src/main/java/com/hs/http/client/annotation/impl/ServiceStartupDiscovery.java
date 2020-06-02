package com.hs.http.client.annotation.impl;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import com.hs.http.client.annotation.ServiceStartup;
import com.hs.http.client.cfg.AppCfgCenter;
import com.hs.http.consts.ServiceRpcHttpClientConsts;

@Order(1)
public class ServiceStartupDiscovery extends ServiceRegisterSpringFactoryImportSelector<ServiceStartup> {
	private static Logger logger = LoggerFactory.getLogger(ServiceStartupDiscovery.class);

	@Override
	public String[] selectImports(AnnotationMetadata metadata) {
		
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(
				metadata.getAnnotationAttributes(getAnnotationClass().getName(), true));
		
		try {
			initApplication(attributes);
		} catch (IOException e) {
			logger.error("服务注册失败，进程停止");
			
			System.exit(-1);
		}
		
		return new String[0];
	}

	private void initApplication(AnnotationAttributes attributes) throws IOException {
		String service = attributes.getString("service");
		String desc = attributes.getString("desc");
		
		AppCfgCenter.setServiceName(service);
			
		if (!AppCfgCenter.registerService(service)) {
			logger.error("服务注册失败 {}" , service);
			
			System.exit(0);
		}
		
		new Timer().scheduleAtFixedRate(new TimerTask() {
			public void run() {
				try {
					if (!AppCfgCenter.pingService(service)) {
						logger.error("心跳失败 {}" , service);
					} else {
						logger.info("心跳成功 {}" , service);
					}
				} catch (IOException e) {
					logger.error("心跳异常 {}" , e.getMessage());
				}
			}
		} , ServiceRpcHttpClientConsts.DEFAULT_TIMER_DELAY , ServiceRpcHttpClientConsts.DEFAULT_TIMER_INTERVAL); 
		
		logger.info("【服务注册】服务名：{} 服务描述：{}" , service , desc);
	}
}
