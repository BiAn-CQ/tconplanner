package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import net.minecraft.util.Mth;
import net.tiffit.tconplanner.screen.PlannerScreen;

import java.util.function.Consumer;


import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SliderWidget extends AbstractWidget {

    private final PlannerScreen parent;
    private final Consumer<Integer> listener;
    private final int min, max;
    private double percent;
    private int value;

    public SliderWidget(int x, int y, int width, int height, Consumer<Integer> listener, int min, int max, int value, PlannerScreen parent) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;
        this.listener = listener;
        this.min = min;
        this.max = max;
        this.value = value;
        percent = (value - min) / (double) (max - min);
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;


        this.isHovered = mouseX >= getX() && mouseY >= getY()
                && mouseX < getX() + width && mouseY < getY() + height;


        PlannerScreen.bindTexture();
        int centerY = getY() + height / 2;


        for (int dx = getX() - 2; dx < getX() + width + 2; dx++) {
            guiGraphics.blit(
                    PlannerScreen.TEXTURE,
                    dx, centerY - 2,
                    176, 78,
                    1, 4
            );
        }


        int sliderX = getX() + (int) (width * percent);
        guiGraphics.blit(
                PlannerScreen.TEXTURE,
                sliderX - 2, getY(),
                178, 78,
                4, 20
        );


        Minecraft mc = Minecraft.getInstance();
        String minText = String.valueOf(min);
        String maxText = String.valueOf(max);
        String valueText = String.valueOf(value);


        guiGraphics.drawString(
                mc.font, minText,
                getX() - mc.font.width(minText) - 5, centerY - 4,
                0xFFFFFFFF, false
        );
        guiGraphics.drawString(
                mc.font, maxText,
                getX() + width + 5, centerY - 4,
                0xFFFFFFFF, false
        );


        guiGraphics.drawCenteredString(
                mc.font, valueText,
                sliderX, getY() + 22,
                0xFFFFFFFF
        );
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        updateValue(mouseX);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isHoveredOrFocused()) {
            updateValue(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void updateValue(double mouseX) {

        percent = Mth.clamp((mouseX - getX()) / width, 0.0, 1.0);


        int oldValue = value;
        value = (int) Math.round((max - min) * percent + min);


        if (value != oldValue) {
            listener.accept(value);
        }
    }


    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}