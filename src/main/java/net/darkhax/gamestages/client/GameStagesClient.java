package net.darkhax.gamestages.client;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class GameStagesClient {

    private GameStagesClient() {
    }

    public static void init() {

        NeoForge.EVENT_BUS.addListener(GameStagesClient::onF3Text);
    }

    public static Player player() {

        return Minecraft.getInstance().player;
    }

    private static void onF3Text(CustomizeGuiOverlayEvent.DebugText event) {

        final Minecraft mc = Minecraft.getInstance();

        if (mc.getDebugOverlay().showDebugScreen()) {

            if (mc.player != null && mc.player.isShiftKeyDown()) {

                final var data = GameStageHelper.getPlayerData(mc.player);

                if (data != null) {

                    event.getRight().add(ChatFormatting.GOLD + ChatFormatting.UNDERLINE.toString() + "GameStages");
                    event.getRight().add("Count: " + data.getStages().size());
                    event.getRight().add("Type: " + data.getClass().getName());
                    event.getRight().add("Stages: " + data.getStages().toString());
                }
            }

            else {

                event.getRight().add(ChatFormatting.GOLD + ChatFormatting.UNDERLINE.toString() + "GameStages [Shift]");
            }
        }
    }
}
