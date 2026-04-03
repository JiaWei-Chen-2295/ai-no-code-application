生成的Vue文件的最后会多一个 template 标签 已解决
## 构建可观测
高: Vue 自动修复链路把“构建失败”吞掉了，外层仍然按成功收尾，状态会失真。
AiGenerateServiceFacade.java (line 193) 在 Flux.concat(...) 后无条件 doOnComplete，
而 AiGenerateServiceFacade.java (line 236) 
到 AiGenerateServiceFacade.java (line 238) 
在修复失败后只是返回提示文本，没有抛错。
结果是聊天记录、版本状态、前端“生成完成”语义都会被标成成功，
但项目其实仍然不能 build。这不是代码风格问题，而是状态语义错误。
[X]已解决
## 前端防注入攻击
中: 前端把后端返回的构建错误原文直接塞进 markdown，再用 v-html 渲染，存在注入风险。AppChatPage.vue (line 528) 把 errorMsg 原样拼进消息内容，AppChatPage.vue (line 67) 用 v-html 输出，而 AppChatPage.vue (line 783) 的 markdown 渲染没有看到对普通 HTML 做统一转义。构建日志里如果带有源码片段或恶意 HTML，理论上会被当成富文本插进页面。
[X]已解决
## 小程序的实现
[]已解决