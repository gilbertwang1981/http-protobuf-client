package com.hs.http.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.hs.http.client.annotation.impl.ServiceStartupDiscovery;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(ServiceStartupDiscovery.class)
public @interface ServiceStartup {
	/**
	 * 服务地址：service
	 * 例子：cart-service
	 * @return
	 */
	String service();
	
	/**
	 * 服务描述
	 * @return
	 */
	String desc();
}
