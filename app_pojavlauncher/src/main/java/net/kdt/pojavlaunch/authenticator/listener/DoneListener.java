package net.kdt.pojavlaunch.authenticator.listener;

import net.kdt.pojavlaunch.value.MinecraftAccount;


public interface DoneListener {
    void onLoginDone(MinecraftAccount account);
}
