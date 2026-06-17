package de.myownbrain.autoLogout.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NearestEntityFinder {
    private static List<Entity> nearestEntity = new ArrayList<>();

    public static void updateNearestEntities(double radius) {
        Minecraft client = Minecraft.getInstance();

        if (client.level == null || client.player == null) return;

        Vec3 playerPos = client.player.position();
        AABB searchBox = new AABB(
            playerPos.x - radius, playerPos.y - radius, playerPos.z - radius,
            playerPos.x + radius, playerPos.y + radius, playerPos.z + radius
        );

        nearestEntity = client.level.getEntities(client.player, searchBox).stream()
            .filter(entity -> entity instanceof LivingEntity)
            .sorted(Comparator.comparingDouble(entity -> playerPos.distanceToSqr(entity.position())))
            .limit(ConfigManager.nearbyEntityCount)
            .toList();
    }

    public static List<Entity> getNearestEntities() {
        return nearestEntity;
    }
}
