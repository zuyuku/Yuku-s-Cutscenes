package zuyuku.yukuscutscenes.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import zuyuku.yukuscutscenes.YukusCutscenes;

public record CutscenePayload(NbtCompound nbt) implements CustomPayload {
    public static final CustomPayload.Id<CutscenePayload> ID = new CustomPayload.Id<>(Identifier.of(YukusCutscenes.MOD_ID, "cutscene_nbt"));
    public static final PacketCodec<RegistryByteBuf, CutscenePayload> CODEC = PacketCodec.tuple(
        PacketCodecs.NBT_COMPOUND, CutscenePayload::nbt,
        CutscenePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
