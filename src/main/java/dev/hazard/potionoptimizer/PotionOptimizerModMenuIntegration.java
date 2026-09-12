package dev.hazard.potionoptimizer;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class PotionOptimizerModMenuIntegration implements ModMenuApi {
   @Override
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return this::buildScreen;
   }

   private Screen buildScreen(Screen parent) {
      PotionOptimizerConfig config = PotionOptimizerClient.getConfig();
      ConfigBuilder builder = ConfigBuilder.create()
         .setParentScreen(parent)
         .setTitle(Text.literal("Potion Optimizer"))
         .setSavingRunnable(config::save);

      ConfigCategory category = builder.getOrCreateCategory(Text.literal("General"));
      ConfigEntryBuilder entryBuilder = builder.entryBuilder();

      category.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enabled"), config.enabled)
         .setTooltip(Text.literal("Shows your splash and lingering potions the moment you throw them, instead of waiting for the server."))
         .setSaveConsumer(value -> config.enabled = value)
         .build());

      return builder.build();
   }
}

