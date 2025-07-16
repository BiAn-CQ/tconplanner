package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;

import net.minecraft.world.item.ItemStack;
import net.tiffit.tconplanner.screen.PlannerScreen;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class OutputToolWidget extends AbstractWidget {

    private final ItemStack stack;
    private final PlannerScreen parent;


    public OutputToolWidget(int x, int y, ItemStack stack, PlannerScreen parent) {
        super(x, y, 16, 16, Component.empty());
        this.parent = parent;
        this.stack = stack;
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;


        this.isHovered = mouseX >= getX() && mouseY >= getY()
                && mouseX < getX() + width && mouseY < getY() + height;


        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();


        guiGraphics.blit(
                PlannerScreen.TEXTURE,
                getX() - 6, getY() - 6,
                176, 117,
                28, 28
        );

        guiGraphics.renderItem(stack, getX(), getY());
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, getX(), getY());


        if (isHovered) {
            parent.postRenderTasks.add(() -> renderTooltip(guiGraphics, mouseX, mouseY));
        }
    }


    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        guiGraphics.renderTooltip(
                Minecraft.getInstance().font,
                stack,
                mouseX,
                mouseY
        );
    }



    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {

    }


    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput p_259858_) {

    }


}