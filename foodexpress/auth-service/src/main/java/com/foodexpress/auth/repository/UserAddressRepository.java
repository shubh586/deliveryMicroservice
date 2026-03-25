package com.foodexpress.auth.repository;

import com.foodexpress.auth.model.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress,String> {
    List<UserAddress> findByUserId(String userId);

    void deleteByIdAndUserId(String id, String userId);

}
