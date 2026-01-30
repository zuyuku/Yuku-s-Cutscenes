package zuyuku.yukuscutscenes.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class CutsceneManager extends PersistentState {
    public NbtCompound data = new NbtCompound();

    public Collection<Identifier> getSuggestions() {
        ArrayList<Identifier> names = new ArrayList<>();
        for(Cutscene cutscene : this.getCutscenes())
            names.add(Identifier.of(cutscene.getName()));
        return names;
    }

    public static CutsceneManager getFromWorld(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(CutsceneManager.TYPE);
    }

    public CutsceneManager(NbtCompound nbt) {
        this.data = nbt;
    }
    public CutsceneManager() {}

    public static final PersistentStateType<CutsceneManager> TYPE =
        new PersistentStateType<CutsceneManager>(
            "cutscene_manager",
            CutsceneManager::new,
            NbtCompound.CODEC.xmap(CutsceneManager::new, CutsceneManager::getData),
            DataFixTypes.LEVEL
        );

    public NbtCompound getData() {
        return this.data;
    }

    public void setData(NbtCompound nbt) {
        this.data = nbt;
        this.markDirty();
    }

    public static ArrayList<Cutscene> makeList(NbtCompound nbt) {
        NbtList list = nbt.getListOrEmpty("CutsceneList");
        ArrayList<Cutscene> cutscenes = new ArrayList<>();
        for(NbtElement element : list)
            if(element instanceof NbtCompound compound)
                cutscenes.add(Cutscene.fromNbt(compound));
        return cutscenes;
    }

    public void setList(ArrayList<Cutscene> cutscenes) {
        NbtCompound nbt = new NbtCompound();
        NbtList list = new NbtList();
        for(Cutscene cutscene : cutscenes)
            list.add(cutscene.toNbt());
        nbt.put("CutsceneList", list);
        this.data = nbt;
        markDirty();
    }

    public void syncToPlayer(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new CutscenePayload(data));
    }

    public void syncToClients(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        for(ServerPlayerEntity player : players)
            syncToPlayer(player);
    }

    public ArrayList<Cutscene> getCutscenes() {
        NbtList list = data.getListOrEmpty("CutsceneList");
        ArrayList<Cutscene> cutscenes = new ArrayList<>();
        for(NbtElement element : list)
            if(element instanceof NbtCompound compound)
                cutscenes.add(Cutscene.fromNbt(compound));
        return cutscenes;
    }
}
