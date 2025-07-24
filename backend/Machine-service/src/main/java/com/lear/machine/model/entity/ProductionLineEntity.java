package com.lear.machine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "PRODUCTION_LINE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductionLineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false,unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "productionLine", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private List<MachineEntity> machines;
} 
