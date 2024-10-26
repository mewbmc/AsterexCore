package io.starseed.asteroidCore.modules.planet.models;

import java.util.UUID;

public class PlanetVisitor {
    private final UUID visitorUuid;
    private final UUID planetOwner;
    private final long visitStart;
    private boolean allowed;

    public PlanetVisitor(UUID visitorUuid, UUID planetOwner) {
        this.visitorUuid = visitorUuid;
        this.planetOwner = planetOwner;
        this.visitStart = System.currentTimeMillis();
        this.allowed = true;
    }

    // Getters and setters
    public UUID getVisitorUuid() { return visitorUuid; }
    public UUID getPlanetOwner() { return planetOwner; }
    public long getVisitStart() { return visitStart; }
    public boolean isAllowed() { return allowed; }
    public void setAllowed(boolean allowed) { this.allowed = allowed; }
}
