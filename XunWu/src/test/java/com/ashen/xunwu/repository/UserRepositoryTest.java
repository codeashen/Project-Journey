package com.ashen.xunwu.repository;

import com.ashen.xunwu.ApplicationTests;
import com.ashen.xunwu.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends ApplicationTests {
    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindOne() {
        User user = userRepository.findById(1L).orElse(new User());
        assertEquals("ashen", user.getName());
    }
}