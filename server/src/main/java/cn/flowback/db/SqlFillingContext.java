package cn.flowback.db;

import com.alibaba.fastjson.JSONObject;

/**
 * 填充策略上下文
 *
 * @author 唐警威
 **/
public class SqlFillingContext {


    static JsonPaddingStrategy jsonPaddingStrategy = new JsonPaddingStrategy();


    static EntityFillingStrategy tablePaddingStrategy = new EntityFillingStrategy();

    public static SqlFillingStrategy getStrategy(Object o) {
        if (o instanceof JSONObject) {
            return jsonPaddingStrategy;
        }
        return  tablePaddingStrategy;
    }
}
