package net.darkhax.gamestages;

import net.darkhax.gamestages.advancement.HasStageTrigger;
import net.darkhax.gamestages.client.GameStagesClient;
import net.darkhax.gamestages.command.GameStageCommands;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.packet.GameStagesPacketHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.advancements.CriterionTrigger;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

@Mod(GameStages.MOD_ID)
public class GameStages {

    public static final String MOD_ID = "gamestages";
    public static final Logger LOG = LogManager.getLogger("Game Stages");

    private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, MOD_ID);
    public static final Supplier<HasStageTrigger> HAS_STAGE_TRIGGER = TRIGGERS.register("has_stage", HasStageTrigger::new);

    public GameStages(IEventBus modBus, Dist dist) {

        GameStageSaveHandler.reloadFakePlayers();
        GameStageSaveHandler.reloadKnownStages();
        TRIGGERS.register(modBus);
        GameStageCommands.initializeCommands(modBus);
        modBus.addListener(GameStagesPacketHandler::register);
        if (dist.isClient()) {
            GameStagesClient.init();
        }
        NeoForge.EVENT_BUS.addListener(GameStages::registerReloadListeners);
    }

    private static void registerReloadListeners(AddReloadListenerEvent event) {

        event.addListener(new GameStageReloader());
    }
}
