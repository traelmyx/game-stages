package net.darkhax.gamestages.test;

import io.netty.buffer.Unpooled;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.data.IStageData;
import net.darkhax.gamestages.event.GameStageEvent;
import net.darkhax.gamestages.packet.MessageStages;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@GameTestHolder(GameStagesTests.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameStagesTests {
    public static final String MOD_ID = "gamestages_gametest";

    @GameTest(template = "empty")
    public static void addCheckRemoveClear(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        GameStageHelper.clearStages(player);
        helper.assertFalse(GameStageHelper.hasStage(player, "one"), "fresh player has stage one");

        GameStageHelper.addStage(player, "one", "two");
        helper.assertTrue(GameStageHelper.hasStage(player, "one"), "stage one missing after add");
        helper.assertTrue(GameStageHelper.hasAllOf(player, "one", "two"), "hasAllOf(one, two) is false");
        helper.assertTrue(GameStageHelper.hasAnyOf(player, "nope", "two"), "hasAnyOf(nope, two) is false");
        helper.assertFalse(GameStageHelper.hasAllOf(player, "one", "nope"), "hasAllOf(one, nope) is true");

        GameStageHelper.removeStage(player, "one");
        helper.assertFalse(GameStageHelper.hasStage(player, "one"), "stage one still there after remove");
        helper.assertTrue(GameStageHelper.hasStage(player, "two"), "remove(one) also removed two");

        helper.assertTrue(GameStageHelper.clearStages(player) == 1, "clear did not report 1 stage");
        helper.assertTrue(GameStageHelper.getPlayerData(player).getStages().isEmpty(), "stages left after clear");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void namesAndValidation(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        GameStageHelper.addStage(player, "Nether");
        helper.assertTrue(GameStageHelper.hasStage(player, "nether"), "stage names should be case-insensitive");
        helper.assertTrue(GameStageHelper.isValidStageName("example:stage_2"), "valid name rejected");
        helper.assertFalse(GameStageHelper.isValidStageName("has space"), "name with a space accepted");
        helper.assertFalse(GameStageHelper.isValidStageName("UPPER"), "upper-case name accepted");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void eventsFireAndCancel(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        AtomicInteger added = new AtomicInteger();
        NeoForge.EVENT_BUS.addListener((GameStageEvent.Add e) -> { if (e.getEntity() == player && e.getStageName().equals("blocked")) e.setCanceled(true); });
        NeoForge.EVENT_BUS.addListener((GameStageEvent.Added e) -> { if (e.getEntity() == player) added.incrementAndGet(); });

        GameStageHelper.addStage(player, "blocked", "allowed");
        helper.assertFalse(GameStageHelper.hasStage(player, "blocked"), "cancelled stage was added");
        helper.assertTrue(GameStageHelper.hasStage(player, "allowed"), "allowed stage missing");
        helper.assertTrue(added.get() == 1, "expected 1 Added event, got " + added.get());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void saveAndLoad(GameTestHelper helper) throws Exception {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        GameStageHelper.addStage(player, "saved_stage", "example:namespaced");
        File dir = Files.createTempDirectory("gamestages-test").toFile();
        String uuid = player.getStringUUID();

        GameStageSaveHandler.onPlayerSave(new PlayerEvent.SaveToFile(player, dir, uuid));
        helper.assertTrue(new File(dir, "gamestages/" + uuid + ".dat").isFile(), "no save file written");

        GameStageHelper.clearStages(player);
        GameStageSaveHandler.onPlayerLoad(new PlayerEvent.LoadFromFile(player, dir, uuid));
        IStageData data = GameStageHelper.getPlayerData(player);
        helper.assertTrue(data.hasStage("saved_stage") && data.hasStage("example:namespaced"), "stages not restored: " + data.getStages());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void commands(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        String name = "@s";
        CommandSourceStack source = player.createCommandSourceStack().withPermission(4).withSuppressedOutput();

        run(helper, source, "gamestage add " + name + " cmd_stage");
        helper.assertTrue(GameStageHelper.hasStage(player, "cmd_stage"), "/gamestage add did not add the stage");
        run(helper, source, "gamestage add " + name + " example:cmd_stage true");
        helper.assertTrue(GameStageHelper.hasStage(player, "example:cmd_stage"), "namespaced stage not added by command");
        run(helper, source, "gamestage remove " + name + " cmd_stage");
        helper.assertFalse(GameStageHelper.hasStage(player, "cmd_stage"), "/gamestage remove did not remove the stage");
        helper.succeed();
    }

    private static void run(GameTestHelper helper, CommandSourceStack source, String command) {
        try {
            helper.getLevel().getServer().getCommands().getDispatcher().execute(command, source);
        } catch (Exception e) {
            helper.fail("/" + command + " failed: " + e.getMessage());
        }
    }

    @GameTest(template = "empty")
    public static void advancementTrigger(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        AdvancementHolder adv = helper.getLevel().getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(MOD_ID, "has_boss_stage"));
        helper.assertTrue(adv != null, "test advancement not loaded");

        GameStageHelper.addStage(player, "unrelated");
        helper.assertFalse(player.getAdvancements().getOrStartProgress(adv).isDone(), "advancement granted for an unrelated stage");
        GameStageHelper.addStage(player, "boss_defeated");
        helper.assertTrue(player.getAdvancements().getOrStartProgress(adv).isDone(), "advancement not granted for its stage");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void craftTweakerGrantOnJoin(GameTestHelper helper) {
        helper.assertTrue(net.neoforged.fml.ModList.get().isLoaded("crafttweaker"), "CraftTweaker is not loaded in the test run");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.succeedWhen(() -> helper.assertTrue(GameStageHelper.hasStage(player, "crt_join_stage"), "script stage not granted on join"));
    }

    @GameTest(template = "empty")
    public static void syncPacketCodec(GameTestHelper helper) {
        MessageStages msg = new MessageStages(List.of("a", "b:c"));
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        MessageStages.STREAM_CODEC.encode(buf, msg);
        MessageStages decoded = MessageStages.STREAM_CODEC.decode(buf);
        helper.assertTrue(decoded.getStages().equals(msg.getStages()), "decoded " + decoded.getStages());
        helper.succeed();
    }
}
