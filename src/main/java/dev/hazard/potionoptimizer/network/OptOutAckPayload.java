package dev.hazard.potionoptimizer.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public record OptOutAckPayload() implements CustomPayload {
   public static final CustomPayload.Id<OptOutAckPayload> ID = new CustomPayload.Id<>(Identifier.of("potionoptimizer", "opt_out_ack"));
   public static final PacketCodec<PacketByteBuf, OptOutAckPayload> CODEC = PacketCodec.unit(new OptOutAckPayload());

   @Override
   public CustomPayload.Id<? extends CustomPayload> getId() {
      return ID;
   }
}

