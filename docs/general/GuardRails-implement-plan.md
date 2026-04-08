# Guardrails 安全防护实施计划

> **框架**: LangChain4j 1.1.0 Guardrails API  
> **目标**: 保护 AI 代码生成服务的输入/输出安全  
> **范围**: 所有生成类型（HTML、Multi-file、Vue）  
> **参考文档**: https://docs.langchain4j.dev/tutorials/guardrails

---

## 架构总览

```
User Input → [InputLengthGuardrail] → [PromptInjectionGuardrail] → [ContentModerationInputGuardrail]
    → LLM Call →
Output → [ContentSafetyOutputGuardrail] → [CodeExfiltrationOutputGuardrail] → User
```

Guardrails 通过 `AiServices.builder()` 注入，在 `AiCodeGeneratorServiceFactory.java` 中做 **统一的 Guardrails 配置**，但**保留两条现有 builder 分支**：

- `VUE_PROJECT` 分支：继续保留 `reasoningStreamingChatModel`、`tools(aiTools)`、`hallucinatedToolNameStrategy(...)`、`chatMemoryProvider(...)`
- `HTML / MUTI_FILE` 分支：继续保留 `chatModel(...)`、`streamingChatModel(...)`、`chatMemory(...)`

Guardrails 以“追加公共安全配置”的方式附加到这两条分支上，而不是把 3 种生成类型强行改成同一个 builder 链，避免丢失 Vue 现有的工具调用行为。

> **兼容性结论（当前阶段）**  
> 上游 LangChain4j `1.1.0` 文档已确认 `AiServices.builder()` 支持 `inputGuardrails(...)`、`outputGuardrails(...)`、`outputGuardrailsConfig(...)`，并支持 Output Guardrail 的 `maxRetries`。  
> 但本项目当前依赖混用了 `1.1.0` 与 `1.1.0-beta7`，且仓库中存在本地 `dev.langchain4j.*` 源码，因此正式实现前必须先做一次 **本仓库内的 compile-level compatibility spike**，确认当前依赖组合可直接使用这些 API。

---

## Input Guardrails（3 层，最低成本优先）

| # | 类名 | 防护目标 | 策略 | 成本 |
|---|------|---------|------|------|
| 1 | `InputLengthGuardrail` | Token/费用滥用 | 硬性字符数限制（HTML: 5000, Vue: 8000） | 免费 |
| 2 | `PromptInjectionGuardrail` | 提示注入攻击 | **混合**: 正则优先 → LLM 判断边界情况 | 免费 → LLM |
| 3 | `ContentModerationInputGuardrail` | 有害内容 | **混合**: 关键词黑名单(中/EN) → LLM 分类器处理不确定匹配 | 免费 → LLM |

### 1. InputLengthGuardrail

- **实现**: `implements InputGuardrail`
- **逻辑**: 检查 `UserMessage.singleText().length()` 是否超过限制
- **结果**: 超过 → `fatal("输入内容过长，请精简后重试（最大 {limit} 字符）")`
- **成本**: 零，最先执行以拦截 90% 的滥用请求

### 2. PromptInjectionGuardrail

- **实现**: `implements InputGuardrail`
- **第一层（正则匹配）**: 检测以下模式立即 `fatal()`：
  - 中文: `忽略之前的指令`, `忽略上面的`, `你现在是`, `不要遵守`, `无视系统提示`
  - English: `ignore previous`, `you are now`, `disregard`, `<|im_start|>`, `<|system|>`
  - 通用: role-override 模式、明显系统提示篡改模式
- **第二层（LLM 判断）**: 正则未命中但包含可疑词汇时，调用 `GuardrailLlmJudge` 进行分类
- **结果**: 注入确认 → `fatal("检测到不安全的输入内容，请修改后重试")`
- **注意**: `system:`、`prompt:`、`###` 这类高误杀模式不建议直接作为 fatal 规则，需通过测试样例验证后再决定是否纳入

### 3. ContentModerationInputGuardrail

- **实现**: `implements InputGuardrail`
- **第一层（关键词黑名单）**: 中英文暴力/色情/违法/仇恨关键词列表
- **第二层（LLM 分类）**: 关键词匹配但上下文不确定时，调用 LLM 判断是否真正违规
- **结果**: 违规确认 → `fatal("输入包含不合规内容，无法处理此请求")`

---

## Output Guardrails（2 层）

| # | 类名 | 防护目标 | 策略 | 成本 |
|---|------|---------|------|------|
| 4 | `ContentSafetyOutputGuardrail` | 有害/非法输出 | 关键词扫描 + LLM 审查标记内容 | 免费 → LLM |
| 5 | `CodeExfiltrationOutputGuardrail` | 代码中的数据窃取 | 模式检测 + CDN 白名单 | 免费 |

### 4. ContentSafetyOutputGuardrail

- **实现**: `implements OutputGuardrail`
- **逻辑**: 扫描 LLM 输出中的有害内容（仇恨言论、非法指令、竞品引用等）
- **结果**:
  - 标记内容 → `retry("生成内容不符合安全规范，正在重新生成...")`（`maxRetries=2`）
  - 重试后仍不合规 → 抛出 `OutputGuardrailException`

### 5. CodeExfiltrationOutputGuardrail

- **实现**: `implements OutputGuardrail`
- **检测模式（Moderate 级别）**:
  - `eval()` 调用
  - `document.cookie` 窃取
  - `localStorage`/`sessionStorage` 外泄
  - `fetch()`/`XMLHttpRequest` 请求到非白名单域名
  - 加密挖矿脚本（`coinhive`, `cryptonight`, `minero`）
  - Base64 混淆隐藏恶意载荷
  - 可疑 `<script src="">` 引用未知域名
- **CDN 白名单**: `picsum.photos`, `unpkg.com`, `cdnjs.cloudflare.com`, `cdn.jsdelivr.net`
- **结果**:
  - 明确威胁 → `fatal("生成的代码包含不安全的模式，已拦截")`
  - v1 暂不做 `successWith()` 自动剥离，避免“拦截成功但代码已损坏且仍按成功返回”的隐性问题

---

## 共享组件

### GuardrailLlmJudge

- 供 `PromptInjectionGuardrail` 和 `ContentModerationInputGuardrail` 共享
- 使用项目已有的 `ChatModel`（deepseek-chat）进行分类
- 输入: 待判断文本 + 判断类型（injection / moderation）
- 输出: boolean（是否违规）+ 原因
- 内置 system prompt 限制其只做分类判断
- 必须补充超时与失败策略，避免 Guardrail 本身成为生成链路的新瓶颈

---

## 集成方式（AiCodeGeneratorServiceFactory）

### 集成原则

1. **先保留分支差异，再叠加 Guardrails**
2. **Guardrails 是公共安全层，不替换原有模型 / Memory / Tool 配置**
3. **Vue 分支的工具调用能力必须完整保留**
4. **正式接入前先做 compatibility spike，验证本仓库实际可编译**

### Phase 0：Compatibility Spike（先做）

在当前仓库中先做最小化验证，不直接上完整实现：

- 创建一个 no-op `InputGuardrail`
- 创建一个 no-op `OutputGuardrail`
- 在 `AiServices.builder()` 中接入：
  - `inputGuardrails(...)`
  - `outputGuardrails(...)`
  - `outputGuardrailsConfig(...)`
- 执行编译验证当前依赖组合是否兼容

**目标**：先证明“本项目当前依赖 + 本地 patch 版 LangChain4j”能够接受这些 builder API，再进入正式开发。

如果 spike 失败，则实现计划切换为：

- 先统一 / 调整 LangChain4j 依赖版本，或
- 适配本地 patch 后的 builder / guardrail 接入方式

### Branch-safe Wiring（保留现有行为的接入方式）

不是把所有类型改造成同一个 builder，而是：

- 先构建 **Vue 专用 builder**
- 再构建 **HTML / Multi-file 专用 builder**
- 最后对这两个 builder 都追加同一套 guardrails 配置

伪代码如下：

```java
private AiServices<AiCodeGeneratorService> applyCommonGuardrails(
        AiServices<AiCodeGeneratorService> builder,
        CodeGenTypeEnum codeGenType,
        ChatModel judgeChatModel
) {
    var llmJudge = new GuardrailLlmJudge(judgeChatModel);

    var inputGuardrails = List.of(
            new InputLengthGuardrail(resolveMaxLength(codeGenType)),
            new PromptInjectionGuardrail(llmJudge),
            new ContentModerationInputGuardrail(llmJudge)
    );

    var outputGuardrails = List.of(
            new ContentSafetyOutputGuardrail(llmJudge),
            new CodeExfiltrationOutputGuardrail()
    );

    return builder
            .inputGuardrails(inputGuardrails.toArray(new InputGuardrail[0]))
            .outputGuardrails(outputGuardrails.toArray(new OutputGuardrail[0]))
            .outputGuardrailsConfig(OutputGuardrailsConfig.builder()
                    .maxRetries(2)
                    .build());
}
```

### 两条 builder 分支的明确接法

#### 1. Vue 分支

Vue 分支保持现有能力不变：

- `streamingChatModel(reasoningStreamingChatModel)`
- `chatMemoryProvider(memoryId -> chatMemory)`
- `tools(aiTools)`
- `hallucinatedToolNameStrategy(...)`

然后在 **build() 之前** 追加公共 Guardrails：

```java
case VUE_PROJECT -> applyCommonGuardrails(
        AiServices.builder(AiCodeGeneratorService.class)
                .streamingChatModel(reasoningStreamingChatModel)
                .chatMemoryProvider(memoryId -> chatMemory)
                .tools(aiTools)
                .hallucinatedToolNameStrategy(toolExecutionRequest ->
                        ToolExecutionResultMessage.from(
                                toolExecutionRequest,
                                "Error: there is no tool called " + toolExecutionRequest.name()
                        )
                ),
        codeGenType,
        chatModel
).build();
```

#### 2. HTML / Multi-file 分支

HTML / Multi-file 分支保持现有能力不变：

- `chatModel(chatModel)`
- `streamingChatModel(streamingChatModel)`
- `chatMemory(chatMemory)`

然后在 **build() 之前** 追加公共 Guardrails：

```java
case HTML, MUTI_FILE -> applyCommonGuardrails(
        AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory),
        codeGenType,
        chatModel
).build();
```

### 这样设计的原因

- **不破坏 Vue 的工具调用链路**
- **不把不同生成类型的模型配置硬揉成一个 builder**
- **Guardrails 的职责清晰：只负责安全，不接管业务分支差异**
- **后续新增生成类型时，只需新增分支 builder，再复用公共 Guardrails 接入**

---

## 实施阶段

### Phase 0：兼容性验证

- 验证本项目当前依赖和本地 `dev.langchain4j.*` patch 是否支持 builder 级 Guardrails API
- 产出 go / no-go 结论

### Phase 1：先接入空 Guardrails

- 在 `AiCodeGeneratorServiceFactory.java` 中引入“分支 builder + 公共 guardrails”结构
- 先接入 no-op guardrails，确保行为不变

### Phase 2：接入低成本 Input Guardrails

- `InputLengthGuardrail`
- `PromptInjectionGuardrail` 的正则快速路径
- `ContentModerationInputGuardrail` 的关键词快速路径

### Phase 3：接入 Output Guardrails

- `ContentSafetyOutputGuardrail`
- `CodeExfiltrationOutputGuardrail`
- 配置 `maxRetries=2`

### Phase 4：接入 LLM Judge

- 仅用于边界场景
- 增加超时、失败策略、日志与限流考虑

### Phase 5：测试与调优

- 单元测试
- 分支集成测试
- Vue 工具调用回归测试
- 误杀率调优

---

## 文件清单

### 新建文件

| 文件路径 | 用途 |
|---------|------|
| `src/.../ai/guardrails/InputLengthGuardrail.java` | 输入长度限制 |
| `src/.../ai/guardrails/PromptInjectionGuardrail.java` | 注入检测（混合模式） |
| `src/.../ai/guardrails/ContentModerationInputGuardrail.java` | 输入内容审核 |
| `src/.../ai/guardrails/ContentSafetyOutputGuardrail.java` | 输出内容安全 |
| `src/.../ai/guardrails/CodeExfiltrationOutputGuardrail.java` | 恶意代码检测 |
| `src/.../ai/guardrails/GuardrailLlmJudge.java` | 共享 LLM 分类器 |
| `src/test/.../guardrails/*Test.java` | 各 Guardrail 单元测试 |

### 修改文件

| 文件路径 | 修改内容 |
|---------|---------|
| `AiCodeGeneratorServiceFactory.java` | 引入“分支 builder + 公共 guardrails”接入方式 |
| `GlobalExceptionHandler.java` | 新增 `InputGuardrailException` / `OutputGuardrailException` 处理器，转换为业务错误码 || `AppController.java` | `chatGenCode()` 使用 `Flux.defer()` + `onErrorResume` 实现 SSE 流错误拦截与结构化错误帧 |
| `AppChatPage.vue` | `onmessage` 处理器新增 `chunk.error` 错误帧解析与中文错误提示 || `pom.xml` | 排除 `node_modules` / `.vite-temp` 避免 Maven resources 插件报错 |

---

## SSE 流式端点的错误传播

### 问题背景

`/api/app/chat/gen/code` 端点返回 `Flux<ServerSentEvent<String>>`。Spring 的 `@ExceptionHandler` **不能捕获响应式管道内部抛出的异常**，也无法处理 Flux 开始 emit 后的错误信号；同时，`appService.chatGenCode()` 内部可能在构建 `Flux` 之前就**同步抛出**业务异常（如参数校验失败），这类同步异常同样逃出 `onErrorResume` 的捕获范围。

### 解决方案

```
用 Flux.defer() 将整个 service 调用包裹为惰性流 →
    所有同步 throw 被 defer 转换为 reactive error signal →
        onErrorResume 统一捕获 → 发出结构化错误帧 →
            前端 EventSource.onmessage 解析 error 帧并展示
```

#### 1. `Flux.defer()` — 捕获同步异常

```java
// loginUser 必须在 defer 外部获取（Servlet 线程上的 HttpSession 访问）
User loginUser = userService.getLoginUser(request);

Flux<ServerSentEvent<String>> stream = Flux.defer(() -> {
    // 所有业务参数校验写在 defer 内部
    ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);

    // chatGenCode() 内部的同步 throw 也被 defer 捕获为 error signal
    Flux<String> contentFlux = appService.chatGenCode(appId, message, loginUser);
    return contentFlux.map(chunk -> ServerSentEvent.<String>builder()
            .data(toChunkJson(chunk)).build());
});
```

#### 2. `onErrorResume` — 统一错误 → SSE 错误帧

```java
Flux<ServerSentEvent<String>> withErrorHandling = stream.onErrorResume(e -> {
    int code;
    String msg;
    if (e instanceof InputGuardrailException) {
        code = ErrorCode.PARAMS_ERROR.getCode();
        msg  = extractGuardrailMessage(e.getMessage());
    } else if (e instanceof OutputGuardrailException) {
        code = ErrorCode.OPERATION_ERROR.getCode();
        msg  = extractGuardrailMessage(e.getMessage());
    } else if (e instanceof BusinessException be) {
        code = be.getCode();
        msg  = be.getMessage();
    } else {
        code = ErrorCode.SYSTEM_ERROR.getCode();
        msg  = "系统错误，请稍后重试";
        log.error("[chatGenCode] unexpected error", e);
    }
    String errorJson = String.format(
            "{\"error\":true,\"code\":%d,\"message\":\"%s\"}", code, msg);
    return Mono.just(ServerSentEvent.<String>builder().data(errorJson).build());
});

// 追加终止帧，确保 EventSource 总能收到 __DONE__ 后正常关闭
return withErrorHandling.concatWith(
        Mono.just(ServerSentEvent.<String>builder().data("__DONE__").build()));
```

#### 3. SSE 帧协议

| 帧类型 | 数据格式 | 说明 |
|-------|---------|------|
| 正常块 | `{"d":"<chunk>"}` | LLM 流式输出的每个 token |
| 错误帧 | `{"error":true,"code":<int>,"message":"<中文>"}` | Guardrail 拦截或业务错误 |
| 终止帧 | `"__DONE__"` | 流结束，客户端关闭 EventSource |

#### 4. 前端处理（AppChatPage.vue）

```typescript
eventSource.onmessage = (event) => {
  if (event.data === '__DONE__') {
    eventSource.close();
    isStreaming.value = false;
    return;
  }
  const chunk = JSON.parse(event.data);
  if (chunk.error) {
    // 显示业务错误提示，移除 AI 占位消息
    message.error(chunk.message);
    messages.value.pop();
    isStreaming.value = false;
    // 等待 __DONE__ 帧再关闭，无需手动 close()
    return;
  }
  // 追加正常 token
  messages.value[messages.value.length - 1].content += chunk.d;
};
```

### 为什么不依赖 GlobalExceptionHandler

| 场景 | GlobalExceptionHandler | Flux.defer + onErrorResume |
|------|----------------------|---------------------------|
| 普通 REST 端点（返回 JSON） | ✅ 有效 | 不适用 |
| SSE 端点流开始前的同步异常 | ✅ 有效 | ✅ 有效（defer 转换） |
| SSE 端点流中途的 reactive error | ❌ 无法捕获 | ✅ 有效 |

因此 SSE 端点**同时需要** `GlobalExceptionHandler`（处理 Servlet 层异常）和 `onErrorResume`（处理响应式管道内异常）。

---

## 错误消息（中文）

| Guardrail | 错误消息 |
|-----------|---------|
| InputLengthGuardrail | `"输入内容过长，请精简后重试（最大 {limit} 字符）"` |
| PromptInjectionGuardrail | `"检测到不安全的输入内容，请修改后重试"` |
| ContentModerationInputGuardrail | `"输入包含不合规内容，无法处理此请求"` |
| ContentSafetyOutputGuardrail | `"生成内容不符合安全规范，正在重新生成..."` |
| CodeExfiltrationOutputGuardrail | `"生成的代码包含不安全的模式，已拦截"` |

---

## 异常 → 业务错误码映射

框架抛出的 `InputGuardrailException` / `OutputGuardrailException` 通过 `GlobalExceptionHandler` 统一转换为业务错误，**不暴露框架内部堆栈**。

```
框架异常消息格式: "The guardrail <class> failed with this message: <业务消息>"
提取规则: 截取 "failed with this message: " 之后的部分作为用户可见消息
```

| 框架异常 | HTTP 语义 | ErrorCode | code | 说明 |
|---------|-----------|-----------|------|------|
| `InputGuardrailException` | 4xx 客户端错误 | `PARAMS_ERROR` | 40000 | 用户输入不合规（过长/注入/违规内容） |
| `OutputGuardrailException` | 5xx 操作失败 | `OPERATION_ERROR` | 50001 | 生成内容经重试仍不合规 |

**处理位置**: `GlobalExceptionHandler.java`

> **日志级别策略**: Guardrail 拦截使用 `log.warn` 而非 `log.error`，区别于真正的系统错误，便于监控时区分安全拦截与系统故障。

---

## 执行顺序设计理由

### Input 链
1. **长度检查**（即时，拦截 90% 滥用） → 2. **注入检测**（正则快速路径） → 3. **内容审核**（最贵放最后）

### Output 链
4. **内容安全**（可 `retry()` 重新生成） → 5. **代码窃取检测**（v1 先 `fatal()`，避免自动剥离导致结果损坏）

---

## 待决事项

- [x] 先完成 compile-level compatibility spike，确认当前依赖组合可直接接入 Guardrails API
- [x] `InputGuardrailException` / `OutputGuardrailException` 转换为业务错误（`GlobalExceptionHandler`）
- [ ] 明确 LLM Judge 的超时、失败策略（当前 fail-open，超时未限定）
- [ ] 为 Vue 分支补充“工具调用不回退”的回归测试
- [ ] 考虑是否需要将违规记录写入数据库用于后续分析
- [ ] CDN 白名单是否需要可配置化（application.yml）
