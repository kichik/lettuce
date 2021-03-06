// Copyright (C) 2011 - Will Glozer.  All rights reserved.

package com.lambdaworks.redis;

import static com.lambdaworks.redis.protocol.CommandType.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.lambdaworks.codec.Base16;
import com.lambdaworks.redis.codec.RedisCodec;
import com.lambdaworks.redis.output.*;
import com.lambdaworks.redis.protocol.*;
import io.netty.channel.ChannelHandler;

/**
 * An asynchronous thread-safe connection to a redis server. Multiple threads may share one {@link RedisAsyncConnectionImpl}
 * provided they avoid blocking and transactional operations such as {@link #blpop} and {@link #multi()}/{@link #exec}.
 * 
 * A {@link ConnectionWatchdog} monitors each connection and reconnects automatically until {@link #close} is called. All
 * pending commands will be (re)sent after successful reconnection.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author Will Glozer
 */
@ChannelHandler.Sharable
public class RedisAsyncConnectionImpl<K, V> extends RedisChannelHandler<K, V> implements RedisAsyncConnection<K, V>,
        RedisClusterAsyncConnection<K, V> {

    protected MultiOutput<K, V> multi;
    private char[] password;
    private int db;
    protected RedisCommandBuilder<K, V> commandBuilder;
    protected RedisCodec<K, V> codec;

    /**
     * Initialize a new connection.
     * 
     * @param writer
     * @param codec Codec used to encode/decode keys and values.
     * @param timeout Maximum time to wait for a response.
     * @param unit Unit of time for the timeout.
     */
    public RedisAsyncConnectionImpl(RedisChannelWriter<K, V> writer, RedisCodec<K, V> codec, long timeout, TimeUnit unit) {
        super(writer, timeout, unit);
        this.codec = codec;
        commandBuilder = new RedisCommandBuilder<K, V>(codec);

    }

    @Override
    public RedisFuture<Long> append(K key, V value) {
        return dispatch(commandBuilder.append(key, value));
    }

    @Override
    public String auth(String password) {
        RedisCommand<K, V, String> cmd = dispatch(commandBuilder.auth(password));
        String status = LettuceFutures.await(cmd, timeout, unit);
        if ("OK".equals(status)) {
            this.password = password.toCharArray();
        }
        return status;
    }

    @Override
    public RedisFuture<String> bgrewriteaof() {
        return dispatch(commandBuilder.bgrewriteaof());
    }

    @Override
    public RedisFuture<String> bgsave() {
        return dispatch(commandBuilder.bgsave());
    }

    @Override
    public RedisFuture<Long> bitcount(K key) {
        return dispatch(commandBuilder.bitcount(key));
    }

    @Override
    public RedisFuture<Long> bitcount(K key, long start, long end) {
        return dispatch(commandBuilder.bitcount(key, start, end));
    }

    @Override
    public RedisFuture<Long> bitpos(K key, boolean state) {
        return dispatch(commandBuilder.bitpos(key, state));
    }

    @Override
    public RedisFuture<Long> bitpos(K key, boolean state, long start, long end) {
        return dispatch(commandBuilder.bitpos(key, state, start, end));
    }

    @Override
    public RedisFuture<Long> bitopAnd(K destination, K... keys) {
        return dispatch(commandBuilder.bitopAnd(destination, keys));
    }

    @Override
    public RedisFuture<Long> bitopNot(K destination, K source) {
        return dispatch(commandBuilder.bitopNot(destination, source));
    }

    @Override
    public RedisFuture<Long> bitopOr(K destination, K... keys) {
        return dispatch(commandBuilder.bitopOr(destination, keys));
    }

    @Override
    public RedisFuture<Long> bitopXor(K destination, K... keys) {
        return dispatch(commandBuilder.bitopXor(destination, keys));
    }

    @Override
    public RedisFuture<KeyValue<K, V>> blpop(long timeout, K... keys) {
        return dispatch(commandBuilder.blpop(timeout, keys));
    }

    @Override
    public RedisFuture<KeyValue<K, V>> brpop(long timeout, K... keys) {
        return dispatch(commandBuilder.brpop(timeout, keys));
    }

    @Override
    public RedisFuture<V> brpoplpush(long timeout, K source, K destination) {
        return dispatch(commandBuilder.brpoplpush(timeout, source, destination));
    }

    @Override
    public RedisFuture<K> clientGetname() {
        return dispatch(commandBuilder.clientGetname());
    }

    @Override
    public RedisFuture<String> clientSetname(K name) {
        return dispatch(commandBuilder.clientSetname(name));
    }

    @Override
    public RedisFuture<String> clientKill(String addr) {
        return dispatch(commandBuilder.clientKill(addr));
    }

    @Override
    public RedisFuture<Long> clientKill(KillArgs killArgs) {
        return dispatch(commandBuilder.clientKill(killArgs));
    }

    @Override
    public RedisFuture<String> clientPause(long timeout) {
        return dispatch(commandBuilder.clientPause(timeout));
    }

    @Override
    public RedisFuture<String> clientList() {
        return dispatch(commandBuilder.clientList());
    }

    @Override
    public RedisFuture<List<Object>> command() {
        return dispatch(commandBuilder.command());
    }

    @Override
    public RedisFuture<List<Object>> commandInfo(String... commands) {
        return dispatch(commandBuilder.commandInfo(commands));
    }

    @Override
    public RedisFuture<List<Object>> commandInfo(CommandType... commands) {
        String[] stringCommands = new String[commands.length];
        for (int i = 0; i < commands.length; i++) {
            stringCommands[i] = commands[i].name();
        }

        return commandInfo(stringCommands);
    }

    @Override
    public RedisFuture<Long> commandCount() {
        return dispatch(commandBuilder.commandCount());
    }

    @Override
    public RedisFuture<List<String>> configGet(String parameter) {
        return dispatch(commandBuilder.configGet(parameter));
    }

    @Override
    public RedisFuture<String> configResetstat() {
        return dispatch(commandBuilder.configResetstat());
    }

    @Override
    public RedisFuture<String> configSet(String parameter, String value) {
        return dispatch(commandBuilder.configSet(parameter, value));
    }

    @Override
    public RedisFuture<String> configRewrite() {
        return dispatch(commandBuilder.configRewrite());
    }

    @Override
    public RedisFuture<Long> dbsize() {
        return dispatch(commandBuilder.dbsize());
    }

    @Override
    public RedisFuture<String> debugObject(K key) {
        return dispatch(commandBuilder.debugObject(key));
    }

    @Override
    public void debugSegfault() {
        dispatch(commandBuilder.debugSegfault());
    }

    @Override
    public void debugOom() {
        dispatch(commandBuilder.debugOom());
    }

    @Override
    public RedisFuture<Long> decr(K key) {
        return dispatch(commandBuilder.decr(key));
    }

    @Override
    public RedisFuture<Long> decrby(K key, long amount) {
        return dispatch(commandBuilder.decrby(key, amount));
    }

    @Override
    public RedisFuture<Long> del(K... keys) {
        return dispatch(commandBuilder.del(keys));
    }

    @Override
    public RedisFuture<String> discard() {
        if (multi != null) {
            multi.cancel();
            multi = null;
        }
        return dispatch(commandBuilder.discard());
    }

    @Override
    public RedisFuture<byte[]> dump(K key) {
        return dispatch(commandBuilder.dump(key));
    }

    @Override
    public RedisFuture<V> echo(V msg) {
        return dispatch(commandBuilder.echo(msg));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RedisFuture<T> eval(String script, ScriptOutputType type, K... keys) {
        return (RedisFuture<T>) dispatch(commandBuilder.eval(script, type, keys));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RedisFuture<T> eval(String script, ScriptOutputType type, K[] keys, V... values) {
        return (RedisFuture<T>) dispatch(commandBuilder.eval(script, type, keys, values));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RedisFuture<T> evalsha(String digest, ScriptOutputType type, K... keys) {
        return (RedisFuture<T>) dispatch(commandBuilder.evalsha(digest, type, keys));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RedisFuture<T> evalsha(String digest, ScriptOutputType type, K[] keys, V... values) {
        return (RedisFuture<T>) dispatch(commandBuilder.evalsha(digest, type, keys, values));
    }

    @Override
    public RedisFuture<Boolean> exists(K key) {
        return dispatch(commandBuilder.exists(key));
    }

    @Override
    public RedisFuture<Boolean> expire(K key, long seconds) {
        return dispatch(commandBuilder.expire(key, seconds));
    }

    @Override
    public RedisFuture<Boolean> expireat(K key, Date timestamp) {

        return expireat(key, timestamp.getTime() / 1000);
    }

    @Override
    public RedisFuture<Boolean> expireat(K key, long timestamp) {
        return dispatch(commandBuilder.expireat(key, timestamp));
    }

    @Override
    public RedisFuture<List<Object>> exec() {
        MultiOutput<K, V> multi = this.multi;
        this.multi = null;
        if (multi == null) {
            multi = new MultiOutput<K, V>(codec);
        }
        return dispatch(EXEC, multi);
    }

    @Override
    public RedisFuture<String> flushall() {
        return dispatch(commandBuilder.flushall());
    }

    @Override
    public RedisFuture<String> flushdb() {
        return dispatch(commandBuilder.flushdb());
    }

    @Override
    public RedisFuture<V> get(K key) {
        return dispatch(commandBuilder.get(key));
    }

    @Override
    public RedisFuture<Long> getbit(K key, long offset) {
        return dispatch(commandBuilder.getbit(key, offset));
    }

    @Override
    public RedisFuture<V> getrange(K key, long start, long end) {
        return dispatch(commandBuilder.getrange(key, start, end));
    }

    @Override
    public RedisFuture<V> getset(K key, V value) {
        return dispatch(commandBuilder.getset(key, value));
    }

    @Override
    public RedisFuture<Long> hdel(K key, K... fields) {
        return dispatch(commandBuilder.hdel(key, fields));
    }

    @Override
    public RedisFuture<Boolean> hexists(K key, K field) {
        return dispatch(commandBuilder.hexists(key, field));
    }

    @Override
    public RedisFuture<V> hget(K key, K field) {
        return dispatch(commandBuilder.hget(key, field));
    }

    @Override
    public RedisFuture<Long> hincrby(K key, K field, long amount) {
        return dispatch(commandBuilder.hincrby(key, field, amount));
    }

    @Override
    public RedisFuture<Double> hincrbyfloat(K key, K field, double amount) {
        return dispatch(commandBuilder.hincrbyfloat(key, field, amount));
    }

    @Override
    public RedisFuture<Map<K, V>> hgetall(K key) {
        return dispatch(commandBuilder.hgetall(key));
    }

    @Override
    public RedisFuture<Long> hgetall(KeyValueStreamingChannel<K, V> channel, K key) {
        return dispatch(commandBuilder.hgetall(channel, key));
    }

    @Override
    public RedisFuture<List<K>> hkeys(K key) {
        return dispatch(commandBuilder.hkeys(key));
    }

    @Override
    public RedisFuture<Long> hkeys(KeyStreamingChannel<K> channel, K key) {
        return dispatch(commandBuilder.hkeys(channel, key));
    }

    @Override
    public RedisFuture<Long> hlen(K key) {
        return dispatch(commandBuilder.hlen(key));
    }

    @Override
    public RedisFuture<List<V>> hmget(K key, K... fields) {
        return dispatch(commandBuilder.hmget(key, fields));
    }

    @Override
    public RedisFuture<Long> hmget(ValueStreamingChannel<V> channel, K key, K... fields) {
        return dispatch(commandBuilder.hmget(channel, key, fields));
    }

    @Override
    public RedisFuture<String> hmset(K key, Map<K, V> map) {
        return dispatch(commandBuilder.hmset(key, map));
    }

    @Override
    public RedisFuture<Boolean> hset(K key, K field, V value) {
        return dispatch(commandBuilder.hset(key, field, value));
    }

    @Override
    public RedisFuture<Boolean> hsetnx(K key, K field, V value) {
        return dispatch(commandBuilder.hsetnx(key, field, value));
    }

    @Override
    public RedisFuture<List<V>> hvals(K key) {
        return dispatch(commandBuilder.hvals(key));
    }

    @Override
    public RedisFuture<Long> hvals(ValueStreamingChannel<V> channel, K key) {
        return dispatch(commandBuilder.hvals(channel, key));
    }

    @Override
    public RedisFuture<Long> incr(K key) {
        return dispatch(commandBuilder.incr(key));
    }

    @Override
    public RedisFuture<Long> incrby(K key, long amount) {
        return dispatch(commandBuilder.incrby(key, amount));
    }

    @Override
    public RedisFuture<Double> incrbyfloat(K key, double amount) {
        return dispatch(commandBuilder.incrbyfloat(key, amount));
    }

    @Override
    public RedisFuture<String> info() {
        return dispatch(commandBuilder.info());
    }

    @Override
    public RedisFuture<String> info(String section) {
        return dispatch(commandBuilder.info(section));
    }

    @Override
    public RedisFuture<List<K>> keys(K pattern) {
        return dispatch(commandBuilder.keys(pattern));
    }

    @Override
    public RedisFuture<Long> keys(KeyStreamingChannel<K> channel, K pattern) {
        return dispatch(commandBuilder.keys(channel, pattern));
    }

    @Override
    public RedisFuture<Date> lastsave() {
        return dispatch(commandBuilder.lastsave());
    }

    @Override
    public RedisFuture<V> lindex(K key, long index) {
        return dispatch(commandBuilder.lindex(key, index));
    }

    @Override
    public RedisFuture<Long> linsert(K key, boolean before, V pivot, V value) {
        return dispatch(commandBuilder.linsert(key, before, pivot, value));
    }

    @Override
    public RedisFuture<Long> llen(K key) {
        return dispatch(commandBuilder.llen(key));
    }

    @Override
    public RedisFuture<V> lpop(K key) {
        return dispatch(commandBuilder.lpop(key));
    }

    @Override
    public RedisFuture<Long> lpush(K key, V... values) {
        return dispatch(commandBuilder.lpush(key, values));
    }

    @Override
    public RedisFuture<Long> lpushx(K key, V value) {
        return dispatch(commandBuilder.lpushx(key, value));
    }

    @Override
    public RedisFuture<List<V>> lrange(K key, long start, long stop) {
        return dispatch(commandBuilder.lrange(key, start, stop));
    }

    @Override
    public RedisFuture<Long> lrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        return dispatch(commandBuilder.lrange(channel, key, start, stop));
    }

    @Override
    public RedisFuture<Long> lrem(K key, long count, V value) {
        return dispatch(commandBuilder.lrem(key, count, value));
    }

    @Override
    public RedisFuture<String> lset(K key, long index, V value) {
        return dispatch(commandBuilder.lset(key, index, value));
    }

    @Override
    public RedisFuture<String> ltrim(K key, long start, long stop) {
        return dispatch(commandBuilder.ltrim(key, start, stop));
    }

    @Override
    public RedisFuture<String> migrate(String host, int port, K key, int db, long timeout) {
        return dispatch(commandBuilder.migrate(host, port, key, db, timeout));
    }

    @Override
    public RedisFuture<List<V>> mget(K... keys) {
        return dispatch(commandBuilder.mget(keys));
    }

    @Override
    public RedisFuture<Long> mget(ValueStreamingChannel<V> channel, K... keys) {
        return dispatch(commandBuilder.mget(channel, keys));
    }

    @Override
    public RedisFuture<Boolean> move(K key, int db) {
        return dispatch(commandBuilder.move(key, db));
    }

    @Override
    public RedisFuture<String> multi() {

        RedisCommand<K, V, String> cmd = dispatch(commandBuilder.multi());
        multi = (multi == null ? new MultiOutput<K, V>(codec) : multi);
        return cmd;
    }

    @Override
    public RedisFuture<String> mset(Map<K, V> map) {
        return dispatch(commandBuilder.mset(map));
    }

    @Override
    public RedisFuture<Boolean> msetnx(Map<K, V> map) {
        return dispatch(commandBuilder.msetnx(map));
    }

    @Override
    public RedisFuture<String> objectEncoding(K key) {
        return dispatch(commandBuilder.objectEncoding(key));
    }

    @Override
    public RedisFuture<Long> objectIdletime(K key) {
        return dispatch(commandBuilder.objectIdletime(key));
    }

    @Override
    public RedisFuture<Long> objectRefcount(K key) {
        return dispatch(commandBuilder.objectRefcount(key));
    }

    @Override
    public RedisFuture<Boolean> persist(K key) {
        return dispatch(commandBuilder.persist(key));
    }

    @Override
    public RedisFuture<Boolean> pexpire(K key, long milliseconds) {
        return dispatch(commandBuilder.pexpire(key, milliseconds));
    }

    @Override
    public RedisFuture<Boolean> pexpireat(K key, Date timestamp) {
        return pexpireat(key, timestamp.getTime());
    }

    @Override
    public RedisFuture<Boolean> pexpireat(K key, long timestamp) {
        return dispatch(commandBuilder.pexpireat(key, timestamp));
    }

    @Override
    public RedisFuture<String> ping() {
        return dispatch(commandBuilder.ping());
    }

    @Override
    public RedisFuture<Long> pttl(K key) {
        return dispatch(commandBuilder.pttl(key));
    }

    @Override
    public RedisFuture<Long> publish(K channel, V message) {
        return dispatch(commandBuilder.publish(channel, message));
    }

    @Override
    public RedisFuture<List<K>> pubsubChannels() {
        return dispatch(commandBuilder.pubsubChannels());
    }

    @Override
    public RedisFuture<List<K>> pubsubChannels(K channel) {
        return dispatch(commandBuilder.pubsubChannels(channel));
    }

    @Override
    public RedisFuture<Map<K, Long>> pubsubNumsub(K... channels) {
        return dispatch(commandBuilder.pubsubNumsub(channels));
    }

    @Override
    public RedisFuture<Long> pubsubNumpat() {
        return dispatch(commandBuilder.pubsubNumpat());
    }

    @Override
    public RedisFuture<String> quit() {
        return dispatch(commandBuilder.quit());
    }

    @Override
    public RedisFuture<List<Object>> role() {
        return dispatch(commandBuilder.role());
    }

    @Override
    public RedisFuture<V> randomkey() {
        return dispatch(commandBuilder.randomkey());
    }

    @Override
    public RedisFuture<String> rename(K key, K newKey) {
        return dispatch(commandBuilder.rename(key, newKey));
    }

    @Override
    public RedisFuture<Boolean> renamenx(K key, K newKey) {
        return dispatch(commandBuilder.renamenx(key, newKey));
    }

    @Override
    public RedisFuture<String> restore(K key, long ttl, byte[] value) {
        return dispatch(commandBuilder.restore(key, ttl, value));
    }

    @Override
    public RedisFuture<V> rpop(K key) {
        return dispatch(commandBuilder.rpop(key));
    }

    @Override
    public RedisFuture<V> rpoplpush(K source, K destination) {
        return dispatch(commandBuilder.rpoplpush(source, destination));
    }

    @Override
    public RedisFuture<Long> rpush(K key, V... values) {
        return dispatch(commandBuilder.rpush(key, values));
    }

    @Override
    public RedisFuture<Long> rpushx(K key, V value) {
        return dispatch(commandBuilder.rpushx(key, value));
    }

    @Override
    public RedisFuture<Long> sadd(K key, V... members) {
        return dispatch(commandBuilder.sadd(key, members));
    }

    @Override
    public RedisFuture<String> save() {
        return dispatch(commandBuilder.save());
    }

    @Override
    public RedisFuture<Long> scard(K key) {
        return dispatch(commandBuilder.scard(key));
    }

    @Override
    public RedisFuture<List<Boolean>> scriptExists(String... digests) {
        return dispatch(commandBuilder.scriptExists(digests));
    }

    @Override
    public RedisFuture<String> scriptFlush() {
        return dispatch(commandBuilder.scriptFlush());
    }

    @Override
    public RedisFuture<String> scriptKill() {
        return dispatch(commandBuilder.scriptKill());
    }

    @Override
    public RedisFuture<String> scriptLoad(V script) {
        return dispatch(commandBuilder.scriptLoad(script));
    }

    @Override
    public RedisFuture<Set<V>> sdiff(K... keys) {
        return dispatch(commandBuilder.sdiff(keys));
    }

    @Override
    public RedisFuture<Long> sdiff(ValueStreamingChannel<V> channel, K... keys) {
        return dispatch(commandBuilder.sdiff(channel, keys));
    }

    @Override
    public RedisFuture<Long> sdiffstore(K destination, K... keys) {
        return dispatch(commandBuilder.sdiffstore(destination, keys));
    }

    @Override
    public String select(int db) {
        RedisCommand<K, V, String> cmd = dispatch(commandBuilder.select(db));
        String status = LettuceFutures.await(cmd, timeout, unit);
        if ("OK".equals(status)) {
            this.db = db;
        }
        return status;
    }

    @Override
    public RedisFuture<String> set(K key, V value) {
        return dispatch(commandBuilder.set(key, value));
    }

    @Override
    public RedisFuture<V> set(K key, V value, SetArgs setArgs) {
        return dispatch(commandBuilder.set(key, value, setArgs));
    }

    @Override
    public RedisFuture<Long> setbit(K key, long offset, int value) {
        return dispatch(commandBuilder.setbit(key, offset, value));
    }

    @Override
    public RedisFuture<String> setex(K key, long seconds, V value) {
        return dispatch(commandBuilder.setex(key, seconds, value));
    }

    @Override
    public RedisFuture<String> psetex(K key, long milliseconds, V value) {
        return dispatch(commandBuilder.psetex(key, milliseconds, value));
    }

    @Override
    public RedisFuture<Boolean> setnx(K key, V value) {
        return dispatch(commandBuilder.setnx(key, value));
    }

    @Override
    public RedisFuture<Long> setrange(K key, long offset, V value) {
        return dispatch(commandBuilder.setrange(key, offset, value));
    }

    @Deprecated
    public void shutdown() {
        dispatch(commandBuilder.shutdown());
    }

    @Override
    public void shutdown(boolean save) {
        dispatch(commandBuilder.shutdown(save));
    }

    @Override
    public RedisFuture<Set<V>> sinter(K... keys) {
        return dispatch(commandBuilder.sinter(keys));
    }

    @Override
    public RedisFuture<Long> sinter(ValueStreamingChannel<V> channel, K... keys) {
        return dispatch(commandBuilder.sinter(channel, keys));
    }

    @Override
    public RedisFuture<Long> sinterstore(K destination, K... keys) {
        return dispatch(commandBuilder.sinterstore(destination, keys));
    }

    @Override
    public RedisFuture<Boolean> sismember(K key, V member) {
        return dispatch(commandBuilder.sismember(key, member));
    }

    @Override
    public RedisFuture<Boolean> smove(K source, K destination, V member) {
        return dispatch(commandBuilder.smove(source, destination, member));
    }

    @Override
    public RedisFuture<String> slaveof(String host, int port) {
        return dispatch(commandBuilder.slaveof(host, port));
    }

    @Override
    public RedisFuture<String> slaveofNoOne() {
        return dispatch(commandBuilder.slaveofNoOne());
    }

    @Override
    public RedisFuture<List<Object>> slowlogGet() {
        return dispatch(commandBuilder.slowlogGet());
    }

    @Override
    public RedisFuture<List<Object>> slowlogGet(int count) {
        return dispatch(commandBuilder.slowlogGet(count));
    }

    @Override
    public RedisFuture<Long> slowlogLen() {
        return dispatch(commandBuilder.slowlogLen());
    }

    @Override
    public RedisFuture<String> slowlogReset() {
        return dispatch(commandBuilder.slowlogReset());
    }

    @Override
    public RedisFuture<Set<V>> smembers(K key) {
        return dispatch(commandBuilder.smembers(key));
    }

    @Override
    public RedisFuture<Long> smembers(ValueStreamingChannel<V> channel, K key) {
        return dispatch(commandBuilder.smembers(channel, key));
    }

    @Override
    public RedisFuture<List<V>> sort(K key) {
        return dispatch(commandBuilder.sort(key));
    }

    @Override
    public RedisFuture<Long> sort(ValueStreamingChannel<V> channel, K key) {
        return dispatch(commandBuilder.sort(channel, key));
    }

    @Override
    public RedisFuture<List<V>> sort(K key, SortArgs sortArgs) {
        return dispatch(commandBuilder.sort(key, sortArgs));
    }

    @Override
    public RedisFuture<Long> sort(ValueStreamingChannel<V> channel, K key, SortArgs sortArgs) {
        return dispatch(commandBuilder.sort(channel, key, sortArgs));
    }

    @Override
    public RedisFuture<Long> sortStore(K key, SortArgs sortArgs, K destination) {
        return dispatch(commandBuilder.sortStore(key, sortArgs, destination));
    }

    @Override
    public RedisFuture<V> spop(K key) {
        return dispatch(commandBuilder.spop(key));
    }

    @Override
    public RedisFuture<V> srandmember(K key) {
        return dispatch(commandBuilder.srandmember(key));
    }

    @Override
    public RedisFuture<Set<V>> srandmember(K key, long count) {
        return dispatch(commandBuilder.srandmember(key, count));
    }

    @Override
    public RedisFuture<Long> srandmember(ValueStreamingChannel<V> channel, K key, long count) {
        return dispatch(commandBuilder.srandmember(channel, key, count));
    }

    @Override
    public RedisFuture<Long> srem(K key, V... members) {
        return dispatch(commandBuilder.srem(key, members));
    }

    @Override
    public RedisFuture<Set<V>> sunion(K... keys) {
        return dispatch(commandBuilder.sunion(keys));
    }

    @Override
    public RedisFuture<Long> sunion(ValueStreamingChannel<V> channel, K... keys) {
        return dispatch(commandBuilder.sunion(channel, keys));
    }

    @Override
    public RedisFuture<Long> sunionstore(K destination, K... keys) {
        return dispatch(commandBuilder.sunionstore(destination, keys));
    }

    @Override
    public RedisFuture<String> sync() {
        return dispatch(commandBuilder.sync());
    }

    @Override
    public RedisFuture<Long> strlen(K key) {
        return dispatch(commandBuilder.strlen(key));
    }

    @Override
    public RedisFuture<Long> ttl(K key) {
        return dispatch(commandBuilder.ttl(key));
    }

    @Override
    public RedisFuture<String> type(K key) {
        return dispatch(commandBuilder.type(key));
    }

    @Override
    public RedisFuture<String> watch(K... keys) {
        return dispatch(commandBuilder.watch(keys));
    }

    @Override
    public RedisFuture<String> unwatch() {
        return dispatch(commandBuilder.unwatch());
    }

    @Override
    public RedisFuture<Long> zadd(K key, double score, V member) {
        return dispatch(commandBuilder.zadd(key, score, member));
    }

    @Override
    public RedisFuture<Long> zadd(K key, Object... scoresAndValues) {
        return dispatch(commandBuilder.zadd(key, scoresAndValues));
    }

    @Override
    public RedisFuture<Long> zcard(K key) {
        return dispatch(commandBuilder.zcard(key));
    }

    @Override
    public RedisFuture<Long> zcount(K key, double min, double max) {
        return dispatch(commandBuilder.zcount(key, min, max));
    }

    @Override
    public RedisFuture<Long> zcount(K key, String min, String max) {
        return dispatch(commandBuilder.zcount(key, min, max));
    }

    @Override
    public RedisFuture<Double> zincrby(K key, double amount, K member) {
        return dispatch(commandBuilder.zincrby(key, amount, member));
    }

    @Override
    public RedisFuture<Long> zinterstore(K destination, K... keys) {
        return dispatch(commandBuilder.zinterstore(destination, keys));
    }

    @Override
    public RedisFuture<Long> zinterstore(K destination, ZStoreArgs storeArgs, K... keys) {
        return dispatch(commandBuilder.zinterstore(destination, storeArgs, keys));
    }

    @Override
    public RedisFuture<List<V>> zrange(K key, long start, long stop) {
        return dispatch(commandBuilder.zrange(key, start, stop));
    }

    @Override
    public RedisFuture<List<ScoredValue<V>>> zrangeWithScores(K key, long start, long stop) {
        return dispatch(commandBuilder.zrangeWithScores(key, start, stop));
    }

    @Override
    public RedisFuture<List<V>> zrangebyscore(K key, double min, double max) {
        return dispatch(commandBuilder.zrangebyscore(key, min, max));
    }

    @Override
    public RedisFuture<List<V>> zrangebyscore(K key, String min, String max) {
        return dispatch(commandBuilder.zrangebyscore(key, min, max));
    }

    @Override
    public RedisFuture<List<V>> zrangebyscore(K key, double min, double max, long offset, long count) {
        return dispatch(commandBuilder.zrangebyscore(key, min, max, offset, count));
    }

    @Override
    public RedisFuture<List<V>> zrangebyscore(K key, String min, String max, long offset, long count) {
        return dispatch(commandBuilder.zrangebyscore(key, min, max, offset, count));
    }

    @Override
    public RedisFuture<List<ScoredValue<V>>> zrangebyscoreWithScores(K key, double min, double max) {
        return dispatch(commandBuilder.zrangebyscoreWithScores(key, min, max));
    }

    @Override
    public RedisFuture<List<ScoredValue<V>>> zrangebyscoreWithScores(K key, String min, String max) {
        return dispatch(commandBuilder.zrangebyscoreWithScores(key, min, max));
    }

    @Override
    public RedisFuture<List<ScoredValue<V>>> zrangebyscoreWithScores(K key, double min, double max, long offset, long count) {
        return dispatch(commandBuilder.zrangebyscoreWithScores(key, min, max, offset, count));
    }

    @Override
    public RedisFuture<List<ScoredValue<V>>> zrangebyscoreWithScores(K key, String min, String max, long offset, long count) {
        return dispatch(commandBuilder.zrangebyscoreWithScores(key, min, max, offset, count));
    }

    //

    @Override
    public RedisFuture<Long> zrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        return dispatch(commandBuilder.zrange(channel, key, start, stop));
    }

    @Override
    public RedisFuture<Long> zrangeWithScores(ScoredValueStreamingChannel<V> channel, K key, long start, long stop) {
        return dispatch(commandBuilder.zrangeWithScores(channel, key, start, stop));
    }

    @Override
    public RedisFuture<Long> zrangebyscore(ValueStreamingChannel<V> channel, K key, double min, double max) {
        return dispatch(commandBuilder.zrangebyscore(channel, key, min, max));
    }

    @Override
    public RedisFuture<Long> zrangebyscore(ValueStreamingChannel<V> channel, K key, String min, String max) {
        return dispatch(commandBuilder.zrangebyscore(channel, key, min, max));
    }

    @Override
    public RedisFuture<Long> zrangebyscore(ValueStreamingChannel<V> channel, K key, double min, double max, long offset,
            long count) {
        return dispatch(commandBuilder.zrangebyscore(channel, key, min, max, offset, count));
    }

    @Override
    public RedisFuture<Long> zrangebyscore(ValueStreamingChannel<V> channel, K key, String min, String max, long offset,
            long count) {
        return dispatch(commandBuilder.zrangebyscore(channel, key, min, max, offset, count));
    }

    @Override
    public RedisFuture<Long> zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, double min, double max) {
        return dispatch(commandBuilder.zrangebyscoreWithScores(channel, key, min, max));
    }

    @Override
    public RedisFuture<Long> zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, String min, String max) {
        return dispatch(commandBuilder.zrangebyscoreWithScores(channel, key, min, max));
    }

    @Override
    public RedisFuture<Long> zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, double min, double max,
            long offset, long count) {
        return dispatch(commandBuilder.zrangebyscoreWithScores(channel, key, min, max, offset, count));
    }

    @Override
    public RedisFuture<Long> zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, String min, String max,
            long offset, long count) {
        return dispatch(commandBuilder.zrangebyscoreWithScores(channel, key, min, max, offset, count));
    }

    @Override
    public RedisFuture<Long> zrank(K key, V member) {
        return dispatch(commandBuilder.zrank(key, member));
    }

    @Override
    public RedisFuture<Long> zrem(K key, V... members) {
        return dispatch(commandBuilder.zrem(key, members));
    }

    @Override
    public RedisFuture<Long> zremrangebyrank(K key, long start, long stop) {
        return dispatch(commandBuilder.zremrangebyrank(key, start, stop));
    }

    @Override
    public RedisFuture<Long> zremrangebyscore(K key, double min, double max) {
        return dispatch(commandBuilder.zremrangebyscore(key, min, max));
    }

    @Override
    public RedisFuture<Long> zremrangebyscore(K key, String min, String max) {
        return dispatch(commandBuilder.zremrangebyscore(key, min, max));
    }

    @Override
    public RedisFuture<List<V>> zrevrange(K key, long start, long stop) {
        return dispatch(commandBuilder.zrevrange(key, start, stop));
    }

    @Override
    public RedisFuture<List<ScoredValue<V>>> zrevrangeWithScores(K key, long start, long stop) {
        return dispatch(commandBuilder.zrevrangeWithScores(key, start, stop));
    }

    @Override
    public RedisFuture<List<V>> zrevrangebyscore(K key, double max, double min) {
        return dispatch(commandBuilder.zrevrangebyscore(key, max, min));
    }

    @Override
    public RedisFuture<List<V>> zrevrangebyscore(K key, String max, String min) {
        return dispatch(commandBuilder.zrevrangebyscore(key, max, min));
    }

    @Override
    public RedisFuture<List<V>> zrevrangebyscore(K key, double max, double min, long offset, long count) {
        return dispatch(commandBuilder.zrevrangebyscore(key, max, min, offset, count));
    }

    @Override
    public RedisFuture<List<V>> zrevrangebyscore(K key, String max, String min, long offset, long count) {
        return dispatch(commandBuilder.zrevrangebyscore(key, max, min, offset, count));
    }

    @Override
    public RedisFuture<List<ScoredValue<V>>> zrevrangebyscoreWithScores(K key, double max, double min) {
        return dispatch(commandBuilder.zrevrangebyscoreWithScores(key, max, min));
    }

    @Override
    public RedisFuture<List<ScoredValue<V>>> zrevrangebyscoreWithScores(K key, String max, String min) {
        return dispatch(commandBuilder.zrevrangebyscoreWithScores(key, max, min));
    }

    @Override
    public RedisFuture<List<ScoredValue<V>>> zrevrangebyscoreWithScores(K key, double max, double min, long offset, long count) {
        return dispatch(commandBuilder.zrevrangebyscoreWithScores(key, max, min, offset, count));
    }

    @Override
    public RedisFuture<List<ScoredValue<V>>> zrevrangebyscoreWithScores(K key, String max, String min, long offset, long count) {
        return dispatch(commandBuilder.zrevrangebyscoreWithScores(key, max, min, offset, count));
    }

    @Override
    public RedisFuture<Long> zrevrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        return dispatch(commandBuilder.zrevrange(channel, key, start, stop));
    }

    @Override
    public RedisFuture<Long> zrevrangeWithScores(ScoredValueStreamingChannel<V> channel, K key, long start, long stop) {
        return dispatch(commandBuilder.zrevrangeWithScores(channel, key, start, stop));
    }

    @Override
    public RedisFuture<Long> zrevrangebyscore(ValueStreamingChannel<V> channel, K key, double max, double min) {
        return dispatch(commandBuilder.zrevrangebyscore(channel, key, max, min));
    }

    @Override
    public RedisFuture<Long> zrevrangebyscore(ValueStreamingChannel<V> channel, K key, String max, String min) {
        return dispatch(commandBuilder.zrevrangebyscore(channel, key, max, min));
    }

    @Override
    public RedisFuture<Long> zrevrangebyscore(ValueStreamingChannel<V> channel, K key, double max, double min, long offset,
            long count) {
        return dispatch(commandBuilder.zrevrangebyscore(channel, key, max, min, offset, count));
    }

    @Override
    public RedisFuture<Long> zrevrangebyscore(ValueStreamingChannel<V> channel, K key, String max, String min, long offset,
            long count) {
        return dispatch(commandBuilder.zrevrangebyscore(channel, key, max, min, offset, count));
    }

    @Override
    public RedisFuture<Long> zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, double max, double min) {
        return dispatch(commandBuilder.zrevrangebyscoreWithScores(channel, key, max, min));
    }

    @Override
    public RedisFuture<Long> zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, String max, String min) {
        return dispatch(commandBuilder.zrevrangebyscoreWithScores(channel, key, max, min));
    }

    @Override
    public RedisFuture<Long> zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, double max, double min,
            long offset, long count) {
        return dispatch(commandBuilder.zrevrangebyscoreWithScores(channel, key, max, min, offset, count));
    }

    @Override
    public RedisFuture<Long> zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, String max, String min,
            long offset, long count) {
        return dispatch(commandBuilder.zrevrangebyscoreWithScores(channel, key, max, min, offset, count));
    }

    @Override
    public RedisFuture<Long> zrevrank(K key, V member) {
        return dispatch(commandBuilder.zrevrank(key, member));
    }

    @Override
    public RedisFuture<Double> zscore(K key, V member) {
        return dispatch(commandBuilder.zscore(key, member));
    }

    @Override
    public RedisFuture<Long> zunionstore(K destination, K... keys) {
        return dispatch(commandBuilder.zunionstore(destination, keys));
    }

    @Override
    public RedisFuture<Long> zunionstore(K destination, ZStoreArgs storeArgs, K... keys) {
        return dispatch(commandBuilder.zunionstore(destination, storeArgs, keys));
    }

    @Override
    public RedisFuture<KeyScanCursor<K>> scan() {
        return dispatch(commandBuilder.scan());
    }

    @Override
    public RedisFuture<KeyScanCursor<K>> scan(ScanArgs scanArgs) {
        return dispatch(commandBuilder.scan(scanArgs));
    }

    @Override
    public RedisFuture<KeyScanCursor<K>> scan(ScanCursor scanCursor, ScanArgs scanArgs) {
        return dispatch(commandBuilder.scan(scanCursor, scanArgs));
    }

    @Override
    public RedisFuture<KeyScanCursor<K>> scan(ScanCursor scanCursor) {
        return dispatch(commandBuilder.scan(scanCursor));
    }

    @Override
    public RedisFuture<StreamScanCursor> scan(KeyStreamingChannel<K> channel) {
        return dispatch(commandBuilder.scanStreaming(channel));
    }

    @Override
    public RedisFuture<StreamScanCursor> scan(KeyStreamingChannel<K> channel, ScanArgs scanArgs) {
        return dispatch(commandBuilder.scanStreaming(channel, scanArgs));
    }

    @Override
    public RedisFuture<StreamScanCursor> scan(KeyStreamingChannel<K> channel, ScanCursor scanCursor, ScanArgs scanArgs) {
        return dispatch(commandBuilder.scanStreaming(channel, scanCursor, scanArgs));
    }

    @Override
    public RedisFuture<StreamScanCursor> scan(KeyStreamingChannel<K> channel, ScanCursor scanCursor) {
        return dispatch(commandBuilder.scanStreaming(channel, scanCursor));
    }

    @Override
    public RedisFuture<ValueScanCursor<V>> sscan(K key) {
        return dispatch(commandBuilder.sscan(key));
    }

    @Override
    public RedisFuture<ValueScanCursor<V>> sscan(K key, ScanArgs scanArgs) {
        return dispatch(commandBuilder.sscan(key, scanArgs));
    }

    @Override
    public RedisFuture<ValueScanCursor<V>> sscan(K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return dispatch(commandBuilder.sscan(key, scanCursor, scanArgs));
    }

    @Override
    public RedisFuture<ValueScanCursor<V>> sscan(K key, ScanCursor scanCursor) {
        return dispatch(commandBuilder.sscan(key, scanCursor));
    }

    @Override
    public RedisFuture<StreamScanCursor> sscan(ValueStreamingChannel<V> channel, K key) {
        return dispatch(commandBuilder.sscanStreaming(channel, key));
    }

    @Override
    public RedisFuture<StreamScanCursor> sscan(ValueStreamingChannel<V> channel, K key, ScanArgs scanArgs) {
        return dispatch(commandBuilder.sscanStreaming(channel, key, scanArgs));
    }

    @Override
    public RedisFuture<StreamScanCursor> sscan(ValueStreamingChannel<V> channel, K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return dispatch(commandBuilder.sscanStreaming(channel, key, scanCursor, scanArgs));
    }

    @Override
    public RedisFuture<StreamScanCursor> sscan(ValueStreamingChannel<V> channel, K key, ScanCursor scanCursor) {
        return dispatch(commandBuilder.sscanStreaming(channel, key, scanCursor));
    }

    @Override
    public RedisFuture<MapScanCursor<K, V>> hscan(K key) {
        return dispatch(commandBuilder.hscan(key));
    }

    @Override
    public RedisFuture<MapScanCursor<K, V>> hscan(K key, ScanArgs scanArgs) {
        return dispatch(commandBuilder.hscan(key, scanArgs));
    }

    @Override
    public RedisFuture<MapScanCursor<K, V>> hscan(K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return dispatch(commandBuilder.hscan(key, scanCursor, scanArgs));
    }

    @Override
    public RedisFuture<MapScanCursor<K, V>> hscan(K key, ScanCursor scanCursor) {
        return dispatch(commandBuilder.hscan(key, scanCursor));
    }

    @Override
    public RedisFuture<StreamScanCursor> hscan(KeyValueStreamingChannel<K, V> channel, K key) {
        return dispatch(commandBuilder.hscanStreaming(channel, key));
    }

    @Override
    public RedisFuture<StreamScanCursor> hscan(KeyValueStreamingChannel<K, V> channel, K key, ScanArgs scanArgs) {
        return dispatch(commandBuilder.hscanStreaming(channel, key, scanArgs));
    }

    @Override
    public RedisFuture<StreamScanCursor> hscan(KeyValueStreamingChannel<K, V> channel, K key, ScanCursor scanCursor,
            ScanArgs scanArgs) {
        return dispatch(commandBuilder.hscanStreaming(channel, key, scanCursor, scanArgs));
    }

    @Override
    public RedisFuture<StreamScanCursor> hscan(KeyValueStreamingChannel<K, V> channel, K key, ScanCursor scanCursor) {
        return dispatch(commandBuilder.hscanStreaming(channel, key, scanCursor));
    }

    @Override
    public RedisFuture<ScoredValueScanCursor<V>> zscan(K key) {
        return dispatch(commandBuilder.zscan(key));
    }

    @Override
    public RedisFuture<ScoredValueScanCursor<V>> zscan(K key, ScanArgs scanArgs) {
        return dispatch(commandBuilder.zscan(key, scanArgs));
    }

    @Override
    public RedisFuture<ScoredValueScanCursor<V>> zscan(K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return dispatch(commandBuilder.zscan(key, scanCursor, scanArgs));
    }

    @Override
    public RedisFuture<ScoredValueScanCursor<V>> zscan(K key, ScanCursor scanCursor) {
        return dispatch(commandBuilder.zscan(key, scanCursor));
    }

    @Override
    public RedisFuture<StreamScanCursor> zscan(ScoredValueStreamingChannel<V> channel, K key) {
        return dispatch(commandBuilder.zscanStreaming(channel, key));
    }

    @Override
    public RedisFuture<StreamScanCursor> zscan(ScoredValueStreamingChannel<V> channel, K key, ScanArgs scanArgs) {
        return dispatch(commandBuilder.zscanStreaming(channel, key, scanArgs));
    }

    @Override
    public RedisFuture<StreamScanCursor> zscan(ScoredValueStreamingChannel<V> channel, K key, ScanCursor scanCursor,
            ScanArgs scanArgs) {
        return dispatch(commandBuilder.zscanStreaming(channel, key, scanCursor, scanArgs));
    }

    @Override
    public RedisFuture<StreamScanCursor> zscan(ScoredValueStreamingChannel<V> channel, K key, ScanCursor scanCursor) {
        return dispatch(commandBuilder.zscanStreaming(channel, key, scanCursor));
    }

    @Override
    public String digest(V script) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(codec.encodeValue(script));
            return new String(Base16.encode(md.digest(), false));
        } catch (NoSuchAlgorithmException e) {
            throw new RedisException("JVM does not support SHA1");
        }
    }

    @Override
    public RedisFuture<List<V>> time() {
        return dispatch(commandBuilder.time());
    }

    @Override
    public RedisFuture<Long> waitForReplication(int replicas, long timeout) {
        return dispatch(commandBuilder.wait(replicas, timeout));
    }

    @Override
    public RedisFuture<Long> pfadd(K key, V value, V... moreValues) {
        return dispatch(commandBuilder.pfadd(key, value, moreValues));
    }

    @Override
    public RedisFuture<Long> pfmerge(K destkey, K sourcekey, K... moreSourceKeys) {
        return dispatch(commandBuilder.pfmerge(destkey, sourcekey, moreSourceKeys));
    }

    @Override
    public RedisFuture<Long> pfcount(K key, K... moreKeys) {
        return dispatch(commandBuilder.pfcount(key, moreKeys));
    }

    @Override
    public RedisFuture<String> clusterMeet(String ip, int port) {
        return dispatch(commandBuilder.clusterMeet(ip, port));
    }

    @Override
    public RedisFuture<String> clusterForget(String nodeId) {
        return dispatch(commandBuilder.clusterForget(nodeId));
    }

    @Override
    public RedisFuture<String> clusterAddSlots(int... slots) {
        return dispatch(commandBuilder.clusterAddslots(slots));
    }

    @Override
    public RedisFuture<String> clusterDelSlots(int... slots) {
        return dispatch(commandBuilder.clusterDelslots(slots));
    }

    @Override
    public RedisFuture<String> clusterInfo() {
        return dispatch(commandBuilder.clusterInfo());
    }

    @Override
    public RedisFuture<String> clusterNodes() {
        return dispatch(commandBuilder.clusterNodes());
    }

    @Override
    public RedisFuture<List<K>> clusterGetKeysInSlot(int slot, int count) {
        return dispatch(commandBuilder.clusterGetKeysInSlot(slot, count));
    }

    @Override
    public RedisFuture<List<Object>> clusterSlots() {
        return dispatch(commandBuilder.clusterSlots());
    }

    @Override
    public RedisFuture<String> clusterSetSlotNode(int slot, String nodeId) {
        return dispatch(commandBuilder.clusterSetSlotNode(slot, nodeId));
    }

    @Override
    public RedisFuture<String> clusterSetSlotMigrating(int slot, String nodeId) {
        return dispatch(commandBuilder.clusterSetSlotMigrating(slot, nodeId));
    }

    @Override
    public RedisFuture<String> clusterSetSlotImporting(int slot, String nodeId) {
        return dispatch(commandBuilder.clusterSetSlotImporting(slot, nodeId));
    }

    @Override
    public RedisFuture<String> clusterFailover(boolean force) {
        return dispatch(commandBuilder.clusterFailover(force));
    }

    @Override
    public RedisFuture<String> clusterReset(boolean hard) {
        return dispatch(commandBuilder.clusterReset(hard));
    }

    @Override
    public RedisFuture<String> asking() {
        return dispatch(commandBuilder.asking());
    }

    @Override
    public RedisFuture<String> clusterReplicate(String nodeId) {
        return dispatch(commandBuilder.clusterReplicate(nodeId));
    }

    @Override
    public RedisFuture<String> clusterFlushslots() {
        return dispatch(commandBuilder.clusterFlushslots());
    }

    @Override
    public RedisFuture<List<String>> clusterSlaves(String nodeId) {
        return dispatch(commandBuilder.clusterSlaves(nodeId));
    }

    @Override
    public RedisFuture<Long> zlexcount(K key, String min, String max) {
        return dispatch(commandBuilder.zlexcount(key, min, max));
    }

    @Override
    public RedisFuture<Long> zremrangebylex(K key, String min, String max) {
        return dispatch(commandBuilder.zremrangebylex(key, min, max));
    }

    @Override
    public RedisFuture<List<V>> zrangebylex(K key, String min, String max) {
        return dispatch(commandBuilder.zrangebylex(key, min, max));
    }

    @Override
    public RedisFuture<List<V>> zrangebylex(K key, String min, String max, long offset, long count) {
        return dispatch(commandBuilder.zrangebylex(key, min, max, offset, count));
    }

    protected <T> RedisCommand<K, V, T> dispatch(CommandType type, CommandOutput<K, V, T> output) {
        return dispatch(type, output, null);
    }

    protected synchronized <T> RedisCommand<K, V, T> dispatch(CommandType type, CommandOutput<K, V, T> output,
            CommandArgs<K, V> args) {
        Command<K, V, T> cmd = new Command<K, V, T>(type, output, args, multi != null);
        return dispatch(cmd);
    }

    @Override
    public synchronized <T> RedisCommand<K, V, T> dispatch(RedisCommand<K, V, T> cmd) {
        if (multi != null && cmd instanceof Command) {
            Command<K, V, T> command = (Command<K, V, T>) cmd;
            command.setMulti(true);
            multi.add(cmd);
        }
        return super.dispatch(cmd);
    }

    public static String string(double n) {
        if (Double.isInfinite(n)) {
            return (n > 0) ? "+inf" : "-inf";
        }
        return Double.toString(n);
    }

    protected boolean isMulti() {
        return multi != null;
    }

    @Override
    public void activated() {

        super.activated();
        // do not block in here, since the channel flow will be interrupted.
        if (password != null) {
            dispatch(commandBuilder.auth(new String(password)));
        }

        if (db != 0) {
            dispatch(commandBuilder.select(db));
        }
    }

}
