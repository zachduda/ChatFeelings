[![Discord](https://img.shields.io/discord/469625341837836290?style=flat-square&logo=Discord&logoColor=bdc7fc&label=Support%20Discord)](https://zachduda.com/discord?utm=github_badge)  [![Build Status](https://ci.zachduda.com/job/ChatFeelings/badge/icon)](https://ci.zachduda.com/job/ChatFeelings/)

![Alt text](Images/chatfeelingsbanner.png?raw=true "ChatFeelings Banner")
The GitHub open source page for the original feeling's Minecraft plugin.

# API
For information on how to use the API, [click here](https://www.spigotmc.org/wiki/chatfeelings-api/).
### Maven:
```[code=xml]
<repository>
      <id>zachduda</id>
      <url>https://maven.zachduda.com/releases</url>
  </repository>
```
```[code=xml]
<dependency>
  <groupId>com.zachduda</groupId>
  <artifactId>chatfeelings</artifactId>
  <version>5.0.0</version>
</dependency>
```

# Spigot

ChatFeelings is a Minecraft plugin for Spigot, Paper and Folia.
Please check out the [Spigot Page](https://www.spigotmc.org/resources/chatfeelings.12987/). for full documentation.

# Custom Feelings
Every feeling has its own file in `plugins/ChatFeelings/Feelings/`. Any other `.yml` file you add there becomes a new
command named after the file (`poke.yml` → `/poke`) after a restart or `/cf reload`:

```yaml
Enable: true
Description: "&7Poke someone!"   # optional, shown in /feelings
Msgs:
  Sender: "&7You poke &f%player%&7!"               # %player% = the target
  Target: "&f%player% &7pokes you!"                # %player% = the sender
  Global: "&f%sender% &7poked &f%target%&7!"       # used when Global-Feelings is on
Sounds:
  Sound1:
    Name: ENTITY_VILLAGER_YES     # or entity.villager.yes; "None" for no sound
    Volume: 2.0                   # 0 - 10
    Pitch: 1.5                    # 0 - 2
  Sound2:
    Name: None
Particle: hug        # optional: None, or a built-in feeling whose particles to borrow
Violent: false       # optional: gives the target a tiny bit of damage (General.Violent-Command-Harm)
Stats-Label: pokes # optional: label used in /cf stats
```

The file name must start with a letter and only use letters, numbers, `-` or `_`. Names already used by the server,
by ChatFeelings or by another plugin are skipped. Permission: `chatfeelings.<name>` (or `chatfeelings.all`), will be needed if
`General.Use-Feeling-Permissions` is on.

# License
This project is licensed under [Creative Commons (CC-BY-NC-4)](https://creativecommons.org/licenses/by-nc/4.0/).
You can do whatever you'd like: just give credit and make sure it's non-commerical.

# Contact Me
If you have any questions or inquiries, you can reach me at https://zachduda.com/contact

# Metrics
[![ChatFeelings bStats Graph](https://bstats.org/signatures/bukkit/ChatFeelings.svg)](https://bstats.org/plugin/bukkit/ChatFeelings/1376)
