package com.hs.http.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import com.hs.http.client.annotation.impl.ServiceClientImpl;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Import(ServiceClientImpl.class)
public @interface ServiceClient {
	/**
	 * 服务名字
	 * @return
	 */
	String service();
	
	/**
	 * 服务接口描述
	 * @return
	 */
	String desc();
}
