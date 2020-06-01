package com.hs.http.client.annotation.impl;

import java.io.IOException;

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
			logger.error("服务注册失败，进程启动失败 {}" , service);
			
			System.exit(-1);
		}
		
		logger.info("【服务注册】服务名：{} 服务描述：{}" , service , desc);
		
		if(!AppCfgCenterMonitor.getInstance().listen()) {
			logger.error("服务监听失败");
			
			System.exit(0);
		}
		
		logger.info("监控端口启动，{}" , ServiceRpcHttpClientConsts.DEFAULT_SERVICE_LISTEN_PORT);
	}
}
