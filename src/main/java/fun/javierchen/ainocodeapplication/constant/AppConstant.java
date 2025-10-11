package fun.javierchen.ainocodeapplication.constant;

public interface AppConstant {

    /**
     * 应用生成目录
     */
    String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/user_code";

    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

    /**
     * 应用部署域名
     */
    String CODE_DEPLOY_HOST = "http://localhost";

    /**l
     * SSE 终止的标志
     */
    String SSE_END_FLAG = "__DONE__";
}
