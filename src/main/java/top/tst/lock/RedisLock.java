package top.tst.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 
 * 根据时间戳等获取分布式锁
 * 
 * @author Liuweiyi
 *
 */
public class RedisLock implements Lock {

	private static Logger logger = LoggerFactory.getLogger(RedisLock.class);

	private RedisTemplate<String, String> redisTemplate;

	private static final int DEFAULT_ACQUIRY_RESOLUTION_MILLIS = 100;

	/**
	 * Lock key path.
	 */
	private String lockKey;

	/**
	 * 锁超时时间，防止线程在入锁以后，无限的执行等待
	 */
	private int expireMsecs = 10 * 60 * 1000;// 1h 时间限制 毫秒

	/**
	 * 锁等待时间，防止线程饥饿
	 */
	private int timeoutMsecs = 10 * 1000;

	private volatile boolean locked = false;

	/**
	 * Detailed constructor with default acquire timeout 10000 msecs and lock
	 * expiration of 60000 msecs.
	 *
	 * @param lockKey
	 *            lock key (ex. account:1, ...)
	 */
	public RedisLock(RedisTemplate<String, String> redisTemplate, String lockKey) {
		this.redisTemplate = redisTemplate;
		this.lockKey = lockKey + "_lock";
	}

	/**
	 * Detailed constructor with default lock expiration of 60000 msecs.
	 *
	 */
	public RedisLock(RedisTemplate<String, String> redisTemplate, String lockKey, int timeoutMsecs) {
		this(redisTemplate, lockKey);
		this.timeoutMsecs = timeoutMsecs;
	}

	/**
	 * Detailed constructor.
	 *
	 */
	public RedisLock(RedisTemplate<String, String> redisTemplate, String lockKey, int timeoutMsecs, int expireMsecs) {
		this(redisTemplate, lockKey, timeoutMsecs);
		this.expireMsecs = expireMsecs;
	}

	/**
	 * @return lock key
	 */
	public String getLockKey() {
		return lockKey;
	}

	private String get(final String key) {
		Object obj = null;
		try {
			obj = redisTemplate.execute(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					StringRedisSerializer serializer = new StringRedisSerializer();
					byte[] data = connection.get(serializer.serialize(key));
					connection.close();
					if (data == null) {
						return null;
					}
					return serializer.deserialize(data);
				}
			});
		} catch (Exception e) {
			logger.error("get redis error, key : {}", key);
		}
		return obj != null ? obj.toString() : null;
	}

	private boolean setNX(final String key, final String value) {
		Object obj = null;
		try {
			obj = redisTemplate.execute(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					StringRedisSerializer serializer = new StringRedisSerializer();
					Boolean success = connection.setNX(serializer.serialize(key), serializer.serialize(value));
					connection.close();
					return success;
				}
			});
		} catch (Exception e) {
			logger.error("setNX redis error, key : {}", key);
		}
		return obj != null ? (Boolean) obj : false;
	}

	private String getSet(final String key, final String value) {
		Object obj = null;
		try {
			obj = redisTemplate.execute(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					StringRedisSerializer serializer = new StringRedisSerializer();
					byte[] ret = connection.getSet(serializer.serialize(key), serializer.serialize(value));
					connection.close();
					return serializer.deserialize(ret);
				}
			});
		} catch (Exception e) {
			logger.error("setNX redis error, key : {}", key);
		}
		return obj != null ? (String) obj : null;
	}

	/**
	 * Acqurired lock release.
	 */
	@Override
	public void unlock() {
		if (locked) {
			redisTemplate.delete(lockKey);
			locked = false;
		}
	}

	@Override
	public void lock() {
		// TODO Auto-generated method stub

	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean tryLock() {
		long expires = System.currentTimeMillis() + expireMsecs + 1;
		String expiresStr = String.valueOf(expires); // 锁到期时间设定
		if (this.setNX(lockKey, expiresStr)) {
			// lock acquired
			locked = true;
			return true;
		}

		String currentValueStr = this.get(lockKey); // redis里的时间
		if (currentValueStr != null && Long.parseLong(currentValueStr) < System.currentTimeMillis()) {
			// 判断是否为空，不为空的情况下，如果被其他线程设置了值，则第二个条件判断是过不去的
			// lock is expired

			String oldValueStr = this.getSet(lockKey, expiresStr);
			// 获取上一个锁到期时间，并设置现在的锁到期时间，
			// 只有一个线程才能获取上一个线上的设置时间，因为jedis.getSet是同步的
			if (oldValueStr != null && oldValueStr.equals(currentValueStr)) {
				// 防止误删（覆盖，因为key是相同的）了他人的锁——这里达不到效果，这里值会被覆盖，但是因为什么相差了很少的时间，所以可以接受

				// [分布式的情况下]:如过这个时候，多个线程恰好都到了这里，但是只有一个线程的设置值和当前值相同，他才有权利获取锁
				// lock acquired
				locked = true;
				return true;
			}
		}
		// 未获取锁
		return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {

		return false;
	}

	@Override
	public Condition newCondition() {
		// TODO Auto-generated method stub
		return null;
	}

}
