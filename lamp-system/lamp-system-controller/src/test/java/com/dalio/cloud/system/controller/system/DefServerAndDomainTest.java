package com.dalio.cloud.system.controller.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.dalio.basic.base.R;
import com.dalio.cloud.system.controller.system.domain.Server;
import com.dalio.cloud.system.controller.system.domain.server.Cpu;
import com.dalio.cloud.system.controller.system.domain.server.Jvm;
import com.dalio.cloud.system.controller.system.domain.server.Mem;
import com.dalio.cloud.system.controller.system.domain.server.Sys;
import com.dalio.cloud.system.controller.system.domain.server.SysFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DefServerController 与 Server 监控领域对象单元测试
 */
class DefServerAndDomainTest {

    @Test
    @DisplayName("测试 DefServerController 与 Server.copyTo() 系统信息采样")
    void testDefServerController() {
        DefServerController controller = new DefServerController();
        R<Server> result = controller.server();
        assertNotNull(result);
        assertTrue(result.getIsSuccess());

        Server server = result.getData();
        assertNotNull(server);
        assertNotNull(server.getCpu());
        assertNotNull(server.getMem());
        assertNotNull(server.getJvm());
        assertNotNull(server.getSys());
        assertNotNull(server.getSysFiles());
    }

    @Test
    @DisplayName("测试 Cpu 实体 Getter/Setter 及计算逻辑")
    void testCpu() {
        Cpu cpu = new Cpu();
        cpu.setCpuNum(8);
        assertEquals(8, cpu.getCpuNum());

        // total <= 0
        cpu.setTotal(0.0);
        assertEquals(0.0, cpu.getSys());
        assertEquals(0.0, cpu.getUsed());
        assertEquals(0.0, cpu.getWait());
        assertEquals(0.0, cpu.getFree());

        // total > 0
        cpu.setTotal(0.8);
        cpu.setSys(0.2);
        cpu.setUsed(0.4);
        cpu.setWait(0.1);
        cpu.setFree(0.1);

        assertEquals(80.0, cpu.getTotal());
        assertTrue(cpu.getSys() > 0);
        assertTrue(cpu.getUsed() > 0);
        assertTrue(cpu.getWait() > 0);
        assertTrue(cpu.getFree() > 0);
    }

    @Test
    @DisplayName("测试 Mem 实体 Getter/Setter 及计算逻辑")
    void testMem() {
        Mem mem = new Mem();
        mem.setTotal(16L * 1024 * 1024 * 1024);
        mem.setUsed(8L * 1024 * 1024 * 1024);
        mem.setFree(8L * 1024 * 1024 * 1024);

        assertEquals(16.0, mem.getTotal());
        assertEquals(8.0, mem.getUsed());
        assertEquals(8.0, mem.getFree());
        assertEquals(50.0, mem.getUsage());
    }

    @Test
    @DisplayName("测试 Jvm 实体 Getter/Setter 及运行时信息计算")
    void testJvm() {
        Jvm jvm = new Jvm();
        jvm.setTotal(512L * 1024 * 1024);
        jvm.setMax(1024L * 1024 * 1024);
        jvm.setFree(256L * 1024 * 1024);
        jvm.setVersion("17");
        jvm.setHome("/usr/lib/jvm");

        assertEquals(512.0, jvm.getTotal());
        assertEquals(1024.0, jvm.getMax());
        assertEquals(256.0, jvm.getFree());
        assertEquals(256.0, jvm.getUsed());
        assertEquals(50.0, jvm.getUsage());
        assertEquals("17", jvm.getVersion());
        assertEquals("/usr/lib/jvm", jvm.getHome());
        assertNotNull(jvm.getName());
        assertNotNull(jvm.getStartTime());
        assertNotNull(jvm.getRunTime());
        assertNotNull(jvm.getInputArgs());
    }

    @Test
    @DisplayName("测试 Sys 与 SysFile 实体")
    void testSysAndSysFile() {
        Sys sys = new Sys();
        sys.setComputerName("host-01");
        sys.setComputerIp("192.168.1.100");
        sys.setUserDir("/app");
        sys.setOsName("Linux");
        sys.setOsArch("x86_64");

        assertEquals("host-01", sys.getComputerName());
        assertEquals("192.168.1.100", sys.getComputerIp());
        assertEquals("/app", sys.getUserDir());
        assertEquals("Linux", sys.getOsName());
        assertEquals("x86_64", sys.getOsArch());

        SysFile sysFile = new SysFile();
        sysFile.setDirName("/data");
        sysFile.setSysTypeName("ext4");
        sysFile.setTypeName("本地固定磁盘");
        sysFile.setTotal("100GB");
        sysFile.setFree("40GB");
        sysFile.setUsed("60GB");
        sysFile.setUsage(60.0);

        assertEquals("/data", sysFile.getDirName());
        assertEquals("ext4", sysFile.getSysTypeName());
        assertEquals("本地固定磁盘", sysFile.getTypeName());
        assertEquals("100GB", sysFile.getTotal());
        assertEquals("40GB", sysFile.getFree());
        assertEquals("60GB", sysFile.getUsed());
        assertEquals(60.0, sysFile.getUsage());

        Server server = new Server();
        server.setCpu(new Cpu());
        server.setMem(new Mem());
        server.setJvm(new Jvm());
        server.setSys(sys);
        server.setSysFiles(List.of(sysFile));
        assertEquals(sys, server.getSys());
        assertEquals(1, server.getSysFiles().size());
    }
}
