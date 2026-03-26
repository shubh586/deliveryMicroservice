package com.foodexpress.auth.repository;

import com.foodexpress.auth.model.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress,String> {
    List<UserAddress> findByUserId(String userId);

    void deleteByIdAndUserId(String id, String userId);

}
