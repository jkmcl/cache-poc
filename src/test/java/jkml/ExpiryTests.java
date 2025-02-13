package jkml;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;

class ExpiryTests {

	private static final String TOKEN_KEY = "TOKEN_KEY";

	private static final int TOKEN_LIFE_TIME_IN_SEC = 5;

	private final Logger logger = LoggerFactory.getLogger(ExpiryTests.class);

	private final AtomicInteger generationCount = new AtomicInteger(0);

	private final AtomicInteger getCount = new AtomicInteger(0);

	@Test
	void testExpireAfter() {
		LoadingCache<String, String> tokenCache = Caffeine.newBuilder().expireAfter(new Expiry<String, String>() {
			private long expireAfterCreateOrUpdate(String token) {
				Instant exp = Instant.ofEpochSecond(Long.parseLong(token));
				return Duration.between(Instant.now(), exp).toNanos();
			}

			@Override
			public long expireAfterCreate(String key, String token, long currentTime) {
				logger.debug("expireAfterCreate called");
				return expireAfterCreateOrUpdate(token);
			}

			@Override
			public long expireAfterUpdate(String key, String token, long currentTime, long currentDuration) {
				logger.debug("expireAfterUpdate called");
				return expireAfterCreateOrUpdate(token);
			}

			@Override
			public long expireAfterRead(String key, String token, long currentTime, long currentDuration) {
				return currentDuration;
			}
		}).build(this::generateToken);

		getTokenFromCache(tokenCache, TOKEN_KEY);
		getTokenFromCache(tokenCache, TOKEN_KEY);
		assertEquals(1, generationCount.get());

		await().pollDelay(Duration.ofSeconds(TOKEN_LIFE_TIME_IN_SEC)).until(() -> true);

		getTokenFromCache(tokenCache, TOKEN_KEY);
		getTokenFromCache(tokenCache, TOKEN_KEY);
		assertEquals(2, generationCount.get());
	}

	private void getTokenFromCache(LoadingCache<String, String> tokenCache, String key) {
		int count = getCount.addAndGet(1);
		logger.info("Getting token from cache (count: {})", count);
		tokenCache.get(key);
	}

	private String generateToken(String key) {
		int count = generationCount.addAndGet(1);
		Instant exp = Instant.now().plusSeconds(TOKEN_LIFE_TIME_IN_SEC);
		logger.debug("Generating new token (count: {}); expiration time: {}", count, exp);
		return Long.toString(exp.getEpochSecond());
	}

}
