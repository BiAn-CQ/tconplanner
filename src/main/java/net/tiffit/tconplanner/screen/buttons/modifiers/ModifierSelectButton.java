package net.tiffit.tconplanner.screen.buttons.modifiers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.screen.ModifierPanel;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.DummyTinkersStationInventory;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.library.client.modifiers.ModifierIconManager;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.impl.DurabilityShieldModifier;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.modifiers.adding.AbstractModifierRecipe;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.LazyToolStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

public class ModifierSelectButton extends Button {

   
    private static final Style ERROR_STYLE = Style.EMPTY.withColor(TextColor.fromRgb(0xFF0000));

    private final IDisplayModifierRecipe recipe;
    private final Modifier modifier;
    private final boolean selected;
    private final Component error;
    private final Component displayName;
    public final ModifierStateEnum state;
    private final PlannerScreen parent;
    private final List<ItemStack> recipeStacks = new ArrayList<>();

    private final MutableComponent levelText;
    private Button.OnPress onPress;

    public ModifierSelectButton(IDisplayModifierRecipe recipe, ModifierStateEnum state, @Nullable Component error, int level, ToolStack tool, PlannerScreen parent) {
        super(
                0, 0, 100, 18,
                Component.empty(),
                button -> {},
                DEFAULT_NARRATION
        );
        this.recipe = recipe;
        this.modifier = recipe.getDisplayResult().getModifier();
        this.parent = parent;
        this.selected = false;
        this.state = state;
        this.error = error;

        for (int i = 0; i < recipe.getInputCount(); i++) {
            recipeStacks.addAll(recipe.getDisplayItems(i));
        }

        this.displayName = level == 0
                ? modifier.getDisplayName()
                : modifier.getDisplayName(level);

        boolean singleUse = modifier instanceof NoLevelsModifier || modifier instanceof DurabilityShieldModifier;

        int maxLevel;
        if (singleUse) {
            maxLevel = 1;
        } else if (recipe instanceof AbstractModifierRecipe) {
            AbstractModifierRecipe abstractRecipe = (AbstractModifierRecipe) recipe;
            maxLevel = abstractRecipe.getLevel().max();
        } else {
            maxLevel = 1;
        }
        int currentLevel = singleUse ? tool.getModifierLevel(modifier) : parent.blueprint.modStack.getLevel(modifier);
        if (currentLevel > maxLevel && maxLevel > 0) currentLevel = maxLevel;

        this.levelText = Component.literal(currentLevel + "/" + (maxLevel > 0 ? maxLevel : "\u221E"));
        if (error != null) {
            this.levelText.withStyle(ERROR_STYLE);
        }

        int finalCurrentLevel = currentLevel;
        this.onPress = button -> {
            if (state == ModifierStateEnum.AVAILABLE || state == ModifierStateEnum.APPLIED) {
                parent.selectedModifier = new ModifierInfo(recipe, finalCurrentLevel);
                parent.refresh();
            }
        };
    }

   
    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;


        this.isHovered = isMouseOver(mouseX, mouseY);


        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        switch (state) {
            case APPLIED:
                RenderSystem.setShaderColor(0.5f, 1f, 0.5f, 1f);
                break;
            case UNAVAILABLE:
                RenderSystem.setShaderColor(1f, 0.5f, 0.5f, 1f);
                break;
            default:
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }


        guiGraphics.blit(PlannerScreen.TEXTURE, getX(), getY(), 0, 224, 100, 18);


        PoseStack poseStack = guiGraphics.pose();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        if (isHoveredOrFocused() && !recipeStacks.isEmpty()) {

            int index = (int) (System.currentTimeMillis() / 1000 % recipeStacks.size());
            guiGraphics.renderItem(recipeStacks.get(index), getX() + 1, getY() + 1);
        } else {
           
            ModifierIconManager.renderIcon(guiGraphics, modifier, getX() + 1, getY() + 1, 0, 16);
        }

        Font font = Minecraft.getInstance().font;

       
        poseStack.pushPose();
        poseStack.translate(getX() + 20, getY() + 2, 0);
        float nameWidth = font.width(displayName);
        int maxWidth = width - 22;
        if (nameWidth > maxWidth) {
            float scale = maxWidth / nameWidth;
            poseStack.scale(scale, scale, 1);
        }
        guiGraphics.drawString(font, displayName, 0, 0, 0xFFFFFFFF);
        poseStack.popPose();

       
        poseStack.pushPose();
        poseStack.translate(getX() + 20, getY() + 11, 0);
        poseStack.scale(0.5f, 0.5f, 1);
        if (recipe.getSlots() != null) {
            SlotType.SlotCount count = recipe.getSlots();
            MutableComponent slotText = (MutableComponent) (count.count() == 1
                                ? TranslationUtil.createComponent("modifiers.usedslot", count.type().getDisplayName())
                                : TranslationUtil.createComponent("modifiers.usedslots", count.count(), count.type().getDisplayName()));
            guiGraphics.drawString(font, slotText, 0, 0, 0xFFFFFFFF);
        }
        poseStack.popPose();

    
        poseStack.pushPose();
        poseStack.translate(getX() + width - 1, getY() + 11, 0);
        poseStack.scale(0.5f, 0.5f, 1);
        int levelTextWidth = font.width(levelText);
        guiGraphics.drawString(font, levelText, -levelTextWidth, 0, 0xFFFFFFFF);
        poseStack.popPose();

   
        if (isHovered) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }


        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }


    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<Component> tooltips = new ArrayList<>(modifier.getDescriptionList());
        if (error != null) {
            tooltips.add(error.copy().withStyle(ERROR_STYLE));
        }
        parent.postRenderTasks.add(() ->

                guiGraphics.renderComponentTooltip(
                        Minecraft.getInstance().font,
                        tooltips,
                        mouseX,
                        mouseY
                )
        );
    }


    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        if (state == ModifierStateEnum.UNAVAILABLE) {
            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_HIT, 1.0F));
        } else {
            super.playDownSound(soundManager);
        }
    }


    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }

 
    public static ModifierSelectButton create(IDisplayModifierRecipe recipe, ToolStack tstack, ItemStack stack, PlannerScreen screen) {
        ITinkerStationRecipe tsrecipe = (ITinkerStationRecipe) recipe;
        ModifierStateEnum mstate = ModifierStateEnum.UNAVAILABLE;
        Component error = null;
        Modifier modifier = recipe.getDisplayResult().getModifier();
        int currentLevel = tstack.getModifierLevel(modifier);

    
        if (currentLevel != 0) {
            mstate = ModifierStateEnum.APPLIED;
        }

        RegistryAccess registryAccess;

        Level level = Minecraft.getInstance().level;
        if (level != null) {
            registryAccess = level.registryAccess();
        } else {

            return null;
        }

        RecipeResult<LazyToolStack> validatedResult = tsrecipe.getValidatedResult(new DummyTinkersStationInventory(stack), registryAccess);


        if (!validatedResult.isSuccess()) {
            error = validatedResult.getMessage();
        } else {
            if (currentLevel >= 1 && (modifier instanceof NoLevelsModifier || modifier instanceof DurabilityShieldModifier)) {

                RecipeResult<?> maxLevelResult = RecipeResult.failure(ModifierPanel.KEY_MAX_LEVEL, modifier.getDisplayName(), 1);
                if (!maxLevelResult.isSuccess()) {
                    error = maxLevelResult.getMessage();
                }
            } else if (mstate != ModifierStateEnum.APPLIED) {
                mstate = ModifierStateEnum.AVAILABLE;
            }
        }


        if (validatedResult.isSuccess() && mstate != ModifierStateEnum.APPLIED) {
            mstate = ModifierStateEnum.AVAILABLE;
        } else if (!validatedResult.isSuccess()) {
            error = validatedResult.getMessage();
        }

        return new ModifierSelectButton(recipe, mstate, error, currentLevel, tstack, screen);
    }


    public enum ModifierStateEnum {
        APPLIED,    
        AVAILABLE,  
        UNAVAILABLE 
    }
}