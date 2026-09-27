package com.example.rentalcars.features.payment.infrastructure.adapter.inbound.rest.mapper;

import com.example.rentalcars.features.payment.domain.model.Payment;
import com.example.rentalcars.features.payment.infrastructure.adapter.inbound.rest.dto.PaymentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentRestMapper {

    PaymentResponse toResponse(Payment domain);
}