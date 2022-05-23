package cn.flowback.core.data;

import cn.flowback.config.FlowBackProperties;
import cn.flowback.core.statistic.LeapArray;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.concurrent.BlockingQueue;

/**
 * caffeine缓存 方式
 *
 * @author 唐警威
 **/
@Component
@ConditionalOnProperty(value = "flowback.datasourcePlatform", havingValue = "caffeine")
public class CaffeineDataService implements DataService {

    Logger logger = LoggerFactory.getLogger(CaffeineDataService.class);

    @Autowired
    private FlowBackProperties flowBackProperties;


    Cache<String, String> cache = Caffeine.newBuilder()
            // 设置缓存最大条目数，超过条目则触发回收。
            .maximumSize(10000)
            .build();


    @Override
    public void saveBatch(BlockingQueue<Object> logs, LeapArray leapArray ) {
        long s = System.currentTimeMillis();
        int size = logs.size();
        int byteSize = 0;
        for (int i = 0; i < size; i++) {
            Object obj = logs.poll();
            if (obj == null) {
                continue;
            }
            cache.put(obj.toString(), obj.toString());
        }
        long e = System.currentTimeMillis();
        logger.info("caffeine缓存耗时:" + (e - s) + " byteSize:" + byteSize + " add:" + size + " , total:" + cache.asMap().keySet().size());
    }


}
