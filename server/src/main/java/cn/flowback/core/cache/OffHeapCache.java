package cn.flowback.core.cache;

import cn.flowback.config.FlowBackProperties;
import org.caffinitas.ohc.Eviction;
import org.caffinitas.ohc.OHCache;
import org.caffinitas.ohc.OHCacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 堆外内存缓存
 */
@Component
public class OffHeapCache {

    @Autowired
    private FlowBackProperties flowBackProperties;

    private static OHCache<String, byte[]> ohCache;

    @PostConstruct
    public void  init(){
        ohCache = OHCacheBuilder.<String, byte[]>newBuilder()
                .keySerializer(new OHCKeySerialize())
                .valueSerializer(new OHCValueSerialize())
                .eviction(Eviction.NONE)
                .throwOOME(true)
                .capacity(flowBackProperties.getOhSize())
                .build();
    }


    public static  OHCache<String, byte[]> cache(){
        return ohCache;
    }


    public static void main(String[] args) {
        int i = 1;
        while (true) {
            boolean put = ohCache.put(i + "", UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
            if(put == false){
                System.out.printf("分配失败");
            }
            ohCache.remove(i+"");
            i++;
        }
    }

}
