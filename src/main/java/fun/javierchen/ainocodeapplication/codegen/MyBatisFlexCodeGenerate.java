package fun.javierchen.ainocodeapplication.codegen;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;



public class MyBatisFlexCodeGenerate {

    private static final String[] tables = new String[]{
          "chat_history"
    };
    public static void main(String[] args) {
        // 用于本地开发
        Dict dict = YamlUtil.loadByPath("application-local.yml");
        // 用于生产环境
        // Dict dict = YamlUtil.loadByPath("application.yml");
        Map<String, Object> dataSourceMap = dict.getByPath("spring.datasource");
        String url = dataSourceMap.get("url").toString();
        String username = dataSourceMap.get("username").toString();
        String password = dataSourceMap.get("password").toString();
        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfigUseStyle2();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }


    public static GlobalConfig createGlobalConfigUseStyle2() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();
        // 生成特定的表的代码
        globalConfig.setGenerateTable(tables);

        //设置根包
        globalConfig.getPackageConfig()
                .setBasePackage("fun.javierchen.gen");

        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();

        globalConfig.enableService();

        globalConfig.enableController();

        globalConfig.enableEntity();

        //可以单独配置某个列
//        ColumnConfig columnConfig = new ColumnConfig();
//        columnConfig.setColumnName("tenant_id");
//        columnConfig.setLarge(true);
//        columnConfig.setVersion(true);
//        globalConfig.getStrategyConfig()
//                .setColumnConfig("tb_account", columnConfig);

        return globalConfig;
    }

}
