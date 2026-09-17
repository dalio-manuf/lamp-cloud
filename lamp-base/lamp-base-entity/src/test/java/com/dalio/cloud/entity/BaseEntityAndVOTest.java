package com.dalio.cloud.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.dalio.cloud.base.entity.user.BaseEmployee;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.base.enumeration.system.LogType;
import com.dalio.cloud.base.vo.result.user.RouterMeta;
import com.dalio.cloud.base.vo.result.user.RouterMetaConfig;
import com.dalio.cloud.base.vo.result.user.VueRouter;
import com.dalio.cloud.file.domain.FileDeleteBO;
import com.dalio.cloud.file.domain.FileGetUrlBO;
import com.dalio.cloud.file.dto.chunk.FileChunkCheckDTO;
import com.dalio.cloud.file.dto.chunk.FileChunksMergeDTO;
import com.dalio.cloud.file.dto.chunk.FileUploadDTO;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.vo.param.FileParamVO;
import com.dalio.cloud.file.vo.param.FileUploadVO;
import com.dalio.cloud.file.vo.result.FileResultVO;
import com.dalio.cloud.msg.vo.update.ExtendMsgSendVO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 实体与 VO 单元测试
 *
 * @author went
 */
class BaseEntityAndVOTest {

    @Test
    @DisplayName("测试 VueRouter 及元数据模型")
    void testVueRouterAndMeta() {
        VueRouter router = new VueRouter();
        router.setPath("/system/user");
        router.setName("UserManagement");
        router.setComponent("system/user/index");
        router.setRedirect("/system/user/list");
        router.setLink("https://example.com");
        router.setResourceType("MENU");
        router.setOpenWith("INNER");
        router.setIsHidden(false);
        router.setIcon("user-icon");
        router.setMetaJson("{}");

        RouterMetaConfig config = new RouterMetaConfig();
        RouterMeta meta = new RouterMeta(config);
        meta.setTitle("用户管理")
                .setIcon("user")
                .setAffix(true)
                .setIgnoreKeepAlive(false)
                .setFrameSrc("http://iframe")
                .setIframeSrc("http://vben5")
                .setLink("http://link")
                .setTransitionName("fade")
                .setHideBreadcrumb(false)
                .setCarryParam(true)
                .setCurrentActiveMenu("/system")
                .setHideTab(false)
                .setHideMenu(false)
                .setHideChildrenInMenu(false)
                .setType("dot")
                .setContent("badge")
                .setDot(true)
                .setHideInMenu(false)
                .setActiveMenu("/system/detail")
                .setActivePath("/system/detail")
                .setI18nKey("menu.user")
                .setKeepAlive("true")
                .setConstant(false)
                .setLocalIcon("custom-user")
                .setOrder(10)
                .setHref("https://external.com")
                .setMultiTab("true")
                .setFixedIndexInTab("0")
                .setComponent("system/user/index");

        router.setMeta(meta);

        assertEquals("/system/user", router.getPath());
        assertEquals("UserManagement", router.getName());
        assertEquals("system/user/index", router.getComponent());
        assertEquals("用户管理", router.getMeta().getTitle());
        assertEquals("user", router.getMeta().getIcon());
        assertTrue(router.getMeta().getAffix());
        assertFalse(router.getMeta().getIgnoreKeepAlive());
        assertEquals("http://iframe", router.getMeta().getFrameSrc());
        assertEquals("http://vben5", router.getMeta().getIframeSrc());
        assertEquals("http://link", router.getMeta().getLink());
        assertEquals("fade", router.getMeta().getTransitionName());
        assertFalse(router.getMeta().getHideBreadcrumb());
        assertTrue(router.getMeta().getCarryParam());
        assertEquals("/system", router.getMeta().getCurrentActiveMenu());
        assertFalse(router.getMeta().getHideTab());
        assertFalse(router.getMeta().getHideMenu());
        assertFalse(router.getMeta().getHideChildrenInMenu());
        assertEquals("dot", router.getMeta().getType());
        assertEquals("badge", router.getMeta().getContent());
        assertTrue(router.getMeta().getDot());
        assertFalse(router.getMeta().getHideInMenu());
        assertEquals("/system/detail", router.getMeta().getActiveMenu());
        assertEquals("/system/detail", router.getMeta().getActivePath());
        assertEquals("menu.user", router.getMeta().getI18nKey());
        assertEquals("true", router.getMeta().getKeepAlive());
        assertFalse(router.getMeta().getConstant());
        assertEquals("custom-user", router.getMeta().getLocalIcon());
        assertEquals(Integer.valueOf(10), router.getMeta().getOrder());
        assertEquals("https://external.com", router.getMeta().getHref());
        assertEquals("true", router.getMeta().getMultiTab());
        assertEquals("0", router.getMeta().getFixedIndexInTab());
        assertEquals("system/user/index", router.getMeta().getComponent());
        assertNotNull(router.getEchoMap());
        assertNotNull(router.toString());
    }

    @Test
    @DisplayName("测试文件相关 DTO 与 VO")
    void testFileDTOAndVO() {
        FileChunksMergeDTO mergeDTO = new FileChunksMergeDTO();
        mergeDTO.setName("test.zip");
        mergeDTO.setSubmittedFileName("origin.zip");
        mergeDTO.setMd5("abc123md5");
        mergeDTO.setChunks(5);
        mergeDTO.setExt("zip");
        mergeDTO.setSize(1024L);
        mergeDTO.setContextType("application/zip");
        mergeDTO.setFolderId(12L);

        assertEquals("test.zip", mergeDTO.getName());
        assertEquals("origin.zip", mergeDTO.getSubmittedFileName());
        assertEquals("abc123md5", mergeDTO.getMd5());
        assertEquals(5, mergeDTO.getChunks());
        assertEquals("zip", mergeDTO.getExt());
        assertEquals(1024L, mergeDTO.getSize());
        assertEquals("application/zip", mergeDTO.getContextType());
        assertEquals(12L, mergeDTO.getFolderId());

        FileUploadDTO uploadDTO = new FileUploadDTO();
        uploadDTO.setName("part.bin");
        uploadDTO.setChunk(1);
        uploadDTO.setChunks(3);
        uploadDTO.setSize(512L);
        uploadDTO.setMd5("md5part");
        uploadDTO.setLastModifiedDate("2026-09-16");
        uploadDTO.setType("bin");
        uploadDTO.setExt(".bin");
        uploadDTO.setFolderId(99L);

        assertEquals(1, uploadDTO.getChunk());
        assertEquals(3, uploadDTO.getChunks());
        assertEquals("part.bin", uploadDTO.getName());
        assertEquals("md5part", uploadDTO.getMd5());
        assertEquals(512L, uploadDTO.getSize());
        assertEquals("2026-09-16", uploadDTO.getLastModifiedDate());
        assertEquals("bin", uploadDTO.getType());
        assertEquals(".bin", uploadDTO.getExt());
        assertEquals(99L, uploadDTO.getFolderId());

        FileChunkCheckDTO checkDTO = new FileChunkCheckDTO();
        checkDTO.setName("part.bin");
        checkDTO.setChunkIndex(1);
        checkDTO.setSize(512L);
        assertEquals(Integer.valueOf(1), checkDTO.getChunkIndex());
        assertEquals("part.bin", checkDTO.getName());
        assertEquals(512L, checkDTO.getSize());

        FileResultVO resultVO = new FileResultVO();
        resultVO.setId(10L);
        resultVO.setOriginalFileName("test.pdf");
        resultVO.setBucket("bucket-a");
        resultVO.setPath("2026/09/test.pdf");
        resultVO.setUrl("http://example.com/test.pdf");
        resultVO.setStorageType(FileStorageType.LOCAL);
        resultVO.setSize(2048L);
        assertEquals(10L, resultVO.getId());
        assertEquals(FileStorageType.LOCAL, resultVO.getStorageType());

        FileUploadVO fileUploadVO = new FileUploadVO();
        fileUploadVO.setBizType("avatar");
        fileUploadVO.setBucket("user-bucket");
        fileUploadVO.setStorageType(FileStorageType.MIN_IO);
        assertEquals("avatar", fileUploadVO.getBizType());
        assertEquals(FileStorageType.MIN_IO, fileUploadVO.getStorageType());

        FileParamVO fileParamVO = new FileParamVO();
        fileParamVO.setBizType("order");
        assertEquals("order", fileParamVO.getBizType());

        FileGetUrlBO getUrlBO = FileGetUrlBO.builder().path("a/b/c").originalFileName("file.txt").build();
        assertEquals("a/b/c", getUrlBO.getPath());

        FileDeleteBO deleteBO = FileDeleteBO.builder().path("a/b/c").bucket("bucket1").build();
        assertEquals("bucket1", deleteBO.getBucket());

        File file = new File();
        file.setId(100L);
        file.setBizType("attachment");
        file.setOriginalFileName("doc.docx");
        file.setStorageType(FileStorageType.ALI_OSS);
        assertEquals(100L, file.getId());
        assertEquals(FileStorageType.ALI_OSS, file.getStorageType());
    }

    @Test
    @DisplayName("测试用户与消息相关实体和 VO")
    void testUserAndMsgEntities() {
        BaseEmployee employee = new BaseEmployee();
        employee.setId(1L);
        employee.setRealName("张三");
        employee.setPositionId(10L);
        employee.setLastDeptId(20L);
        employee.setLastCompanyId(30L);
        employee.setPositionStatus("10");
        employee.setActiveStatus("20");
        employee.setState(true);
        employee.setIsDefault(true);
        employee.setCreatedOrgId(5L);

        assertEquals("张三", employee.getRealName());
        assertEquals(1L, employee.getId());
        assertEquals(10L, employee.getPositionId());
        assertEquals(20L, employee.getLastDeptId());
        assertEquals(30L, employee.getLastCompanyId());
        assertEquals("10", employee.getPositionStatus());
        assertEquals("20", employee.getActiveStatus());
        assertTrue(employee.getState());
        assertTrue(employee.getIsDefault());
        assertEquals(5L, employee.getCreatedOrgId());

        BaseOrg org = new BaseOrg();
        org.setId(20L);
        org.setName("技术部");
        org.setParentId(0L);
        assertEquals("技术部", org.getName());

        ExtendMsgSendVO sendVO = new ExtendMsgSendVO();
        sendVO.setCode("TMPL_001")
                .setBizId(123L)
                .setBizType("ORDER")
                .setAuthor("Admin")
                .addParam("code", "123456")
                .addParam(com.dalio.basic.model.Kv.builder().key("k").value("v").build())
                .addRecipient("13800138000", "ext1")
                .addRecipient("13800138001")
                .addRecipient(com.dalio.cloud.msg.vo.save.ExtendMsgRecipientSaveVO.builder().recipient("13800138002").build());

        assertEquals("TMPL_001", sendVO.getCode());
        assertEquals(Long.valueOf(123L), sendVO.getBizId());
        assertEquals("ORDER", sendVO.getBizType());
        assertEquals("Admin", sendVO.getAuthor());
        assertEquals(2, sendVO.getParamList().size());
        assertEquals(3, sendVO.getRecipientList().size());

        sendVO.clearParam();
        sendVO.clearRecipient();
        assertEquals(0, sendVO.getParamList().size());
        assertEquals(0, sendVO.getRecipientList().size());

        assertNotNull(sendVO.toString());
        assertTrue(sendVO.toString().contains("TMPL_001"));
    }

    @Test
    @DisplayName("测试数据库实体类建造者与字段")
    void testEntities() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        BaseEmployee emp = BaseEmployee.builder()
                .id(1L).createdBy(1L).createdTime(now).updatedBy(1L).updatedTime(now)
                .isDefault(true).userId(2L).positionId(3L).realName("张三")
                .lastCompanyId(4L).lastDeptId(5L).positionStatus("10").activeStatus("20")
                .state(true).createdOrgId(6L).build();
        assertNotNull(emp);
        assertEquals(1L, emp.getId());

        com.dalio.cloud.base.entity.user.BasePosition pos = com.dalio.cloud.base.entity.user.BasePosition.builder()
                .id(2L).createdBy(1L).createdTime(now).updatedBy(1L).updatedTime(now)
                .name("开发工程师").orgId(10L).state(true).remarks("备注").build();
        assertNotNull(pos);
        assertEquals("开发工程师", pos.getName());

        com.dalio.cloud.base.entity.user.BaseEmployeeRoleRel empRole = com.dalio.cloud.base.entity.user.BaseEmployeeRoleRel.builder()
                .id(3L).createdBy(1L).createdTime(now).employeeId(1L).roleId(2L).build();
        assertNotNull(empRole);
        assertEquals(1L, empRole.getEmployeeId());

        com.dalio.cloud.base.entity.user.BaseEmployeeOrgRel empOrg = com.dalio.cloud.base.entity.user.BaseEmployeeOrgRel.builder()
                .id(4L).createdBy(1L).createdTime(now).employeeId(1L).orgId(10L).build();
        assertNotNull(empOrg);
        assertEquals(10L, empOrg.getOrgId());

        com.dalio.cloud.base.entity.user.BaseOrgRoleRel orgRole = com.dalio.cloud.base.entity.user.BaseOrgRoleRel.builder()
                .id(5L).createdBy(1L).createdTime(now).orgId(10L).roleId(2L).build();
        assertNotNull(orgRole);
        assertEquals(2L, orgRole.getRoleId());

        com.dalio.cloud.base.entity.user.BaseOrg org = com.dalio.cloud.base.entity.user.BaseOrg.builder()
                .id(10L).createdBy(1L).createdTime(now).updatedBy(1L).updatedTime(now)
                .name("技术部").type("01").shortName("技术").parentId(0L).treeGrade(1)
                .treePath(",0,").sortValue(1).state(true).remarks("部门").build();
        assertNotNull(org);
        assertEquals("技术部", org.getName());

        com.dalio.cloud.base.entity.system.BaseRole role = com.dalio.cloud.base.entity.system.BaseRole.builder()
                .id(2L).createdBy(1L).createdTime(now).updatedBy(1L).updatedTime(now)
                .name("管理员").code("ADMIN").remarks("备注").state(true).readonly(false)
                .category("10").build();
        assertNotNull(role);
        assertEquals("ADMIN", role.getCode());

        com.dalio.cloud.base.entity.system.BaseRoleResourceRel roleRes = com.dalio.cloud.base.entity.system.BaseRoleResourceRel.builder()
                .id(6L).createdBy(1L).createdTime(now).roleId(2L).resourceId(100L).build();
        assertNotNull(roleRes);
        assertEquals(100L, roleRes.getResourceId());

        com.dalio.cloud.base.entity.system.BaseOperationLog opLog = com.dalio.cloud.base.entity.system.BaseOperationLog.builder()
                .id(7L).createdBy(1L).createdTime(now).requestIp("127.0.0.1").type(LogType.OPT)
                .userName("admin").description("测试操作").classPath("com.test").actionMethod("doTest")
                .requestUri("/test").httpMethod(com.dalio.cloud.model.enumeration.HttpMethod.POST)
                .startTime(now).finishTime(now).consumingTime(10L)
                .ua("Chrome").build();
        assertNotNull(opLog);
        assertEquals("admin", opLog.getUserName());

        com.dalio.cloud.file.entity.File file = com.dalio.cloud.file.entity.File.builder()
                .id(8L).createdBy(1L).createdTime(now).updatedBy(1L).updatedTime(now)
                .bizType("doc").fileType(null).storageType(FileStorageType.LOCAL).bucket("b1")
                .path("/path").url("http://url").uniqueFileName("ufn").originalFileName("ofn")
                .contentType("txt").size(100L).build();
        assertNotNull(file);
        assertEquals("doc", file.getBizType());
    }
}
