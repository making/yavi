package am.ik.yavi.builder;

import java.io.Serializable;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;

class LambdaUtils {

    /**
     * If the Function is a simple lambda method reference (e.g. `UserDto::getName`),
     * it returns the method itself (`UserDto.getName()`).
     * Original source code found here
     * https://habr.com/ru/post/522774/
     *
     * @param lambda method reference. It should be {@link Serializable} - this way it can be converted to
     *               {@link SerializedLambda} to extract method signatures
     * @return method reference if resolved, otherwise null
     */
    @Nullable
    public static Method unreferenceLambdaMethod(Serializable lambda) {
        SerializedLambda serializedLambda = getSerializedLambda(lambda);
        if (serializedLambda != null
                && (serializedLambda.getImplMethodKind() == MethodHandleInfo.REF_invokeVirtual
                || serializedLambda.getImplMethodKind() == MethodHandleInfo.REF_invokeStatic)) {
            Class<?> cls = implClassForName(serializedLambda.getImplClass());
            Class<?>[] argumentClasses = parseArgumentClasses(serializedLambda.getImplMethodSignature());
            return Stream.of(cls.getDeclaredMethods())
                    .filter(method -> method.getName().equals(serializedLambda.getImplMethodName())
                            && Arrays.equals(method.getParameterTypes(), argumentClasses))
                    .findFirst().orElse(null);
        }
        return null;
    }

    /**
     * Parses impl method signature: returns array of argument classes.
     *
     * @param implMethodSignature value of serializedLambda.getImplMethodSignature()
     *                            (see TestLambdaUtilsTest for examples)
     * @return array of parsed classes
     */
    static Class<?>[] parseArgumentClasses(String implMethodSignature) {
        int parenthesesPos = implMethodSignature.indexOf(')');

        if (!implMethodSignature.startsWith("(") && parenthesesPos > 0) {
            throw new IllegalStateException("Wrong format of implMethodSignature " + implMethodSignature);
        }
        String argGroup = implMethodSignature.substring(1, parenthesesPos);
        List<Class<?>> classes = new ArrayList<>();
        for (String token : argGroup.split(";")) {
            if (token.isEmpty()) {
                continue;
            }
            classes.add(parseType(token, false));
        }
        return classes.toArray(new Class[0]);
    }

    private static Class<?> parseType(String typeName, boolean allowVoid) {
        if ("Z".equals(typeName)) {
            return boolean.class;
        } else if ("B".equals(typeName)) {
            return byte.class;
        } else if ("C".equals(typeName)) {
            return char.class;
        } else if ("S".equals(typeName)) {
            return short.class;
        } else if ("I".equals(typeName)) {
            return int.class;
        } else if ("J".equals(typeName)) {
            return long.class;
        } else if ("F".equals(typeName)) {
            return float.class;
        } else if ("D".equals(typeName)) {
            return double.class;
        } else if ("V".equals(typeName)) {
            if (allowVoid) {
                return void.class;
            } else {
                throw new IllegalStateException("void (V) type is not allowed");
            }
        } else {
            if (!typeName.startsWith("L")) {
                throw new IllegalStateException("Wrong format of argument type "
                        + "(should start with 'L'): " + typeName);
            }
            String implClassName = typeName.substring(1);
            return implClassForName(implClassName);
        }
    }

    @Nullable
    private static SerializedLambda getSerializedLambda(Serializable lambda) {
        for (Class<?> cl = lambda.getClass(); cl != null; cl = cl.getSuperclass()) {
            try {
                Method m = cl.getDeclaredMethod("writeReplace");
                m.setAccessible(true);
                Object replacement = m.invoke(lambda);
                if (!(replacement instanceof SerializedLambda)) {
                    break;
                }
                return (SerializedLambda) replacement;
            } catch (NoSuchMethodException e) {
                // skip, continue
            } catch (IllegalAccessException | InvocationTargetException | SecurityException e) {
                throw new IllegalStateException("Failed to call writeReplace", e);
            }
        }
        return null;
    }

    private static Class<?> implClassForName(String implClassName) {
        String className = implClassName.replace('/', '.');
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load class " + implClassName, e);
        }
    }

}
