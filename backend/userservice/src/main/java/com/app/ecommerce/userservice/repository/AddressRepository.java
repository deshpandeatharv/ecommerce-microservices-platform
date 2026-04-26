package com.app.ecommerce.userservice.repository;

import com.app.ecommerce.userservice.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
