package net.tiffit.tconplanner.screen.ext;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.Icon;

import java.util.function.Supplier;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ExtIconButton extends Button {

    private static final Supplier<Boolean> ALWAYS_TRUE = () -> true;

    private final Icon icon;
    private final Screen screen;
    private Holder.Reference<SoundEvent> pressSound = SoundEvents.UI_BUTTON_CLICK;
    private int color = 0xFFFFFFFF;

    private Supplier<Boolean> enabledFunc = ALWAYS_TRUE;


    public ExtIconButton(int x, int y, Icon icon, Component tooltip, OnPress action, Screen screen) {
        super(
                x, y, 12, 12,
                Component.empty(),
                action,
                DEFAULT_NARRATION
        );
        this.icon = icon;
        this.screen = screen;

    }


    public ExtIconButton withColor(int color) {
        this.color = color;
        return this;
    }


    public ExtIconButton withEnabledFunc(Supplier<Boolean> func) {
        this.enabledFunc = func;
        return this;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!enabledFunc.get()) return false;
        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!enabledFunc.get()) return;


        this.isHovered = isMouseOver(mouseX, mouseY);


        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        RenderSystem.setShaderColor(r, g, b, isHovered ? 1.0f : 0.8f);


        icon.render( guiGraphics, getX(), getY());


        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();


        if (isHovered) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }


    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        Component tooltip = this.getMessage();
        guiGraphics.renderTooltip(
                Minecraft.getInstance().font,
                tooltip,
                mouseX,
                mouseY
        );
    }


    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        if (pressSound != null) {
            soundManager.play(SimpleSoundInstance.forUI(pressSound, 1.0F));
        }
    }


    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}