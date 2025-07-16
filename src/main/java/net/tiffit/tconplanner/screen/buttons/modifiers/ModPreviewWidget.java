package net.tiffit.tconplanner.screen.buttons.modifiers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.tiffit.tconplanner.screen.PlannerScreen;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;

public class ModPreviewWidget extends AbstractWidget {
    private final ItemStack stack;
    private final boolean disabled;
    private final PlannerScreen parent;

    public ModPreviewWidget(int x, int y, ItemStack stack, PlannerScreen parent) {
        super(x, y, 16, 16, Component.empty());
        this.parent = parent;
        this.disabled = stack.isEmpty();

        this.stack = disabled ? new ItemStack(Items.BARRIER) : stack.copy();
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;


        this.isHovered = isMouseOver(mouseX, mouseY);


        guiGraphics.renderItem(stack, getX(), getY());
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, getX(), getY());


        if (isHovered && !disabled) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }


    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> guiGraphics.renderTooltip(
                Minecraft.getInstance().font,
                stack,
                mouseX,
                mouseY
        ));
    }


    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {}


    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}
