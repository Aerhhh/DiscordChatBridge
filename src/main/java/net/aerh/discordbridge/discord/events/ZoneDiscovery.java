package net.aerh.discordbridge.discord.events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.DiscoverZoneEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.jetbrains.annotations.NotNull;

public final class ZoneDiscovery extends EntityEventSystem<EntityStore, DiscoverZoneEvent.Display> {

    private final ComponentType<EntityStore, PlayerRef> playerRefComponent = PlayerRef.getComponentType();
    private final ZoneDiscoverySender sender;

    public ZoneDiscovery(@NotNull ZoneDiscoverySender sender) {
        super(DiscoverZoneEvent.Display.class);
        this.sender = sender;
    }

    @NotNull
    @Override
    public Query<EntityStore> getQuery() {
        return this.playerRefComponent;
    }

    @Override
    public void handle(
            int index,
            @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
            @NonNullDecl DiscoverZoneEvent.Display event
    ) {
        PlayerRef playerRef = archetypeChunk.getComponent(index, this.playerRefComponent);
        if (playerRef == null) {
            return;
        }

        WorldMapTracker.ZoneDiscoveryInfo info = event.getDiscoveryInfo();
        sender.send(playerRef, info);
    }

    @FunctionalInterface
    public interface ZoneDiscoverySender {
        void send(@NotNull PlayerRef player, @NotNull WorldMapTracker.ZoneDiscoveryInfo info);
    }
}