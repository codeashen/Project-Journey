package com.ashen.xunwu.repository;

import com.ashen.xunwu.entity.SupportAddress;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SupportAddressRepository extends CrudRepository<SupportAddress, Long> {
    // 根据行政级别获取地址信息
    List<SupportAddress> findAllByLevel(String level);
    // 根据英文简称和行政级别查询
    SupportAddress findByEnNameAndLevel(String enName, String level);
    // 根据英文简称和归属地查询
    SupportAddress findByEnNameAndBelongTo(String enName, String belongTo);
    // 根据行政级别和归属地查询列表
    List<SupportAddress> findAllByLevelAndBelongTo(String level, String belongTo);
}
