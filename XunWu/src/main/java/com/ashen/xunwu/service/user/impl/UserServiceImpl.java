package com.ashen.xunwu.service.user.impl;

import com.ashen.xunwu.entity.Role;
import com.ashen.xunwu.service.ServiceResult;
import com.ashen.xunwu.service.user.IUserService;
import com.google.common.collect.Lists;
import com.ashen.xunwu.base.LoginUserUtil;
import com.ashen.xunwu.entity.User;
import com.ashen.xunwu.repository.RoleRepository;
import com.ashen.xunwu.repository.UserRepository;
import com.ashen.xunwu.web.dto.UserDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public User findUserByName(String username) {
        // 查询用户
        User user = userRepository.findByName(username);
        if (user == null) {
            return null;
        }
        // 查询用户权限
        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            throw new DisabledException("权限非法");
        }
        // 设置用户权限列表
        List<GrantedAuthority> authorities = roles.stream()
                .map(e -> new SimpleGrantedAuthority("ROLE_" + e.getName())).collect(Collectors.toList());
        user.setAuthorityList(authorities);
        
        return user;
    }

    @Override
    public ServiceResult<UserDTO> findById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ServiceResult.notFound();
        }
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        return ServiceResult.of(userDTO);
    }

    /**
     * 通过手机号查询用户
     * @param telephone
     * @return
     */
    @Override
    public User findUserByPhone(String telephone) {
        // 查询用户信息
        User user = userRepository.findUserByPhoneNumber(telephone);
        if (user == null) {
            return null;
        }
        // 查询用户权限
        List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            throw new DisabledException("权限非法");
        }
        
        // 填充security用户权限
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
        user.setAuthorityList(authorities);
        return user;
    }

    /**
     * 通过手机号添加用户
     * @param telephone
     * @return
     */
    @Override
    @Transactional
    public User addUserByPhone(String telephone) {
        User user = new User();
        user.setPhoneNumber(telephone);
        user.setName(telephone.substring(0, 3) + "****" + telephone.substring(7, telephone.length()));
        Date now = new Date();
        user.setCreateTime(now);
        user.setLastLoginTime(now);
        user.setLastUpdateTime(now);
        user = userRepository.save(user);

        Role role = new Role();
        role.setName("USER");
        role.setUserId(user.getId());
        roleRepository.save(role);
        user.setAuthorityList(Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER")));
        return user;
    }

    /**
     * 修改指定用户信息
     * @param profile
     * @param value
     * @return
     */
    @Override
    @Transactional
    public ServiceResult<String> modifyUserProfile(String profile, String value) {
        Long userId = LoginUserUtil.getLoginUserId();
        if (profile == null || profile.isEmpty()) {
            return new ServiceResult<>(false, "属性不可以为空");
        }
        switch (profile) {
            case "name":
                userRepository.updateUsername(userId, value);
                break;
            case "email":
                userRepository.updateEmail(userId, value);
                break;
            case "password":
                userRepository.updatePassword(userId, this.passwordEncoder.encode(value));
                break;
            default:
                return new ServiceResult<>(false, "不支持的属性");
        }
        return ServiceResult.success();
    }
}
