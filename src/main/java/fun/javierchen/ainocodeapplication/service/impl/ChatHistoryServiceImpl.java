package fun.javierchen.ainocodeapplication.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import fun.javierchen.ainocodeapplication.constant.UserConstant;
import fun.javierchen.ainocodeapplication.core.model.CodeParseResult;
import fun.javierchen.ainocodeapplication.mapper.ChatHistoryMapper;
import fun.javierchen.ainocodeapplication.model.User;
import fun.javierchen.ainocodeapplication.model.dto.chathistory.ChatHistoryQueryRequest;
import fun.javierchen.ainocodeapplication.model.entity.ChatHistory;
import fun.javierchen.ainocodeapplication.model.entity.App;
import fun.javierchen.ainocodeapplication.service.ChatHistoryService;
import fun.javierchen.ainocodeapplication.service.AppService;
import fun.javierchen.ainocodeapplication.model.enums.MessageTypeEnum;
import fun.javierchen.ainocodeapplication.exceptiom.BusinessException;
import fun.javierchen.ainocodeapplication.exceptiom.ErrorCode;
import fun.javierchen.ainocodeapplication.utils.ThrowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.mybatisflex.core.query.QueryMethods.max;
import static com.mybatisflex.core.query.QueryMethods.select;

/**
 * 对话历史 服务实现类
 *
 * @author JavierChen
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private AppService appService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUserMessage(Long appId, Long userId, String message) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 ID 不能为空");
        ThrowUtils.throwIf(message == null || message.trim().isEmpty(), ErrorCode.PARAMS_ERROR, "消息不能为空");

        // 2. 检查应用是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3. 保存用户消息
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setMessage(message);
        chatHistory.setMessageType(MessageTypeEnum.USER.getValue());
        chatHistory.setAppId(appId);
        chatHistory.setUserId(userId);
        chatHistory.setCreateTime(LocalDateTime.now());
        chatHistory.setUpdateTime(LocalDateTime.now());
        chatHistory.setIsDelete(0);

        return this.save(chatHistory);
    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest queryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (queryRequest == null) {
            return queryWrapper;
        }
        Long appId = queryRequest.getAppId();
        String message = queryRequest.getMessage();
        String messageType = queryRequest.getMessageType();
        Long userId = queryRequest.getUserId();
        LocalDateTime lastCreateTime = queryRequest.getLastCreateTime();
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        // 拼接基础的查询条件
        queryWrapper.where(ChatHistory::getAppId).eq(appId)
                .and(ChatHistory::getMessage).like(message)
                .and(ChatHistory::getMessageType).eq(messageType)
                .and(ChatHistory::getUserId).eq(userId);
        // 游标查询 查询小于lastCreateTime的数据
        if (lastCreateTime != null) {
            queryWrapper.and(ChatHistory::getCreateTime).lt(lastCreateTime);
        }
        // 排序
        if (sortField != null && !sortField.trim().isEmpty()) {

            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按照创建时间降序
            queryWrapper.orderBy(ChatHistory::getCreateTime, false);
        }
        return queryWrapper;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAiMessage(Long appId, Long userId, String message, boolean isCode, String errorMessage) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 ID 不能为空");

        // 2. 检查应用是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3. 保存 AI 消息（包括成功和失败的情况）
        ChatHistory chatHistory = new ChatHistory();
        if (errorMessage != null) {
            // 如果有错误信息，则保存错误信息
            chatHistory.setMessage(errorMessage);
        } else {
            chatHistory.setMessage(message);
        }
        chatHistory.setMessageType(MessageTypeEnum.AI.getValue());
        chatHistory.setAppId(appId);
        chatHistory.setUserId(userId);
        chatHistory.setIsCode(isCode ? 1 : 0);

        // 如果是代码消息且不是第一次生成，则版本号+1
        if (isCode) {
            // 查询该应用下最新的代码版本号
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .select(max("codeVersion").as("maxVersion"))
                    .from(ChatHistory.class)
                    .where(ChatHistory::getAppId).eq(appId)
                    .and(ChatHistory::getIsCode).eq(1)
                    .and(ChatHistory::getIsDelete).eq(0);

            ChatHistory maxVersionChatHistory = this.getOne(queryWrapper);
            int maxVersion = 0;
            if (maxVersionChatHistory != null && maxVersionChatHistory.getCodeVersion() != null) {
                maxVersion = maxVersionChatHistory.getCodeVersion();
            }

            // 版本号+1
            chatHistory.setCodeVersion(maxVersion + 1);
        } else {
            chatHistory.setCodeVersion(0);
        }

        chatHistory.setCreateTime(LocalDateTime.now());
        chatHistory.setUpdateTime(LocalDateTime.now());
        chatHistory.setIsDelete(0);

        return this.save(chatHistory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveAiMessageWithParseResult(Long appId, Long userId, String message,
                                                CodeParseResult parseResult, String errorMessage) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 ID 不能为空");

        // 2. 检查应用是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3. 确定是否为代码消息和消息内容
        boolean isCode = parseResult != null && parseResult.isParseSuccess() && parseResult.isHasValidCode();
        String finalMessage = errorMessage != null ? errorMessage : message;

        // 4. 保存 AI 消息
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setMessage(finalMessage);
        chatHistory.setMessageType(MessageTypeEnum.AI.getValue());
        chatHistory.setAppId(appId);
        chatHistory.setUserId(userId);
        chatHistory.setIsCode(isCode ? 1 : 0);

        // 5. 如果是代码消息，设置版本号
        if (isCode) {
            // 查询该应用下最新的代码版本号
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .select(max("codeVersion").as("maxVersion"))
                    .from(ChatHistory.class)
                    .where(ChatHistory::getAppId).eq(appId)
                    .and(ChatHistory::getIsCode).eq(1)
                    .and(ChatHistory::getIsDelete).eq(0);

            Integer result = this.getOneAs(queryWrapper, Integer.class);
            int maxVersion = 0;
            if (result != null) {
                maxVersion = result;
            }

            // 版本号+1
            chatHistory.setCodeVersion(maxVersion + 1);
        } else {
            chatHistory.setCodeVersion(0);
        }

        chatHistory.setCreateTime(LocalDateTime.now());
        chatHistory.setUpdateTime(LocalDateTime.now());
        chatHistory.setIsDelete(0);

        return this.save(chatHistory);
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }


    @Override
    public List<ChatHistory> getChatHistoryByAppId(Long appId, Long userId, int pageSize, Long lastId) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 ID 不能为空");
        ThrowUtils.throwIf(pageSize <= 0, ErrorCode.PARAMS_ERROR, "分页大小必须大于0");

        // 2. 检查应用是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        // 3. 验证用户是否有权限查看该应用的对话历史（仅应用创建者和管理员可见）
        if (!app.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限查看该应用的对话历史");
        }

        // 4. 查询对话历史
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(ChatHistory.class)
                .where(ChatHistory::getAppId).eq(appId)
                .and(ChatHistory::getIsDelete).eq(0)
                .orderBy(ChatHistory::getCreateTime, false); // 按时间倒序

        // 如果指定了lastId，则查询比该ID更早的记录
        if (lastId != null && lastId > 0) {
            queryWrapper.and(ChatHistory::getId).lt(lastId);
        }

        queryWrapper.limit(pageSize);

        return this.list(queryWrapper);
    }

    @Override
    public List<ChatHistory> getAllChatHistoryForAdmin(int pageSize, int pageNum) {
        // 1. 参数校验
        ThrowUtils.throwIf(pageSize <= 0, ErrorCode.PARAMS_ERROR, "分页大小必须大于0");
        ThrowUtils.throwIf(pageNum <= 0, ErrorCode.PARAMS_ERROR, "页码必须大于0");

        // 2. 查询所有对话历史，按时间倒序排序
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(ChatHistory.class)
                .where(ChatHistory::getIsDelete).eq(0)
                .orderBy(ChatHistory::getCreateTime, false) // 按时间倒序
                .limit(pageSize)
                .offset((pageNum - 1) * pageSize);

        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteChatHistoryByAppId(Long appId) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");

        // 2. 删除指定应用的所有对话历史
        QueryWrapper queryWrapper = QueryWrapper.create()
                .from(ChatHistory.class)
                .where(ChatHistory::getAppId).eq(appId);

        ChatHistory updateEntity = new ChatHistory();
        updateEntity.setIsDelete(1);
        updateEntity.setUpdateTime(LocalDateTime.now());

        return this.update(updateEntity, queryWrapper);
    }
}