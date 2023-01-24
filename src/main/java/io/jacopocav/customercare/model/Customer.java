package io.jacopocav.customercare.model;

import static jakarta.persistence.FetchType.LAZY;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Customer extends CommonModel {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false, length = 16)
    private String fiscalCode;
    @Column(nullable = false)
    private String address;
    @OneToMany(fetch = LAZY)
    private List<Device> devices;
}
