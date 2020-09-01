package org.myframework.util;

import java.util.Collection;
import java.util.Map;

public class ValidationUtil {
    /**
     * 判断项目中的集合Collection是否为null或者size为0
     * @param objects Collection
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> objects) {
       return objects == null || objects.isEmpty();
    }

    /**
     * 判断项目中的Map是否为null或者size为0
     * @param map map
     * @return map是否为空
     */
    public static boolean isEmpty(Map<?,?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断项目中的Array是否为null或者size为0
     * @param objects Array
     * @return Array是否为空
     */
    public static boolean isEmpty(Object[] objects) {
        return objects == null || objects.length == 0;
    }

    /**
     * 判断项目中String是否为null或者""
     * @param object 字符串
     * @return 字符串是否为空
     */
    public static boolean isEmpty(String object) {
        return object == null || "".equals(object);
    }
}
