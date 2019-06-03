package com.zgw.qgb.helper.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * <pre>
 * author: Blankj
 * blog : http://blankj.com
 * time : 2017/12/15
 * desc : utils about reflect
 * update by tsinling at 2018/8/23  add desc：  reflect(final Context context, final String packageName_dexFilePath, final String className)
 *
 * 使用用例，以java.lang.String 为例,
 *
 * 无参构造, 以下效果同  new String（）.length()
 * int length = ReflectUtils.reflect("java.lang.String(全路径类名)").newInstance().method("count" ).get();
 * 一参构造 以下效果同  new String（"hello"）.length()
 * int length = ReflectUtils.reflect("java.lang.String(全路径类名)").newInstance("hello").method("length" ).get();
 *
 * String subStr = new String（"hello"）.subString(1,3) 等同于
 * String subStr = ReflectUtils.reflect("java.lang.String(").newInstance("hello").method("subString" ,1,3).get();
 *
 *
 * 若是加载第三方apk，dex，jar（dex格式，并非字节码格式）时 参见reflect（Context，path_packageName,全路径类名）
 * Object object = ReflectUtils.reflect(this,dexFilePath,"x.x.x.ReflectUtils(全路径类名)").newInstance().method("reflect"，"" ).get();
 *
 * </pre>
 */

public final class ReflectUtils {
    private final Class<?> type;
    private final Object object;

    private ReflectUtils(final Class<?> type) {
        this(type, type);
    }

    private ReflectUtils(final Class<?> type, Object object) {
        this.type = type;
        this.object = object;
    }
///////////////////////////////////////////////////////////////////////////
// reflect
///////////////////////////////////////////////////////////////////////////

    /**
     * Reflect the class.
     *
     * @param className The name of class.
     * @return the single {@link ReflectUtils} instance
     * @throws ReflectException if reflect unsuccessfully
     */
    public static ReflectUtils reflect(final String className)
            throws ReflectException {
        return reflect(forName(className));
    }

    /**
     * jar 包需要通过Android SDK中 build-tools/版本号（例26.0.0）/dx.bat（windows）
     * 通过命令  dx --dex --output=target.jar（-output="输出的jar包名"）  origin.jar（原来的jar包）
     * 将原本字节码（.class）格式的jar 转为 android 平台上所用的 .dex格式的jar
     * aar 包暂无更好的方法使其转成 .dex ，建议打包称Apk，以Apk形式进行热加载。
     *
     * reflectWithInstalledApk & reflectWithLoadDex
     * 通过第三方Apk包名（确保设备上已经安装该包名的Apk）进行反射
     * 或者 本质为dex文件的 apk，dex，jar（jar为字节码格式需要特殊处理）文件的路径名，
     * 实现运行时的类加载后进行反射
     * @param context
     * @param packageName_dexFilePath the path of dexFile  （eg：apk，dex，jar）
     *                            or packageName （Please make sure the app is installed）
     * @param className           The name of the class that needs to be reflected
     * @return the single {@link ReflectUtils} instance
     * @throws ReflectException if reflect unsuccessfully
     */

    @SuppressWarnings("suggest reflectWithInstalledApk or reflectWithLoadDex instead ")
    public static ReflectUtils reflect(final Context context, final String packageName_dexFilePath, final String className)
            throws ReflectException {
        if (packageName_dexFilePath.contains(File.separator)) {  // 是文件路径
            return reflectWithLoadDex(context, packageName_dexFilePath, className);
        } else { // 包名
            Context packageContext = getPackageContext(context, packageName_dexFilePath);
            Context context_ = null != packageContext ? packageContext : context;
            return reflect(forName(className, context_.getClassLoader()));
        }
    }


    /**
     * Reflect the class.
     *
     * @param className   The name of class.
     * @param classLoader The loader of class.
     * @return the single {@link ReflectUtils} instance
     * @throws ReflectException if reflect unsuccessfully
     */
    public static ReflectUtils reflect(final String className, final ClassLoader classLoader)
            throws ReflectException {
        return reflect(forName(className, classLoader));
    }

    /**
     * Reflect the class.
     *
     * @param clazz The class.
     * @return the single {@link ReflectUtils} instance
     * @throws ReflectException if reflect unsuccessfully
     */
    public static ReflectUtils reflect(final Class<?> clazz)
            throws ReflectException {
        return new ReflectUtils(clazz);
    }

    /**
     * Reflect the class.
     *
     * @param object The object.
     * @return the single {@link ReflectUtils} instance
     * @throws ReflectException if reflect unsuccessfully
     */
    public static ReflectUtils reflect(final Object object)
            throws ReflectException {
        return new ReflectUtils(object == null ? Object.class : object.getClass(), object);
    }

    /**
     * @param context
     * @param packageName packageName （Please make sure the app is installed）
     * @param className   The name of the class that needs to be reflected
     * @return the single {@link ReflectUtils} instance
     * @throws ReflectException if reflect unsuccessfully
     */
    public static ReflectUtils reflectWithInstalledApk(final Context context, final String packageName, final String className)
            throws ReflectException {
        Context packageContext = getPackageContext(context, packageName);

        if (null != packageContext) {
            return reflect(forName(className, packageContext.getClassLoader()));
        } else {
            return reflect(className);
        }
    }

    /**
     * jar包为字节码（.class），android则为.dex
     * 此处的jar包需要为.dex, 请在使用jar包前，通常使用
     * SDK目录下 build-tools 文件下的 dx.bat(Windows)进行  .class -->.dex 转换。
     *
     * @param context
     * @param dexFilePath the path of dexFile  （eg：apk(dex)，dex，jar）
     * @param className   The name of the class that needs to be reflected
     * @return the single {@link ReflectUtils} instance
     * @throws ReflectException if reflect unsuccessfully
     */
    public static ReflectUtils reflectWithLoadDex(final Context context, final String dexFilePath, final String className)
            throws ReflectException {
        ClassLoader classLoader = null;
        try {
            File dexOutputDir = context.getDir("dex", 0);
            classLoader = new DexClassLoader(dexFilePath, dexOutputDir.getAbsolutePath(), null, context.getClassLoader());
            Class<?> clazz = classLoader.loadClass(className);
            return reflect(clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return reflect(className, classLoader);
        }
    }

    /**
     * 由于 jar包是字节码（.class）格式， 在安卓中加载需要.dex格式文件，
     * 故在android 该方法不起作用
     *
     * @param jarFilePath
     * @param className
     * @return
     * @throws ReflectException
     */
    @SuppressWarnings({"only JAVA platform", "unCheck", "nouse"})
    @Deprecated
    public static ReflectUtils reflectWithLoadJar(final String jarFilePath, final String className)
            throws ReflectException {
        try {
            URL url = new File(jarFilePath).toURI().toURL();
            ClassLoader classLoader = new URLClassLoader(new URL[]{url}, Thread.currentThread().getContextClassLoader());
            Class<?> clazz = classLoader.loadClass(className);
            return reflect(clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return reflect(className);
        }
    }


    @Nullable
    private static Context getPackageContext(Context context, String packageName) {
        try {
            return context.createPackageContext(packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ReflectException(e);
        }
    }

    private static Class<?> forName(String name, ClassLoader classLoader) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new ReflectException(e);
        }
    }
///////////////////////////////////////////////////////////////////////////
// newInstance
///////////////////////////////////////////////////////////////////////////

    /**
     * Create and initialize a new instance.
     *
     * @return the single {@link ReflectUtils} instance
     */
    public ReflectUtils newInstance() {
        return newInstance(new Object[0]);
    }

    /**
     * Create and initialize a new instance.
     *
     * @param args The args.
     * @return the single {@link ReflectUtils} instance
     */
    public ReflectUtils newInstance(Object... args) {
        Class<?>[] types = getArgsType(args);
        try {
            Constructor<?> constructor = type().getDeclaredConstructor(types);
            return newInstance(constructor, args);
        } catch (NoSuchMethodException e) {
            List<Constructor<?>> list = new ArrayList<>();
            for (Constructor<?> constructor : type().getDeclaredConstructors()) {
                if (match(constructor.getParameterTypes(), types)) {
                    list.add(constructor);
                }
            }
            if (list.isEmpty()) {
                throw new ReflectException(e);
            } else {
                sortConstructors(list);
                return newInstance(list.get(0), args);
            }
        }
    }

    private Class<?>[] getArgsType(final Object... args) {
        if (args == null) return new Class[0];
        Class<?>[] result = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            Object value = args[i];
            result[i] = value == null ? NULL.class : value.getClass();
        }
        return result;
    }

    private void sortConstructors(List<Constructor<?>> list) {
        Collections.sort(list, new Comparator<Constructor<?>>() {
            @Override
            public int compare(Constructor<?> o1, Constructor<?> o2) {
                Class<?>[] types1 = o1.getParameterTypes();
                Class<?>[] types2 = o2.getParameterTypes();
                int len = types1.length;
                for (int i = 0; i < len; i++) {
                    if (!types1[i].equals(types2[i])) {
                        if (wrapper(types1[i]).isAssignableFrom(wrapper(types2[i]))) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
                return 0;
            }
        });
    }

    private ReflectUtils newInstance(final Constructor<?> constructor, final Object... args) {
        try {
            return new ReflectUtils(
                    constructor.getDeclaringClass(),
                    accessible(constructor).newInstance(args)
            );
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
///////////////////////////////////////////////////////////////////////////
// field
///////////////////////////////////////////////////////////////////////////

    /**
     * Get the field.
     *
     * @param name The name of field.
     * @return the single {@link ReflectUtils} instance
     */
    public ReflectUtils field(final String name) {
        try {
            Field field = getField(name);
            return new ReflectUtils(field.getType(), field.get(object));
        } catch (IllegalAccessException e) {
            throw new ReflectException(e);
        }
    }

    /**
     * Set the field.
     *
     * @param name  The name of field.
     * @param value The value.
     * @return the single {@link ReflectUtils} instance
     */
    public ReflectUtils field(String name, Object value) {
        try {
            Field field = getField(name);
            field.set(object, unwrap(value));
            return this;
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private Field getField(String name) throws IllegalAccessException {
        Field field = getAccessibleField(name);
        if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL) {
            try {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            } catch (NoSuchFieldException ignore) {
// runs in android will happen
            }
        }
        return field;
    }

    private Field getAccessibleField(String name) {
        Class<?> type = type();
        try {
            return accessible(type.getField(name));
        } catch (NoSuchFieldException e) {
            do {
                try {
                    return accessible(type.getDeclaredField(name));
                } catch (NoSuchFieldException ignore) {
                }
                type = type.getSuperclass();
            } while (type != null);
            throw new ReflectException(e);
        }
    }

    private Object unwrap(Object object) {
        if (object instanceof ReflectUtils) {
            return ((ReflectUtils) object).get();
        }
        return object;
    }
///////////////////////////////////////////////////////////////////////////
// method
///////////////////////////////////////////////////////////////////////////

    /**
     * Invoke the method.
     *
     * @param name The name of method.
     * @return the single {@link ReflectUtils} instance
     * @throws ReflectException if reflect unsuccessfully
     */
    public ReflectUtils method(final String name) throws ReflectException {
        return method(name, new Object[0]);
    }

    /**
     * Invoke the method.
     *
     * @param name The name of method.
     * @param args The args.
     * @return the single {@link ReflectUtils} instance
     * @throws ReflectException if reflect unsuccessfully
     */
    public ReflectUtils method(final String name, final Object... args) throws ReflectException {
        Class<?>[] types = getArgsType(args);
        try {
            Method method = exactMethod(name, types);
            return method(method, object, args);
        } catch (NoSuchMethodException e) {
            try {
                Method method = similarMethod(name, types);
                return method(method, object, args);
            } catch (NoSuchMethodException e1) {
                throw new ReflectException(e1);
            }
        }
    }

    private ReflectUtils method(final Method method, final Object obj, final Object... args) {
        try {
            accessible(method);
            if (method.getReturnType() == void.class) {
                method.invoke(obj, args);
                return reflect(obj);
            } else {
                return reflect(method.invoke(obj, args));
            }
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    private Method exactMethod(final String name, final Class<?>[] types)
            throws NoSuchMethodException {
        Class<?> type = type();
        try {
            return type.getMethod(name, types);
        } catch (NoSuchMethodException e) {
            do {
                try {
                    return type.getDeclaredMethod(name, types);
                } catch (NoSuchMethodException ignore) {
                }
                type = type.getSuperclass();
            } while (type != null);
            throw new NoSuchMethodException();
        }
    }

    private Method similarMethod(final String name, final Class<?>[] types)
            throws NoSuchMethodException {
        Class<?> type = type();
        List<Method> methods = new ArrayList<>();
        for (Method method : type.getMethods()) {
            if (isSimilarSignature(method, name, types)) {
                methods.add(method);
            }
        }
        if (!methods.isEmpty()) {
            sortMethods(methods);
            return methods.get(0);
        }
        do {
            for (Method method : type.getDeclaredMethods()) {
                if (isSimilarSignature(method, name, types)) {
                    methods.add(method);
                }
            }
            if (!methods.isEmpty()) {
                sortMethods(methods);
                return methods.get(0);
            }
            type = type.getSuperclass();
        } while (type != null);
        throw new NoSuchMethodException("No similar method " + name + " with params "
                + Arrays.toString(types) + " could be found on type " + type() + ".");
    }

    private void sortMethods(final List<Method> methods) {
        Collections.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                Class<?>[] types1 = o1.getParameterTypes();
                Class<?>[] types2 = o2.getParameterTypes();
                int len = types1.length;
                for (int i = 0; i < len; i++) {
                    if (!types1[i].equals(types2[i])) {
                        if (wrapper(types1[i]).isAssignableFrom(wrapper(types2[i]))) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
                return 0;
            }
        });
    }

    private boolean isSimilarSignature(final Method possiblyMatchingMethod,
                                       final String desiredMethodName,
                                       final Class<?>[] desiredParamTypes) {
        return possiblyMatchingMethod.getName().equals(desiredMethodName)
                && match(possiblyMatchingMethod.getParameterTypes(), desiredParamTypes);
    }

    private boolean match(final Class<?>[] declaredTypes, final Class<?>[] actualTypes) {
        if (declaredTypes.length == actualTypes.length) {
            for (int i = 0; i < actualTypes.length; i++) {
                if (actualTypes[i] == NULL.class
                        || wrapper(declaredTypes[i]).isAssignableFrom(wrapper(actualTypes[i]))) {
                    continue;
                }
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private <T extends AccessibleObject> T accessible(T accessible) {
        if (accessible == null) return null;
        if (accessible instanceof Member) {
            Member member = (Member) accessible;
            if (Modifier.isPublic(member.getModifiers())
                    && Modifier.isPublic(member.getDeclaringClass().getModifiers())) {
                return accessible;
            }
        }
        if (!accessible.isAccessible()) accessible.setAccessible(true);
        return accessible;
    }
///////////////////////////////////////////////////////////////////////////
// proxy
///////////////////////////////////////////////////////////////////////////

    /**
     * Create a proxy for the wrapped object allowing to typesafely invoke
     * methods on it using a custom interface.
     *
     * @param proxyType The interface type that is implemented by the proxy.
     * @return a proxy for the wrapped object
     */
    @SuppressWarnings("unchecked")
    public <P> P proxy(final Class<P> proxyType) {
        final boolean isMap = (object instanceof Map);
        final InvocationHandler handler = new InvocationHandler() {
            @Override
            @SuppressWarnings("null")
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                try {
                    return reflect(object).method(name, args).get();
                } catch (ReflectException e) {
                    if (isMap) {
                        Map<String, Object> map = (Map<String, Object>) object;
                        int length = (args == null ? 0 : args.length);
                        if (length == 0 && name.startsWith("get")) {
                            return map.get(property(name.substring(3)));
                        } else if (length == 0 && name.startsWith("is")) {
                            return map.get(property(name.substring(2)));
                        } else if (length == 1 && name.startsWith("set")) {
                            map.put(property(name.substring(3)), args[0]);
                            return null;
                        }
                    }
                    throw e;
                }
            }
        };
        return (P) Proxy.newProxyInstance(proxyType.getClassLoader(),
                new Class[]{proxyType},
                handler);
    }

    /**
     * Get the POJO property name of an getter/setter
     */
    private static String property(String string) {
        int length = string.length();
        if (length == 0) {
            return "";
        } else if (length == 1) {
            return string.toLowerCase();
        } else {
            return string.substring(0, 1).toLowerCase() + string.substring(1);
        }
    }

    private Class<?> type() {
        return type;
    }

    private Class<?> wrapper(final Class<?> type) {
        if (type == null) {
            return null;
        } else if (type.isPrimitive()) {
            if (boolean.class == type) {
                return Boolean.class;
            } else if (int.class == type) {
                return Integer.class;
            } else if (long.class == type) {
                return Long.class;
            } else if (short.class == type) {
                return Short.class;
            } else if (byte.class == type) {
                return Byte.class;
            } else if (double.class == type) {
                return Double.class;
            } else if (float.class == type) {
                return Float.class;
            } else if (char.class == type) {
                return Character.class;
            } else if (void.class == type) {
                return Void.class;
            }
        }
        return type;
    }

    /**
     * Get the result.
     *
     * @param <T> The value type.
     * @return the result
     */
    @SuppressWarnings("unchecked")
    public <T> T get() {
        return (T) object;
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ReflectUtils && object.equals(((ReflectUtils) obj).get());
    }

    @Override
    public String toString() {
        return object.toString();
    }

    private static class NULL {
    }

    public static class ReflectException extends RuntimeException {
        private static final long serialVersionUID = 858774075258496016L;

        public ReflectException(String message) {
            super(message);
        }

        public ReflectException(String message, Throwable cause) {
            super(message, cause);
        }

        public ReflectException(Throwable cause) {
            super(cause);
        }
    }
}