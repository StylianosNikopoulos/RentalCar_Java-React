package com.example.rentalcars.features.reservation.domain.model;

import com.example.rentalcars.core.domain.AggregateRoot;
import com.example.rentalcars.core.valueobject.DateRange;
import com.example.rentalcars.core.valueobject.Money;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Reservation extends AggregateRoot {
    private UUID userId;
    private UUID vehicleId;
    private String vehicleName;
    private String vehicleBrand;
    private DateRange period;
    private Money totalAmount;
    private ReservationStatus status;
    private String email;

    @Builder
    public Reservation(UUID id, LocalDateTime createdAt, LocalDateTime updatedAt,
                       UUID userId, UUID vehicleId, String vehicleName, String vehicleBrand,
                       DateRange period, Money totalAmount, ReservationStatus status, String email) {
        super(id, createdAt, updatedAt);
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.vehicleBrand = vehicleBrand;
        this.period = period;
        this.totalAmount = totalAmount;
        this.status = status;
        this.email = email;
    }

    public void cancel() {
        if (this.status == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed reservation.");
        }
        this.status = ReservationStatus.CANCELED;
    }

    public void confirm() {
        if (this.status == ReservationStatus.CANCELED) {
            throw new IllegalStateException("Cannot confirm a canceled reservation.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void markAsCompleted() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE reservations can be completed.");
        }
        this.status = ReservationStatus.COMPLETED;
    }

    public void calculateTotal(Money dailyPrice) {
        if (this.period == null) {
            throw new IllegalStateException("Cannot calculate total without a period.");
        }
        long days = period.toDays();
        if (days <= 0) days = 1;  // At least 1 day
        BigDecimal totalValue = dailyPrice.amount().multiply(BigDecimal.valueOf(days));
        this.totalAmount = new Money(totalValue, dailyPrice.currency());
    }
}