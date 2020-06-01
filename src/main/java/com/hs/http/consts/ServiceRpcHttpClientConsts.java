package com.hs.http.consts;

public interface ServiceRpcHttpClientConsts {
	public static final String SERVICE_RPC_HTTP_CONNECT_TIMEOUT = "service-rpc-connect-timeout";
	public static final String SERVICE_RPC_HTTP_READ_TIMEOUT = "service-rpc-read-timeout";
	public static final String SERVICE_RPC_HTTP_MAX_CONNS = "service-rpc-max-conn-num";
	public static final String SERVICE_RPC_HTTP_MAX_CONNS_PER_ROUTE = "service-rpc-max-conn-per-route";
	public static final String SERVICE_RPC_HTTP_RETRY_TIMES = "service-rpc-retry-times";
	public static final String SERVICE_RPC_HTTP_KEEPALIVE_TIME = "service-rpc-keep-alive-time";
	public static final String SERVICE_RPC_HTTP_DEFAULT_DOMAIN = "service-rpc-default-doamin-name";
	
	public static final Integer REDIS_CONFIG_MAX_IDLE = 10;
	public static final Integer REDIS_CONFIG_MAX_TOTAL = 30;
	public static final Integer REDIS_CONFIG_MIN_IDLE = 5;
	public static final Integer REDIS_CONFIG_MAX_WAIT_IN_MILLS = 1000;
	public static final Integer REDIS_CONFIG_POOL_TMO = 1000;
	
	public static final String DEFAULT_REDIS_KEY_PREFIX = "service-discovery-key-prefix";
	
	public static final Integer DEFAULT_SERVICE_LISTEN_PORT = 11918;
}
