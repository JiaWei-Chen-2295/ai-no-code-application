package fun.javierchen.ainocodeapplication.common;

import fun.javierchen.ainocodeapplication.constant.CommonConstant;
import lombok.Data;

import java.io.Serializable;

/**
 * 页面请求分页请求参数
 *
 * @author JavierChen
 * @date 2025/02/16
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = -1905982117102051621L;
    /**
     * 页面大小
     */
    private int pageSize = 10;
    /**
     * 当前页码
     */
    private int pageNum = 1;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;


}
