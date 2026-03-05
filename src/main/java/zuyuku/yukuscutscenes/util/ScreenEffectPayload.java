package zuyuku.yukuscutscenes.util;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import zuyuku.yukuscutscenes.YukusCutscenes;

public record ScreenEffectPayload(String name, int introTicks, int holdTicks, int outroTicks, String lerpType, String command) implements CustomPayload {
    public static final CustomPayload.Id<ScreenEffectPayload> ID = new CustomPayload.Id<>(Identifier.of(YukusCutscenes.MOD_ID, "screen_effect_nbt"));
    public static final PacketCodec<RegistryByteBuf, ScreenEffectPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, ScreenEffectPayload::name,
        PacketCodecs.INTEGER, ScreenEffectPayload::introTicks,
        PacketCodecs.INTEGER, ScreenEffectPayload::holdTicks,
        PacketCodecs.INTEGER, ScreenEffectPayload::outroTicks,
        PacketCodecs.STRING, ScreenEffectPayload::lerpType,
        PacketCodecs.STRING, ScreenEffectPayload::command,
        ScreenEffectPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
