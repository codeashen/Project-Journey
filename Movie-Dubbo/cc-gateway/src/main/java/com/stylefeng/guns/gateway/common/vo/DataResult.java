package com.stylefeng.guns.gateway.common.vo;

import lombok.Data;

/**
 * 同一响应值
 *
 * @param <D>
 */
@Data
public class DataResult<D> {
    // 响应状态码 【0-成功，1-业务失败，999-表示系统异常】
    private int status;
    // 返回信息
    private String msg;
    // 返回数据实体;
    private D data;
    // 图片前缀
    private String imgPre;

    // 分页使用
    private int nowPage;
    private int totalPage;

    private DataResult() {
    }

    public static <D> DataResult<D> success(D d) {
        DataResult<D> dataResult = new DataResult<>();
        dataResult.setStatus(0);
        dataResult.setData(d);
        return dataResult;
    }

    public static <D> DataResult<D> success(String msg) {
        DataResult<D> DataResult = new DataResult<>();
        DataResult.setStatus(0);
        DataResult.setMsg(msg);
        return DataResult;
    }

    public static <D> DataResult<D> success(int nowPage, int totalPage, String imgPre, D d) {
        DataResult<D> dataResult = new DataResult<>();
        dataResult.setStatus(0);
        dataResult.setData(d);
        dataResult.setImgPre(imgPre);
        dataResult.setTotalPage(totalPage);
        dataResult.setNowPage(nowPage);

        return dataResult;
    }

    public static <D> DataResult<D> success(String imgPre, D d) {
        DataResult<D> dataResult = new DataResult<>();
        dataResult.setStatus(0);
        dataResult.setData(d);
        dataResult.setImgPre(imgPre);

        return dataResult;
    }

    public static <D> DataResult<D> serviceFail(String msg) {
        DataResult<D> DataResult = new DataResult<>();
        DataResult.setStatus(1);
        DataResult.setMsg(msg);
        return DataResult;
    }

    public static <D> DataResult<D> appFail(String msg) {
        DataResult<D> DataResult = new DataResult<>();
        DataResult.setStatus(999);
        DataResult.setMsg(msg);
        return DataResult;
    }
}
