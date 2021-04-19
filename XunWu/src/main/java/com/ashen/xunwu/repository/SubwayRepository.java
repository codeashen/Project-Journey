package com.ashen.xunwu.repository;

import com.ashen.xunwu.entity.Subway;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SubwayRepository extends CrudRepository<Subway, Long> {
    List<Subway> findAllByCityEnName(String cityEnName);
}
