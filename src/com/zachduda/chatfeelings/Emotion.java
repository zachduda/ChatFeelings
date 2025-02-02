package com.zachduda.chatfeelings;

import org.bukkit.Sound;

public class Emotion {

    private final String name;
    private final String sendermsg;
    private final String targetmsg;
    private final String globalmsg;
    private final String permission;
    private final Sound sound1;
    private final Sound sound2;
    private final float sound1pitch;
    private final float sound1vol;
    private final float sound2pitch;
    private final float sound2vol;
    private final boolean usesound;
    private final boolean isharmful;

    public Emotion(String name, String sendermsg, String targetmsg, String globalmsg, String permission, Sound sound1, Sound sound2, float sound1pitch, float sound2pitch, float sound1vol, float sound2vol, boolean usesound, boolean isharmful) {
        this.name = name;
        this.sendermsg = sendermsg;
        this.targetmsg = targetmsg;
        this.globalmsg = globalmsg;
        this.permission = permission;
        this.sound1 = sound1;
        this.sound2 = sound2;
        this.sound1pitch = sound1pitch;
        this.sound2pitch = sound2pitch;
        this.sound1vol = sound1vol;
        this.sound2vol = sound2vol;
        this.usesound = usesound;
        this.isharmful = isharmful;
    }

    public String getName() {
        return name;
    }

    public String getSenderMsg() {
        return sendermsg;
    }

    public String getTargetMsg() {
        return targetmsg;
    }

    public String getGlobalMsg() {
        return globalmsg;
    }

    public String getPermission() {
        return permission;
    }

    public Sound getSound1() {
        return sound1;
    }

    public Sound getSound2() {
        return sound2;
    }

    public float getSound1Pitch() {
        return sound1pitch;
    }

    public float getSound2Pitch() {
        return sound2pitch;
    }

    public float getSound1Vol() {
        return sound1vol;
    }

    public float getSound2Vol() {
        return sound2vol;
    }

    public boolean useSound() {
        return usesound;
    }

    public boolean isHarmful() {
       return isharmful;
    }
}
