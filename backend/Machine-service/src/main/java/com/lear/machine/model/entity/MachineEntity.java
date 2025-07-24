package com.lear.machine.model.entity;

import com.lear.machine.model.enums.MachineType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "MACHINE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MachineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false,unique = true)
    private String name;

    @Column(nullable = false)
    private String brand;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MachineType type;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<StepEntity> steps;

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private List<HistoryLogEntity> historyLogs;

    @ManyToOne
    @JoinColumn(name = "productionLine_id")
    private ProductionLineEntity productionLine;
} 
