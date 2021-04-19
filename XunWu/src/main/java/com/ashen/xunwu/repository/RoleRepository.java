package com.ashen.xunwu.repository;

import com.ashen.xunwu.entity.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RoleRepository extends CrudRepository<Role, Long> {
    List<Role> findRolesByUserId(Long id);
}
