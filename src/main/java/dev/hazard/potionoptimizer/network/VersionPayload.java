package dev.hazard.potionoptimizer.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public record VersionPayload(String version) implements CustomPayload {
   public static final CustomPayload.Id<VersionPayload> ID = new CustomPayload.Id<>(Identifier.of("potionoptimizer", "version"));
   public static final PacketCodec<PacketByteBuf, VersionPayload> CODEC = PacketCodec.tuple(
      PacketCodecs.STRING, VersionPayload::version, VersionPayload::new
   );

   @Override
   public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
   }
}

