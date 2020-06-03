package com.hs.http.client.redis;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hs.http.consts.ServiceRpcHttpClientConsts;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisRepository {
	private static Logger logger = LoggerFactory.getLogger(RedisRepository.class);
	
	private JedisPool jedisPool;
	
	private static RedisRepository instance;
	
	public static RedisRepository getInstance() {
		if (instance == null) {
			synchronized (RedisRepository.class) {
				if (instance == null) {
					instance = new RedisRepository();
				}
			}
		}
		
		return instance;
	}
	
	private String getRedisHost() {
		return System.getenv("REDIS_HOST");
	}
	
	public String getRedisPort() {
		return System.getenv("REDIS_PORT");
	}
	
	public String getRedisPass() {
		return System.getenv("REDIS_PASS");
	}
	
	public Integer getRedisMaxIdle() {
		String idle = System.getenv("REDIS_MAX_IDLE");
		if (idle == null) {
			return ServiceRpcHttpClientConsts.REDIS_CONFIG_MAX_IDLE;
		} else {
			return Integer.parseInt(idle);
		}
	}
	
	public Integer getRedisMinIdle() {
		String idle = System.getenv("REDIS_MIN_IDLE");
		if (idle == null) {
			return ServiceRpcHttpClientConsts.REDIS_CONFIG_MIN_IDLE;
		} else {
			return Integer.parseInt(idle);
		}
	}
	
	public Integer getRedisMaxTotal() {
		String total = System.getenv("REDIS_MAX_TOTAL");
		if (total == null) {
			return ServiceRpcHttpClientConsts.REDIS_CONFIG_MAX_TOTAL;
		} else {
			return Integer.parseInt(total);
		}
	}
	
	public Integer getRedisMaxWaitInMills() {
		String wait = System.getenv("REDIS_MAX_WAIT_IN_MILLS");
		if (wait == null) {
			return ServiceRpcHttpClientConsts.REDIS_CONFIG_MAX_WAIT_IN_MILLS;
		} else {
			return Integer.parseInt(wait);
		}
	}
	
	public Integer getRedisPoolTimeout() {
		String timeout = System.getenv("REDIS_POOL_TIMEOUT");
		if (timeout == null) {
			return ServiceRpcHttpClientConsts.REDIS_CONFIG_POOL_TMO;
		} else {
			return Integer.parseInt(timeout);
		}
	}
	
	private RedisRepository() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setBlockWhenExhausted(false);
		config.setJmxEnabled(true);
		config.setMaxIdle(getRedisMaxIdle());
		config.setMaxTotal(getRedisMaxTotal());
		config.setMaxWaitMillis(getRedisMaxWaitInMills());
		config.setMinIdle(getRedisMinIdle());
		
		jedisPool = new JedisPool(config , getRedisHost() , Integer.parseInt(getRedisPort()) , 
				getRedisPoolTimeout() , getRedisPass());
	}
	
	public Boolean set(String key , String value , int milliseconds) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			
			if (milliseconds <= 0) {
				return (jedis != null) && (jedis.set(key, value) != null);
			} else {
				return (jedis != null) && (jedis.psetex(key, milliseconds, value) != null);
			}
		} catch (Exception e) {
			logger.error("set redis缓存失败, {}" , e.getMessage());
			
			return false;
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
	
	public String get(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if (jedis != null) {
				return jedis.get(key);
			}
		} catch (Exception e) {
			logger.error("set redis缓存失败, {}" , e.getMessage());
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		
		return null;
	}
	
	public Boolean isExsited(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if (jedis != null) {
				return jedis.exists(key);
			}
		} catch (Exception e) {
			logger.error("set redis缓存失败, {}" , e.getMessage());
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		
		return Boolean.FALSE;
	}
	
	public Boolean sadd(String key , String value , int exp) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if (jedis == null) {
				logger.error("链接池已满，key:{} value:{}" , key , value);
				
				return false;
			}
			
			jedis.sadd(key, value);
			if (exp > 0) {
				jedis.expire(key, exp);
			}
			
			return true;
		} catch (Exception e) {
			logger.error("访问redis发生异常,sadd操作，{}, {} , {}" ,  e.getMessage() , key , value);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		
		return false;
	}
	
	public Boolean srem(String key , String value) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if (jedis == null) {
				logger.error("链接池已满，key:{} value:{}" , key , value);
				
				return false;
			}
			
			jedis.srem(key, value);
			
			return true;
		} catch (Exception e) {
			logger.error("访问redis发生异常,srem操作，{}, {} , {}" ,  e.getMessage() , key , value);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		
		return false;
	}
	
	public Set<String> smembers(String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if (jedis == null) {
				logger.error("链接池已满，key:{}" , key);
				
				return Collections.emptySet();
			}
			
			return jedis.smembers(key);
		} catch (Exception e) {
			logger.error("访问redis发生异常,smembers操作，{}, {}" ,  e.getMessage() , key);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		
		return Collections.emptySet();
	}
}
