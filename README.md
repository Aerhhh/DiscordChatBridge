# Discord Chat Bridge

A Hytale server plugin that bridges chat between Discord and your game server.

## Features

- Relay player chat messages to Discord and vice versa
- Configurable event notifications (join, leave, death, kills, world changes)
- Zone discovery notifications
- Customizable message templates
- Role colors displayed in-game
- Mention prevention (optional)

## Installation

1. Download the latest JAR and place it in your server's plugins folder.
2. Start the server to generate the default `config.json`.
3. Configure your Discord bot token and channel ID in `config.json`.
4. Restart the server.

## Configuration

| Option               | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `Enabled`            | Enable or disable the plugin's functionality                                |
| `RelayGameToDiscord` | Send game chat to Discord                                                   |
| `RelayDiscordToGame` | Send Discord chat to in-game chat                                           |
| `Debug`              | Emit kill/death events for non-player entities (for exmaple, local testing) |

### Discord Settings

| Option                  | Description                                                           |
|-------------------------|-----------------------------------------------------------------------|
| `BotToken`              | Your [Discord bot token](https://discord.com/developers/applications) |
| `ChannelId`             | The Discord channel ID to bridge                                      |
| `PresenceMessage`       | Bot status message                                                    |
| `IgnoreBotMessages`     | Ignore messages from other bots                                       |
| `IgnoreWebhookMessages` | Ignore webhook messages                                               |
| `AllowMentions`         | Allow @mentions from game chat                                        |
| `Locale`                | Locale for zone/region display names (e.g., `en-US`)                  |

### Events

Each event has its own section with `Enabled` and `Message` fields:
- `ServerStart` / `ServerStop`
- `PlayerJoin` / `PlayerLeave`
- `WorldEnter` / `WorldLeave` / `WorldChange`
- `PlayerDeath` / `PlayerKill`
- `ZoneDiscovery`

Available placeholders for messages:
- `%player%` - Player name
- `%world%` - World name
- `%cause%` - For death/kill events (e.g., creature or damage cause)
- `%killer%` / `%victim%` / `%item%` / `%projectile%` - For kill events

`PlayerKill` supports additional optional fields:

- `MessageWithItem`
- `MessageWithProjectile`
- `MessageWithProjectileUnknown`

Note: `Debug` mode only expands kill/death events to non-player entities (falls back to "Unknown Entity" when no name is
available). World enter/leave/change remain player-only.

- `%from%` / `%to%` - For world change events
- `%zone%` / `%region%` - For zone discovery events

Example:

```json
"Events": {
  "PlayerJoin": {
    "Enabled": true,
    "Message": ":inbox_tray: %player% joined the server."
  }
}
```