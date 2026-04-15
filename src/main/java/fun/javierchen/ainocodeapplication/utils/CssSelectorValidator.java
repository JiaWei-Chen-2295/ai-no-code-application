package fun.javierchen.ainocodeapplication.utils;

import java.util.regex.Pattern;

/**
 * CSS 选择器白名单验证工具
 * 仅允许安全的 CSS 选择器字符，拒绝任何可能触发 XSS 或注入的内容
 *
 * @author JavierChen
 */
public final class CssSelectorValidator {

    /**
     * 仅允许 CSS 选择器中合法的字符集合：
     * 字母、数字、空白、连字符、下划线、#、.、>、+、~、:、(、)、[、]、=、*、^、$、|、"、,
     */
    private static final Pattern SAFE_SELECTOR_PATTERN =
            Pattern.compile("^[a-zA-Z0-9\\s\\-_#.>+~:(\\)\\[\\]=*^$|\",]+$");

    private CssSelectorValidator() {
    }

    /**
     * 校验 CSS 选择器是否合法安全
     *
     * @param selector 待校验的 CSS 选择器字符串
     * @return true 表示合法，false 表示包含非法字符
     */
    public static boolean isValid(String selector) {
        if (selector == null || selector.isBlank()) {
            return false;
        }
        return SAFE_SELECTOR_PATTERN.matcher(selector).matches();
    }
}
