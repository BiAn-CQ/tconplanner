package net.tiffit.tconplanner.screen.buttons.modifiers;

import slimeknights.tconstruct.library.modifiers.IncrementalModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;

public class ModifierLevelInfo {

    public static int getNeededPerLevelFromEntry(ModifierId modifierId, int level, int amount) {

        ModifierEntry entry = IncrementalModifierEntry.of(modifierId, level, amount, 0);
        if (entry instanceof IncrementalModifierEntry incrementalEntry) {
            return incrementalEntry.getNeeded();
        }
        return 0;
    }
}
