package net.tiffit.tconplanner.util;


import net.minecraft.network.chat.Component;



    public final class TranslationUtil {
        public static Component createComponent(String key, Object... inserts) {
            return Component.translatable("gui.tconplanner." + key, inserts);
        }
    }
