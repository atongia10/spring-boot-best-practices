package com.gs.tax.utilityproject.repository;

import com.gs.tax.utilityproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
