package net.aerh.discordbridge.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Configuration for event broadcast settings.
 */
public final class EventsConfig {

    public static final BuilderCodec<EventsConfig> CODEC = BuilderCodec
            .builder(EventsConfig.class, EventsConfig::new)
            .append(new KeyedCodec<>("ServerStart", EventMessageConfig.CODEC),
                    (cfg, value) -> cfg.serverStart = value,
                    cfg -> cfg.serverStart)
            .add()
            .append(new KeyedCodec<>("ServerStop", EventMessageConfig.CODEC),
                    (cfg, value) -> cfg.serverStop = value,
                    cfg -> cfg.serverStop)
            .add()
            .append(new KeyedCodec<>("PlayerJoin", EventMessageConfig.CODEC),
                    (cfg, value) -> cfg.playerJoin = value,
                    cfg -> cfg.playerJoin)
            .add()
            .append(new KeyedCodec<>("PlayerLeave", EventMessageConfig.CODEC),
                    (cfg, value) -> cfg.playerLeave = value,
                    cfg -> cfg.playerLeave)
            .add()
            .append(new KeyedCodec<>("WorldEnter", EventMessageConfig.CODEC),
                    (cfg, value) -> cfg.worldEnter = value,
                    cfg -> cfg.worldEnter)
            .add()
            .append(new KeyedCodec<>("WorldLeave", EventMessageConfig.CODEC),
                    (cfg, value) -> cfg.worldLeave = value,
                    cfg -> cfg.worldLeave)
            .add()
            .append(new KeyedCodec<>("WorldChange", EventMessageConfig.CODEC),
                    (cfg, value) -> cfg.worldChange = value,
                    cfg -> cfg.worldChange)
            .add()
            .append(new KeyedCodec<>("PlayerDeath", EventMessageConfig.CODEC),
                    (cfg, value) -> cfg.playerDeath = value,
                    cfg -> cfg.playerDeath)
            .add()
            .append(new KeyedCodec<>("PlayerKill", PlayerKillConfig.CODEC),
                    (cfg, value) -> cfg.playerKill = value,
                    cfg -> cfg.playerKill)
            .add()
            .append(new KeyedCodec<>("ZoneDiscovery", EventMessageConfig.CODEC),
                    (cfg, value) -> cfg.zoneDiscovery = value,
                    cfg -> cfg.zoneDiscovery)
            .add()
            .build();

    private static final String DEFAULT_SERVER_START = ":white_check_mark: Server is now online!";
    private static final String DEFAULT_SERVER_STOP = ":octagonal_sign: Server is shutting down.";
    private static final String DEFAULT_PLAYER_JOIN = ":inbox_tray: %player% joined the server.";
    private static final String DEFAULT_PLAYER_LEAVE = ":outbox_tray: %player% left the server.";
    private static final String DEFAULT_WORLD_ENTER = ":compass: %player% entered %world%.";
    private static final String DEFAULT_WORLD_LEAVE = ":door: %player% left %world%.";
    private static final String DEFAULT_WORLD_CHANGE = ":repeat: %player% moved from %from% to %to%.";
    private static final String DEFAULT_PLAYER_DEATH = ":skull: %player% died to %cause%.";
    private static final String DEFAULT_PLAYER_KILL = ":crossed_swords: %killer% killed %victim%.";
    private static final String DEFAULT_PLAYER_KILL_WITH_ITEM = ":crossed_swords: %killer% killed %victim% using %item%.";
    private static final String DEFAULT_PLAYER_KILL_WITH_PROJECTILE = ":bow_and_arrow: %killer% shot %victim% with %projectile%.";
    private static final String DEFAULT_PLAYER_KILL_WITH_PROJECTILE_UNKNOWN = ":bow_and_arrow: %killer% shot %victim%.";
    private static final String DEFAULT_ZONE_DISCOVERY = ":map: %player% discovered %zone% (%region%).";

    private EventMessageConfig serverStart = new EventMessageConfig(true, DEFAULT_SERVER_START);
    private EventMessageConfig serverStop = new EventMessageConfig(true, DEFAULT_SERVER_STOP);
    private EventMessageConfig playerJoin = new EventMessageConfig(true, DEFAULT_PLAYER_JOIN);
    private EventMessageConfig playerLeave = new EventMessageConfig(true, DEFAULT_PLAYER_LEAVE);
    private EventMessageConfig worldEnter = new EventMessageConfig(true, DEFAULT_WORLD_ENTER);
    private EventMessageConfig worldLeave = new EventMessageConfig(true, DEFAULT_WORLD_LEAVE);
    private EventMessageConfig worldChange = new EventMessageConfig(true, DEFAULT_WORLD_CHANGE);
    private EventMessageConfig playerDeath = new EventMessageConfig(true, DEFAULT_PLAYER_DEATH);
    private PlayerKillConfig playerKill = new PlayerKillConfig(
            true,
            DEFAULT_PLAYER_KILL,
            DEFAULT_PLAYER_KILL_WITH_ITEM,
            DEFAULT_PLAYER_KILL_WITH_PROJECTILE,
            DEFAULT_PLAYER_KILL_WITH_PROJECTILE_UNKNOWN
    );
    private EventMessageConfig zoneDiscovery = new EventMessageConfig(true, DEFAULT_ZONE_DISCOVERY);

    public EventMessageConfig getServerStart() {
        return serverStart;
    }

    public EventMessageConfig getServerStop() {
        return serverStop;
    }

    public EventMessageConfig getPlayerJoin() {
        return playerJoin;
    }

    public EventMessageConfig getPlayerLeave() {
        return playerLeave;
    }

    public EventMessageConfig getWorldEnter() {
        return worldEnter;
    }

    public EventMessageConfig getWorldLeave() {
        return worldLeave;
    }

    public EventMessageConfig getWorldChange() {
        return worldChange;
    }

    public EventMessageConfig getPlayerDeath() {
        return playerDeath;
    }

    public PlayerKillConfig getPlayerKill() {
        return playerKill;
    }

    public EventMessageConfig getZoneDiscovery() {
        return zoneDiscovery;
    }
}