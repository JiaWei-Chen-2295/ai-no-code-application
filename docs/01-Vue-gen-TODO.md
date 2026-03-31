Vue 项目生成流程分析与优化规划
现状分析
生成流水线：

核心文件链路：

前端：ChatPage.vue → EventSource SSE → 展示流式输出 + iframe预览
后端：AppController.streamChat() → AppService.streamChat() → TokenStream2FluxAdaptor → AI Tool调用
工具链：VueProjectBuilder.buildVueProject() → ProcessBuilder("npm", "run", "build") → 部署
发现的关键问题
1. 构建脆弱性（最严重）
template_vue/package.json 的 build 脚本：

run-p 并行执行 vue-tsc --build（TypeScript类型检查）和 vite build。AI生成的代码几乎不可能通过严格的TS类型检查，导致构建必然失败，用户看不到任何结果。

修复：将 build 脚本改为只运行 vite build，跳过类型检查：

2. 磁盘空间浪费
TemplateCopyUtil.java 中 createSymbolicLink 在 Windows 上可能静默失败（需要管理员权限或开发者模式），失败后回退到 copyDirectory，导致每个项目 ~195MB（node_modules完整复制）。当前已有十几个项目，占用数GB。

修复方案：

使用 mklink /J（Junction）替代符号链接，不需要管理员权限
添加失败日志，不要静默回退
提供清理脚本，将已有的完整复制替换为junction
3. 构建速度慢
每次 VueProjectBuilder.buildVueProject() 都运行完整的 npm run build，包括：

TypeScript编译检查
Vite完整打包
对于AI迭代场景（用户可能连续修改多次），每次都全量构建太慢。

优化方案：

去掉 vue-tsc（如上）可节省约50%时间
考虑用 vite build --mode development 跳过压缩
长远：预热 Vite 缓存，或用 esbuild-only 模式
4. 错误处理链路断裂
VueProjectBuilder.java 中：

构建失败时返回的错误信息可能不够友好，且SSE流可能已经关闭，用户端无法得知失败原因。

ChatPage.vue 中 EventSource 的 onerror 处理简单粗暴，没有区分可恢复/不可恢复错误。

修复：

VueProjectBuilder 捕获 stderr 输出，返回有意义的错误信息给AI
构建失败时让AI可以自动重试修复
前端增加失败状态展示
5. 版本管理低效
AppService.java 中每个版本（v1, v2, v3...）都完整复制模板+src，每个版本独立构建。

优化：

版本间只保存diff（变更的文件），共享 node_modules/public/配置文件
或者只保留最新2个版本，旧版本只保留dist产物
6. Prompt 优化空间
vue-prompt.txt 需要更明确地约束AI输出格式，减少生成无效代码的概率，比如：

明确告知可用的依赖（vue, vue-router, pinia 等）
强制使用 <script setup lang="ts"> 但不要求严格类型
限制文件路径必须在 src 下
优先级排序
优先级	问题	影响	工作量
P0	build脚本去掉vue-tsc	构建成功率从30%→90%	改1行
P0	symlink失败改用Junction	每个项目节省195MB	改1个方法
P1	构建错误回传给AI自修复	用户体验大幅提升	中等
P1	前端错误状态展示	用户不再面对空白	中等
P2	版本间共享资源	磁盘+速度	较大
P2	Prompt精细化	减少无效生成	小
P3	开发模式构建加速	迭代速度	小
建议立即执行（P0）
要我现在就修复这两个 P0 问题吗？ 改动非常小：

template_vue/package.json + web-template/package.json："build": "vite build"
TemplateCopyUtil.java：symlink 失败时使用 mklink /J（Windows Junction）