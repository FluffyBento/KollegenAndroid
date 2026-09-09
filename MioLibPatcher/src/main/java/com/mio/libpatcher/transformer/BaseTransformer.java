package com.mio.libpatcher.transformer;

import com.mio.libpatcher.util.LogUtil;
import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.List;

public interface BaseTransformer extends ClassFileTransformer {
    ClassPool pool = ClassPool.getDefault();

    
    default String getTargetClassName() {
        return "";
    }

    
    default List<String> getTargetClassNames() {
        String name = getTargetClassName();
        if (name.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(name);
    }

    void transform(CtClass clazz) throws Throwable;

    @Override
    default byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!isTargetClass(className)) {
            return classfileBuffer;
        }
        LogUtil.info("Patch target class: " + className);
        CtClass clazz = null;
        try {
            clazz = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
            transform(clazz);
            return clazz.toBytecode();
        } catch (Throwable e) {
            LogUtil.error("Failed to transform class: " + className, e);
        } finally {
            if (clazz != null) {
                clazz.detach();
            }
        }
        return classfileBuffer;
    }

    default boolean isTargetClass(String className) {
        String dottedName = className.replace('/', '.');
        if (getTargetClassName().equals(dottedName)) {
            return true;
        }
        for (String name : getTargetClassNames()) {
            if (name.equals(dottedName)) {
                return true;
            }
        }
        return false;
    }
}
