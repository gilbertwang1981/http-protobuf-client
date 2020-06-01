package com.hs.http.client.annotation.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hs.http.consts.ServiceRpcHttpClientConsts;

public class AppCfgCenterMonitor extends Thread {
	
	private static AppCfgCenterMonitor instance = null;
	
	private static Logger logger = LoggerFactory.getLogger(AppCfgCenterMonitor.class);
	
	private ServerSocket server = null;
	
	public static AppCfgCenterMonitor getInstance() {
		if (instance == null) {
			synchronized (AppCfgCenterMonitor.class) {
				if (instance == null) {
					instance = new AppCfgCenterMonitor();
				}
			}
		}
		
		return instance;
	}
	
	public Boolean listen() {
		try {
			server = new ServerSocket(ServiceRpcHttpClientConsts.DEFAULT_SERVICE_LISTEN_PORT);
			
			start();
			
			return Boolean.TRUE;
		} catch (Exception e) {
			logger.error("启动监控服务端口异常，{}" , e.getMessage());
			
			return Boolean.FALSE;
		}
	}
	
	public void run() {
		while (Boolean.TRUE) {
			Socket socket = null;
			try {
				socket = server.accept();
			} catch (Exception e) {
				logger.error("接受客户端ping请求失败，{}" , e.getMessage());
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						logger.error("关闭socket失败, {}" , e.getMessage());
					}
				}
			}
			
		}
	}
}
