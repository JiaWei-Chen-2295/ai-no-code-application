## Plan: Click-to-Generate Element Modification Suggestions

Users click a page element in the preview iframe, provide a modification instruction, and receive 3 AI-generated suggestions (title, description, priority). One-click apply feeds a suggestion into the existing `chatGenCode` flow. Supports all gen types (HTML, Multi-file, Vue).

---

### Phase 1: Backend — New Endpoint & AI Integration

Status: Implemented

**Step 1.1** — Create `ElementSuggestionRequest` DTO  
- New file: `src/main/java/fun/javierchen/ainocodeapplication/model/dto/app/ElementSuggestionRequest.java`  
- Follow `AppAddRequest` pattern (`@Data`, `Serializable`)  
- Fields: `appId` (Long), `message` (String), `cssSelector` (String), `elementContext` (String, optional), `codeGenType` (String)

**Step 1.2** — Create `ElementSuggestionVO` response VO  
- New file: `src/main/java/fun/javierchen/ainocodeapplication/model/vo/ElementSuggestionVO.java`  
- Follow `AppVO` pattern (`@Data`, `@Builder`, `Serializable`)  
- Fields: `title`, `description`, `priority` (1=High-value core, 2=Experience optimization, 3=Nice-to-have)

**Step 1.3** — Create `ElementSuggestionsResult` AI result model  
- New file: `src/main/java/fun/javierchen/ainocodeapplication/ai/model/ElementSuggestionsResult.java`  
- Follow `HtmlCodeResult` pattern with `@Description` annotations  
- Field: `suggestions` (List with inner title/description/priority)

**Step 1.4** — Create prompt template  
- New file: `src/main/resources/prompt/element-suggestion-prompt.txt`  
- Chinese system prompt (matching existing style) instructing AI to analyze the target element + user instruction → output exactly 3 structured suggestions with priority definitions

**Step 1.5** — Add dedicated suggestion AI service interface  
- New file: `src/main/java/fun/javierchen/ainocodeapplication/ai/ElementSuggestionAiService.java`  
- Non-streaming method with `@SystemMessage(fromResource = "prompt/element-suggestion-prompt.txt")` → returns `ElementSuggestionsResult`  
- Note: final implementation uses a dedicated interface instead of extending `AiCodeGeneratorService`, because `AiCodeGeneratorService` contains `@MemoryId` methods and LangChain4j requires `ChatMemoryProvider` for that interface at build time

**Step 1.6** — Update `AiCodeGeneratorServiceFactory` (*depends on 1.5*)  
- Modify: `src/main/java/fun/javierchen/ainocodeapplication/ai/AiCodeGeneratorServiceFactory.java`  
- Ensure non-streaming `ChatModel` is configured  
- Final implementation adds `createSuggestionService()` returning `ElementSuggestionAiService`

**Step 1.7** — Add service layer method (*depends on 1.3, 1.5*)  
- Modify: `src/main/java/fun/javierchen/ainocodeapplication/service/AppService.java` + `src/main/java/fun/javierchen/ainocodeapplication/service/impl/AppServiceImpl.java`  
- `generateElementSuggestions(request, loginUser)` → validates app ownership, constructs prompt (message + selector + context), calls AI, maps result to `List<ElementSuggestionVO>`

**Step 1.8** — Add controller endpoint (*depends on 1.1, 1.2, 1.7*)  
- Modify: `src/main/java/fun/javierchen/ainocodeapplication/controller/AppController.java`  
- `POST /app/element/suggestions` → `BaseResponse<List<ElementSuggestionVO>>`  
- Auth required, input validation: `message ≤ 2000`, `cssSelector ≤ 500` (regex whitelist), `elementContext ≤ 5000`

**Step 1.9** — CSS selector validation utility (*parallel with 1.1–1.4*)  
- New file: `src/main/java/fun/javierchen/ainocodeapplication/utils/CssSelectorValidator.java`  
- Regex whitelist: `[a-zA-Z0-9\s\-_#.>+~:()[\]=*^$|",]` — rejects `<script>`, event handlers, etc.

---

### Phase 2: Frontend — Element Picker & Suggestion UI

Status: Implemented

**Step 2.1** — Create element picker injection utility (*parallel with Phase 1*)  
- New file: `front/ai-no-code/src/utils/elementPicker.ts`  
- Injects into iframe `contentDocument`: devtools-style overlay highlight, click captures CSS selector + outerHTML (truncated 2000 chars)  
- Vue apps: additionally reads `el.__vueParentComponent?.type?.__file` for source file hint  
- Communicates via `postMessage` to parent window  
- `enable()` / `disable()` API, prevents default navigation and blocks UI click / pointer events while active  
- Final implementation uses same-origin iframe preview URLs (`/api/static/preview/...`) so picker can access `contentDocument`

**Step 2.2** — Add API method & types (*parallel with 2.1*)  
- Modify: `front/ai-no-code/src/api/appController.ts` — add `getElementSuggestions()`  
- Modify: `front/ai-no-code/src/api/typings.d.ts` — add `ElementSuggestionRequest` & `ElementSuggestionVO` types

**Step 2.3** — Update `AppChatPage.vue` (*depends on 2.1, 2.2*)  
- Modify: `front/ai-no-code/src/page/AppChatPage.vue`  
- **Preview header**: "选择元素" toggle button (colored indicator when active)  
- **On element selected**: Show suggestion input panel with selector info, text input for instruction, "获取建议" button  
- **Suggestions display**: 3 cards with priority badges (red/orange/blue for 1/2/3), title, description  
- **Apply button**: Feeds selector + element context + selected suggestion into existing `streamChat()` as user message  
- **Cleanup**: Remove listeners on unmount; disable picker on teardown; re-inject picker after iframe reload when picker mode remains active

---

### Element Context Strategy (per gen type)

| Gen Type | `cssSelector` | `elementContext` |
|----------|---------------|------------------|
| HTML | Full CSS path (`div#root > section > button:nth-of-type(1)`) | outerHTML snippet (truncated 2000 chars) |
| Multi-file | Full CSS path | outerHTML snippet (truncated 2000 chars) |
| Vue | Full CSS path | Component file path + line range hint via `__vueParentComponent` |

---

### Verification

1. Unit test `CssSelectorValidator` — valid selectors pass, XSS payloads (`<script>alert(1)</script>`) rejected
2. Integration test `POST /app/element/suggestions` — returns 3 suggestions with correct structure
3. **Manual — HTML app**: Preview → click "选择元素" → hover outline → click → enter instruction → 3 suggestions → click "Apply" → flows into chat
4. **Manual — Vue app**: Same flow, verify component file path captured
5. **Security**: Malicious selector rejected; XSS in elementContext sanitized (DOMPurify on frontend + length truncation)
6. **Edge cases**: Button disabled when no preview or during streaming; very long outerHTML truncated

Current verification state:

- Backend and frontend files compile in editor diagnostics after the changes
- Manual verification remains recommended for HTML and Vue preview flows

---

### Scope

- **Included**: Element picker, AI suggestion generation, one-click apply, all 3 gen types
- **Excluded**: Editing suggestions before apply, suggestion history persistence, batch apply, suggestion caching
