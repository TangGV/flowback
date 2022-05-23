import com.github.benmanes.caffeine.cache.*;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import reactor.core.publisher.Flux;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * caffeine 测试
 * https://gitee.com/cckevincyh/caffeine-demo#%E5%9F%BA%E4%BA%8E%E6%97%B6%E9%97%B4
 *
 * @author 唐警威
 **/
public class FluxTest {


    @Test
    public void fulxTest() {

        Random random = new Random();
        Flux.generate(ArrayList::new ,(list,sink) ->{
            int i = random.nextInt(199);
            list.add(i);
            sink.next(i);
            if(list.size()  == 10){
                sink.complete();
            }
            return  list;
        }).subscribe(System.out::println);
        System.out.println("执行完成");
    }




    @Test
    public void fulxTest2() {

        Random random = new Random();
        Flux.create(sink -> {
                    int i = random.nextInt(199);
                    for (int j = 0; j < 10; j++) {
                        sink.next(i);
                    }
                    sink.complete();
                }
        ).subscribe(System.out::println);
        System.out.println("执行完成");
    }

}
