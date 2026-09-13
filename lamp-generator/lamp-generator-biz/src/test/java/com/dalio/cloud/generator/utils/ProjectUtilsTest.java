package com.dalio.cloud.generator.utils;

import com.dalio.basic.database.properties.DatabaseProperties;
import com.dalio.cloud.generator.enumeration.ProjectTypeEnum;
import com.dalio.cloud.generator.vo.save.ProjectGeneratorVO;

/**
 * @author admin
 * @version v1.0
 * @date 2022/4/5 5:54 PM
 * @create [2022/4/5 5:54 PM ] [admin] [初始创建]
 */
public class ProjectUtilsTest {
    public static void main(String[] args) {
        ProjectGeneratorVO vo = new ProjectGeneratorVO();
        vo.setProjectPrefix("lamp");
        vo.setOutputDir("/data/projects/gitlab/lamp-cloud-pro-datasource-column");
        vo.setType(ProjectTypeEnum.CLOUD);
        vo.setAuthor("阿汤哥");
        vo.setServiceName("test");
        vo.setModuleName("test");
        vo.setParent("com.dalio.cloud");
        vo.setGroupId("com.dalio.cloud");
        vo.setUtilParent("com.dalio.basic");
        vo.setVersion("5.4.0");
        vo.setDescription("测试服务");
        vo.setServerPort(8080);
        ProjectUtils.generator(vo, new DatabaseProperties());
    }
}
