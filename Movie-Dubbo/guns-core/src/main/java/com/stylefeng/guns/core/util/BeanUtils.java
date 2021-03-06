package com.stylefeng.guns.core.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sf.jsqlparser.expression.operators.relational.OldOracleJoinBinaryExpression;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.core.Converter;

/**
 * 此方法不能使用内部类，否则反射会出错
 */
public class BeanUtils {
    /**
     * the beanCopierMap
     */
    private static final ConcurrentMap<String, BeanCopier> beanCopierMap = new ConcurrentHashMap<>();

    /**
     * @param source
     * @param target
     * @return T
     * @description 两个类对象之间转换
     */
    public static <T> T copy(Object source, Class<T> target) {
        T ret = null;
        if (source != null) {
            try {
                //if (!target.getName().contains("$"))
                ret = target.newInstance();
//                else {
//                    ret = ((Class<T>)Thread.currentThread().getContextClassLoader().loadClass(target.getName().replaceAll("\\$\\d", ""))).newInstance();
//
//                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("create class[" + target.getName()
                        + "] instance error", e);
            }
            BeanCopier beanCopier = getBeanCopier(source.getClass(), target);
            beanCopier.copy(source, ret, new DeepCopyConverter(target));
        }
        return ret;
    }

    /**
     * 基于cglib进行对象组复制
     *
     * @param datas
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> copyByList(List<?> datas, Class<T> clazz) {
        List<T> result = new ArrayList<>(datas.size());
        for (Object data : datas) {
            result.add(copy(data, clazz));
        }
        return result;
    }


    public static class DeepCopyConverter implements Converter {

        /**
         * The Target.
         */
        private Class<?> target;

        /**
         * Instantiates a new Deep copy converter.
         *
         * @param target the target
         */
        public DeepCopyConverter(Class<?> target) {
            this.target = target;
        }

        @Override
        public Object convert(Object value, Class targetClazz, Object methodName) {
            if (value instanceof List) {
                List values = (List) value;
                List retList = new ArrayList<>(values.size());
                for (final Object source : values) {
                    String tempFieldName = methodName.toString().replace("set",
                            "");
                    String fieldName = tempFieldName.substring(0, 1)
                            .toLowerCase() + tempFieldName.substring(1);
                    Class clazz = ClassUtils.getElementType(target, fieldName);
                    retList.add(!ClassUtils.isPrimitive(clazz) ? BeanUtils.copy(source, clazz) : source);
                }
                return retList;
            } else if (value != null && value.getClass().isArray()) {
                Object[] values = (Object[]) value;
                Object[] retList = null;

                for (int i = 0; i < values.length; i++) {
                    Object source = values[i];
                    if (source == null)
                        continue;
                    Class clazz = source.getClass();
                    if (retList == null)
                        retList = (Object[]) java.lang.reflect.Array.newInstance(clazz, values.length);
                    retList[i] = !ClassUtils.isPrimitive(clazz) ? BeanUtils.copy(source, clazz) : source;
                }
                return retList;
            } else if (value instanceof Map) {
                // TODO 暂时用不到，后续有需要再补充
            } else if (targetClazz.isInterface()) {
                // TODO 暂时用不到，后续有需要再补充
            } else if (!ClassUtils.isPrimitive(targetClazz)) {
                return BeanUtils.copy(value, targetClazz);
            }
            return value;
        }
    }

    /**
     * @param source
     * @param target
     * @return BeanCopier
     * @description 获取BeanCopier
     */
    public static BeanCopier getBeanCopier(Class<?> source, Class<?> target) {
        String beanCopierKey = generateBeanKey(source, target);
        if (beanCopierMap.containsKey(beanCopierKey)) {
            return beanCopierMap.get(beanCopierKey);
        } else {
            BeanCopier beanCopier = BeanCopier.create(source, target, true);
            beanCopierMap.putIfAbsent(beanCopierKey, beanCopier);
        }
        return beanCopierMap.get(beanCopierKey);
    }

    /**
     * @param source
     * @param target
     * @return String
     * @description 生成两个类的key
     */
    public static String generateBeanKey(Class<?> source, Class<?> target) {
        return source.getName() + "@" + target.getName();
    }
}
