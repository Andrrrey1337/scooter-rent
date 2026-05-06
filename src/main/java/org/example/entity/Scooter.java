package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "scooters")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Scooter {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scooter_seq")
    @SequenceGenerator(name = "scooter_seq", sequenceName = "scooters_id_seq", allocationSize = 50)
    private Long id;

    @Column(name = "serial_number",  nullable = false, unique = true, length = 20)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private ScooterModel scooterModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_point_id")
    private RentalPoint rentalPoint;

    @Column(name = "battery_level", nullable = false)
    @Builder.Default
    private Integer batteryLevel = 0;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 8)
    private BigDecimal longitude;

    @Column(name = "status", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ScooterStatus scooterStatus = ScooterStatus.MAINTENANCE;

    @Column(name = "mileage", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal mileage = BigDecimal.ZERO;
}
