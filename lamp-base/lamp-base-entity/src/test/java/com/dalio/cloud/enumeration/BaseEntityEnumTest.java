package com.dalio.cloud.enumeration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.dalio.cloud.base.enumeration.system.LogType;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.msg.enumeration.MsgInterfaceLoggingStatusEnum;
import com.dalio.cloud.msg.enumeration.MsgTemplateTypeEnum;
import com.dalio.cloud.msg.enumeration.NoticeRemindModeEnum;
import com.dalio.cloud.msg.enumeration.SourceType;
import com.dalio.cloud.msg.enumeration.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 基础实体模块枚举单元测试
 *
 * @author went
 */
class BaseEntityEnumTest {

    @Test
    @DisplayName("测试 FileStorageType 枚举")
    void testFileStorageType() {
        for (FileStorageType type : FileStorageType.values()) {
            assertNotNull(type.getDesc());
            assertEquals(type.name(), type.getCode());
            assertEquals(type, FileStorageType.valueOf(type.name()));
            assertEquals(type, FileStorageType.get(type.name()));
            assertEquals(type, FileStorageType.match(type.name().toLowerCase(), null));
            assertTrue(type.eq(type));
            assertFalse(type.eq((FileStorageType) null));
            assertFalse(type.eq((String) null));
            assertTrue(type.eq(type.name()));
        }
        assertNull(FileStorageType.get("NON_EXISTENT"));
        assertEquals(FileStorageType.LOCAL, FileStorageType.match("UNKNOWN", FileStorageType.LOCAL));
    }

    @Test
    @DisplayName("测试 NoticeRemindModeEnum 枚举")
    void testNoticeRemindModeEnum() {
        for (NoticeRemindModeEnum mode : NoticeRemindModeEnum.values()) {
            assertNotNull(mode.getDesc());
            assertNotNull(mode.getValue());
            assertEquals(mode.getValue(), mode.getCode());
            assertEquals(mode, NoticeRemindModeEnum.valueOf(mode.name()));
            assertEquals(mode, NoticeRemindModeEnum.get(mode.name()));
            assertEquals(mode, NoticeRemindModeEnum.match(mode.name().toLowerCase(), null));
            assertFalse(mode.eq(mode));
            assertFalse(mode.eq((NoticeRemindModeEnum) null));
            assertFalse(mode.eq((String) null));
            assertTrue(mode.eq(mode.getCode()));
        }
        assertNull(NoticeRemindModeEnum.get("NON_EXISTENT"));
        assertEquals(NoticeRemindModeEnum.NOTICE, NoticeRemindModeEnum.match("UNKNOWN", NoticeRemindModeEnum.NOTICE));
    }

    @Test
    @DisplayName("测试 MsgTemplateTypeEnum 枚举")
    void testMsgTemplateTypeEnum() {
        for (MsgTemplateTypeEnum type : MsgTemplateTypeEnum.values()) {
            assertNotNull(type.getDesc());
            assertNotNull(type.getValue());
            assertEquals(type.getValue(), type.getCode());
            assertEquals(type, MsgTemplateTypeEnum.valueOf(type.name()));
            assertEquals(type, MsgTemplateTypeEnum.get(type.name()));
            assertEquals(type, MsgTemplateTypeEnum.match(type.name().toLowerCase(), null));
            assertFalse(type.eq(type));
            assertFalse(type.eq((MsgTemplateTypeEnum) null));
            assertFalse(type.eq((String) null));
            assertTrue(type.eq(type.getCode()));
        }
        assertNull(MsgTemplateTypeEnum.get("NON_EXISTENT"));
        assertEquals(MsgTemplateTypeEnum.SMS, MsgTemplateTypeEnum.match("UNKNOWN", MsgTemplateTypeEnum.SMS));
    }

    @Test
    @DisplayName("测试 TaskStatus 枚举")
    void testTaskStatus() {
        for (TaskStatus status : TaskStatus.values()) {
            assertNotNull(status.getDesc());
            assertNotNull(status.getExtra());
            assertEquals(status.name(), status.getCode());
            assertEquals(status.name(), status.getValue());
            assertEquals(status, TaskStatus.valueOf(status.name()));
            assertEquals(status, TaskStatus.get(status.name()));
            assertEquals(status, TaskStatus.match(status.name().toLowerCase(), null));
            assertTrue(status.eq(status));
            assertFalse(status.eq((TaskStatus) null));
            assertFalse(status.eq((String) null));
            assertTrue(status.eq(status.name()));
        }
        assertNull(TaskStatus.get("NON_EXISTENT"));
        assertEquals(TaskStatus.DRAFT, TaskStatus.match("UNKNOWN", TaskStatus.DRAFT));
    }

    @Test
    @DisplayName("测试 MsgInterfaceLoggingStatusEnum 枚举")
    void testMsgInterfaceLoggingStatusEnum() {
        for (MsgInterfaceLoggingStatusEnum status : MsgInterfaceLoggingStatusEnum.values()) {
            assertNotNull(status.getDesc());
            assertNotNull(status.getValue());
            assertEquals(status.name(), status.getCode());
            assertEquals(status, MsgInterfaceLoggingStatusEnum.valueOf(status.name()));
            assertEquals(status, MsgInterfaceLoggingStatusEnum.get(status.name()));
            assertEquals(status, MsgInterfaceLoggingStatusEnum.match(status.name().toLowerCase(), null));
            assertTrue(status.eq(status));
            assertFalse(status.eq((MsgInterfaceLoggingStatusEnum) null));
            assertFalse(status.eq((String) null));
            assertTrue(status.eq(status.name()));
        }
        assertNull(MsgInterfaceLoggingStatusEnum.get("NON_EXISTENT"));
        assertEquals(MsgInterfaceLoggingStatusEnum.INIT, MsgInterfaceLoggingStatusEnum.match("UNKNOWN", MsgInterfaceLoggingStatusEnum.INIT));
    }

    @Test
    @DisplayName("测试 SourceType 枚举")
    void testSourceType() {
        for (SourceType source : SourceType.values()) {
            assertNotNull(source.getDesc());
            assertEquals(source.name(), source.getCode());
            assertEquals(source.name(), source.getValue());
            assertEquals(source, SourceType.valueOf(source.name()));
            assertEquals(source, SourceType.get(source.name()));
            assertEquals(source, SourceType.match(source.name().toLowerCase(), null));
            assertTrue(source.eq(source));
            assertFalse(source.eq((SourceType) null));
            assertFalse(source.eq((String) null));
            assertTrue(source.eq(source.name()));
        }
        assertNull(SourceType.get("NON_EXISTENT"));
        assertEquals(SourceType.APP, SourceType.match("UNKNOWN", SourceType.APP));
    }

    @Test
    @DisplayName("测试 LogType 枚举")
    void testLogType() {
        for (LogType logType : LogType.values()) {
            assertNotNull(logType.getDesc());
            assertEquals(logType.name(), logType.getCode());
            assertEquals(logType, LogType.valueOf(logType.name()));
            assertEquals(logType, LogType.get(logType.name()));
            assertEquals(logType, LogType.match(logType.name().toLowerCase(), null));
            assertTrue(logType.eq(logType));
            assertFalse(logType.eq((LogType) null));
            assertFalse(logType.eq((String) null));
            assertTrue(logType.eq(logType.name()));
        }
        assertNull(LogType.get("NON_EXISTENT"));
        assertEquals(LogType.OPT, LogType.match("UNKNOWN", LogType.OPT));
    }
}
