package com.qianmi.common.config.domain;

import java.util.List;

/**
 * 结果信息实体Bean
 * Created by aqlu on 15/5/27.
 */
public class ResultInfo {

    public static String SUCCESSFUL_MSG = "Operation successful!";
    public static String FAILED_MSG = "Operation failed!";

    public static ResultInfo SUCCESSFUL() {
        return new ResultInfo(true, SUCCESSFUL_MSG);
    }

    public static ResultInfo FAILED() {
        return new ResultInfo(false, FAILED_MSG);
    }

    private boolean success;

    /**
     * <pre>
     * 结果码，六位数字设计: ABCDEF；
     * AB代表系统，00：通用系统；
     * CD代表模块，00：通用模块；
     * EF代表结果，00：成功，01：失败；
     *
     * 示例：{@link ResultCode}
     * 000000 -> 表示所有系统所有模块操作成功；
     * 000001 -> 表示所有系统所有模块操作失败；
     * </pre>
     */
    private String code;

    private String message;

    public List getDatas() {
        return datas;
    }

    public void setDatas(List datas) {
        this.datas = datas;
    }

    private List datas;

    public ResultInfo(boolean success) {
        this(success, success ? SUCCESSFUL_MSG : FAILED_MSG, success ? ResultCode.SUCCESSFUL : ResultCode.FAILED);
    }

    public ResultInfo(boolean success, String message) {
        this(success, message, success ? ResultCode.SUCCESSFUL : ResultCode.FAILED);
    }

    public ResultInfo(boolean success, String message, String code) {
        this.success = success;
        this.message = message;
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static final class ResultCode {

        /**
         * 操作成功；
         */
        public static String SUCCESSFUL = "000000";

        /**
         * 操作失败；
         */
        public static String FAILED = "000001";

        /**
         * 禁止访问；
         */
        public static String ACCESS_DENIED = "000002";

        /**
         * 修改密码时，验证旧密码失败；
         */
        public static String  CHANGE_PWD_WITH_WORNG_OLD_PWD = "000101";
    }
}
