package fun.javierchen.ainocodeapplication.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import fun.javierchen.ainocodeapplication.model.entity.ChatHistory;
import fun.javierchen.ainocodeapplication.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static com.mybatisflex.core.query.QueryMethods.max;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatHistoryServiceImplTest {

    @Resource
    private ChatHistoryService chatHistoryService;
    
    @Test
    void testGetMaxCodeVersion() {
        Long appId = 334550192596959232L;
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(max("codeVersion").as("maxVersion"))
                .from(ChatHistory.class)
                .where(ChatHistory::getAppId).eq(appId)
                .and(ChatHistory::getIsCode).eq(1)
                .and(ChatHistory::getIsDelete).eq(0);

        // 使用 getOneAs 方法获取聚合查询结果
        int result = chatHistoryService.getOneAs(queryWrapper, Integer.class);
        System.out.println(result);
    }

}