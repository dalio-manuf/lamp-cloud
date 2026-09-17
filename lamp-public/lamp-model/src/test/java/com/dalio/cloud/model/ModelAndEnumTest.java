package com.dalio.cloud.model;

import com.dalio.basic.context.ContextUtil;
import com.dalio.cloud.model.constant.Condition;
import com.dalio.cloud.model.constant.EchoApi;
import com.dalio.cloud.model.constant.EchoDictType;
import com.dalio.cloud.model.entity.base.SysEmployee;
import com.dalio.cloud.model.entity.base.SysOrg;
import com.dalio.cloud.model.entity.base.SysPosition;
import com.dalio.cloud.model.entity.base.SysRole;
import com.dalio.cloud.model.entity.system.SysResource;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.model.enumeration.*;
import com.dalio.cloud.model.enumeration.base.*;
import com.dalio.cloud.model.enumeration.system.*;
import com.dalio.cloud.model.vo.BaseEventVO;
import com.dalio.cloud.model.vo.result.AppendixResultVO;
import com.dalio.cloud.model.vo.result.Option;
import com.dalio.cloud.model.vo.result.ResourceApiVO;
import com.dalio.cloud.model.vo.result.UserQuery;
import com.dalio.cloud.model.vo.save.AppendixSaveVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * lamp-model 模块枚举与通用模型全面测试
 */
class ModelAndEnumTest {

    @Test
    @DisplayName("测试常用枚举匹配与方法")
    void testEnums() {
        // BooleanEnum
        assertTrue(BooleanEnum.TRUE.eq(true));
        assertTrue(BooleanEnum.TRUE.eq(1));
        assertTrue(BooleanEnum.TRUE.eq("1"));
        assertFalse(BooleanEnum.TRUE.eq(false));
        assertEquals("true", BooleanEnum.TRUE.getCode());
        assertEquals(BooleanEnum.TRUE, BooleanEnum.valueOf("TRUE"));
        assertEquals(BooleanEnum.FALSE, BooleanEnum.valueOf("FALSE"));
        assertTrue(BooleanEnum.TRUE.eq(BooleanEnum.TRUE));

        // HttpMethod
        assertEquals(HttpMethod.GET, HttpMethod.get("GET"));
        assertEquals(HttpMethod.POST, HttpMethod.get("POST"));
        assertEquals(HttpMethod.ALL, HttpMethod.get("ALL"));
        assertEquals("GET", HttpMethod.GET.getCode());
        assertEquals("GET", HttpMethod.GET.getValue());
        assertTrue(HttpMethod.GET.eq(HttpMethod.GET));

        // OrgTypeEnum
        assertEquals(OrgTypeEnum.COMPANY, OrgTypeEnum.get("10"));
        assertEquals(OrgTypeEnum.COMPANY, OrgTypeEnum.get("COMPANY"));
        assertEquals(OrgTypeEnum.DEPT, OrgTypeEnum.get("20"));
        assertTrue(OrgTypeEnum.COMPANY.eq(OrgTypeEnum.COMPANY));

        // Sex
        assertEquals(Sex.M, Sex.get("1"));
        assertEquals(Sex.W, Sex.get("2"));
        assertTrue(Sex.M.eq(Sex.M));

        // StateEnum
        assertEquals(StateEnum.ENABLE, StateEnum.match("1"));
        assertEquals(StateEnum.DISABLE, StateEnum.match("0"));
        assertTrue(StateEnum.ENABLE.eq(true));
        assertTrue(StateEnum.ENABLE.eq(1));
        assertTrue(StateEnum.ENABLE.eq("1"));

        // ActiveStatusEnum & UserStatusEnum & RoleCategoryEnum
        assertEquals(ActiveStatusEnum.ACTIVATED, ActiveStatusEnum.get("20"));
        assertEquals(ActiveStatusEnum.NOT_ACTIVE, ActiveStatusEnum.match("10", null));
        assertTrue(ActiveStatusEnum.ACTIVATED.eq(ActiveStatusEnum.ACTIVATED));

        assertEquals(UserStatusEnum.NORMAL, UserStatusEnum.get("0"));
        assertTrue(UserStatusEnum.NORMAL.eq(UserStatusEnum.NORMAL));

        assertEquals(RoleCategoryEnum.FUNCTION, RoleCategoryEnum.get("10"));
        assertEquals(RoleCategoryEnum.DATA_SCOPE, RoleCategoryEnum.get("30"));
        assertTrue(RoleCategoryEnum.FUNCTION.eq(RoleCategoryEnum.FUNCTION));

        // ResourceTypeEnum & DictClassifyEnum & DefTenantStatusEnum & TenantConnectTypeEnum
        assertEquals(ResourceTypeEnum.MENU, ResourceTypeEnum.get("20"));
        assertTrue(ResourceTypeEnum.MENU.eq(ResourceTypeEnum.MENU));

        assertEquals(DictClassifyEnum.SYSTEM, DictClassifyEnum.get("10"));
        assertTrue(DictClassifyEnum.SYSTEM.eq(DictClassifyEnum.SYSTEM));

        assertEquals(DefTenantStatusEnum.NORMAL, DefTenantStatusEnum.get("NORMAL"));
        assertTrue(DefTenantStatusEnum.NORMAL.eq(DefTenantStatusEnum.NORMAL));

        assertEquals(TenantConnectTypeEnum.SYSTEM, TenantConnectTypeEnum.get("SYSTEM"));
        assertTrue(TenantConnectTypeEnum.SYSTEM.eq(TenantConnectTypeEnum.SYSTEM));

        // FileType & DateType & DataTypeEnum & DictDataTypeEnum & MsgTemplateCodeEnum
        assertEquals(FileType.IMAGE, FileType.get("IMAGE"));
        assertTrue(FileType.IMAGE.eq(FileType.IMAGE));
        assertEquals("IMAGE", FileType.IMAGE.getCode());

        assertEquals(DateType.MONTH, DateType.get("MONTH"));
        assertEquals(DateType.WEEK, DateType.get("WEEK"));
        assertEquals(DateType.DAY, DateType.get("DAY"));
        assertEquals(DateType.NUL, DateType.get("NUL"));
        assertTrue(DateType.MONTH.eq(DateType.MONTH));
        assertEquals("MONTH", DateType.MONTH.getCode());
        assertEquals(30, DateType.MONTH.getDay());

        assertEquals(DataTypeEnum.SYSTEM, DataTypeEnum.get("SYSTEM"));
        assertTrue(DataTypeEnum.SYSTEM.eq(DataTypeEnum.SYSTEM));

        assertEquals(DictDataTypeEnum.STRING, DictDataTypeEnum.get("STRING"));
        assertEquals(DictDataTypeEnum.NUMBER, DictDataTypeEnum.get("NUMBER"));
        assertEquals(DictDataTypeEnum.BOOLEAN, DictDataTypeEnum.get("BOOLEAN"));
        assertTrue(DictDataTypeEnum.STRING.eq(DictDataTypeEnum.STRING));

        assertNotNull(MsgTemplateCodeEnum.values());
        for (MsgTemplateCodeEnum item : MsgTemplateCodeEnum.values()) {
            assertNotNull(item.getDesc());
            assertNotNull(item.getValue());
            assertNotNull(item.name());
        }
    }

    @Test
    @DisplayName("测试 AppendixSaveVO 构建与防御校验")
    void testAppendixSaveVO() {
        // 1. buildDelete
        AppendixSaveVO delVO = AppendixSaveVO.buildDelete(100L);
        assertEquals(100L, delVO.getBizId());

        // 2. build with fileId
        AppendixSaveVO vo1 = AppendixSaveVO.build(100L, "avatar", 200L);
        assertEquals(100L, vo1.getBizId());
        assertEquals(1, vo1.getTypeFiles().size());
        assertEquals("avatar", vo1.getTypeFiles().get(0).getBizType());
        assertEquals(List.of(200L), vo1.getTypeFiles().get(0).getFileIdList());

        // 3. build with list
        AppendixSaveVO vo2 = AppendixSaveVO.build(100L, "attachments", List.of(201L, 202L));
        assertEquals(2, vo2.getTypeFiles().get(0).getFileIdList().size());

        // 4. TypeFile build null check
        assertNull(AppendixSaveVO.TypeFile.build(null, 1L));
        assertNull(AppendixSaveVO.TypeFile.build("type", (Long) null));
        assertNull(AppendixSaveVO.TypeFile.build("", List.of(1L)));

        // 5. bizId 校验防御
        assertThrows(RuntimeException.class, () -> AppendixSaveVO.buildDelete(null));
        assertThrows(RuntimeException.class, () -> AppendixSaveVO.build(null, "type", 1L));
        assertThrows(RuntimeException.class, () -> AppendixSaveVO.build(1L, "", 1L));
        assertThrows(RuntimeException.class, () -> AppendixSaveVO.build(null, "type", List.of(1L)));
        assertThrows(RuntimeException.class, () -> AppendixSaveVO.build(1L, "", List.of(1L)));
        assertThrows(RuntimeException.class, () -> AppendixSaveVO.build(null, new AppendixSaveVO.TypeFile()));
        assertThrows(RuntimeException.class, () -> AppendixSaveVO.build(null, List.of(new AppendixSaveVO.TypeFile())));

        // 6. TypeFile direct build & array / list overloads
        AppendixSaveVO.TypeFile tf = AppendixSaveVO.TypeFile.build("doc", 123L);
        assertNotNull(tf);
        assertNotNull(tf.toString());
        assertEquals("doc", tf.getBizType());
        assertEquals(List.of(123L), tf.getFileIdList());

        AppendixSaveVO voArray = AppendixSaveVO.build(101L, tf);
        assertEquals(1, voArray.getTypeFiles().size());

        AppendixSaveVO voList = AppendixSaveVO.build(102L, List.of(tf));
        assertEquals(1, voList.getTypeFiles().size());

        // null handling in setters
        AppendixSaveVO voEmpty = new AppendixSaveVO();
        voEmpty.setTypeFiles((AppendixSaveVO.TypeFile) null);
        voEmpty.setTypeFiles((AppendixSaveVO.TypeFile[]) null);
        voEmpty.setTypeFiles(new AppendixSaveVO.TypeFile[]{});
        voEmpty.setTypeFiles((List<AppendixSaveVO.TypeFile>) null);
        assertTrue(voEmpty.getTypeFiles().isEmpty());
        assertNotNull(voEmpty.toString());
    }

    @Test
    @DisplayName("测试 StateEnum & MsgTemplateCodeEnum 边界条件与匹配分支")
    void testStateAndMsgTemplateEnumExt() {
        // StateEnum
        assertEquals(StateEnum.DISABLE, StateEnum.match(null));
        assertEquals(StateEnum.ENABLE, StateEnum.match("999", StateEnum.ENABLE));
        assertEquals(StateEnum.DISABLE, StateEnum.match("999"));
        assertEquals("true", StateEnum.ENABLE.getCode());
        assertEquals("false", StateEnum.DISABLE.getCode());
        assertEquals(1, StateEnum.ENABLE.getInteger());
        assertEquals(true, StateEnum.ENABLE.getBool());
        assertNotNull(StateEnum.ENABLE.getDesc());
        assertTrue(StateEnum.ENABLE.eq(StateEnum.ENABLE));
        assertFalse(StateEnum.ENABLE.eq(StateEnum.DISABLE));
        assertFalse(StateEnum.ENABLE.eq((StateEnum) null));
        assertFalse(StateEnum.ENABLE.eq((Integer) null));
        assertFalse(StateEnum.ENABLE.eq((String) null));
        assertFalse(StateEnum.ENABLE.eq((Boolean) null));
        assertFalse(StateEnum.ENABLE.eq(0));
        assertFalse(StateEnum.ENABLE.eq("0"));
        assertFalse(StateEnum.ENABLE.eq(false));

        // MsgTemplateCodeEnum
        assertEquals(MsgTemplateCodeEnum.REGISTER_SMS, MsgTemplateCodeEnum.get("REGISTER_SMS"));
        assertEquals(MsgTemplateCodeEnum.REGISTER_SMS, MsgTemplateCodeEnum.match("register_sms", null));
        assertNull(MsgTemplateCodeEnum.get("NON_EXIST"));
        assertEquals(MsgTemplateCodeEnum.MOBILE_LOGIN, MsgTemplateCodeEnum.match("NON_EXIST", MsgTemplateCodeEnum.MOBILE_LOGIN));
        assertTrue(MsgTemplateCodeEnum.REGISTER_SMS.eq(MsgTemplateCodeEnum.REGISTER_SMS));
        assertFalse(MsgTemplateCodeEnum.REGISTER_SMS.eq(MsgTemplateCodeEnum.REGISTER_EMAIL));
        assertEquals("REGISTER_SMS", MsgTemplateCodeEnum.REGISTER_SMS.getCode());

        // BooleanEnum null checks and getters
        assertFalse(BooleanEnum.TRUE.eq((BooleanEnum) null));
        assertFalse(BooleanEnum.TRUE.eq((Boolean) null));
        assertFalse(BooleanEnum.TRUE.eq((Integer) null));
        assertFalse(BooleanEnum.TRUE.eq((String) null));
        assertFalse(BooleanEnum.TRUE.eq(0));
        assertFalse(BooleanEnum.TRUE.eq("0"));
        assertFalse(BooleanEnum.TRUE.eq(false));
        assertEquals(1, BooleanEnum.TRUE.getInteger());
        assertEquals(true, BooleanEnum.TRUE.getBool());
        assertEquals("1", BooleanEnum.TRUE.getStr());
        assertEquals("是", BooleanEnum.TRUE.getDesc());

        // HttpMethod
        assertNull(HttpMethod.get("INVALID_METHOD"));
        assertNull(HttpMethod.match("INVALID_METHOD", null));
        assertFalse(HttpMethod.GET.eq((HttpMethod) null));
        assertFalse(HttpMethod.GET.eq(HttpMethod.POST));

        // OrgTypeEnum
        assertNull(OrgTypeEnum.get("UNKNOWN"));
        assertNull(OrgTypeEnum.match("UNKNOWN", null));
        assertFalse(OrgTypeEnum.COMPANY.eq((OrgTypeEnum) null));

        // RoleCategoryEnum
        assertNull(RoleCategoryEnum.get("UNKNOWN"));
        assertNull(RoleCategoryEnum.match("UNKNOWN", null));
        assertFalse(RoleCategoryEnum.FUNCTION.eq((RoleCategoryEnum) null));

        // ResourceTypeEnum
        assertNull(ResourceTypeEnum.get("UNKNOWN"));
        assertNull(ResourceTypeEnum.match("UNKNOWN", null));
        assertFalse(ResourceTypeEnum.MENU.eq((ResourceTypeEnum) null));

        // DefTenantStatusEnum & TenantConnectTypeEnum
        assertNull(DefTenantStatusEnum.get("UNKNOWN"));
        assertNull(DefTenantStatusEnum.match("UNKNOWN", null));
        assertFalse(DefTenantStatusEnum.NORMAL.eq((DefTenantStatusEnum) null));

        assertNull(TenantConnectTypeEnum.get("UNKNOWN"));
        assertNull(TenantConnectTypeEnum.match("UNKNOWN", null));
        assertFalse(TenantConnectTypeEnum.SYSTEM.eq((TenantConnectTypeEnum) null));
    }

    @Test
    @DisplayName("测试 VO 与模型对象读写及工厂方法")
    void testVos() {
        Option option = new Option();
        option.setLabel("label");
        option.setValue("value");
        assertEquals("label", option.getLabel());
        assertEquals("value", option.getValue());

        List<Option> options = Option.mapOptions(BooleanEnum.values());
        assertNotNull(options);
        assertEquals(2, options.size());

        AppendixResultVO appendix = AppendixResultVO.builder().bizId(1L).bizType("DOC").id(10L).build();
        assertEquals(1L, appendix.getBizId());
        assertEquals("DOC", appendix.getBizType());
        assertEquals(10L, appendix.getId());

        ResourceApiVO api = new ResourceApiVO();
        api.setUri("/api/test");
        api.setRequestMethod("GET");
        assertEquals("/api/test", api.getUri());
        assertEquals("GET", api.getRequestMethod());

        // UserQuery
        UserQuery fullQuery = UserQuery.buildFull(1L, 2L);
        assertEquals(1L, fullQuery.getUserId());
        assertEquals(2L, fullQuery.getEmployeeId());
        assertTrue(fullQuery.getFull());

        UserQuery rolesQuery = UserQuery.buildRoles(1L, 2L);
        assertTrue(rolesQuery.getRoles());

        UserQuery orgQuery = UserQuery.buildOrg(1L, 2L);
        assertTrue(orgQuery.getOrg());

        UserQuery posQuery = UserQuery.buildPosition(1L, 2L);
        assertTrue(posQuery.getPosition());

        UserQuery resQuery = UserQuery.buildResource(1L, 2L);
        assertTrue(resQuery.getResource());

        UserQuery empQuery = UserQuery.buildEmployee(1L, 2L);
        assertTrue(empQuery.getEmployee());

        UserQuery uQuery = UserQuery.buildUser(1L, 2L);
        assertTrue(uQuery.getUser());

        // BaseEventVO
        try {
            ContextUtil.setUserId(999L);
            BaseEventVO event = new BaseEventVO().copy();
            assertNotNull(event.getMap());
            ContextUtil.remove();
            assertNull(ContextUtil.getUserId());
            event.write();
            assertEquals(999L, ContextUtil.getUserId());
        } finally {
            ContextUtil.remove();
        }

        // Empty BaseEventVO
        BaseEventVO emptyEvent = new BaseEventVO();
        emptyEvent.setMap(new HashMap<>());
        emptyEvent.write();
    }

    @Test
    @DisplayName("测试 实体与常量覆盖")
    void testEntitiesAndConstants() {
        // SysRole
        SysRole role = new SysRole();
        role.setId(10L);
        role.setName("管理员");
        role.setCode("ADMIN");
        role.setCategory("10");
        assertEquals(10L, role.getId());
        assertEquals("管理员", role.getName());
        assertEquals("ADMIN", role.getCode());
        assertEquals("10", role.getCategory());

        SysRole role2 = SysRole.builder().code("USER").name("普通用户").build();
        List<String> codes = SysRole.getRoleCode(List.of(role, role2));
        assertEquals(List.of("ADMIN", "USER"), codes);
        assertTrue(SysRole.getRoleCode(null).isEmpty());
        assertTrue(SysRole.getRoleCode(Collections.emptyList()).isEmpty());

        assertTrue(SysRole.contains(List.of(role, role2), "ADMIN"));
        assertFalse(SysRole.contains(List.of(role, role2), "GUEST"));
        assertFalse(SysRole.contains(null, "ADMIN"));
        assertFalse(SysRole.contains(List.of(role), null));
        assertFalse(SysRole.contains(List.of(role), ""));

        // SysUser
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setNickName("Admin");
        user.setEmail("admin@lamp.com");
        user.setMobile("13800000000");
        user.setState(true);
        assertNotNull(user.getEchoMap());
        assertEquals("admin", user.getUsername());
        assertEquals("Admin", user.getNickName());

        // SysOrg
        SysOrg org = new SysOrg();
        org.setId(100L);
        org.setName("研发中心");
        org.setType("10");
        assertEquals(100L, org.getId());
        assertEquals("研发中心", org.getName());

        // SysPosition
        SysPosition pos = new SysPosition();
        pos.setId(200L);
        pos.setName("架构师");
        assertEquals(200L, pos.getId());
        assertEquals("架构师", pos.getName());

        // SysEmployee
        SysEmployee emp = new SysEmployee();
        emp.setId(300L);
        emp.setUserId(1L);
        emp.setRealName("张三");
        assertEquals(300L, emp.getId());
        assertEquals("张三", emp.getRealName());

        // SysResource
        SysResource resource = new SysResource();
        resource.setId(400L);
        resource.setName("用户管理");
        resource.setCode("user:view");
        assertEquals(400L, resource.getId());
        assertEquals("用户管理", resource.getName());

        // Constants
        assertNotNull(Condition.LIKE);
        assertNotNull(EchoApi.DICTIONARY_ITEM_FEIGN_CLASS);
        assertNotNull(EchoDictType.Base.POSITION_STATUS);
        assertNotNull(EchoDictType.Global.AREA_LEVEL);
    }
}
