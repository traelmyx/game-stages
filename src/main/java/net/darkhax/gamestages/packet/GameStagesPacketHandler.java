package net.darkhax.gamestages.packet;

import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.data.IStageData;
import net.darkhax.gamestages.data.StageData;
import net.darkhax.gamestages.event.StagesSyncedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class GameStagesPacketHandler {

    private static final String PROTOCOL = "10";

    public static void register(RegisterPayloadHandlersEvent event) {

        final PayloadRegistrar registrar = event.registrar(PROTOCOL).optional();
        registrar.playToClient(MessageStages.TYPE, MessageStages.STREAM_CODEC, GameStagesPacketHandler::processSyncStagesMessage);
    }

    public static void syncPlayerStages(ServerPlayer player, MessageStages msg) {

        if (player.connection != null && player.connection.hasChannel(MessageStages.TYPE)) {
            PacketDistributor.sendToPlayer(player, msg);
        }
    }

    private static void processSyncStagesMessage(MessageStages message, IPayloadContext ctx) {

        // Reset the client data to a new object.
        final IStageData clientData = new StageData();

        // Re-add all stages
        for (final String stageName : message.getStages()) {
            clientData.addStage(stageName);
        }

        GameStageSaveHandler.setClientData(clientData);

        // Alert all the listeners.
        NeoForge.EVENT_BUS.post(new StagesSyncedEvent(clientData, ctx.player()));
    }
}
