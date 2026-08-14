package com.shoptourr.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "travelers")
public class TravelerEntity {

    @Id
    private UUID id;

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Column(nullable = false)
    private String name;

    @Column(name = "color_hex", nullable = false)
    private String colorHex;

    @Column(name = "avatar_glyph", nullable = false)
    private String avatarGlyph;

    @Column(name = "is_owner", nullable = false)
    private boolean owner;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTripId() { return tripId; }
    public void setTripId(UUID tripId) { this.tripId = tripId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public String getAvatarGlyph() { return avatarGlyph; }
    public void setAvatarGlyph(String avatarGlyph) { this.avatarGlyph = avatarGlyph; }
    public boolean isOwner() { return owner; }
    public void setOwner(boolean owner) { this.owner = owner; }
}
