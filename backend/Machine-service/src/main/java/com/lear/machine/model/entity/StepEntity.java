package com.lear.machine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "STEP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StepEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "machine_id", nullable = false)
    private MachineEntity machine;

    @Column(nullable = false,unique = true)
    private String name;

    private String description;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "requires_manual_tracking", nullable = false)
    private Boolean requiresManualTracking;
}
