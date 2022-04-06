package com.snoopy.grpc.base.utils;

import org.slf4j.LoggerFactory;

/**
 * 基础日志工具类（可自己扩展）
 *
 * @author :   kehanjiang
 * @date :   2021/11/8  10:51
 */
public class LoggerBaseUtil {
    public static void info(Object object, String msg) {
        LoggerFactory.getLogger(object.getClass()).info(msg);
    }

    public static void info(Object object, String format, Object... arguments) {
        LoggerFactory.getLogger(object.getClass()).info(format, arguments);
    }

    public static void warn(Object object, String msg) {
        LoggerFactory.getLogger(object.getClass()).warn(msg);
    }

    public static void warn(Object object, String format, Object... arguments) {
        LoggerFactory.getLogger(object.getClass()).warn(format, arguments);
    }

    public static void debug(Object object, String msg) {
        LoggerFactory.getLogger(object.getClass()).debug(msg);
    }

    public static void debug(Object object, String format, Object... arguments) {
        LoggerFactory.getLogger(object.getClass()).debug(format, arguments);
    }

    public static void error(Object object, String msg) {
        LoggerFactory.getLogger(object.getClass()).error(msg);
    }

    public static void error(Object object, String msg, Throwable t) {
        LoggerFactory.getLogger(object.getClass()).error(msg, t);
    }

}
