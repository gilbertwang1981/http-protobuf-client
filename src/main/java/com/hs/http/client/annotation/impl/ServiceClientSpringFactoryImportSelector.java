package com.hs.http.client.annotation.impl;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.GenericTypeResolver;


public abstract class ServiceClientSpringFactoryImportSelector<T> implements DeferredImportSelector, BeanClassLoaderAware, EnvironmentAware {
	private Class<T> annotationClass;
	
	@SuppressWarnings("unchecked")
	protected ServiceClientSpringFactoryImportSelector() {
		this.annotationClass = (Class<T>) GenericTypeResolver
				.resolveTypeArgument(this.getClass() , ServiceClientSpringFactoryImportSelector.class);
	}
	
	protected Class<T> getAnnotationClass() {
		return this.annotationClass;
	}
}
