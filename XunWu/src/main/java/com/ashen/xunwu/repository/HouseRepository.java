package com.ashen.xunwu.repository;

import com.ashen.xunwu.entity.House;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * 房屋JPA持久层接口
 * 继承PagingAndSortingRepository接口用于分页查询
 * 继承JpaSpecificationExecutor接口用于多条件查询
 */
public interface HouseRepository extends PagingAndSortingRepository<House, Long>, JpaSpecificationExecutor<House> {
    @Modifying
    @Query("update House as house set house.cover = :cover where house.id = :id")
    void updateCover(@Param(value = "id") Long id, @Param(value = "cover") String cover);

    @Modifying
    @Query("update House as house set house.status = :status where house.id = :id")
    void updateStatus(@Param(value = "id") Long id, @Param(value = "status") int status);

    @Modifying
    @Query("update House as house set house.watchTimes = house.watchTimes + 1 where house.id = :id")
    void updateWatchTimes(@Param(value = "id") Long houseId);
}
