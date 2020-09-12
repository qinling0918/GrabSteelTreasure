package com.zgw.qgb.helper.utils;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

/**
 * Created by qinling on 2020/6/4 18:12
 * Description: ViewBinding 工具类
 */
public class ViewBindingUtil {

    public static <Binding extends ViewBinding> Binding create(Class<?> bindingClazz, LayoutInflater inflater) {
        return create(bindingClazz, inflater, null);
    }

    public static <Binding extends ViewBinding> Binding create(Class<?> bindingClazz, LayoutInflater inflater, ViewGroup root) {
        return create(bindingClazz, inflater, root, false);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <Binding extends ViewBinding> Binding create(Class<?> bindingClazz, LayoutInflater inflater, ViewGroup root, boolean attachToRoot) {
        Class<?> clazz = null;
        Binding binding = null;
        List<ParameterizedType> parameterizedTypes = getParameterizedTypeClass(bindingClazz);
        for (ParameterizedType parameterizedType : parameterizedTypes) {
            clazz = getViewBindingClass(parameterizedType);
            if (clazz !=null) break;
        }
        if (clazz != null) {
            try {
                Method method = clazz.getMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
                binding = (Binding) method.invoke(null, inflater, root, attachToRoot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Objects.requireNonNull(binding);
    }

    /**
     * 从这些泛型类中 找到 ViewBinding类型的类，一个类可能有多个泛型，此处寻找包含ViewBinding的类
     *
     * @param parameterizedType 包含泛型的类
     * @return
     */
    private static Class<?> getViewBindingClass(ParameterizedType parameterizedType) {
        Type[] types = Objects.requireNonNull(parameterizedType).getActualTypeArguments();
        for (Type type : types) {
            if (type instanceof  Class){
                Class<?> temp = (Class<?>) type;
                if (ViewBinding.class.isAssignableFrom(temp)) {
                    return temp;
                }
            }


        }
        return null;
    }

    /**
     * 根据传入的类，获取其以及其父类中包含 泛型的类。
     *
     * @param clazz 类
     * @return 取其以及其父类中包含 泛型的类。
     */
    public static List<ParameterizedType> getParameterizedTypeClass(Class<?> clazz) {
        List<ParameterizedType> clazzs = new ArrayList<ParameterizedType>();
        while (clazz != null) {
            if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
                clazzs.add((ParameterizedType) clazz.getGenericSuperclass());
            }
            clazz = clazz.getSuperclass();
        }
        return clazzs;
    }
}