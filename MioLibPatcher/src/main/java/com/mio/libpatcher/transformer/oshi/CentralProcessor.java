package com.mio.libpatcher.transformer.oshi;

import com.mio.libpatcher.transformer.BaseTransformer;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.util.Arrays;
import java.util.List;


public class CentralProcessor implements BaseTransformer {
    @Override
    public List<String> getTargetClassNames() {
        return Arrays.asList(
                "oshi.software.os.linux.proc.CentralProcessor",
                "oshi.software.os.linux.LinuxHardwareAbstractionLayer",
                "oshi.hardware.platform.linux.LinuxCentralProcessor");
    }

    @Override
    public void transform(CtClass clazz) throws Throwable {
        String name = clazz.getName();
        if (name.equals("oshi.software.os.linux.proc.CentralProcessor")) {
            transformOshi1x(clazz);
        } else if (name.equals("oshi.software.os.linux.LinuxHardwareAbstractionLayer")) {
            transformOshi1xHal(clazz);
        } else {
            transformOshi6x(clazz);
        }
    }

    
    private static void transformOshi1x(CtClass clazz) throws Throwable {
        CtMethod nameMethod = clazz.getDeclaredMethod("getName");
        nameMethod.setBody("{return System.getProperty(\"cpu.name\",\"\");}");
        
        
        try {
            CtConstructor ctor = clazz.getDeclaredConstructor(new CtClass[]{CtClass.intType});
            ctor.setBody("{this.processorNumber = $1;"
                    + "this.curProcTicks = new long[4];"
                    + "this.prevProcTicks = new long[4];"
                    + "this.procTickTime = System.currentTimeMillis();}");
        } catch (NotFoundException ignored) {
            
        }
        
        try {
            CtMethod countMethod = clazz.getDeclaredMethod("getLogicalProcessorCount");
            countMethod.setBody("{return java.lang.Runtime.getRuntime().availableProcessors();}");
        } catch (NotFoundException ignored) {
            
        }
    }

    
    private static void transformOshi1xHal(CtClass clazz) throws Throwable {
        try {
            clazz.getDeclaredMethod("getProcessors");
        } catch (NotFoundException e) {
            return; 
        }
        
        boolean intCtor = false;
        try {
            CtClass cpuClass = clazz.getClassPool().get("oshi.software.os.linux.proc.CentralProcessor");
            cpuClass.getDeclaredConstructor(new CtClass[]{CtClass.intType});
            intCtor = true;
        } catch (NotFoundException ignored) {
            
        }
        String ctor = intCtor
                ? "new oshi.software.os.linux.proc.CentralProcessor(i)"
                : "new oshi.software.os.linux.proc.CentralProcessor()";
        String body = "{"
                + "int n = java.lang.Runtime.getRuntime().availableProcessors();"
                + "oshi.hardware.Processor[] arr = new oshi.hardware.Processor[n];"
                + "for (int i = 0; i < n; i++) { arr[i] = " + ctor + "; }"
                + "return arr;"
                + "}";
        CtMethod method = clazz.getDeclaredMethod("getProcessors");
        method.setBody(body);
    }

    
    private static void transformOshi6x(CtClass clazz) throws Throwable {
        CtMethod method = clazz.getDeclaredMethod("initProcessorCounts");
        
        
        String returnType = method.getReturnType().getName();
        String tupleCtor;
        if (returnType.endsWith("Quartet")) {
            tupleCtor = "new oshi.util.tuples.Quartet(logProcs, null, null, new java.util.ArrayList())";
        } else if (returnType.endsWith("Triplet")) {
            tupleCtor = "new oshi.util.tuples.Triplet(logProcs, null, null)";
        } else {
            tupleCtor = "new oshi.util.tuples.Pair(logProcs, null)";
        }
        String body = "{"
                + "java.util.List logProcs = new java.util.ArrayList();"
                + "int n = java.lang.Runtime.getRuntime().availableProcessors();"
                + "for (int i = 0; i < n; i++) {"
                + "logProcs.add(new oshi.hardware.CentralProcessor$LogicalProcessor(i, i, 0));"
                + "}"
                + "return " + tupleCtor + ";"
                + "}";
        method.setBody(body);
    }
}
