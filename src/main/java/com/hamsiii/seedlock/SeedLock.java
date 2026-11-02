package com.hamsiii.seedlock;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.Map;

public class SeedLock implements ModInitializer {
    public static final String ALLOWED_NAME = "hamsiii";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            removeLiteral(dispatcher, "seed");
            dispatcher.register(
                CommandManager.literal("seed")
                    .requires(SeedLock::isAllowed)
                    .executes(ctx -> {
                        long seed = ctx.getSource().getWorld().getSeed();
                        ctx.getSource().sendFeedback(() -> Text.literal("Seed: [" + seed + "]"), false);
                        return 1;
                    })
            );
        });
    }

    private static boolean isAllowed(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            return true;
        }
        return player.getName().getString().equalsIgnoreCase(ALLOWED_NAME);
    }

    @SuppressWarnings("unchecked")
    private static void removeLiteral(CommandDispatcher<ServerCommandSource> dispatcher, String name) {
        try {
            CommandNode<ServerCommandSource> root = dispatcher.getRoot();
            for (String fieldName : new String[]{"children", "literals", "arguments"}) {
                Field field = CommandNode.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                ((Map<String, ?>) field.get(root)).remove(name);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("SeedLock: /seed komutu kaldirilamadi", e);
        }
    }
}
