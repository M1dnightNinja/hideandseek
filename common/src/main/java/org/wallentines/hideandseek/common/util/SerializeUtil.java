package org.wallentines.hideandseek.common.util;

import java.util.HashMap;
import java.util.Map;

public class SerializeUtil {

    public static <K,V> HashMap<String, V> stringify(HashMap<K, V> map) {

        HashMap<String, V> out = new HashMap<>();
        for(Map.Entry<K, V> ent : map.entrySet()) {
            out.put(ent.getKey().toString(), ent.getValue());
        }
        return out;
    }

}
