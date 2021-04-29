package com.cloud.srb.core;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import org.junit.Test;

/**
 * @ClassName CodeGenerator
 * @Author xsshuai
 * @Date 2021/4/24 12:05 上午
 **/
public class CodeGenerator {

    @Test
    public void genCode() {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "/src/main/java");
        gc.setAuthor("xsshuai");
        gc.setOpen(false);//生成后是否打开资源管理器
        gc.setServiceName("%sService");//去掉service接口的首字母i
        gc.setIdType(IdType.AUTO);//主键策略
        gc.setSwagger2(true); //实体属性 Swagger2 注解
        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://localhost:3306/db2021_srb_core?useUnicode=true&useSSL=false&characterEncoding=utf8");
        // dsc.setSchemaName("public");
        dsc.setDriverName("com.mysql.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("123456");
        dsc.setDbType(DbType.MYSQL);
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.cloud.srb.core");
        pc.setEntity("pojo.entity");
        mpg.setPackageInfo(pc);

        //策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);//数据库表映射到实体的命名策略
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);//数据库表字段映射到实体的命名策略
        strategy.setEntityLombokModel(true);//lombok
        strategy.setLogicDeleteFieldName("is_deleted");//逻辑删除字段名;
        strategy.setEntityBooleanColumnRemoveIsPrefix(true);//去掉布尔值的is_前缀(确保tinyint(1))
        strategy.setRestControllerStyle(true);//restful api风格控制器
        mpg.setStrategy(strategy);

        //执行
        mpg.execute();
    }
}
