package com.example.clouddbbe.module.user.repository;

import com.example.clouddbbe.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {

    boolean existsByEmail(String email);

}
