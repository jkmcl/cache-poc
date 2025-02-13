package jkml;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class MyService {

	private final Logger logger = LoggerFactory.getLogger(MyService.class);

	private int methodACallCount = 0;

	private int methodBCallCount = 0;

	public int getMethodACallCount() {
		return methodACallCount;
	}

	public int getMethodBCallCount() {
		return methodBCallCount;
	}

	@Cacheable(MyConfiguration.CACHE_A_NAME)
	public String methodA(String key) {
		++methodACallCount;
		logger.info("methodA called");
		return LocalDateTime.now().toString();
	}

	@Cacheable(MyConfiguration.CACHE_B_NAME)
	public String methodB(String key) {
		++methodBCallCount;
		logger.info("methodB called");
		return LocalDateTime.now().toString();
	}

}
