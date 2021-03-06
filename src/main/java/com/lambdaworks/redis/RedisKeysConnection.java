package com.lambdaworks.redis;

import java.util.Date;
import java.util.List;

import com.lambdaworks.redis.output.KeyStreamingChannel;
import com.lambdaworks.redis.output.ValueStreamingChannel;

/**
 * Synchronous executed commands for Keys (Key manipulation/querying).
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 3.0
 */
public interface RedisKeysConnection<K, V> {
    /**
     * Delete a key.
     * 
     * @param keys the key
     * @return Long integer-reply The number of keys that were removed.
     */
    Long del(K... keys);

    /**
     * Return a serialized version of the value stored at the specified key.
     * 
     * @param key the key
     * @return byte[] bulk-string-reply the serialized value.
     */
    byte[] dump(K key);

    /**
     * Determine if a key exists.
     * 
     * @param key the key
     * @return Boolean integer-reply specifically:
     * 
     *         <code>1</code> if the key exists. <code>0</code> if the key does not exist.
     */
    Boolean exists(K key);

    /**
     * Set a key's time to live in seconds.
     * 
     * @param key the key
     * @param seconds the seconds type: long
     * @return Boolean integer-reply specifically:
     * 
     *         <code>1</code> if the timeout was set. <code>0</code> if <code>key</code> does not exist or the timeout could not
     *         be set.
     */
    Boolean expire(K key, long seconds);

    /**
     * Set the expiration for a key as a UNIX timestamp.
     * 
     * @param key the key
     * @param timestamp the timestamp type: posix time
     * @return Boolean integer-reply specifically:
     * 
     *         <code>1</code> if the timeout was set. <code>0</code> if <code>key</code> does not exist or the timeout could not
     *         be set (see: <code>EXPIRE</code>).
     */
    Boolean expireat(K key, Date timestamp);

    /**
     * Set the expiration for a key as a UNIX timestamp.
     * 
     * @param key the key
     * @param timestamp the timestamp type: posix time
     * @return Boolean integer-reply specifically:
     * 
     *         <code>1</code> if the timeout was set. <code>0</code> if <code>key</code> does not exist or the timeout could not
     *         be set (see: <code>EXPIRE</code>).
     */
    Boolean expireat(K key, long timestamp);

    /**
     * Find all keys matching the given pattern.
     * 
     * @param pattern the pattern type: patternkey (pattern)
     * @return List&lt;K&gt; array-reply list of keys matching <code>pattern</code>.
     */
    List<K> keys(K pattern);

    /**
     * Find all keys matching the given pattern.
     * 
     * @param channel the channel
     * @param pattern the pattern
     * @return Long array-reply list of keys matching <code>pattern</code>.
     */
    Long keys(KeyStreamingChannel<K> channel, K pattern);

    /**
     * Atomically transfer a key from a Redis instance to another one.
     * 
     * @param host the host
     * @param port the port
     * @param key the key
     * @param db the database
     * @param timeout the timeout in milliseconds
     * @return String simple-string-reply The command returns OK on success.
     */
    String migrate(String host, int port, K key, int db, long timeout);

    /**
     * Move a key to another database.
     * 
     * @param key the key
     * @param db the db type: long
     * @return Boolean integer-reply specifically:
     */
    Boolean move(K key, int db);

    /**
     * returns the kind of internal representation used in order to store the value associated with a key.
     * 
     * @param key
     * @return String
     */
    String objectEncoding(K key);

    /**
     * returns the number of seconds since the object stored at the specified key is idle (not requested by read or write
     * operations).
     * 
     * @param key
     * @return number of seconds since the object stored at the specified key is idle.
     */
    Long objectIdletime(K key);

    /**
     * returns the number of references of the value associated with the specified key.
     * 
     * @param key
     * @return Long
     */
    Long objectRefcount(K key);

    /**
     * Remove the expiration from a key.
     * 
     * @param key the key
     * @return Boolean integer-reply specifically:
     * 
     *         <code>1</code> if the timeout was removed. <code>0</code> if <code>key</code> does not exist or does not have an
     *         associated timeout.
     */
    Boolean persist(K key);

    /**
     * Set a key's time to live in milliseconds.
     * 
     * @param key the key
     * @param milliseconds the milliseconds type: long
     * @return integer-reply, specifically:
     * 
     *         <code>1</code> if the timeout was set. <code>0</code> if <code>key</code> does not exist or the timeout could not
     *         be set.
     */
    Boolean pexpire(K key, long milliseconds);

    /**
     * Set the expiration for a key as a UNIX timestamp specified in milliseconds.
     * 
     * @param key the key
     * @param timestamp the milliseconds-timestamp type: posix time
     * @return Boolean integer-reply specifically:
     * 
     *         <code>1</code> if the timeout was set. <code>0</code> if <code>key</code> does not exist or the timeout could not
     *         be set (see: <code>EXPIRE</code>).
     */
    Boolean pexpireat(K key, Date timestamp);

    /**
     * Set the expiration for a key as a UNIX timestamp specified in milliseconds.
     * 
     * @param key the key
     * @param timestamp the milliseconds-timestamp type: posix time
     * @return Boolean integer-reply specifically:
     * 
     *         <code>1</code> if the timeout was set. <code>0</code> if <code>key</code> does not exist or the timeout could not
     *         be set (see: <code>EXPIRE</code>).
     */
    Boolean pexpireat(K key, long timestamp);

    /**
     * Get the time to live for a key in milliseconds.
     * 
     * @param key the key
     * @return Long integer-reply TTL in milliseconds, or a negative value in order to signal an error (see the description
     *         above).
     */
    Long pttl(K key);

    /**
     * Return a random key from the keyspace.
     * 
     * @return V bulk-string-reply the random key, or <code>nil</code> when the database is empty.
     */
    V randomkey();

    /**
     * Rename a key.
     * 
     * @param key the key
     * @param newKey the newkey type: key
     * @return String simple-string-reply
     */
    String rename(K key, K newKey);

    /**
     * Rename a key, only if the new key does not exist.
     * 
     * @param key the key
     * @param newKey the newkey type: key
     * @return Boolean integer-reply specifically:
     * 
     *         <code>1</code> if <code>key</code> was renamed to <code>newkey</code>. <code>0</code> if <code>newkey</code>
     *         already exists.
     */
    Boolean renamenx(K key, K newKey);

    /**
     * Create a key using the provided serialized value, previously obtained using DUMP.
     * 
     * @param key the key
     * @param ttl the ttl type: long
     * @param value the serialized-value type: string
     * @return String simple-string-reply The command returns OK on success.
     */
    String restore(K key, long ttl, byte[] value);

    /**
     * Sort the elements in a list, set or sorted set.
     * 
     * @return List&lt;V&gt; array-reply list of sorted elements.
     */
    List<V> sort(K key);

    /**
     * Sort the elements in a list, set or sorted set.
     * 
     * @return Long array-reply list of sorted elements.
     */
    Long sort(ValueStreamingChannel<V> channel, K key);

    /**
     * Sort the elements in a list, set or sorted set.
     * 
     * @return List&lt;V&gt; array-reply list of sorted elements.
     */
    List<V> sort(K key, SortArgs sortArgs);

    /**
     * Sort the elements in a list, set or sorted set.
     * 
     * @return Long array-reply list of sorted elements.
     */
    Long sort(ValueStreamingChannel<V> channel, K key, SortArgs sortArgs);

    Long sortStore(K key, SortArgs sortArgs, K destination);

    /**
     * Get the time to live for a key.
     * 
     * @param key the key
     * @return Long integer-reply TTL in seconds, or a negative value in order to signal an error (see the description above).
     */
    Long ttl(K key);

    /**
     * Determine the type stored at key.
     * 
     * @param key the key
     * @return String simple-string-reply type of <code>key</code>, or <code>none</code> when <code>key</code> does not exist.
     */
    String type(K key);

    /**
     * Incrementally iterate the keys space.
     */
    KeyScanCursor<K> scan();

    /**
     * Incrementally iterate the keys space.
     */
    KeyScanCursor<K> scan(ScanArgs scanArgs);

    /**
     * Incrementally iterate the keys space.
     */
    KeyScanCursor<K> scan(ScanCursor scanCursor, ScanArgs scanArgs);

    /**
     * Incrementally iterate the keys space.
     */
    KeyScanCursor<K> scan(ScanCursor scanCursor);

    /**
     * Incrementally iterate the keys space.
     */
    StreamScanCursor scan(KeyStreamingChannel<K> channel);

    /**
     * Incrementally iterate the keys space.
     */
    StreamScanCursor scan(KeyStreamingChannel<K> channel, ScanArgs scanArgs);

    /**
     * Incrementally iterate the keys space.
     * 
     * @param channel
     * @param scanCursor the cursor type: long
     * @param scanArgs
     */
    StreamScanCursor scan(KeyStreamingChannel<K> channel, ScanCursor scanCursor, ScanArgs scanArgs);

    /**
     * Incrementally iterate the keys space.
     */
    StreamScanCursor scan(KeyStreamingChannel<K> channel, ScanCursor scanCursor);
}
