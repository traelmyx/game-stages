package net.darkhax.gamestages.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class HasStageTrigger extends SimpleCriterionTrigger<HasStageTrigger.Instance> {

    public void trigger(ServerPlayer player) {

        this.trigger(player, instance -> GameStageHelper.hasStage(player, instance.stage()));
    }

    @Override
    public Codec<Instance> codec() {

        return Instance.CODEC;
    }

    public record Instance(Optional<ContextAwarePredicate> player, String stage) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            Codec.STRING.fieldOf("stage").forGetter(Instance::stage)
        ).apply(i, Instance::new));
    }
}
