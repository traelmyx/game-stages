package net.darkhax.gamestages.event;

import net.darkhax.gamestages.data.IStageData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class StagesSyncedEvent extends PlayerEvent {
    
    private final IStageData data;
    
    public StagesSyncedEvent(IStageData data) {
        
        this(data, net.darkhax.gamestages.client.GameStagesClient.player());
    }
    
    public StagesSyncedEvent(IStageData data, Player player) {
        
        super(player);
        this.data = data;
    }
    
    public IStageData getData () {
        
        return this.data;
    }
}
