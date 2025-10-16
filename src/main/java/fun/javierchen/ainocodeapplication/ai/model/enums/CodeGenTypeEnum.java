package fun.javierchen.ainocodeapplication.ai.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CodeGenTypeEnum {
    HTML("html", "单文件网页"),
    MUTI_FILE("mutiFile", "多文件"),
    VUE_PROJECT("vueProject", "vue 工程模式");

    private String type;
    private String typeName;

    /**
     * 根据类型获取枚举
     *
     * @param type
     * @return
     */
    public static CodeGenTypeEnum getEnumByType(String type) {
        for (CodeGenTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }
}
