package fun.javierchen.ainocodeapplication.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import fun.javierchen.ainocodeapplication.core.model.CodeParseResult;
import fun.javierchen.ainocodeapplication.model.User;
import fun.javierchen.ainocodeapplication.model.dto.chathistory.ChatHistoryQueryRequest;
import fun.javierchen.ainocodeapplication.model.entity.ChatHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层。
 *
 * @author 16010
 * @since 2025-10-11
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存用户消息
     *
     * @param appId   应用ID
     * @param userId  用户ID
     * @param message 消息内容
     * @return 是否保存成功
     */
    boolean saveUserMessage(Long appId, Long userId, String message);


    /**
     * 保存AI消息
     *
     * @param appId        应用ID
     * @param userId       用户ID
     * @param message      消息内容
     * @param isCode       是否为代码消息
     * @param errorMessage 错误信息（如果AI回复失败）
     * @return 是否保存成功
     */
    boolean saveAiMessage(Long appId, Long userId, String message, boolean isCode, String errorMessage);

    /**
     * 使用游标分页查询对话历史
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param loginUser
     * @return
     */
     Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                User loginUser);

    /**
     * 获取查询Wrapper
     *
     * @param queryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest queryRequest);

    /**
     * 保存AI消息（基于解析结果，推荐方法）
     *
     * @param appId        应用ID
     * @param userId       用户ID
     * @param message      消息内容
     * @param parseResult  代码解析结果
     * @param errorMessage 错误信息（如果AI回复失败）
     * @return 是否保存成功
     */
    boolean saveAiMessageWithParseResult(Long appId, Long userId, String message,
                                         CodeParseResult parseResult, String errorMessage);

    /**
     * 根据应用ID获取对话历史
     *
     * @param appId    应用ID
     * @param userId   用户ID
     * @param pageSize 分页大小
     * @param lastId   最后一条记录的ID，用于加载更多历史记录
     * @return 对话历史列表
     */
    List<ChatHistory> getChatHistoryByAppId(Long appId, Long userId, int pageSize, Long lastId);

    /**
     * 管理员查看所有对话历史
     *
     * @param pageSize 分页大小
     * @param pageNum  页码
     * @return 对话历史列表
     */
    List<ChatHistory> getAllChatHistoryForAdmin(int pageSize, int pageNum);

    /**
     * 删除指定应用的所有对话历史
     *
     * @param appId 应用ID
     * @return 是否删除成功
     */
    boolean deleteChatHistoryByAppId(Long appId);

    /**
     * 从数据库加载记忆到内存中
     * @param appId
     * @return
     */
    int loadHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
