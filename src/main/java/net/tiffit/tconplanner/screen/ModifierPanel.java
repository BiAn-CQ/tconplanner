package net.tiffit.tconplanner.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.screen.buttons.*;
import net.tiffit.tconplanner.screen.buttons.modifiers.*;
import net.tiffit.tconplanner.util.*;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.impl.DurabilityShieldModifier;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;

import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.LazyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import net.minecraft.network.chat.TextColor;


import net.tiffit.tconplanner.util.TranslationUtil;

import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class ModifierPanel extends PlannerPanel {
    public static final String KEY_MAX_LEVEL = TConstruct.makeTranslationKey("recipe", "modifier.max_level");
    private static final SlotType[] ValidSlots = new SlotType[]{SlotType.UPGRADE, SlotType.ABILITY};

    public ModifierPanel(int x, int y, int width, int height, ItemStack result, ToolStack tool, List<IDisplayModifierRecipe> modifiers, PlannerScreen parent) {
        super(x, y, width, height, parent);
        initSlotWidgets();
        initModifierWidgets(result, tool, modifiers);
    }


    private void initSlotWidgets() {
        int slotIndex = 0;
        for (SlotType slotType : ValidSlots) {

            List<Component> tooltips = new ArrayList<>();


            MutableComponent coloredName = Component.literal("")
                    .withStyle(Style.EMPTY.withColor(slotType.getColor()))
                    .append(slotType.getDisplayName());

            tooltips.add(TranslationUtil.createComponent("slots.available", coloredName));
            tooltips.add(Component.literal(""));
            MutableComponent creativeSlotText = Component.literal(TranslationUtil.createComponent("modifiers.addcreativeslot").getString())
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x00FF00)));
            tooltips.add(creativeSlotText);

            int slots = parent.blueprint.getFreeSlots(slotType);
            MutableComponent slotsRemaining = Component.literal(String.valueOf(slots));
            int creativeSlots = parent.blueprint.creativeSlots.getOrDefault(slotType, 0);
            if (creativeSlots > 0) {
                slotsRemaining.append(Component.literal(" (+" + creativeSlots + ")"));
            }

            addChild(new TooltipTextWidget(
                    108, 23 + slotIndex * 12,
                    TextPosEnum.LEFT,
                    slotsRemaining,
                    tooltips,
                    parent
            ).withColor(0xFF000000 | slotType.getColor().getValue())

                    .withClickHandler((mouseX, mouseY, mouseButton) ->
                            handleCreativeSlotButton(slotType, parent.blueprint.getFreeSlots(slotType), creativeSlots, mouseButton)));
            slotIndex++;
        }

        addChild(new BannerWidget(7, 0, TranslationUtil.createComponent("banner.modifiers"), parent));
    }



    private void initModifierWidgets(ItemStack result, ToolStack tool, List<IDisplayModifierRecipe> modifiers) {
        int modGroupStartY = 23;
        int modGroupStartX = 2;
        Blueprint blueprint = parent.blueprint;
        ModifierInfo selectedModifier = parent.selectedModifier;
        ModifierStack modifierStack = parent.modifierStack;


        if (modifierStack != null) {
            initModifierStackWidgets(modGroupStartX, modGroupStartY, blueprint, modifierStack, result, tool);
        }

        else if (selectedModifier == null) {
            initModifierListWidgets(modGroupStartX, modGroupStartY, modifiers, blueprint, tool, result);
        }

        else {
            initSingleModifierWidgets(modGroupStartX, modGroupStartY, selectedModifier, blueprint, tool, result);
        }
    }


    private void initModifierStackWidgets(int x, int y, Blueprint blueprint, ModifierStack modifierStack, ItemStack result, ToolStack tool) {

        PaginatedPanel<ModifierStackButton> stackGroup = new PaginatedPanel<>(
                x, y, 100, 18, 1, 5, 2, "modifierstackgroup", parent
        );
        addChild(stackGroup);


        ToolStack displayStack = ToolStack.from(blueprint.createOutput(false));
        List<ModifierInfo> modStack = modifierStack.getStack();
        Blueprint resultingBlueprint = blueprint.clone();
        resultingBlueprint.modStack = modifierStack;
        Level level = Minecraft.getInstance().level;
        RegistryAccess registryAccess = null;
        if (level != null) {

            registryAccess = level.registryAccess();
        }

        Component validationError = null;
        if (registryAccess != null) {
            slimeknights.tconstruct.library.recipe.RecipeResult<slimeknights.tconstruct.library.tools.nbt.ToolStack> validationResult = resultingBlueprint.validate(registryAccess);

            if (!validationResult.isSuccess()) {
                validationError = validationResult.getMessage();
            }

        }



        RecipeResult<LazyToolStack> recipeResult;
        if (validationError == null) {

            recipeResult = RecipeResult.success(LazyToolStack.from(displayStack.createStack()));
        } else {

            recipeResult = RecipeResult.failure(validationError);
        }

        boolean isValid = recipeResult.isSuccess();

        Map<ModifierId, Integer> levelCount = new HashMap<>();
        for (int i = 0; i < modStack.size(); i++) {
            ModifierInfo info = modStack.get(i);
            int newLevel = levelCount.getOrDefault(info.modifier.getId(), 0) + 1;
            levelCount.put(info.modifier.getId(), newLevel);
            displayStack.addModifier(info.modifier.getId(), 1);
            if (info.count != null) {
                displayStack.getPersistentData().addSlots(info.count.type(), -info.count.count());
            }
            displayStack.rebuildStats();
            stackGroup.addChild(new ModifierStackButton(info, i, newLevel, displayStack.copy().createStack(), parent));
        }
        stackGroup.refresh();


        addChild(new TextButton(
                x + 50 - 58 / 2, 158,
                TranslationUtil.createComponent("modifierstack.save"),
                () -> {
                    if (isValid) {
                        parent.blueprint.modStack = parent.modifierStack;
                        parent.modifierStack = null;
                        parent.refresh();
                    }
                },
                parent
        ).withColor(isValid ? 0x50FF50 : 0x1A0000)
                .withTooltip(isValid ? null : validationError));


        addChild(new TextButton(
                x + 50 - 58 / 2, 180,
                TranslationUtil.createComponent("modifierstack.cancel"),
                () -> {
                    parent.modifierStack = null;
                    parent.refresh();
                },
                parent
        ).withColor(0xE02121));


        if (parent.selectedModifierStackIndex != -1) {
            addChild(new StackMoveButton(x + 50 - 9, 130, true, stackGroup, parent));
            addChild(new StackMoveButton(x + 50 - 9, 141, false, stackGroup, parent));
        }
    }


    private void initModifierListWidgets(int x, int y, List<IDisplayModifierRecipe> modifiers, Blueprint blueprint, ToolStack tool, ItemStack result) {
        PaginatedPanel<ModifierSelectButton> modifiersGroup = new PaginatedPanel<>(
                x, y, 100, 18, 1, 9, 2, "modifiersgroup", parent
        );
        addChild(modifiersGroup);


        for (IDisplayModifierRecipe recipe : modifiers) {

            if (recipe.getToolWithoutModifier().stream()
                    .anyMatch(stack -> ToolStack.from(stack).getDefinition() == blueprint.toolDefinition)) {
                modifiersGroup.addChild(ModifierSelectButton.create(recipe, tool, result, parent));
            }
        }

        modifiersGroup.sort(Comparator.comparingInt(value -> value.state.ordinal()));
        modifiersGroup.refresh();


        addChild(new IconButton(
                100, 0, new Icon(5, 0),
                TranslationUtil.createComponent("editmodifierstack"),
                parent,
                e -> {
                    parent.modifierStack = blueprint.clone().modStack;
                    parent.selectedModifierStackIndex = -1;
                    parent.refresh();
                }
        ));
    }


    private void initSingleModifierWidgets(int x, int y, ModifierInfo selectedModifier, Blueprint blueprint, ToolStack tool, ItemStack result) {

        ModifierSelectButton modSelectButton = ModifierSelectButton.create(
                selectedModifier.recipe, tool, result, parent
        );
        if (modSelectButton != null) {
            modSelectButton.setX(x);
        }
        if (modSelectButton != null) {
            modSelectButton.setY(y);
        }
        if (modSelectButton != null) {
            addChild(modSelectButton);
        }

        Modifier modifier = selectedModifier.modifier;
        ITinkerStationRecipe tsrecipe = (ITinkerStationRecipe) selectedModifier.recipe;


        addChild(new ModPreviewWidget(x + 50 - 9, 50, result, parent));


        int arrowOffset = 11;
        ModLevelButton addButton = new ModLevelButton(x + 50 + arrowOffset - 2, 50, 1, parent);

        Component addError = null;
        boolean addSuccess = true;
        if ((modifier instanceof NoLevelsModifier || modifier instanceof DurabilityShieldModifier)
                && tool.getModifierLevel(modifier) >= 1) {
            addSuccess = false;
            addError = Component.translatable(KEY_MAX_LEVEL, modifier.getDisplayName(), 1);
        } else {

            Level level = Minecraft.getInstance().level;
            RegistryAccess registryAccess = null;
            if (level != null) {

                registryAccess = level.registryAccess();
            }

            if (registryAccess != null) {
                slimeknights.tconstruct.library.recipe.RecipeResult<slimeknights.tconstruct.library.tools.nbt.LazyToolStack> validationResult = tsrecipe.getValidatedResult(new DummyTinkersStationInventory(result), registryAccess);
                addSuccess = validationResult.isSuccess();

                if (!addSuccess) {
                    addError = validationResult.getMessage();
                }
            } else {
                addSuccess = false;
                addError = Component.literal("无法获取 RegistryAccess");
            }
        }

        if (!addSuccess) {
            addButton.disable(addError.copy().withStyle(Style.EMPTY.withColor(0xFF0000)));
            addChild(new ModPreviewWidget(addButton.getX() + addButton.getWidth() + 2, 50, ItemStack.EMPTY, parent));
        } else if (blueprint.modStack.getIncrementalDiff(modifier) > 0) {
            addButton.disable(Component.literal(TranslationUtil.createComponent("modifiers.error.incrementnotmax").getString())
                    .withStyle(Style.EMPTY.withColor(0xFF0000)));
            addChild(new ModPreviewWidget(addButton.getX() + addButton.getWidth() + 2, 50, ItemStack.EMPTY, parent));
        } else {
            Blueprint copy = blueprint.clone();
            copy.modStack.push(selectedModifier);
            addChild(new ModPreviewWidget(addButton.getX() + addButton.getWidth() + 2, 50, copy.createOutput(), parent));
        }
        addChild(addButton);


        ModLevelButton subtractButton = new ModLevelButton(x + 50 - arrowOffset - 18, 50, -1, parent);

        boolean subtractSuccess = true;

        Component subtractError = null;


        Object validationResultObj = ToolValidator.validateModRemoval(blueprint, tool, selectedModifier);

        RecipeResult<LazyToolStack> validationResult = null;


        if (validationResultObj instanceof RecipeResult<?> tempResult && tempResult.getResult() instanceof LazyToolStack) {
            validationResult = (RecipeResult<LazyToolStack>) tempResult;
        }


        subtractSuccess = validationResult != null && validationResult.isSuccess();


        if (validationResult != null && !validationResult.isSuccess()) {
            try {
                subtractError = validationResult.getMessage();
            } catch (UnsupportedOperationException e) {
                LOGGER.error("调用发生异常", e);
                subtractError = Component.literal("无法获取错误信息");
            }
        }

        ItemStack previewStack = null;
        if (validationResult != null) {
            previewStack = subtractButton.isDisabled() ? ItemStack.EMPTY : validationResult.getResult().getStack();
        }
        addChild(new ModPreviewWidget(
                subtractButton.getX() - 2 - 18, 50,
                previewStack,
                parent
        ));
        addChild(subtractButton);

        int level = 1;
        int amount = 1;
        int perLevel = ModifierLevelInfo.getNeededPerLevelFromEntry(modifier.getId(), level, amount);
        if (perLevel > 0 && blueprint.modStack.getLevel(modifier) > 0) {
            addChild(new SliderWidget(
                    x + 10, 70, 80, 20,
                    val -> {
                        blueprint.modStack.setIncrementalDiff(modifier, perLevel - val);
                        parent.refresh();
                    },
                    1, perLevel,
                    perLevel - blueprint.modStack.getIncrementalDiff(modifier),
                    parent
            ));
        }


        addChild(new TextButton(
                x + 50 - 58 / 2, 115,
                TranslationUtil.createComponent("modifiers.exit"),
                () -> {
                    parent.selectedModifier = null;
                    parent.refresh();
                },
                parent
        ).withColor(0xE02121));
    }


    private boolean handleCreativeSlotButton(SlotType type, int remainingSlots, int creativeSlots, int mouseButton) {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if (mouseButton == 0) {
            parent.blueprint.addCreativeSlot(type);
            parent.refresh();
            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_PLACE, 2.0F, 0.08F));
            return true;
        } else if (mouseButton == 1) {
            if (creativeSlots > 0 && remainingSlots > 0) {
                parent.blueprint.removeCreativeSlot(type);
                parent.refresh();
                soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 2.0F, 0.08F));
                return true;
            }

            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.BAMBOO_FALL, 2.0F, 0.08F));
        }
        return false;
    }
}
