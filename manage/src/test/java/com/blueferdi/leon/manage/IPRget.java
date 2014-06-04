package com.blueferdi.leon.manage;

import java.io.PrintStream;
import java.lang.management.*;
import java.util.List;

public class IPRget 
{
    private static final int KB = 1024;
    private static final int MB = 1024 * KB;
    private static final int GB = 1024 * MB;
    
    public static void main(String[] args)
    {
        PrintStream out = System.out;
        
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();
        CompilationMXBean compilation = ManagementFactory.getCompilationMXBean();
        List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        List<MemoryManagerMXBean> mmbeans = ManagementFactory.getMemoryManagerMXBeans();
        List<MemoryPoolMXBean> mpbeans = ManagementFactory.getMemoryPoolMXBeans();
        RuntimeMXBean rtbean = ManagementFactory.getRuntimeMXBean();
        ThreadMXBean threadbean = ManagementFactory.getThreadMXBean();

//        HotSpotDiagnosticMXBean diagnos = ManagementFactory.getDiagnosticMXBean();  
//        HotspotClassLoadingMBean hotspotcl = ManagementFactory.getHotspotClassLoadingMBean();
//        HotspotCompilationMBean hotspotcompilation = ManagementFactory.getHotspotCompilationMBean();
//        HotspotMemoryMBean hotspotmemory = ManagementFactory.getHotspotMemoryMBean();
//        HotspotRuntimeMBean hotspotrt = ManagementFactory.getHotspotRuntimeMBean();
//        HotspotThreadMBean hotspotthread = ManagementFactory.getHotspotThreadMBean();
        
        out.println("operating system" + "\nOS NAME: " + os.getName() + "\nOS ARCH: " + os.getArch() + "\nOS VERSION: " + os.getVersion() + "\nPROCESSORS: "
                + os.getAvailableProcessors() + "\nLOADAVERAGE: " + os.getSystemLoadAverage() + "\n");
        
        out.println("class loading\nLOAD CLASS: " + cl.getLoadedClassCount() + "\nTOTAL LOAD CLASS: " + cl.getTotalLoadedClassCount() + "\nUNLOAD CLASS: "
                + cl.getUnloadedClassCount() + "\n");
        
        out.println("compilation\nNAME: " + compilation.getName() + "\nTOTAL TIME: " + compilation.getTotalCompilationTime() + "\nMONITOR SUPPORTED: "
                + compilation.isCompilationTimeMonitoringSupported() + "\n");
        
        out.println("GC");
        for(GarbageCollectorMXBean gc : gcs)
        {
            out.println("NAME: " + gc.getName() + "\nMEMORY POOL: " + IPRget.arrayToStr(gc.getMemoryPoolNames()) + "\nCOUNT: "
                    + gc.getCollectionCount() + "\nTIME: " + gc.getCollectionTime() + "\nISVALID: " + gc.isValid() + "\n");
        }
        
        out.println("memory\nHEAP USAGE: " + usageToString(memory.getHeapMemoryUsage()) + "\nNONHEAP USAGE: "
                + usageToString(memory.getNonHeapMemoryUsage()) + "\nOBJECT PENDING COUNT: " + memory.getObjectPendingFinalizationCount()
                + "\nISVERBOSE: " + memory.isVerbose() + "\n");
        
        out.println("MemoryManager");
        for(MemoryManagerMXBean bean : mmbeans)
        {
            out.println("NAME: " + bean.getName() + "\nMEMORY POOL: " + arrayToStr(bean.getMemoryPoolNames()) + "\nisValid: " + bean.isValid() + "\n");
        }
        
        out.println("MemoryPool");
        for(MemoryPoolMXBean pool : mpbeans)
        {
            out.println("NAME: " + pool.getName() + "\nUSAGE: " + usageToString(pool.getUsage()) + "\nUSAGETHRESHOLD: "  + "\nUSAGETHRESHOLDCOUNT: "  +
                    "\nCOLLECTIONUSAGE: " + usageToString(pool.getCollectionUsage()) + "\nCOLLECTIONTHRESHOLD: " + "\nCOLLECTIONTHRESHOLDCOUNT: "  +
                    "\nPEAKUSAGE: " + usageToString(pool.getPeakUsage()) + "\nTYPE: " + pool.getType().name() +
                    "\nCOLLECTIONUSAGETHRESHOLDEXCEEDED: " + "\nCOLLECTIONTHRESHOLDSUPPORTED: " + pool.isCollectionUsageThresholdSupported() +
                    "\nUSAGETHRESHOLDEXCEEDED: " + "\nUSAGETHRESHOLDSUPPORT: " + pool.isUsageThresholdSupported() + 
                    "\nISVALID: " + pool.isValid() + "\n");
        }
        
        out.println("RUNTIME\nNAME: " + rtbean.getName() + "\nVMNAME: " + rtbean.getVmName() + "\nVMVENDOR: " + rtbean.getVmVendor() + "\nVMVERSION: " + rtbean.getVmVersion() + 
                "\nSPECNAME: " + rtbean.getSpecName() + "\nSPECVENDOR: " + rtbean.getSpecVendor() + "\nSPECVERSION: " + rtbean.getSpecVersion() + 
                "\nPARAMS: " + rtbean.getInputArguments() + "\nSTARTTIME: " + rtbean.getStartTime() + "\nUPTIME: " + rtbean.getUptime() + "\n");
        
        out.println("THREAD\nCOUNT: " + threadbean.getTotalStartedThreadCount() + "\n");
        
    }
    
    public static String arrayToStr(Object[] array)
    {
        StringBuilder builder = new StringBuilder();
        
        for(Object o : array)
        {
            builder.append(o.toString()).append(", ");
        }
        
        return builder.substring(0, builder.lastIndexOf(","));
    }
    
    public static String usageToString(MemoryUsage usage)
    {
        if(usage == null)
            return "";
        
        StringBuilder builder = new StringBuilder();
        
        builder.append(" commmited: ").append(usage.getCommitted()/MB)
                .append(" init: ").append(usage.getInit()/MB)
                .append(" max: ").append(usage.getMax()/MB)
                .append(" used: ").append(usage.getUsed()/MB);
        
        return builder.toString();
    }
}
