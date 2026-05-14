package com.crypto.service;

import com.crypto.dto.request.AddressRequest;
import com.crypto.dto.response.AddressResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AddressService {
    Page<AddressResponse> getUserAddresses(Pageable pageable, String blockchain, Long categoryId, Boolean favorite);
    AddressResponse getAddressById(Long id);
    AddressResponse createAddress(AddressRequest addressRequest);
    AddressResponse updateAddress(Long id, AddressRequest addressRequest);
    void deleteAddress(Long id);
    AddressResponse refreshAddressBalance(Long id);
    Page<AddressResponse> searchAddresses(String searchTerm, Pageable pageable);
    List<AddressResponse> getFavoriteAddresses();
    void initiateVerification(Long id);
    AddressResponse confirmVerification(Long id, String code);
}