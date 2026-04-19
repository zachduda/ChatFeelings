# ChatFeelings — Patched Fork

***This is a fork*** of [ChatFeelings by zach_attack](https://www.spigotmc.org/resources/chatfeelings.12987/).

> **Original plugin:** https://github.com/zachduda/ChatFeelings
> **License:** [Creative Commons CC-BY-NC-4.0](https://creativecommons.org/licenses/by-nc/4.0/) — credit must be given, non-commercial use only.

---

## What's changed in this fork

**Fixes to `ChatFeelingsAPI` and `FeelingSendEvent`:**

- Fixed a `NullPointerException` crash when calling `getSendersMessage()`, `getTargetsMessage()`, or `getGlobalEmoteMessage()` via the API (e.g. from DiscordSRV alerts)
- Root cause: `getSenderEmoteMessage()` was looking up YAML keys in lowercase (e.g. `stab`) but `emotes.yml` uses Title Case keys (e.g. `Stab`), causing the lookup to return null
- All three message methods now handle null safely with a fallback message
- Minecraft color codes (`&7`, `&c`, etc.) are now stripped from API message returns, making them safe to use in external contexts like Discord
- `%player%` placeholder is now correctly replaced in Sender and Target messages (in addition to `%sender%` and `%target%` which were already handled)

---

## DiscordSRV alerts.yml integration

This fork makes ChatFeelings fully compatible with DiscordSRV v1 alerts. Example alert to sync feelings to your in-game chat Discord channel:

```yaml
- Trigger: com.zachduda.chatfeelings.api.FeelingSendEvent
  Channel: global
  Embed:
    Enabled: true
    Color: "#FF55FF"
    Author:
      ImageUrl: "https://crafthead.net/helm/${#event.getSender().getName()}"
      Name: "${#event.getGlobalEmoteMessage()}"
    Timestamp: false
```

---

## Original README

[![Discord](https://img.shields.io/discord/469625341837836290?style=flat-square&logo=Discord&logoColor=bdc7fc&label=Support%20Discord)](https://zachduda.com/discord?utm=github_badge)

The original plugin supports Minecraft 1.13–1.21 and is feature-rich with particles, sound effects, screen shakes, seasonal commands, and more.

For full documentation, visit the [Spigot page](https://www.spigotmc.org/resources/chatfeelings.12987/).

For API documentation, visit the [Spigot wiki](https://www.spigotmc.org/wiki/chatfeelings-api/).
