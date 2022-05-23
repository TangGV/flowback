import com.github.benmanes.caffeine.cache.*;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * caffeine 测试
 * https://gitee.com/cckevincyh/caffeine-demo#%E5%9F%BA%E4%BA%8E%E6%97%B6%E9%97%B4
 *
 * @author 唐警威
 **/
public class CaffeineTest {


    @Test
    public void testManualLoadCache6() throws ExecutionException, InterruptedException {
        Cache<String, byte[]> cache = Caffeine.newBuilder()
                .build();
        cache.put("test1", new byte[1024 * 1024 * 1024]);
        cache.put("test2", new byte[1024 * 1024 * 1024]);
        cache.put("test3", new byte[1024 * 1024 * 1024]);
        cache.put("test4", new byte[1024 * 1024 * 1024]);
        //Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
        System.out.println(cache.asMap());
    }

    @Test
    public void testRemovalListener() {
        Cache<String, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(50, TimeUnit.SECONDS)
                .maximumSize(100)
                .removalListener((RemovalListener<String, String>) (key, value, cause) -> System.out.println(Thread.currentThread().getName() + "--" + MessageFormat.format("key:[{0}],value:[{1}],cause:[{2}]",key,value,cause)))
                .build();

        cache.put("test1", "value1");
        cache.put("test2", "value2");
        cache.put("test3", "value3");
        cache.put("test4", "value4");

    }


    @Test
    public void testWriter() {
        Cache<String, String> cache = Caffeine.newBuilder()
                .expireAfterWrite(50, TimeUnit.SECONDS)
                .maximumSize(100)
                .writer(new CacheWriter<String, String>() {
                    @Override
                    public void write(@NonNull String key, @NonNull String value) {
                        // 持久化或者次级缓存
                        System.out.println(MessageFormat.format("key:[{0}],value:[{1}]", key, value));
                    }

                    @Override
                    public void delete(@NonNull String key, @Nullable String value, @NonNull RemovalCause cause) {
                        // 从持久化或者次级缓存中删除
                        System.out.println(MessageFormat.format("key:[{0}],value:[{1}],cause:[{2}]", key, value, cause));
                    }
                })
                .build();

        cache.put("test1", "value1");
        cache.put("test2", "value2");

        System.out.println("===========");

        System.out.println(cache.asMap());
        cache.invalidate("test1");
        System.out.println(cache.asMap());
        cache.put("test2", "value222");

        /**
         * 打印结果：
         * key:[test1],value:[value1]
         * key:[test2],value:[value2]
         * ===========
         * {test1=value1, test2=value2}
         * key:[test1],value:[value1],cause:[EXPLICIT]
         * {test2=value2}
         * key:[test2],value:[value222]
         */
    }


    @Test
    public void testRecordStats3() {
        LoadingCache<String, String> asyncCache = Caffeine.newBuilder()
                .maximumSize(1)
                //自定义数据采集器
                .recordStats(() -> new StatsCounter() {
                    @Override
                    public void recordHits(@NonNegative int count) {
                        System.out.println("recordHits:" + count);
                    }

                    @Override
                    public void recordMisses(@NonNegative int count) {
                        System.out.println("recordMisses:" + count);
                    }

                    @Override
                    public void recordLoadSuccess(@NonNegative long loadTime) {
                        System.out.println("recordLoadSuccess:" + loadTime);
                    }

                    @Override
                    public void recordLoadFailure(@NonNegative long loadTime) {
                        System.out.println("recordLoadFailure:" + loadTime);
                    }

                    @Override
                    public void recordEviction() {
                        System.out.println("recordEviction...");
                    }

                    @Override
                    public @NonNull CacheStats snapshot() {
                        return null;
                    }
                })
                .build(new CacheLoader<String, String>() {
                    @Override
                    public @Nullable String load(@NonNull String key) throws Exception {
                        throw new RuntimeException("failed");
                    }
                });

        asyncCache.get("test1");
        System.out.println(asyncCache.asMap());

        /**
         * 打印：
         * recordMisses:1
         * recordLoadFailure:41100
         */
    }

    @Test
    public void testRecordStats4() {
        Cache<String, String> cache = Caffeine.newBuilder()
                .maximumSize(1)
                //打开数据采集
                .recordStats()
                .build();


        cache.put("test1", "value1");
        cache.put("test2", "value2");
        System.out.println(cache.asMap());//{test1=value1, test2=value2}
        cache.getIfPresent("test1");
        cache.getIfPresent("test3");
        cache.cleanUp();
        System.out.println(cache.asMap());//{test2=value2}
        System.out.println(cache.stats().hitRate());//查询缓存的命中率 0.5
        System.out.println(cache.stats().hitCount());//命中次数 1
        System.out.println(cache.stats().evictionCount());//被驱逐的缓存数量 1
        System.out.println(cache.stats().averageLoadPenalty());//新值被载入的平均耗时
        /**
         * 打印结果：
         * {test1=value1, test2=value2}
         * {test2=value2}
         * 0.5
         * 1
         * 1
         * 0.0
         */
    }


    @Test
    public void testRecordStats5() {
        LoadingCache<String, String> asyncCache = Caffeine.newBuilder()
                .maximumSize(1)
                //打开数据采集
                .recordStats()
                .build(new CacheLoader<String, String>() {
                    @Override
                    public @Nullable String load(@NonNull String key) throws Exception {
                        System.out.println("-----------");
                        return "LOAD";
                    }
                });

        asyncCache.get("test1");
        asyncCache.get("test1");
        System.out.println(asyncCache.asMap());//{test1=value1}
        System.out.println(asyncCache.stats().hitRate());//查询缓存的命中率 0.5
        System.out.println(asyncCache.stats().hitCount());//命中次数 1
        System.out.println(asyncCache.stats().evictionCount());//被驱逐的缓存数量 0
        System.out.println(asyncCache.stats().averageLoadPenalty());//新值被载入的平均耗时 21100.0
        /**
         * 打印：
         * {test1=value1}
         * 0.5
         * 1
         * 0
         * 21100.0
         */
    }
}
