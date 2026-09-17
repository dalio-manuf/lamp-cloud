package com.dalio.cloud.base.biz.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.exception.BizException;
import com.dalio.cloud.base.entity.user.BaseEmployee;
import com.dalio.cloud.base.service.user.BaseEmployeeOrgRelService;
import com.dalio.cloud.base.service.user.BaseEmployeeService;
import com.dalio.cloud.base.vo.query.user.BaseEmployeePageQuery;
import com.dalio.cloud.base.vo.result.user.BaseEmployeeResultVO;
import com.dalio.cloud.base.vo.save.user.BaseEmployeeSaveVO;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.service.tenant.DefUserService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BaseEmployeeBiz 单元测试
 */
class BaseEmployeeBizTest {

    @Test
    @DisplayName("测试 save 员工与用户创建流程及手机号防重")
    void testSave() {
        BaseEmployeeService baseEmployeeService = Mockito.mock(BaseEmployeeService.class);
        BaseEmployeeOrgRelService baseEmployeeOrgRelService = Mockito.mock(BaseEmployeeOrgRelService.class);
        DefUserService defUserService = Mockito.mock(DefUserService.class);

        BaseEmployeeBiz biz = new BaseEmployeeBiz(baseEmployeeService, baseEmployeeOrgRelService, defUserService);

        BaseEmployeeSaveVO saveVO = new BaseEmployeeSaveVO();
        saveVO.setMobile("13800000000");
        saveVO.setRealName("张三");

        // 1. 手机号重复校验
        when(defUserService.checkMobile("13800000000", null)).thenReturn(true);
        assertThrows(BizException.class, () -> biz.save(saveVO));

        // 2. 正常流程
        when(defUserService.checkMobile("13800000000", null)).thenReturn(false);
        DefUser createdUser = new DefUser();
        createdUser.setId(1001L);
        when(defUserService.save(any())).thenReturn(createdUser);

        BaseEmployee employee = new BaseEmployee();
        employee.setId(2001L);
        when(baseEmployeeService.save(any())).thenReturn(employee);

        BaseEmployee result = biz.save(saveVO);
        assertNotNull(result);
        assertEquals(2001L, result.getId());
        assertEquals(1001L, saveVO.getUserId());
        assertTrue(saveVO.getIsDefault());
    }

    @Test
    @DisplayName("测试 getEmployeeUserById 级联查询机构和用户")
    void testGetEmployeeUserById() {
        BaseEmployeeService baseEmployeeService = Mockito.mock(BaseEmployeeService.class);
        BaseEmployeeOrgRelService baseEmployeeOrgRelService = Mockito.mock(BaseEmployeeOrgRelService.class);
        DefUserService defUserService = Mockito.mock(DefUserService.class);

        BaseEmployeeBiz biz = new BaseEmployeeBiz(baseEmployeeService, baseEmployeeOrgRelService, defUserService);

        // 1. 员工不存在返回 null
        when(baseEmployeeService.getById(999L)).thenReturn(null);
        assertNull(biz.getEmployeeUserById(999L));

        // 2. 员工存在
        BaseEmployee employee = new BaseEmployee();
        employee.setId(100L);
        employee.setUserId(200L);
        employee.setRealName("李四");
        when(baseEmployeeService.getById(100L)).thenReturn(employee);
        when(baseEmployeeOrgRelService.findOrgIdListByEmployeeId(100L)).thenReturn(List.of(10L, 20L));

        DefUser defUser = new DefUser();
        defUser.setId(200L);
        defUser.setNickName("小李");
        when(defUserService.getById(200L)).thenReturn(defUser);

        BaseEmployeeResultVO vo = biz.getEmployeeUserById(100L);
        assertNotNull(vo);
        assertEquals(100L, vo.getId());
        assertEquals(List.of(10L, 20L), vo.getOrgIdList());
        assertNotNull(vo.getDefUser());
        assertEquals(200L, vo.getDefUser().getId());
    }

    @Test
    @DisplayName("测试 findPageResultVO 分页组合查询")
    void testFindPageResultVO() {
        BaseEmployeeService baseEmployeeService = Mockito.mock(BaseEmployeeService.class);
        BaseEmployeeOrgRelService baseEmployeeOrgRelService = Mockito.mock(BaseEmployeeOrgRelService.class);
        DefUserService defUserService = Mockito.mock(DefUserService.class);

        BaseEmployeeBiz biz = new BaseEmployeeBiz(baseEmployeeService, baseEmployeeOrgRelService, defUserService);

        // 1. 搜索条件含手机号但用户不存在
        PageParams<BaseEmployeePageQuery> params = new PageParams<>();
        BaseEmployeePageQuery query = new BaseEmployeePageQuery();
        query.setMobile("13900000000");
        params.setModel(query);
        params.setCurrent(1L);
        params.setSize(10L);

        when(defUserService.findUserIdList(any())).thenReturn(Collections.emptyList());
        IPage<BaseEmployeeResultVO> emptyPage = biz.findPageResultVO(params);
        assertTrue(emptyPage.getRecords().isEmpty());

        // 2. 搜索条件含手机号且匹配到用户
        when(defUserService.findUserIdList(any())).thenReturn(List.of(200L));
        Page<BaseEmployeeResultVO> page = new Page<>(1, 10);
        BaseEmployeeResultVO record = new BaseEmployeeResultVO();
        record.setId(100L);
        record.setUserId(200L);
        page.setRecords(List.of(record));

        when(baseEmployeeService.findPageResultVO(params)).thenReturn(page);
        DefUser user = new DefUser();
        user.setId(200L);
        user.setUsername("testuser");
        when(defUserService.listByIds(List.of(200L))).thenReturn(List.of(user));

        IPage<BaseEmployeeResultVO> resPage = biz.findPageResultVO(params);
        assertEquals(1, resPage.getRecords().size());
        assertNotNull(resPage.getRecords().get(0).getDefUser());
        assertEquals(200L, resPage.getRecords().get(0).getDefUser().getId());
    }
}
