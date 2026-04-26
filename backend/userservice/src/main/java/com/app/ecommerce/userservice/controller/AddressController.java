package com.app.ecommerce.userservice.controller;

import com.app.ecommerce.userservice.model.User;
import com.app.ecommerce.userservice.payload.AddressDTO;
import com.app.ecommerce.userservice.service.AddressService;
import com.app.ecommerce.userservice.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressDTO> createAddress(
            @Valid @RequestBody AddressDTO addressDTO) {

        User user = authUtil.loggedInUser();
        AddressDTO saved = addressService.createAddress(addressDTO, user);

        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAllAddresses() {
        return ResponseEntity.ok(addressService.getAddresses());
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(
            @PathVariable Long addressId) {

        return ResponseEntity.ok(addressService.getAddressesById(addressId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<AddressDTO>> getMyAddresses() {

        User user = authUtil.loggedInUser();
        return ResponseEntity.ok(addressService.getUserAddresses(user));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable Long addressId,
            @RequestBody AddressDTO addressDTO) {

        return ResponseEntity.ok(
                addressService.updateAddress(addressId, addressDTO)
        );
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable Long addressId) {

        return ResponseEntity.ok(
                addressService.deleteAddress(addressId)
        );
    }
}