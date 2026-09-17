package com.dalio.cloud.job.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * XxlJobInfoVO 单元测试
 *
 * @author went
 */
class XxlJobInfoVOTest {

    @Test
    @DisplayName("测试 XxlJobInfoVO 静态工厂构建及 getter/setter 方法")
    void testCreateAndGettersSetters() {
        LocalDateTime now = LocalDateTime.now();
        XxlJobInfoVO vo = XxlJobInfoVO.create("testGroup", "testDesc", now, "testHandler", "testParam");

        assertNotNull(vo);
        assertEquals("testGroup", vo.getJobGroupName());
        assertEquals("testDesc", vo.getJobDesc());
        assertEquals("admin", vo.getAuthor());
        assertEquals("", vo.getAlarmEmail());
        assertEquals("CRON", vo.getScheduleType());
        assertNotNull(vo.getScheduleTime());
        assertEquals("DO_NOTHING", vo.getMisfireStrategy());
        assertEquals("FIRST", vo.getExecutorRouteStrategy());
        assertEquals("testHandler", vo.getExecutorHandler());
        assertEquals("testParam", vo.getExecutorParam());
        assertEquals("SERIAL_EXECUTION", vo.getExecutorBlockStrategy());
        assertEquals(Integer.valueOf(-1), vo.getExecutorTimeout());
        assertEquals(Integer.valueOf(-1), vo.getExecutorFailRetryCount());
        assertEquals("BEAN", vo.getGlueType());

        // 测试链式 setter 和 getter
        Date date = new Date();
        vo.setAuthor("user1")
                .setAlarmEmail("test@example.com")
                .setScheduleType("FIX_RATE")
                .setScheduleConf("1000")
                .setMisfireStrategy("FIRE_ONCE_NOW")
                .setExecutorRouteStrategy("RANDOM")
                .setExecutorHandler("newHandler")
                .setExecutorParam("newParam")
                .setExecutorBlockStrategy("DISCARD_LATER")
                .setExecutorTimeout(60)
                .setExecutorFailRetryCount(3)
                .setGlueType("GLUE_GROOVY")
                .setGlueSource("println 'hello'")
                .setGlueRemark("remark")
                .setGlueupdatedTime(date)
                .setChildJobId("1,2,3")
                .setScheduleTime("2026-09-16 22:00:00")
                .setJobGroupName("group2")
                .setJobDesc("desc2");

        assertEquals("user1", vo.getAuthor());
        assertEquals("test@example.com", vo.getAlarmEmail());
        assertEquals("FIX_RATE", vo.getScheduleType());
        assertEquals("1000", vo.getScheduleConf());
        assertEquals("FIRE_ONCE_NOW", vo.getMisfireStrategy());
        assertEquals("RANDOM", vo.getExecutorRouteStrategy());
        assertEquals("newHandler", vo.getExecutorHandler());
        assertEquals("newParam", vo.getExecutorParam());
        assertEquals("DISCARD_LATER", vo.getExecutorBlockStrategy());
        assertEquals(Integer.valueOf(60), vo.getExecutorTimeout());
        assertEquals(Integer.valueOf(3), vo.getExecutorFailRetryCount());
        assertEquals("GLUE_GROOVY", vo.getGlueType());
        assertEquals("println 'hello'", vo.getGlueSource());
        assertEquals("remark", vo.getGlueRemark());
        assertEquals(date, vo.getGlueupdatedTime());
        assertEquals("1,2,3", vo.getChildJobId());
        assertEquals("2026-09-16 22:00:00", vo.getScheduleTime());
        assertEquals("group2", vo.getJobGroupName());
        assertEquals("desc2", vo.getJobDesc());

        String str = vo.toString();
        assertNotNull(str);
        assertTrue(str.contains("jobGroupName=group2"));
    }
}
