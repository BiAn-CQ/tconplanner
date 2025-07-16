package net.tiffit.tconplanner.screen.buttons.modifiers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import net.minecraft.world.item.ItemStack;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.TranslationUtil;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.tools.SlotType;

import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.Minecraft;

public class ModifierStackButton extends Button {

    private final Modifier modifier;
    private final IDisplayModifierRecipe recipe;
    private final ModifierInfo modifierInfo;
    private final PlannerScreen parent;
    private final Component displayName;
    private final ItemStack display;
    private final int index;

    public ModifierStackButton(ModifierInfo modifierInfo, int index, int level, ItemStack display, PlannerScreen parent) {
        super(
                0, 0, 100, 18,
                Component.empty(),

                button -> {
                    parent.selectedModifierStackIndex = index;
                    parent.refresh();
                },
                DEFAULT_NARRATION
        );
        this.modifierInfo = modifierInfo;
        this.parent = parent;
        this.modifier = modifierInfo.modifier;
        this.recipe = modifierInfo.recipe;
        this.display = display;
        this.index = index;
        this.displayName = modifier.getDisplayName(level);



    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;


        this.isHovered = isMouseOver(mouseX, mouseY);


        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();


        if (parent.selectedModifierStackIndex == index) {
            RenderSystem.setShaderColor(1f, 0.78f, 0f, 1f); // 对应 RGB(255, 200, 0)
        } else {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }


        guiGraphics.blit(PlannerScreen.TEXTURE, getX(), getY(), 0, 224, 100, 18);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);



        guiGraphics.renderItem(display, getX() + 1, getY() + 1);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, display, getX() + 1, getY() + 1);

        Font font = Minecraft.getInstance().font;
        PoseStack poseStack = guiGraphics.pose();


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
            MutableComponent text = (MutableComponent) (count.count() == 1
                                ? TranslationUtil.createComponent("modifiers.usedslot", count.type().getDisplayName())
                                : TranslationUtil.createComponent("modifiers.usedslots", count.count(), count.type().getDisplayName()));
            guiGraphics.drawString(font, text, 0, 0, 0xFFFFFFFF);
        }
        poseStack.popPose();


        if (isHovered) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }


    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> {
            guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    (Component) modifier.getDescriptionList(),
                    mouseX,
                    mouseY
            );
        });
    }


    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {
    }
}