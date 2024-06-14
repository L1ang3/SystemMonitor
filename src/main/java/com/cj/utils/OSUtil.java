package com.cj.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OperatingSystem.ProcessSort;
import oshi.util.Util;
import oshi.hardware.HWDiskStore;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

/**
 * OS util
 *
 * @author chenjie 2020-08-20
 */
public class OSUtil {
    private static Logger logger = LoggerFactory.getLogger(OSUtil.class);

    private static final SystemInfo SI = new SystemInfo();
    public static final String TWO_DECIMAL = "0.00";
    private static HardwareAbstractionLayer hal = SI.getHardware();
    private static OperatingSystem os = SI.getOperatingSystem();
    private static final int logicalProcessorCount = hal.getProcessor().getLogicalProcessorCount();
    private static final GlobalMemory memory = hal.getMemory();

    /**
     * get cpu usage
     *
     * @return
     */
    public static double cpuUsage() {
        SystemInfo si = new SystemInfo();
        CentralProcessor processor = hal.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
        df.setRoundingMode(RoundingMode.HALF_UP);

        return Double.parseDouble(df.format(cpuUsage));
    }

    /**
     * get mem usage
     *
     * @return
     */
    public static double memUsage() {
        SystemInfo si = new SystemInfo();

        double memUsage = 100d * (memory.getTotal() - memory.getAvailable()) / memory.getTotal();
        DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
        df.setRoundingMode(RoundingMode.HALF_UP);

        return Double.parseDouble(df.format(memUsage));
    }

    /**
     * get process cpu usage
     *
     * @return
     */
    public static double getProcessCpu(int pid) {
        OSProcess p = os.getProcess(pid);
        Util.sleep(1000);
        double cpuUsage = os.getProcess(pid).getProcessCpuLoadBetweenTicks(p);
        cpuUsage = cpuUsage * 100d / logicalProcessorCount;
        return cpuUsage;
    }

    /**
     * get process mem usage
     *
     * @return
     */
    public static double getProcessMem(int pid) {
        OSProcess p = os.getProcess(pid);
        double memUsage = 100d * p.getResidentSetSize() / memory.getTotal();
        return memUsage;
    }

    /**
     * find process pid
     *
     * @return
     */
    public static int findProcessPid(String name) {
        List<OSProcess> processList = os.getProcesses();
        for (OSProcess item : processList) {
            if (name.equals(item.getName())) {
                return item.getProcessID();
            }
        }
        return -1;
    }

    public static String getDiskUsageInfo() {
        StringBuilder diskInfo = new StringBuilder();
        List<HWDiskStore> diskStores = SI.getHardware().getDiskStores();
        for (HWDiskStore disk : diskStores) {
            diskInfo.append("Disk Name: ").append(disk.getName()).append("\n");
            diskInfo.append("Disk Size: ").append(disk.getSize() / (1024 * 1024 * 1024)).append(" GB\n");
            diskInfo.append("Disk Model: ").append(disk.getModel()).append("\n");
            diskInfo.append("Disk Read Bytes: ").append(disk.getReadBytes()).append("\n");
            diskInfo.append("Disk Write Bytes: ").append(disk.getWriteBytes()).append("\n");
            diskInfo.append("Disk Read Transfer Rate: ").append(disk.getTransferTime()).append(" ms\n");

            long freeSpace = disk.getSize() - disk.getWriteBytes();
            double usagePercentage = (double) (disk.getSize() - freeSpace) / disk.getSize() * 100;
            diskInfo.append("Disk Usage: ").append(String.format("%.2f", usagePercentage)).append("%\n\n");
        }
        return diskInfo.toString();
    }

    public static String getNetworkInfo() {
        StringBuilder networkInfo = new StringBuilder();
        List<NetworkIF> networkIFs = SI.getHardware().getNetworkIFs();
        for (NetworkIF net : networkIFs) {
            networkInfo.append("Network Interface Name: ").append(net.getName()).append("\n");
            networkInfo.append("MAC Address: ").append(net.getMacaddr()).append("\n");
            networkInfo.append("IPv4 Address: ").append(net.getIPv4addr().toString()).append("\n");
            networkInfo.append("IPv6 Address: ").append(net.getIPv6addr().toString()).append("\n");
            networkInfo.append("Packets Sent: ").append(net.getPacketsSent()).append("\n");
            networkInfo.append("Packets Received: ").append(net.getPacketsRecv()).append("\n");
            networkInfo.append("Bytes Sent: ").append(net.getBytesSent()).append("\n");
            networkInfo.append("Bytes Received: ").append(net.getBytesRecv()).append("\n");
            networkInfo.append("Speed: ").append(net.getSpeed()).append(" bps\n");
        }
        return networkInfo.toString();
    }

    public static String getTCPConnectionInfo() {
        StringBuilder tcpConnectionInfo = new StringBuilder();
        OperatingSystem os = SI.getOperatingSystem();
        List<OSProcess> processes = os.getProcesses(0, ProcessSort.CPU);

        for (OSProcess process : processes) {
            if (process.getName().equals("tcp") || process.getName().equals("tcp6")) {
                tcpConnectionInfo.append("Process ID: ").append(process.getProcessID()).append("\n");
                tcpConnectionInfo.append("Process Name: ").append(process.getName()).append("\n");
                tcpConnectionInfo.append("User: ").append(process.getUser()).append("\n");
            }
        }
        return tcpConnectionInfo.toString();
    }

    public static String getOperatingSystemInfo() {
        OperatingSystem os = SI.getOperatingSystem();
        return "Operating System: " + os.toString() + '\n';
    }
}
