package com.mio.libpatcher.transformer;

import java.util.ArrayList;
import java.util.List;

import com.mio.libpatcher.util.LogUtil;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Opcode;


public class ASMTransformer implements BaseTransformer {

    private static Boolean isASM504Result;
    
    @Override
    public List<String> getTargetClassNames() {
        List<String> list = new ArrayList<>();
        
        if (!isASM504()) return list;
        list.add("org.objectweb.asm.ClassVisitor");
        list.add("org.objectweb.asm.MethodVisitor");
        list.add("org.objectweb.asm.FieldVisitor");
        list.add("org.objectweb.asm.AnnotationVisitor");
        list.add("org.objectweb.asm.signature.SignatureVisitor");
        return list;
    }

    
    @Override
    public void transform(CtClass clazz) throws CannotCompileException {
        for (CtConstructor ctor : clazz.getDeclaredConstructors()) {
            if (!ctor.isClassInitializer()) {
                CodeIterator it = ctor.getMethodInfo().getCodeAttribute().iterator();
                
                
                
                while (it.hasNext()) {
                    try {
                        int pos = it.next();

                        if (it.byteAt(pos) != Opcode.NEW) continue;

                        int dup = it.next();
                        if (it.byteAt(dup) != Opcode.DUP) continue;

                        int invokespecial = it.next();
                        if (it.byteAt(invokespecial) != Opcode.INVOKESPECIAL) continue;

                        int athrow = it.next();
                        if (it.byteAt(athrow) != Opcode.ATHROW) continue;


                        
                        
                        for (int i = pos; i < athrow + 1; ++i) {
                            it.writeByte(Opcode.NOP, i);
                        }
                        break;
                    } catch (BadBytecode e) {
                        throw new CannotCompileException(
                                "Failed to parse bytecode while searching for the" +
                                        "IllegalArgumentException pattern, is this ASM 5.0.4?", e
                        );
                    }
                }
            }
        }
    }

    private boolean isASM504() {
        
        String override = System.getProperty("miolibpatcher.asmBackport");
        if (override != null) {
            return Boolean.parseBoolean(override);
        }
        if (isASM504Result == null) {
            isASM504Result = detectASM504();
        }
        return isASM504Result;
    }

    private static boolean detectASM504() {
        try {
            
            
            Class<?> asmClass = Class.forName("org.objectweb.asm.ClassReader", false, ClassLoader.getSystemClassLoader());
            Package asmPackage = asmClass.getPackage();
            String implVersion = asmPackage.getImplementationVersion();
            return "5.0.4".equals(implVersion);
        } catch (Exception e) {
            LogUtil.info("Unable to get ASM version info, ASMTransformer patch will be skipped: " + e);
        }
        return false;
    }
}
