package cn.flowback.common.utils;
import com.alibaba.fastjson.JSONObject;
import com.github.luben.zstd.Zstd;

import java.util.UUID;


public class ZstdUtils {


        public static void main(String[] args) {
            String s = UUID.randomUUID().toString();
            JSONObject object = new JSONObject();
            object.put("table","test_work");
            object.put("time",System.currentTimeMillis());
            object.put("c11",s);
            object.put("c22",s);
            object.put("c33",s);
            object.put("c44",s);
            object.put("c55",s);
            object.put("c66",s);
            object.put("c77",s);
            object.put("c88",s);
            object.put("c99",s);
            object.put("c100",s);
            object.put("c111",s);
            object.put("c121",s);

            String s2 = UUID.randomUUID().toString();
            JSONObject object2 = new JSONObject();
            object2.put("table","test_work");
            object2.put("time",System.currentTimeMillis());
            object2.put("c11",s2);
            object2.put("c22",s2);
            object2.put("c33",s2);
            object2.put("c44",s2);
            object2.put("c55",s2);
            object2.put("c66",s2);
            object2.put("c77",s2);
            object2.put("c88",s2);
            object2.put("c99",s2);
            object2.put("c100",s2);
            object2.put("c111",s2);
            object2.put("c121",s2);
            System.out.println(object.toJSONString().getBytes().length);
            System.out.println(object2.toJSONString().getBytes().length);

            byte[] compress = compress(object.toJSONString().getBytes());
            byte[] compress2 = compress(object2.toJSONString().getBytes());

            System.out.println(compress.length);
            System.out.println(compress2.length);

            String decompress = decompress(compress);
            System.out.println(decompress);
            String decompress2 = decompress(compress2);
            System.out.println(decompress2);


        }

        /**
         * 压缩
         */
        public static byte[] compress(byte[] bytes) {
            return Zstd.compress(bytes);
        }

        /**
         * 解压
         */
        public static String decompress(byte[] bytes) {
            int size = (int) Zstd.decompressedSize(bytes);
            byte[] ob = new byte[size];
            Zstd.decompress(ob, bytes);
            return new String(ob);
        }

        /**
         * 解压
         */
        public static byte[] decompressBytes(byte[] bytes) {
            int size = (int) Zstd.decompressedSize(bytes);
            byte[] ob = new byte[size];
            Zstd.decompress(ob, bytes);
            return ob;
        }
}
