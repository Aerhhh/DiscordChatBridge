package net.aerh.discordbridge.discord.events;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Kill/death feed based on death components, with optional debug.
 */
public final class KillFeed extends RefChangeSystem<EntityStore, DeathComponent> {

    private final ComponentType<EntityStore, PlayerRef> playerRefComponent = PlayerRef.getComponentType();
    private final ComponentType<EntityStore, DisplayNameComponent> displayNameComponent = DisplayNameComponent.getComponentType();
    private final KillFeedFormatter killFeed;
    private final Supplier<Boolean> debugSupplier;

    public KillFeed(
            @NotNull KillFeedFormatter killFeed,
            @NotNull Supplier<Boolean> debugSupplier
    ) {
        this.killFeed = killFeed;
        this.debugSupplier = debugSupplier;
    }

    @NotNull
    @Override
    public ComponentType<EntityStore, DeathComponent> componentType() {
        return DeathComponent.getComponentType();
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void onComponentAdded(
            @NotNull Ref<EntityStore> ref,
            @NotNull DeathComponent component,
            @NotNull Store<EntityStore> store,
            @NotNull CommandBuffer<EntityStore> commandBuffer
    ) {
        Damage damage = component.getDeathInfo();
        if (damage == null) {
            return;
        }

        PlayerRef victimPlayer = store.getComponent(ref, playerRefComponent);
        if (victimPlayer == null && !isDebugEnabled()) {
            return;
        }

        DisplayNameComponent displayName = store.getComponent(ref, displayNameComponent);
        killFeed.dispatchDeathMessage(damage, victimPlayer, displayName, store);
    }

    @Override
    public void onComponentSet(
            @NotNull Ref<EntityStore> ref,
            DeathComponent oldComponent,
            @NotNull DeathComponent newComponent,
            @NotNull Store<EntityStore> store,
            @NotNull CommandBuffer<EntityStore> commandBuffer
    ) {
    }

    @Override
    public void onComponentRemoved(
            @NotNull Ref<EntityStore> ref,
            @NotNull DeathComponent component,
            @NotNull Store<EntityStore> store,
            @NotNull CommandBuffer<EntityStore> commandBuffer
    ) {
    }

    private boolean isDebugEnabled() {
        Boolean debug = debugSupplier.get();
        return debug != null && debug;
    }
}