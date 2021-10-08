package jkml;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;

class ExpiryTests {

	private static final String TOKEN_KEY = "TOKEN_KEY";

	private static final int TOKEN_LIFE_TIME_IN_SEC = 5;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final AtomicInteger getTokenCount = new AtomicInteger(0);

	@Test
	void testExpireAfterCreate() {
		LoadingCache<String, String> tokenCache = Caffeine.newBuilder().expireAfter(new Expiry<String, String>() {
			public long expireAfterCreate(String key, String token, long currentTime) {
				log.info("expireAfterCreate called");
				Instant now = Instant.now();
				Instant exp = extractExpirationTimeFromToken(token);
				log.debug("Current time: {}; Expiration time: {}", now, exp);
				return Duration.between(now, exp).toNanos();
			}

			public long expireAfterUpdate(String key, String token, long currentTime, long currentDuration) {
				return currentDuration;
			}

			public long expireAfterRead(String key, String token, long currentTime, long currentDuration) {
				return currentDuration;
			}
		}).maximumSize(1).build(key -> generateToken(key));

		log.info("Get token from cache 1");
		tokenCache.get(TOKEN_KEY);
		log.info("Get token from cache 2");
		tokenCache.get(TOKEN_KEY);
		assertEquals(1, getTokenCount.get());

		await().pollDelay(Duration.ofSeconds(TOKEN_LIFE_TIME_IN_SEC)).until(() -> true);

		log.info("Get token from cache 3");
		tokenCache.get(TOKEN_KEY);
		log.info("Get token from cache 4");
		tokenCache.get(TOKEN_KEY);
		assertEquals(2, getTokenCount.get());
	}

	private String generateToken(String key) {
		long iat = Instant.now().getEpochSecond();
		long exp = iat + TOKEN_LIFE_TIME_IN_SEC;
		String token = Long.toString(iat) + "|" + Long.toString(exp);
		log.info("Generated token: {}", token);
		getTokenCount.addAndGet(1);
		return token;
	}

	private static Instant extractExpirationTimeFromToken(String token) {
		String[] claims = token.split(Pattern.quote("|"));
		return Instant.ofEpochSecond(Long.parseLong(claims[1]));
	}

}
