package com.campusconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 100)
    private String building;

    @Column(name = "room_number", length = 50)
    private String roomNumber;

    @Column(nullable = false)
    private Integer capacity = 100;

    @Column(length = 255)
    private String facilities;

    @Column(nullable = false)
    private boolean active = true;

    public Venue() {}

    public Venue(String name, String building, String roomNumber, Integer capacity, String facilities) {
        this.name = name;
        this.building = building;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.facilities = facilities;
        this.active = true;
    }

    public String getDisplayName() {
        if (building != null && !building.isBlank()) {
            return name + " (" + building + (roomNumber != null ? ", " + roomNumber : "") + ")";
        }
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

