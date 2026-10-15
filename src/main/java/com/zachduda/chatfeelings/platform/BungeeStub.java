package com.zachduda.chatfeelings.platform;

import net.md_5.bungee.api.plugin.Plugin;

public final class BungeeStub extends Plugin {
    @Override
    public void onEnable() {
        getLogger().severe("--------------------------------------------------------------");
        getLogger().severe("ERROR! ChatFeelings is NOT a BungeeCord plugin.");
        getLogger().severe("--------------------------------------------------------------");
        getLogger().severe("Please remove ChatFeelings.jar from your proxy's plugins folder once it");
        getLogger().severe("has stopped, and put it in your Spigot/Paper server's plugins folder instead.");
        getLogger().severe("--------------------------------------------------------------");
    }
}
