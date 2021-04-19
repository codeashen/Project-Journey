package com.ashen.xunwu.web.form;

import lombok.Data;

/**
 * 租房请求参数结构体
 */
@Data
public class RentSearch {
    // 城市
    private String cityEnName;
    // 地区
    private String regionEnName;
    // 价格区间
    private String priceBlock;
    // 面积区间
    private String areaBlock;
    // 房屋朝向
    private int direction;
    // 房间
    private int room;
    // 关键词
    private String keywords;
    // 租住方式
    private int rentWay = -1;
    
    // 排序字段
    private String orderBy = "lastUpdateTime";
    private String orderDirection = "desc";
    // 分页字段
    private int start = 0;
    private int size = 5;
    
    public int getSize() {
        if (this.size < 1) {
            return 5;
        } else {
            return Math.min(this.size, 100);
        }
    }

    public int getRentWay() {
        if (rentWay > -2 && rentWay < 2) {
            return rentWay;
        } else {
            return -1;
        }
    }
}
