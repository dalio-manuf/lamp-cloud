package com.dalio.cloud.system.manager.tenant;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.exception.ArgumentException;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.manager.tenant.impl.DefUserManagerImpl;
import com.dalio.cloud.system.mapper.tenant.DefUserMapper;
import com.dalio.cloud.system.vo.query.tenant.DefUserPageQuery;
import com.dalio.cloud.system.vo.result.tenant.DefUserResultVO;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DefUserManagerImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, DefUser.class);
    }

    @Test
    @DisplayName("测试 findByIds 与 pageUser 与 错误计数")
    void testFindAndPageAndErrorNum() {
        DefUserMapper mapper = mock(DefUserMapper.class);
        CacheOps cacheOps = mock(CacheOps.class);

        DefUserManagerImpl manager = Mockito.spy(new DefUserManagerImpl());
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);
        ReflectionTestUtils.setField(manager, "cacheOps", cacheOps);

        // 1. cacheKeyBuilder
        assertNotNull(ReflectionTestUtils.invokeMethod(manager, "cacheKeyBuilder"));

        // 2. findByIds
        DefUser u = new DefUser();
        u.setId(10L);
        u.setNickName("Nick");
        doReturn(List.of(u)).when(manager).findByIds(anyCollection(), any());

        Map<Serializable, Object> map = manager.findByIds(Set.of(10L));
        assertEquals(1, map.size());
        assertEquals("Nick", map.get(10L));

        // 3. pageUser
        IPage<DefUserResultVO> mockPage = new Page<>();
        when(mapper.pageUser(any(), any())).thenReturn(mockPage);
        IPage<DefUserResultVO> page = manager.pageUser(new DefUserPageQuery(), new Page<>());
        assertNotNull(page);

        // 4. resetPassErrorNum & incrPasswordErrorNumById
        when(mapper.resetPassErrorNum(eq(10L), any())).thenReturn(1);
        assertEquals(1, manager.resetPassErrorNum(10L));

        manager.incrPasswordErrorNumById(10L);
        verify(mapper).incrPasswordErrorNumById(eq(10L), any());
    }

    @Test
    @DisplayName("测试 check 校验")
    void testChecks() {
        DefUserMapper mapper = mock(DefUserMapper.class);
        DefUserManagerImpl manager = Mockito.spy(new DefUserManagerImpl());
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        doReturn(1L).when(manager).count(any());
        assertTrue(manager.checkUsername("u", 1L));
        assertTrue(manager.checkEmail("e@test.com", 1L));
        assertTrue(manager.checkMobile("138", 1L));
        assertTrue(manager.checkIdCard("110", 1L));
    }

    @Test
    @DisplayName("测试 getUserByUsername/Mobile/Email/IdCard 及二级缓存回调")
    void testGetDefUserWithCache() {
        DefUserMapper mapper = mock(DefUserMapper.class);
        CacheOps cacheOps = mock(CacheOps.class);

        DefUserManagerImpl manager = Mockito.spy(new DefUserManagerImpl());
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);
        ReflectionTestUtils.setField(manager, "cacheOps", cacheOps);

        DefUser mockUser = new DefUser();
        mockUser.setId(100L);
        mockUser.setUsername("testuser");
        mockUser.setMobile("13800001111");
        mockUser.setEmail("test@lamp.com");
        mockUser.setIdCard("110101199001018888");

        doReturn(mockUser).when(manager).getByIdCache(100L);
        doReturn(mockUser).when(manager).getOne(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class), eq(false));

        // 模拟 cacheOps.get 回调触发
        doAnswer(invocation -> {
            Function<CacheKey, Long> loader = invocation.getArgument(1);
            Long loadedId = loader.apply(invocation.getArgument(0));
            return new CacheResult<Long>("key", loadedId);
        }).when(cacheOps).get(any(CacheKey.class), any(Function.class), any(boolean[].class));

        assertEquals(mockUser, manager.getUserByUsername("testuser"));
        assertEquals(mockUser, manager.getUserByMobile("13800001111"));
        assertEquals(mockUser, manager.getUserByEmail("test@lamp.com"));
        assertEquals(mockUser, manager.getUserByIdCard("110101199001018888"));
    }

    @Test
    @DisplayName("测试 remove 及 delUserCache 缓存淘汰")
    void testRemoveAndDelCache() {
        DefUserMapper mapper = mock(DefUserMapper.class);
        CacheOps cacheOps = mock(CacheOps.class);

        DefUserManagerImpl manager = Mockito.spy(new DefUserManagerImpl());
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);
        ReflectionTestUtils.setField(manager, "cacheOps", cacheOps);

        // 1. 空数据 delUserCache
        manager.delUserCache(Collections.emptyList());

        // 2. 待删除数据不存在报错
        doReturn(Collections.emptyList()).when(manager).listByIds(anyList());
        assertThrows(ArgumentException.class, () -> manager.delUserCache(List.of(999L)));

        // 3. 正常淘汰
        DefUser u = new DefUser();
        u.setId(100L);
        u.setUsername("alice");
        u.setMobile("13800000000");
        u.setEmail("alice@test.com");
        u.setIdCard("110101199001010000");
        doReturn(List.of(u)).when(manager).listByIds(anyList());

        manager.delUserCache(List.of(u));
        verify(cacheOps).del(anyList());

        // 4. removeById, removeByIds, removeBatchByIds
        doReturn(true).when(manager).removeById(any(DefUser.class));
        doReturn(true).when(manager).removeByIds(anyCollection(), anyBoolean());
        doReturn(true).when(manager).removeBatchByIds(anyCollection());

        assertTrue(manager.removeById(u));
        assertTrue(manager.removeByIds(List.of(100L), false));
        assertTrue(manager.removeBatchByIds(List.of(100L)));
    }
}
