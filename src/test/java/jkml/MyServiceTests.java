package jkml;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyServiceTests {

	private static final String KEY1 = "key1";

	private static final String KEY2 = "key2";

	@Autowired
	private MyService svc;

	void testMethod(Function<String, String> method, IntSupplier methodCallCount, Duration cacheTtl) {
		method.apply(KEY1);
		assertEquals(1, methodCallCount.getAsInt());
		method.apply(KEY1);
		assertEquals(1, methodCallCount.getAsInt());
		method.apply(KEY2);
		assertEquals(2, methodCallCount.getAsInt());
		method.apply(KEY2);
		assertEquals(2, methodCallCount.getAsInt());

		await().pollDelay(cacheTtl.dividedBy(2)).until(() -> true);

		method.apply(KEY1);
		assertEquals(2, methodCallCount.getAsInt());
		method.apply(KEY2);
		assertEquals(2, methodCallCount.getAsInt());

		await().pollDelay(cacheTtl.dividedBy(2)).until(() -> true);

		method.apply(KEY1);
		assertEquals(3, methodCallCount.getAsInt());
		method.apply(KEY2);
		assertEquals(4, methodCallCount.getAsInt());
	}

	@Test
	void testMethodA() {
		testMethod(svc::methodA, svc::getMethodACallCount, MyConfiguration.CACHE_A_TTL);
	}

	@Test
	void testMethodB() {
		testMethod(svc::methodB, svc::getMethodBCallCount, MyConfiguration.CACHE_B_TTL);
	}

}
