package com.ashen.xunwu.service.house;

import com.ashen.xunwu.service.ServiceResult;
import com.ashen.xunwu.base.SubscribeStatusEnum;
import com.ashen.xunwu.service.ServiceMultiResult;
import com.ashen.xunwu.web.dto.HouseDTO;
import com.ashen.xunwu.web.dto.HouseSubscribeDTO;
import com.ashen.xunwu.web.form.DatatableSearch;
import com.ashen.xunwu.web.form.HouseForm;
import com.ashen.xunwu.web.form.MapSearch;
import com.ashen.xunwu.web.form.RentSearch;
import org.springframework.data.util.Pair;

import java.util.Date;

/**
 * 房屋管理服务接口
 */
public interface IHouseService {

    ServiceResult<HouseDTO> save(HouseForm houseForm);

    ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody);

    // 查询完整房源信息
    ServiceResult<HouseDTO> findCompleteOne(Long id);

    ServiceResult<HouseDTO> update(HouseForm houseForm);

    // 移除图片
    ServiceResult<String> removePhoto(Long id);

    // 更新封面
    ServiceResult<String> updateCover(Long coverId, Long targetId);

    // 新增标签
    ServiceResult<String> addTag(Long houseId, String tag);

    // 移除标签
    ServiceResult<String> removeTag(Long houseId, String tag);

    // 更新房源状态
    ServiceResult<String> updateStatus(Long id, int status);

    // 查询房源信息
    ServiceMultiResult<HouseDTO> query(RentSearch rentSearch);

    // 加入预约清单
    ServiceResult<String> addSubscribeOrder(Long houseId);

    // 获取对应状态的预约列表
    ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> querySubscribeList(SubscribeStatusEnum status, int start, int size);

    // 预约看房时间
    ServiceResult<String> subscribe(Long houseId, Date orderTime, String telephone, String desc);

    // 取消预约
    ServiceResult<String> cancelSubscribe(Long houseId);

    // 管理员查询预约信息接口
    ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> findSubscribeList(int start, int size);

    // 完成预约
    ServiceResult<String> finishSubscribe(Long houseId);

    // 全地图查询
    ServiceMultiResult<HouseDTO> wholeMapQuery(MapSearch mapSearch);

    // 精确范围数据查询
    ServiceMultiResult<HouseDTO> boundMapQuery(MapSearch mapSearch);
}
