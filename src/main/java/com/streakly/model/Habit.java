package com.streakly.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "habits")
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(nullable = false)
    private LocalDate createdOn;

    protected Habit() {
    }

    public Habit(String name, Frequency frequency) {
        this.name = name;
        this.frequency = frequency;
        this.createdOn = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public boolean isArchived() {
        return archived;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
