package com.ashen.xunwu.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 地区
 */
@Data
@Entity
@Table(name = "support_address")
public class SupportAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 上一级行政单位
    @Column(name = "belong_to")
    private String belongTo;

    @Column(name = "en_name")
    private String enName;

    @Column(name = "cn_name")
    private String cnName;

    // 行政级别
    private String level;

    // 百度地图经度
    @Column(name = "baidu_map_lng")
    private double baiduMapLongitude;
    // 百度地图纬度
    @Column(name = "baidu_map_lat")
    private double baiduMapLatitude;

    /**
     * 行政级别定义
     */
    public enum Level {
        CITY("city"),       // 城市
        REGION("region");   // 区域

        private String value;

        Level(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Level of(String value) {
            for (Level level : Level.values()) {
                if (level.getValue().equals(value)) {
                    return level;
                }
            }
            throw new IllegalArgumentException();
        }
    }
}
