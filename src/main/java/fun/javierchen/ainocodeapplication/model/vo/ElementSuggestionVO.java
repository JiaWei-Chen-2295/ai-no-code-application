package fun.javierchen.ainocodeapplication.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 元素修改建议视图对象
 *
 * @author JavierChen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElementSuggestionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 建议标题
     */
    private String title;

    /**
     * 建议描述
     */
    private String description;

    /**
     * 优先级：1=高价值核心, 2=体验优化, 3=锦上添花
     */
    private Integer priority;
}
