package top.tst.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {

	private static Logger logger = LoggerFactory.getLogger(RedisConfig.class);

	// 获取springboot配置文件的值 (get的时候获取)
	@Value("${spring.redis.hostName}")
	private String host;

	@Value("${spring.redis.password}")
	private String password;

	/**
	 * @Bean 和 @ConfigurationProperties
	 *       该功能在官方文档是没有提到的，我们可以把@ConfigurationProperties和@Bean和在一起使用。
	 *       举个例子，我们需要用@Bean配置一个Config对象，Config对象有a，b，c成员变量需要配置，
	 *       那么我们只要在yml或properties中定义了a=1,b=2,c=3，
	 *       然后通过@ConfigurationProperties就能把值注入进Config对象中
	 * @return
	 */
	@Bean
	@ConfigurationProperties(prefix = "spring.redis.pool")
	public JedisPoolConfig getRedisConfig() {
		JedisPoolConfig config = new JedisPoolConfig();
		return config;
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.redis")
	public JedisConnectionFactory getConnectionFactory() {
		JedisConnectionFactory factory = new JedisConnectionFactory();
		factory.setUsePool(true);
		JedisPoolConfig config = getRedisConfig();
		factory.setPoolConfig(config);
		logger.warn("JedisConnectionFactory bean init success.");
		return factory;
	}

	@Bean
	public RedisTemplate<?, ?> redisTemplate() {
		JedisConnectionFactory factory = getConnectionFactory();
		logger.warn("[redis] =============={}", this.host + "," + factory.getHostName() + "," + factory.getDatabase());
		logger.warn("[redis] =============={}", this.password + "," + factory.getPassword());
		logger.warn("[redis] ======maxidle:{}", factory.getPoolConfig().getMaxIdle());
		// factory.setHostName(this.host);
		// factory.setPassword(this.password);
		RedisTemplate<?, ?> template = new StringRedisTemplate(getConnectionFactory());
		Jackson2JsonRedisSerializer<Object> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.ANY);
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jsonRedisSerializer.setObjectMapper(objectMapper);
		template.setValueSerializer(jsonRedisSerializer);

		return template;
	}

	/**
	 * 
	 * 特定容器 有效了时间设定
	 * 
	 * @author Liuweiyi
	 * @param redisTemplate
	 * @return
	 */
	@Bean
	public CacheManager cacheManager(RedisTemplate<?, ?> redisTemplate) {
		RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
		cacheManager.setDefaultExpiration(1800);// 默认30分钟
		Map<String, Long> expiresMap = new HashMap<>();
//		expiresMap.put(ExchangeRatesService.CACHE_NAME_USDT_RATE, 60L);// 特定容器默认60秒
//		expiresMap.put(ExchangeRatesService.CACHE_NAME, 60L);// 特定容器默认60秒
//		expiresMap.put(ExchangeRatesService.CACHE_NAME_USDT_CHANGE, 60L);// 特定容器默认60秒
		cacheManager.setExpires(expiresMap);
		return cacheManager;
	}

}
