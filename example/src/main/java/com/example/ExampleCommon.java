package com.example;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

public class ExampleCommon {

    public static final Identifier ID = new Identifier("hello");
    public World world;

    public void init(World world) {
        this.world = world;

        System.out.println(ID.getNamespace());
        System.out.println(ID.getPath());

        if (this.world.isClient) {
            System.out.println("World is a client");
        } else {
            System.out.println("World is a server");
            ServerWorld serverworld = (ServerWorld) this.world;
            List<Entity> entities = serverworld.getEntities(EntityType.PIG, (e) -> true);
            for (Entity e : entities) {
                System.out.printf("entity %s: (%f, %f, %f)\n", e.getUuid(), e.getX(), e.getY(), e.getZ());
            }
        }
    }
}