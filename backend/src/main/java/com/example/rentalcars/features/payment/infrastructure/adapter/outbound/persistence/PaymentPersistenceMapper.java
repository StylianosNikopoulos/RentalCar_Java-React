package com.example.rentalcars.features.payment.infrastructure.adapter.outbound.persistence;

import com.example.rentalcars.features.payment.domain.model.Payment;
import com.example.rentalcars.core.valueobject.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {Money.class})
public interface PaymentPersistenceMapper {
    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    PaymentJpaEntity toEntity(Payment payment);

    @Mapping(target = "amount", expression = "java(new Money(entity.getAmount(), entity.getCurrency()))")
    @Mapping(target = "userEmail", source = "reservation.user.email")
    Payment toDomain(PaymentJpaEntity entity);
}
