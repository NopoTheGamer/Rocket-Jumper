package com.nopo.rocket_jumper.main;

import net.fabricmc.api.ModInitializer;

public class RocketJumper implements ModInitializer {
    public boolean isClient = false;
    public static RocketJumper instance;
    public static RocketJumper getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
    instance = this;
        try {
            Class<?> clazz = Class.forName("net.minecraft.client.MinecraftClient");
            isClient = true;
        } catch (ClassNotFoundException | RuntimeException e) {
            isClient = false;
        }
    }
}
