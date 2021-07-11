package jkml;

import java.time.Duration;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class MyConfiguration {

	public static final String CACHE_A_NAME = "cache_A";

	public static final String CACHE_B_NAME = "cache_B";

	public static final Duration CACHE_A_TTL = Duration.ofSeconds(4);

	public static final Duration CACHE_B_TTL = Duration.ofSeconds(6);

	private Logger log = LoggerFactory.getLogger(MyConfiguration.class);

	@Autowired
	private CacheProperties cacheProperties;

	@PostConstruct
	public void init() {
		if (CacheType.NONE.equals(cacheProperties.getType())) {
			log.warn("Caching is disabled");
		} else {
			log.info("Caching is enabled");
		}
	}

	@Bean
	public CacheManagerCustomizer<CaffeineCacheManager> caffeineCacheManagerCustomizer() {
		return cacheManager -> {
			log.info("Registering caches");
			cacheManager.registerCustomCache(CACHE_A_NAME, Caffeine.newBuilder().expireAfterWrite(CACHE_A_TTL).build());
			cacheManager.registerCustomCache(CACHE_B_NAME, Caffeine.newBuilder().expireAfterWrite(CACHE_B_TTL).build());
		};
	}

}
