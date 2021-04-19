package com.ashen.xunwu.web.form;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 房屋表格查询表单
 */
@Data
public class DatatableSearch {
    // Datatables要求回显字段
    private int draw;
    // Datatables规定分页字段
    private int start;
    private int length;

    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createTimeMin;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createTimeMax;

    private String city;
    private String title;
    private String direction;   //升序还是降序
    private String orderBy;     //排序的字段
}
