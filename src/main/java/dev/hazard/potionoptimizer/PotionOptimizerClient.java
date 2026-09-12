package dev.hazard.potionoptimizer;

import dev.hazard.potionoptimizer.network.OptOutAckPayload;
import dev.hazard.potionoptimizer.network.OptOutPayload;
import dev.hazard.potionoptimizer.network.VersionPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class PotionOptimizerClient implements ClientModInitializer {
   private static final Logger LOGGER = LoggerFactory.getLogger("potionoptimizer");
   private static final float THROW_ROLL = -20.0F;
   private static final float THROW_POWER = 0.5F;
   private static final float THROW_DIVERGENCE = 0.0F;
   private static final int MAX_LIFETIME_TICKS = 40;
   private static final double HANDOFF_RANGE_SQUARED = 36.0D;
   private static final int FIRST_PREDICTED_ID = -30000;
   private static PotionOptimizerConfig config;
   private PotionEntity predicted;
   private int predictedAge;
   private int nextPredictedId = -30000;
   private boolean optedOut;

   @Override
   public void onInitializeClient() {
      config = PotionOptimizerConfig.load();
      PayloadTypeRegistry.playC2S().register(VersionPayload.ID, VersionPayload.CODEC);
      PayloadTypeRegistry.playS2C().register(OptOutPayload.ID, OptOutPayload.CODEC);
      PayloadTypeRegistry.playC2S().register(OptOutAckPayload.ID, OptOutAckPayload.CODEC);
      
      ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
         this.optedOut = false;
         this.clearPrediction();
         ClientPlayNetworking.send(new VersionPayload(getModVersion()));
      });

      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
         this.optedOut = false;
         this.clearPrediction();
      });

      ClientPlayNetworking.registerGlobalReceiver(OptOutPayload.ID, (payload, context) -> {
         boolean wasAlreadyOptedOut = this.optedOut;
         this.optedOut = true;
         this.clearPrediction();
         ClientPlayNetworking.send(new OptOutAckPayload());
         if (!wasAlreadyOptedOut) {
            context.player().sendMessage(Text.literal("Potion Optimizer has been disabled for this server."), false);
         }
      });

      UseItemCallback.EVENT.register(this::onUseItem);
      ClientEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
      ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
   }

   public static PotionOptimizerConfig getConfig() {
      return config;
   }

   private static String getModVersion() {
      return FabricLoader.getInstance().getModContainer("potionoptimizer")
         .map(container -> container.getMetadata().getVersion().getFriendlyString())
         .orElse("unknown");
   }

   private TypedActionResult<ItemStack> onUseItem(PlayerEntity player, World world, Hand hand) {
      ItemStack stack = player.getStackInHand(hand);
      try {
         MinecraftClient client = MinecraftClient.getInstance();
         if (!config.enabled || this.optedOut || !world.isClient() || player != client.player) {
            return TypedActionResult.pass(stack);
         }

         ClientWorld clientWorld = client.world;
         if (clientWorld == null) {
            return TypedActionResult.pass(stack);
         }

         PotionEntity entity = this.createPotionEntity(clientWorld, player, stack);
         if (entity == null) {
            return TypedActionResult.pass(stack);
         }

         this.clearPrediction();
         entity.setVelocity(player, player.getPitch(), player.getYaw(), THROW_ROLL, THROW_POWER, THROW_DIVERGENCE);
         entity.setId(this.nextPredictedId--);
         clientWorld.addEntity(entity);
         this.predicted = entity;
         this.predictedAge = 0;
      } catch (Exception e) {
         LOGGER.error("Failed to spawn a predicted potion", e);
         this.clearPrediction();
      }

      return TypedActionResult.pass(stack);
   }

   private PotionEntity createPotionEntity(ClientWorld world, PlayerEntity player, ItemStack stack) {
      if (stack.getItem() instanceof SplashPotionItem || stack.getItem() instanceof LingeringPotionItem) {
         PotionEntity entity = new PotionEntity(world, player);
         entity.setItem(stack.copy());
         return entity;
      } else {
         return null;
      }
   }

   private void onEntityLoad(Entity entity, ClientWorld world) {
      try {
         if (this.predicted == null || this.predicted.isRemoved() || entity == this.predicted || !(entity instanceof PotionEntity)) {
            return;
         }

         if (entity.getId() < 0) {
            return;
         }

         if (entity.getPos().squaredDistanceTo(this.predicted.getPos()) <= HANDOFF_RANGE_SQUARED) {
            this.clearPrediction();
         }
      } catch (Exception e) {
         LOGGER.error("Failed to hand off to the server's potion", e);
         this.clearPrediction();
      }
   }

   private void onEndTick(MinecraftClient client) {
      if (this.predicted != null) {
         if (client.world == null || this.predicted.isRemoved() || ++this.predictedAge > MAX_LIFETIME_TICKS) {
            this.clearPrediction();
         }
      }
   }

   private void clearPrediction() {
      if (this.predicted != null) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.world != null && !this.predicted.isRemoved()) {
            client.world.removeEntity(this.predicted.getId(), RemovalReason.DISCARDED);
         }

         this.predicted = null;
         this.predictedAge = 0;
      }
   }
}

