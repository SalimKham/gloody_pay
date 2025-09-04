package com.zsoft.gloody.gloodypayroll.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;


@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "address_line1")
    private String addressLine1;
    @Column(name = "address_line2")
    private String addressLine2;
    @Column(name = "city")
    private String city;
    @Column(name = "state")
    private String state;
    @Column(name = "postal_code")
    private String postalCode;
    @Column(name = "country_code")
    private String countryCode;

    public String toString() {
        return  String.format("%s%s%s", Objects.nonNull(addressLine1) ? addressLine1 + ", " : "",  Objects.nonNull(addressLine2) ? addressLine2 + ", " : "", Objects.nonNull(city) ? city : "");
    }
}
