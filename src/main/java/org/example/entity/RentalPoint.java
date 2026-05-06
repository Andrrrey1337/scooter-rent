package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "rental_points")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RentalPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rental_points_seq")
    @SequenceGenerator(name = "rental_points_seq", sequenceName = "rental_points_id_seq", allocationSize = 50)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "street", length = 50)
    private String street;

    @Column(name = "house_number", length = 10)
    private String houseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private RentalPoint parent;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 8)
    private BigDecimal longitude;

}
