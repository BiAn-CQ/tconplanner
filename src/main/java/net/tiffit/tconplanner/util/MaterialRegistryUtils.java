package net.tiffit.tconplanner.util;

import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatType;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;

import javax.annotation.Nullable;

public class MaterialRegistryUtils {


    @Nullable
    public static <T extends IMaterialStats> Class<T> getClassForStat(MaterialStatsId statsId) {
        IMaterialRegistry registry = MaterialRegistry.getInstance();
        MaterialStatType<T> statType = registry.getStatType(statsId);
        if (statType != null) {
            return (Class<T>) statType.getDefaultStats().getClass();
        }
        return null;
    }
}
