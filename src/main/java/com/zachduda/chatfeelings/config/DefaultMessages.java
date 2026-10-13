package com.zachduda.chatfeelings.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** The default value of every key in messages.yml, in the order they're written to a new file. */
final class DefaultMessages {
    static final Map<String, String> VALUES;

    private static final Map<String, String> BUILDING = new LinkedHashMap<>();

    static {
        d("Prefix", "&#c3ff9b&lC&r&#c3ff9bhat&r&f&lF&r&feelings &8&l┃ &f");
        d("Prefix-Header", "&#c3ff9b&lC&r&#c3ff9bhat &r&f&lF&r&feelings");
        d("Reload", "&8&l> &#c3ff9b&l✓  &7Plugin reloaded in &f%time%");
        d("Console-Name", "The Server");
        d("No-Permission", "&#FF8C6BSorry. &fYou don't have permission for that.");
        d("Feelings-Help", "&#c3ff9bAvailable Feelings:");
        d("Feelings-Help-Page", "&r&#7aa35ePage &#c3ff9b%page%&8/&#7aa35e%pagemax%");
        d("Sending-World-Disabled", "&#FF8C6BSorry. &fYou can't use feelings in this world.");
        d("Disabled-Serverwide-Targets", "&#FF8C6BNot Allowed. &fThis server has disabled emoting everyone.");
        d("Receiving-World-Disabled", "&#FF8C6BSorry. &fYour target is in a world with feelings disabled.");
        d("Page-Not-Found", "&#FF8C6BOops. &fThat page doesn't exist, try &7/feelings 1");
        d("No-Player", "&#FF8C6BOops! &fYou need to provide a player to do that to.");
        d("No-Player-Mute", "&#FF8C6BOops! &fYou must provide a player to mute.");
        d("No-Player-Unmute", "&#FF8C6BOops! &fYou must provide a player to unmute.");
        d("Player-Offline", "&#FF8C6BPlayer Offline. &fWe couldn't find &7&l%player% &fon the server.");
        d("Player-Never-Joined", "&#FF8C6BHmm. &fThat player has never joined before.");
        d("Outside-Of-Radius", "&#FF8C6BHmm. &fYou're too far away from &7%player% &fto use that.");
        d("Cooldown-Active", "&#FF8C6BSlow Down. &fWait &7%time% &fbefore doing that again.");
        d("Ignore-Cooldown", "&#FF8C6BSlow Down. &fPlease wait before ignoring again.");
        d("Console-Not-Player", "&#FF8C6BGoofball! &fThe &7CONSOLE&f is not a real player.");
        d("Sender-Is-Target", "&#FF8C6BYou Silly! &fYou can't %command% &fyourself.");
        d("Is-Muted", "&#FF8C6BYou're Muted. &fYou can no longer use feelings.");
        d("Folder-Not-Found", "&#FF8C6BHmm. &fThere is no data to display here.");
        d("Stats-Header-Own", "&#f4fcab&lYour Statistics:");
        d("Stats-Header-Other", "&#f4fcab&l%player%'s Statistics:");
        d("Ignore-List-Header", "&#FF8C6B&lIgnored Players:");
        d("Ignore-List-None", "   &8&l> &fYou are currently not ignoring anyone!");
        d("Ignore-List-All", "   &8&l> &fYou are ignoring all feelings.");
        d("Ignore-List-Cooldown", "&#FF8C6BPlease Wait. &fYou must wait before checking who you're ignoring.");
        d("Mute-List-Header", "&#f4fcabMuted Players:");
        d("Mute-List-Player", "&r  &8&l> &f%player%");
        d("Mute-List-Total-One", "&r  &7There is &f&l%total% &7muted player.");
        d("Mute-List-Total-Many", "&r  &7There are &f&l%total% &7muted players.");
        d("Mute-List-Total-Zero", "&r  &8&l> &#c3ff9b&lYay! &7No players are currently muted.");
        d("Player-Has-Been-Muted", "&#FF8C6BUser Muted. &7%player% &fcan no longer use feelings.");
        d("Player-Muted-Via-Essentials", "&#FF8C6BOops! &7%player%&f is muted via Essentials, use /unmute!");
        d("Player-Muted-Via-LiteBans", "&#FF8C6BOops! &7%player%&f is muted via LiteBans, use /unmute!");
        d("Player-Muted-Via-AdvancedBan", "&#FF8C6BOops! &7%player%&f is muted via AdvancedBan, use /unmute!");
        d("Extra-Mute-Present", "&r&7&oThey're already muted via your punishment system. &#f4fcab&oSee /cf mutelist");
        d("Player-Has-Been-Unmuted", "&#c3ff9bUser Unmuted. &7%player% &fcan now use feelings again.");
        d("Cant-Mute-Self", "&#FF8C6BYou Silly! &fYou can't mute yourself.");
        d("Player-Already-Muted", "&#FF8C6BOops. &fThis player is already muted.");
        d("Player-Already-Unmuted", "&#FF8C6BOops. &fYou can't unmute a player who's not muted.");
        d("Already-Mute-Unmute-Suggestion", "&7&oCould you have meant &#f4fcab&o/cf unmute");
        d("No-Perm-Mute-Suggestion", "&7&oCould you have meant &#f4fcab&o/cf ignore&7&o?");
        d("Emote-Disabled", "&#FF8C6BEmote Disabled. &fThis emotion has been disabled by the server.");
        d("Ignoring-On-Player", "&7You've now &#FF8C6B&lBLOCKED &r&7feelings from: &f%player%");
        d("Ignoring-Off-Player", "&7Now &#c3ff9b&lALLOWING &7feelings from: &f%player%");
        d("Ignoring-On-All", "&7You've now &#FF8C6B&lBLOCKED &r&7feelings from all players.");
        d("Ignoring-Off-All", "&7Now &#c3ff9b&lALLOWING &7feelings from all players.");
        d("Cant-Ignore-Self", "&#FF8C6BYou Silly! &fYou can't ignore yourself.");
        d("Target-Is-Ignoring", "&#FF8C6BBummer! &fThis player has blocked you.");
        d("Target-Is-Ignoring-All", "&#FF8C6BBummer! &fThis player is not accepting feelings.");
        d("Command-List-Page", "&7To go to the next page do &#c3ff9b/feelings %page%");
        d("Command-List-Player", "&r &f(player)");
        d("Command-List-NoPerm", "&7You aren't able to use this feeling.");
        d("Command-Help.Descriptions.Help", "&7Shows you this page.");
        d("Command-Help.Descriptions.Ignore", "&7Toggle ignoring feelings from players");
        d("Command-Help.Descriptions.Ignore-All", "&7Toggles everyone being able to use feelings.");
        d("Command-Help.Descriptions.Stats", "&7Shows how many feelings you've sent.");
        d("Command-Help.Descriptions.Stats-Others", "&7Shows another player's total sent feelings.");
        d("Command-Help.Descriptions.Mute", "&7Prevents a player from using feelings.");
        d("Command-Help.Descriptions.Unmute", "&7Re-allows feeling usage by a player.");
        d("Command-Help.Descriptions.Mute-List", "&7Lists players that are currently muted.");
        d("Command-Help.Descriptions.Plugin-Version", "&7Shows the current version info.");
        d("Command-Help.Descriptions.Plugin-Reload", "&7Reload all config and message files.");
        d("Command-Help.Descriptions.Feelings", "&7Show all feelings available for use.");
        d("Command_Descriptions.Hug", "Give someone a nice warm hug!");
        d("Command_Descriptions.Slap", "Slap some sense back into someone.");
        d("Command_Descriptions.Poke", "Poke someone to get their attention.");
        d("Command_Descriptions.Highfive", "Show your support, and give a highfive!");
        d("Command_Descriptions.Facepalm", "Need to show some disapproval?");
        d("Command_Descriptions.Yell", "Yell at someone as loud as possible!");
        d("Command_Descriptions.Bite", "Bite a player right on the arm.");
        d("Command_Descriptions.Snuggle", "Snuggle up with the power of warm hugs!");
        d("Command_Descriptions.Shake", "Shake someone to their core.");
        d("Command_Descriptions.Stab", "Stab someone with a knife. Ouch!");
        d("Command_Descriptions.Kiss", "Give a kiss on the cheek. How sweet!");
        d("Command_Descriptions.Punch", "Punch someone back from insanity!");
        d("Command_Descriptions.Murder", "Finna kill someone here.");
        d("Command_Descriptions.Boi", "Still in iFunny hell? Here's this!");
        d("Command_Descriptions.Cry", "Down in the dumps? Let it all out.");
        d("Command_Descriptions.Dab", "Cringe never dies! Prove it to the world.");
        d("Command_Descriptions.Scorn", "Shame a player for what they've done.");
        d("Command_Descriptions.Lick", "Lick someone like an ice-cream sundae!");
        d("Command_Descriptions.Pat", "Pat a player's head for being good.");
        d("Command_Descriptions.Stalk", "Stalk a player carefully... carefully.");
        d("Command_Descriptions.Sus", "Pure single-boned suspicion.");
        d("Command_Descriptions.Wave", "Say farewell, and wave adieu. How elegant!");
        d("Command_Descriptions.Welcomeback", "Give a warm welcome-back to returning players!");
        d("Command_Descriptions.Boop", "Boop someone right on their nose!");
        VALUES = Collections.unmodifiableMap(BUILDING);
    }

    private DefaultMessages() {
    }

    private static void d(String key, String value) {
        BUILDING.put(key, value);
    }
}
