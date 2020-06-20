package com.example;

import net.minecraft.client.MinecraftClient;

public class ExampleClient {

    MinecraftClient minecraft;

    public void init(MinecraftClient client) {
        // test comment
        minecraft = client;
        client.textRenderer.getStringWidth("Hello, world!");
    }
}
