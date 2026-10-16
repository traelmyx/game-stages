package net.darkhax.gamestages.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import java.nio.file.Files;

// Run with -Dsmoketest=true: creates a flat world, takes a screenshot and quits. Exit code 1 on failure.
public final class ClientSmoke {

    public static final boolean ENABLED = Boolean.getBoolean("smoketest");
    private static final long TIMEOUT_MS = 5 * 60 * 1000;

    private static int stage;
    private static int ticks;

    private ClientSmoke() {
    }

    public static boolean start() {
        if (!ENABLED) {
            return false;
        }
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT_MS);
                fail("timed out");
            } catch (InterruptedException ignored) {
            }
        }, "smoketest-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
        return true;
    }

    public static void tick(Minecraft mc) {
        try {
            switch (stage) {
                case 0 -> {
                    Screen screen = mc.screen;
                    if (screen != null && mc.getOverlay() == null && ++ticks >= 20) {
                        if (screen.getClass().getSimpleName().equals("LoadingErrorScreen")) {
                            fail("mod loading errors");
                        }
                        ticks = 0;
                        createWorld(mc);
                        stage = 1;
                    }
                }
                case 1 -> {
                    if (mc.level != null && mc.player != null && mc.screen == null) {
                        stage = 2;
                    }
                }
                case 2 -> {
                    ticks++;
                    if (ticks == 60) {
                        buildScene(mc);
                    }
                    if (ticks == 150) {
                        Screenshot.grab(mc.gameDirectory, "scene.png", mc.getMainRenderTarget(), msg -> {});
                    }
                    if (ticks >= 220) {
                        mc.options.hideGui = false;
                        Screenshot.grab(mc.gameDirectory, "world.png", mc.getMainRenderTarget(), msg -> {});
                        Files.writeString(mc.gameDirectory.toPath().resolve("smoke-ok"), "ok\n");
                        stage = 3;
                        mc.stop();
                    }
                }
                default -> {
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            fail(t.toString());
        }
    }

    private static void createWorld(Minecraft mc) {
        LevelSettings settings = new LevelSettings("smoke", GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel("smoke", settings, new WorldOptions(0L, false, false),
            registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
            mc.screen);
    }

    // stone, iron, gold and diamond columns of rising height, used for the mod icon
    private static void buildScene(Minecraft mc) {
        var server = mc.getSingleplayerServer();
        var pos = mc.player.blockPosition();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ() + 4;
        String[] blocks = {"stone", "iron_block", "gold_block", "diamond_block"};
        server.execute(() -> {
            var source = server.createCommandSourceStack().withSuppressedOutput();
            var commands = server.getCommands();
            commands.performPrefixedCommand(source, "time set 6000");
            commands.performPrefixedCommand(source, "gamemode spectator @a");
            for (int i = 0; i < blocks.length; i++) {
                commands.performPrefixedCommand(source, String.format("fill %d %d %d %d %d %d %s", x + 3 - i, y, z, x + 3 - i, y + i, z, blocks[i]));
            }
            commands.performPrefixedCommand(source, String.format("tp @a %.2f %.2f %.2f 0 20", x + 2.0, y + 3.6, z - 5.2));
        });
        mc.options.hideGui = true;
    }

    public static void fail(String why) {
        System.err.println("Smoke test failed: " + why);
        System.err.flush();
        Runtime.getRuntime().halt(1);
    }
}
