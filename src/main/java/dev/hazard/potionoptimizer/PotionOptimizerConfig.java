package dev.hazard.potionoptimizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class PotionOptimizerConfig {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("potionoptimizer.json");
   public boolean enabled = true;

   public static PotionOptimizerConfig load() {
      if (Files.exists(PATH)) {
         try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
            PotionOptimizerConfig config = GSON.fromJson(reader, PotionOptimizerConfig.class);
            if (config != null) {
               return config;
            }
         } catch (IOException e) {
            // ignore and fallback
         }
      }

      PotionOptimizerConfig config = new PotionOptimizerConfig();
      config.save();
      return config;
   }

   public void save() {
      try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
         GSON.toJson(this, writer);
      } catch (IOException e) {
         // ignore
      }
   }
}

