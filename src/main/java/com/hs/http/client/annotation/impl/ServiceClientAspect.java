package com.hs.http.client.annotation.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.protobuf.Message;
import com.hs.http.client.annotation.ServiceRpc;

@Aspect
@Component
public class ServiceClientAspect {
	
	private static Logger logger = LoggerFactory.getLogger(ServiceClientAspect.class);
	
	private com.hs.service.rpc.ServiceRpc rpc = new com.hs.service.rpc.ServiceRpc();
	
	@Pointcut("@annotation(com.hs.http.client.annotation.ServiceRpc)")
    public void serviceRpcPointCut() {
    }

	@Around("serviceRpcPointCut() && @annotation(serviceRpc)")
    public Object around(ProceedingJoinPoint point , ServiceRpc serviceRpc) throws Throwable {
		logger.info("服务调用 {} {} {} {}" , point.getTarget().getClass().getName() , serviceRpc.path() , serviceRpc.desc() , 
				ServiceMgr.getInstance().getService(point.getTarget().getClass().getName()));
		
		MethodSignature signature = (MethodSignature) point.getSignature();
		if (signature.getParameterNames().length != 1) {
			logger.error("入参数量不对，查看方法原型 {}" , signature.getParameterNames().length);
			
			return point.proceed();
		}
		
		try {
			Message argument = (Message) point.getArgs()[0];
			if (argument == null) {
				logger.error("入参为空 {}" , serviceRpc.path());
				
				return point.proceed();
			}

			return rpc.serviceRpc(ServiceMgr.getInstance().getService(point.getTarget().getClass().getName()) , serviceRpc.path() , argument.toByteArray());
		} catch (Exception e) {
			logger.error("调用RPC异常 {}" , e.getMessage());
			
			return point.proceed();
		}
	}
}
