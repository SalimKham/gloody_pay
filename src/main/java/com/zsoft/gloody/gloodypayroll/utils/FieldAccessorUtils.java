package com.zsoft.gloody.gloodypayroll.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FieldAccessorUtils {
    private FieldAccessorUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getFieldNameByJsonProperty(Class<?> className, String jsonPropertyName) {
        Field[] fields = className.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(JsonProperty.class)) {
                String annotationValue = field.getAnnotation(JsonProperty.class).value();
                if (Objects.equals(annotationValue, jsonPropertyName)) {
                    return field.getName();
                }
            }
        }
        return jsonPropertyName;
    }

    public static Field findField(Class<?> className, String jsonFieldName) {
        String fieldName = FieldAccessorUtils.getFieldNameByJsonProperty(className, jsonFieldName);
        return ReflectionUtils.findField(className, fieldName);
    }

    public static <T> void setFieldValue(Class<?> className, Field field, T object, T valueFrom) {
        if (Modifier.isPrivate(field.getModifiers())) {
            Method setter = getFieldSetterMethod(className, field);
            Method getter = getFieldGetterMethod(className, field);
            if (setter != null && getter != null) {
                Object value = ReflectionUtils.invokeMethod(getter, valueFrom);
                ReflectionUtils.invokeMethod(setter, object, value);
            }
        } else {
            ReflectionUtils.setField(field, object, ReflectionUtils.getField(field, valueFrom));
        }
    }

    public static <T> Object getFieldValue(Class<?> className, Field field, T valueFrom) {
        if (Modifier.isPrivate(field.getModifiers())) {
            Method getter = getFieldGetterMethod(className, field);
            if (getter != null) {
                return ReflectionUtils.invokeMethod(getter, valueFrom);
            }
        } else {
            return ReflectionUtils.getField(field, valueFrom);
        }
        return null;
    }

    public static Method getFieldGetterMethod(Class<?> className, Field field) {
        String name = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
        try {
            if (field.getType().equals(Boolean.TYPE)) {
                return className.getDeclaredMethod("is" + name);
            }
            return className.getDeclaredMethod("get" + name);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Method getFieldSetterMethod(Class<?> className, Field field) {
        String name = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
        try {
            return className.getDeclaredMethod("set" + name, field.getType());
        } catch (Exception ex) {
            return null;
        }
    }

    public static List<String> getAllFields(Class<?> className) {
        Field[] fields = className.getDeclaredFields();
        return Arrays.stream(fields)
                .map(Field::getName)
                .collect(Collectors.toList());
    }
}
