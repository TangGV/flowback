package cn.flowback.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis工具类
 *
 * @author 唐警威
 **/
@Component
public class RedisUtils {


    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 将参数中的字符串值设置为键的值，不设置过期时间
     *
     * @param key
     * @param value 必须要实现 Serializable 接口
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 将参数中的字符串值设置为键的值，设置过期时间
     *
     * @param key
     * @param value   必须要实现 Serializable 接口
     * @param timeout 单位秒
     */
    public void set(String key, String value, Long timeout) {
        if (timeout == 0) {
            redisTemplate.opsForValue().set(key, value);
            return;
        }
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }


    /**
     * 设置值，如果不存在，那么设置成功，如果存在，那么设置失败
     *
     * @param key
     * @param value 必须要实现 Serializable 接口
     */
    public boolean setNx(String key, String value, Long timeout, TimeUnit timeUnit) {
        if (timeout == 0) {
            throw new IllegalArgumentException("timeout 不能为0");
        }
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
    }

    /**
     * 设置值，如果不存在，那么设置成功，如果存在，那么设置失败
     *
     * @param key
     * @param value   必须要实现 Serializable 接口
     * @param timeout 超时，单位秒
     */
    public boolean setNx(String key, String value, Long timeout) {
        if (timeout == 0) {
            throw new IllegalArgumentException("timeout 不能为0");
        }
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
    }

    public boolean hSetNx(String key, String userId, String value) {
        return redisTemplate.opsForHash().putIfAbsent(key, userId, value);
    }


    /**
     * 获取匹配keys的数据
     *
     * @param keys
     * @return
     */
    public Set<String> keys(String keys) {
        return redisTemplate.keys(keys);
    }

    /**
     * 获取与指定键相关的值
     *
     * @param key
     * @return
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取与指定键相关的值
     *
     * @param key
     * @return
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String dataJson = redisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(dataJson)) {
            return null;
        }
        return JSON.parseObject(dataJson, clazz);
    }

    /**
     * 获取与指定键相关的值
     *
     * @param key
     * @return
     */
    public <T> List<T> getArray(String key, Class<T> clazz) {
        String dataJson = redisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(dataJson)) {
            return null;
        }
        return JSON.parseArray(dataJson, clazz);
    }

    /**
     * 获取与指定键相关的值
     *
     * @param key
     * @return
     */
    public Object hGet(String key, String name) {
        return redisTemplate.opsForHash().get(key, name);
    }

    /**
     * 获取与指定键相关的值
     *
     * @param key
     * @return
     */
    public Long hDel(String key, String name) {
        return redisTemplate.opsForHash().delete(key, name);
    }


    /**
     * 设置与指定键相关的值
     *
     * @param key
     * @return
     */
    public void hSet(String key, String name, String value) {
        redisTemplate.opsForHash().put(key, name, value);
    }


    /**
     * 设置某个键的过期时间
     *
     * @param key 键值
     * @param ttl 过期秒数
     */
    public boolean expire(String key, Long ttl) {
        return redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
    }

    /**
     * 判断某个键是否存在
     *
     * @param key 键值
     */
    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 向集合添加元素
     *
     * @param key
     * @param value
     * @return 返回值为设置成功的value数
     */
    public Long sAdd(String key, String... value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    /**
     * 向集合添加元素
     *
     * @param key
     * @param value
     * @return 返回值为设置成功的value数
     */
    public Long sAdd(String key, Collection<String> value) {
        return redisTemplate.opsForSet().add(key, value.toArray(new String[]{}));
    }

    /**
     * 获取集合中的某个元素
     *
     * @param key
     * @return 返回值为redis中键值为key的value的Set集合
     */
    public Set<String> sGetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 将给定分数的指定成员添加到键中存储的排序集合中
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 将给定分数的指定成员添加到键中存储的排序集合中
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public Double zIncrScore(String key, String value, double score) {
        return redisTemplate.opsForZSet().incrementScore(key, value, score);
    }

    /**
     * 返回指定键中存储的排序集合和分数 升序
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> zRangeWithScores(String key, int start, int end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    /**
     * 返回指定键中存储的排序集合和分数 降序
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> zReverseRangeWithScores(String key, int start, int end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }


    public Long zRemoveRank(String key, int rankEnd, int i) {
        return redisTemplate.opsForZSet().removeRange(key, 3, -1);
    }


    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    public Long zReverseRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }


    /**
     * 返回指定排序集中给定成员的分数
     *
     * @param key
     * @param value
     * @return
     */
    public Double zScore(String key, String value) {
        return redisTemplate.opsForZSet().score(key, value);
    }


    /**
     * 将keyList里的key进行并集并且存储到key中
     *
     * @param key          要存储到的key
     * @param unionKeyList 要进行并集的key列表
     */
    public long zUnionStore(String key, Collection<String> unionKeyList) {
        if (CollectionUtils.isEmpty(unionKeyList)) {
            return -1;
        }
        if (unionKeyList.size() < 2) {
            System.out.println("要并集的key至少为2个");
        }
        ;
        String firstKey = "";
        List<String> dataList = Lists.newArrayList();
        for (String unionKey : unionKeyList) {
            if (StringUtils.isBlank(firstKey)) {
                firstKey = unionKey;
                continue;
            }
            dataList.add(unionKey);
        }
        return redisTemplate.opsForZSet().unionAndStore(firstKey, dataList, key);
    }

    /**
     * 删除指定的键
     *
     * @param key
     * @return
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 删除多个键
     *
     * @param keys
     * @return
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * hash 类型的自增
     *
     * @param key
     * @return
     */
    public Long hincrby(String key, Object value, Long number) {
        return redisTemplate.opsForHash().increment(key, value, number);
    }


    /**
     * Long 类型的自增
     *
     * @param key
     * @return
     */
    public Long incr(String key, Long number) {
        return redisTemplate.opsForValue().increment(key, number);
    }

    /**
     * Long 类型的自增
     *
     * @param key
     * @return
     */
    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * Long 类型的自减
     *
     * @param key
     * @return
     */
    public Long decr(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * Long 类型的自减
     *
     * @param key
     * @return
     */
    public Long decr(String key, Integer number) {
        return redisTemplate.opsForValue().decrement(key, number);
    }


    public RedisTemplate<String, String> getRedisTemplate() {
        return redisTemplate;
    }

    public Boolean getBit(String key, long offset) {
        return redisTemplate.opsForValue().getBit(key, offset);
    }

    public Boolean setBit(String buildSignKey, int offset, boolean b) {
        return redisTemplate.opsForValue().setBit(buildSignKey, offset, b);
    }

    /**
     * bitfield get操作
     *
     * @param buildSignKey
     * @param type
     * @param offset
     * @return
     */
    public List<Long> bitfield(String buildSignKey, BitFieldSubCommands.BitFieldType type, long offset) {
        BitFieldSubCommands bitFieldSubCommands = BitFieldSubCommands.create();
        bitFieldSubCommands = bitFieldSubCommands.get(type).valueAt(offset);
        return redisTemplate.opsForValue().bitField(buildSignKey, bitFieldSubCommands);
    }

    public List<Long> bitfieldTwo(String buildSignKey, int limit, int offset) {
        return (List<Long>) redisTemplate.execute((RedisCallback<List<Long>>) con -> con.bitField(buildSignKey.getBytes(),
                BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(limit)).valueAt(offset)));
    }


    public Long lPush(String key, String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

}
