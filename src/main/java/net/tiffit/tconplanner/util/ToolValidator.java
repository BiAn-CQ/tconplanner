package net.tiffit.tconplanner.util;

import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.ModifierInfo;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.nbt.LazyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import slimeknights.tconstruct.library.modifiers.IncrementalModifierEntry;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IncrementalModifierRecipe;


public final class ToolValidator {

    public static Object validateModRemoval(Blueprint bp, ToolStack tool, ModifierInfo modInfo) {
        ToolStack toolClone = tool.copy();
        int toolBaseLevel = ToolStack.from(bp.createOutput(false)).getModifierLevel(modInfo.modifier);
        int minLevel = Math.max(0, toolBaseLevel);
        if (bp.modStack.getLevel(modInfo.modifier) + toolBaseLevel <= minLevel || !bp.modStack.isRecipeUsed((ITinkerStationRecipe) modInfo.recipe)) {
            return RecipeResult.failure(Component.translatable("gui.tconplanner.modifiers.error.minlevel"));
        }
        toolClone.removeModifier(modInfo.modifier.getId(), 1);


        IncrementalModifierEntry entry = (IncrementalModifierEntry) toolClone.getUpgrades().getEntry(modInfo.modifier.getId());
        if (entry != null) {
            int neededPerLevel = 1;
            IncrementalModifierEntry newEntry = (IncrementalModifierEntry) IncrementalModifierEntry.of(modInfo.modifier, entry.getLevel(), entry.getAmount(0), neededPerLevel);


            ModifierNBT newUpgrades = toolClone.getUpgrades().withoutModifier(modInfo.modifier.getId(), entry.getLevel());

            newUpgrades = newUpgrades.withModifier(modInfo.modifier.getId(), newEntry.getLevel());


            toolClone.setUpgrades(newUpgrades);
        }



        IncrementalModifierRecipe recipe = new IncrementalModifierRecipe(
                null, null, 1, 1, null, 0, modInfo.modifier.getId(), null, null, null, false, false);
        RecipeResult<LazyToolStack> validatedResultSubtract = recipe.getValidatedResult(null, null);
        if (validatedResultSubtract.hasError()) return validatedResultSubtract;

        Blueprint bpClone = bp.clone();
        bpClone.modStack.pop(modInfo);

        RecipeResult<ItemStack> bpResult = RecipeResult.success(bpClone.createOutput(false));
        if (bpResult.hasError()) return bpResult;

        return RecipeResult.success(toolClone.createStack());
    }
}
