package fun.javierchen.ainocodeapplication.core.parser;

/**
 * 根据泛型进行选择使用的解析方式
 * @param <T>
 */
public interface CodeParser<T> {

    T parserCode(String codeContent);

}
