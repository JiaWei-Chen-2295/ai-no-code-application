package fun.javierchen.ainocodeapplication.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用版本视图对象
 *
 * @author JavierChen
 */
@Data
public class AppVersionVO implements Serializable {

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 版本创建时间
     */
    private LocalDateTime createTime;

    /**
     * 版本消息内容（AI生成的代码内容摘要）
     */
    private String message;

    /**
     * 是否为当前部署版本
     */
    private Boolean isDeployed;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 预览URL
     */
    private String previewUrl;

    /**
     * 对话历史ID
     */
    private Long chatHistoryId;

    private static final long serialVersionUID = 1L;
}
