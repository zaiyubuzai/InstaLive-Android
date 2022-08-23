package com.venus.framework.rest;

import java.lang.reflect.Type;

/**
 * Created by ywu on 14-10-12.
 */
public interface HttpBodyParserFactory {

    /**
     * 为兼容AD模块暂时保留, 应统一使用Type
     *
     * @deprecated 使用Type代替Class
     */
    <T> HttpBodyParser<T> createParser(Class<T> type);

    <T> HttpBodyParser<T> createParser(Type type);

}
