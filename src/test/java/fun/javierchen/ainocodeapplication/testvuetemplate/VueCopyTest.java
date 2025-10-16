package fun.javierchen.ainocodeapplication.testvuetemplate;

import fun.javierchen.ainocodeapplication.utils.TemplateCopyUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class VueCopyTest {
    @Test
    void copyFile() throws IOException {
        long appId = 324234L;
        String templatePath = "D:\\a_my_project\\ai-no-code-application\\front\\ai-no-code\\template_vue";
//        String targetPath = System.getProperty("user.dir") + "/tmp" + appId;
        String targetPath = "D:\\a_my_project\\ai-no-code-application\\src\\main\\resources\\web-template";
        TemplateCopyUtil.CopyResult copyResult = TemplateCopyUtil.copyTemplate(templatePath, targetPath);
        TemplateCopyUtil.CopyConfig copyConfig = new TemplateCopyUtil.CopyConfig();


        System.out.println(copyResult);

    }

}
