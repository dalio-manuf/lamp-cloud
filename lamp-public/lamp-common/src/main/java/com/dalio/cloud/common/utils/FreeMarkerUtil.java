package com.dalio.cloud.common.utils;

import cn.hutool.crypto.digest.DigestUtil;
import freemarker.cache.MruCacheStorage;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.TemplateClassResolver;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import com.dalio.basic.utils.StrPool;

import java.io.StringWriter;
import java.util.Map;

/**
 * 模板引擎工具类
 *
 * @author admin
 * @version v1.0
 * @date 2022/7/25 12:24 PM
 * @create [2022/7/25 12:24 PM ] [admin] [初始创建]
 */
@Slf4j
public class FreeMarkerUtil {
    private static final Configuration FREEMARKER_CFG;
    private static final StringTemplateLoader SL;

    static {
        FREEMARKER_CFG = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        SL = new StringTemplateLoader();
        FREEMARKER_CFG.setBooleanFormat("c");
        FREEMARKER_CFG.setNumberFormat("0.##");
        // 防范 SSTI 模板注入漏洞，禁止实例化任意 Java 类
        FREEMARKER_CFG.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
        generateSharedVariable();
        FREEMARKER_CFG.setCacheStorage(new MruCacheStorage(2000, Integer.MAX_VALUE));
        FREEMARKER_CFG.setTemplateUpdateDelayMilliseconds(6000000L);
        TemplateLoader[] loaders = new TemplateLoader[]{SL};
        MultiTemplateLoader mt = new MultiTemplateLoader(loaders);
        FREEMARKER_CFG.setTemplateLoader(mt);
    }

    private FreeMarkerUtil() {
    }

    private static void generateSharedVariable() {
        try {
            BeansWrapper wrapper = new BeansWrapper(Configuration.VERSION_2_3_30);
            TemplateHashModel staticModels = wrapper.getStaticModels();
            TemplateHashModel strPool = (TemplateHashModel) staticModels.get("com.dalio.basic.utils.StrPool");
            FREEMARKER_CFG.setSharedVariable("StrPool", strPool);
            TemplateHashModel dateUtils = (TemplateHashModel) staticModels.get("com.dalio.basic.utils.DateUtils");
            FREEMARKER_CFG.setSharedVariable("DateUtils", dateUtils);
            TemplateHashModel argumentAssert = (TemplateHashModel) staticModels.get("com.dalio.basic.utils.ArgumentAssert");
            FREEMARKER_CFG.setSharedVariable("ArgumentAssert", argumentAssert);
            TemplateHashModel beanPlusUtil = (TemplateHashModel) staticModels.get("com.dalio.basic.utils.BeanPlusUtil");
            FREEMARKER_CFG.setSharedVariable("BeanPlusUtil", beanPlusUtil);
            TemplateHashModel collHelper = (TemplateHashModel) staticModels.get("com.dalio.basic.utils.CollHelper");
            FREEMARKER_CFG.setSharedVariable("CollHelper", collHelper);
            TemplateHashModel springUtils = (TemplateHashModel) staticModels.get("com.dalio.basic.utils.SpringUtils");
            FREEMARKER_CFG.setSharedVariable("SpringUtils", springUtils);
            TemplateHashModel strHelper = (TemplateHashModel) staticModels.get("com.dalio.basic.utils.StrHelper");
            FREEMARKER_CFG.setSharedVariable("StrHelper", strHelper);
            TemplateHashModel treeUtil = (TemplateHashModel) staticModels.get("com.dalio.basic.utils.TreeUtil");
            FREEMARKER_CFG.setSharedVariable("TreeUtil", treeUtil);
        } catch (TemplateModelException e) {
            log.error(e.getMessage(), e);
        }
    }

    @SneakyThrows
    public static String generateString(String strTemplate, Map<String, Object> parameters) {
        if (strTemplate == null) {
            return null;
        }
        String templateName = DigestUtil.md5Hex(strTemplate);
        if (SL.findTemplateSource(templateName) == null) {
            synchronized (SL) {
                if (SL.findTemplateSource(templateName) == null) {
                    SL.putTemplate(templateName, strTemplate);
                }
            }
        }

        StringWriter writer = new StringWriter();
        Template template = FREEMARKER_CFG.getTemplate(templateName, StrPool.UTF8);
        template.process(parameters, writer);
        return writer.toString();
    }
}
