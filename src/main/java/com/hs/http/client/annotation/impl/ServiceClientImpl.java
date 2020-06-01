package com.hs.http.client.annotation.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import com.hs.http.client.annotation.ServiceClient;

@Order(1)
public class ServiceClientImpl extends ServiceClientSpringFactoryImportSelector<ServiceClient> {
	
	private static Logger logger = LoggerFactory.getLogger(ServiceClientImpl.class);

	@Override
	public String[] selectImports(AnnotationMetadata metadata) {
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(
				metadata.getAnnotationAttributes(getAnnotationClass().getName(), true));
		
		initApplication(attributes , metadata.getClassName());

		return new String[0];
	}

	private void initApplication(AnnotationAttributes attributes , String clazz) {
		String service = attributes.getString("service");
		String desc = attributes.getString("desc");
		
		logger.info("初始化服务调用 {} {} {}" , service , desc , clazz);
		
		ServiceMgr.getInstance().setService(clazz , service);
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
	}

	@Override
	public void setEnvironment(Environment environment) {
	}
}
