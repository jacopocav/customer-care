package io.jacopocav.customercare.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Device extends CommonModel {
    @Id
    @GeneratedValue
    private UUID id;
    @Enumerated(STRING)
    @Column(nullable = false)
    private Status status;
    @Column(nullable = false, length = 6)
    private String color;

    @ManyToOne(fetch = LAZY)
    private Customer customer;

    public enum Status {
        ACTIVE, INACTIVE, LOST
    }
}
