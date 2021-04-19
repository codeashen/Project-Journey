package com.ashen.xunwu.service.search;

import com.ashen.xunwu.service.ServiceMultiResult;
import com.ashen.xunwu.service.ServiceResult;
import com.ashen.xunwu.web.form.MapSearch;
import com.ashen.xunwu.web.form.RentSearch;
import com.ashen.xunwu.service.search.common.HouseBucketDTO;

import java.util.List;

/**
 * ES检索接口
 */
public interface ISearchService {
    /**
     * 索引目标房源
     * @param houseId
     */
    void index(Long houseId);

    /**
     * 移除房源索引
     * @param houseId
     */
    void remove(Long houseId);

    /**
     * 查询房源接口
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<Long> query(RentSearch rentSearch);

    /**
     * 获取补全建议关键词
     * @param prefix
     * @return
     */
    ServiceResult<List<String>> suggest(String prefix);

    /**
     * 聚合查询，聚合特定小区的房间数
     * @param cityEnName 城市名
     * @param regionEnName 地区名
     * @param district 小区名
     * @return
     */
    ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district);

    /**
     * 聚合城市数据
     * @param cityEnName
     * @return
     */
    ServiceMultiResult<HouseBucketDTO> mapAggregate(String cityEnName);

    /**
     * 城市级别查询
     */
    ServiceMultiResult<Long> mapQuery(String cityEnName, String orderBy,
                                      String orderDirection, int start, int size);
    /**
     * 精确范围数据查询
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<Long> mapQuery(MapSearch mapSearch);
}
