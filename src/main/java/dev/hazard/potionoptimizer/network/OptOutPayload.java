package dev.hazard.potionoptimizer.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public record OptOutPayload() implements CustomPayload {
   public static final CustomPayload.Id<OptOutPayload> ID = new CustomPayload.Id<>(Identifier.of("potionoptimizer", "opt_out"));
   public static final PacketCodec<PacketByteBuf, OptOutPayload> CODEC = PacketCodec.unit(new OptOutPayload());

   @Override
   public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
   }
}

