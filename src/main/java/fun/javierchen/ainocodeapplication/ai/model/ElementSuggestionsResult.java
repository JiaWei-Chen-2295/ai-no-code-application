package fun.javierchen.ainocodeapplication.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.util.List;

@Description("分析页面元素并生成修改建议的结果")
@Data
public class ElementSuggestionsResult {

    @Description("针对目标元素的修改建议列表，必须恰好包含 3 条建议")
    private List<ElementSuggestion> suggestions;

    @Description("单条元素修改建议")
    @Data
    public static class ElementSuggestion {

        @Description("建议标题，简洁明了，不超过20个字")
        private String title;

        @Description("建议描述，详细说明如何修改以及预期效果，不超过100个字")
        private String description;

        @Description("优先级：1=高价值核心（直接提升核心功能或用户体验），2=体验优化（改善交互或视觉细节），3=锦上添花（可有可无的增强）")
        private Integer priority;
    }
}
