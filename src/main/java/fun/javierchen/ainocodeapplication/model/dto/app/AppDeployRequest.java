package fun.javierchen.ainocodeapplication.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 部署版本号（可选，不传则部署最新版本）
     */
    private Integer version;

    private static final long serialVersionUID = 1L;
}
