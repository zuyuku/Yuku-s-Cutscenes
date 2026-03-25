package zuyuku.yukuscutscenes.util;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public class ServerCutsceneManager {
    private static HashMap<ServerPlayerEntity, ArrayList<Integer>> playerTracker = new HashMap<>();

    public static void tick(MinecraftServer server) {
        for(ServerPlayerEntity player : playerTracker.keySet()) {
            ArrayList<Integer> tickLengths = playerTracker.get(player);
            if(tickLengths.isEmpty()) {
                playerTracker.remove(player);
                if(!ServerScreenEffectManager.inScreenEffect(player))
                    player.setInvulnerable(false);
                return;
            }
            if(player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
                player.setInvulnerable(true);
            tickLengths.set(0, tickLengths.get(0).intValue()-1);
            if (tickLengths.get(0) <= 0) 
                tickLengths.removeFirst();
        }
    }

    public static void cancelCutscene(ServerPlayerEntity player) {
        if(!inCutscene(player))
            return;
        playerTracker.get(player).removeFirst();
    }

    public static boolean inCutscene(ServerPlayerEntity player) {
        return playerTracker.get(player) != null;
    }

    public static void addInstance(ServerPlayerEntity player, int ticks) {
        ArrayList<Integer> playerInstances = playerTracker.get(player);
        if(playerInstances != null) {
            playerInstances.add(ticks);
            return;
        }
        playerInstances = new ArrayList<>();
        playerInstances.add(ticks);
        playerTracker.put(player, playerInstances);
    }
}
