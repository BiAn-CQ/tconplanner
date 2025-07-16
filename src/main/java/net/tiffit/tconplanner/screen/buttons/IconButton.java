package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.Icon;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.awt.*;

public class IconButton extends Button {

    private final Icon icon;
    private final PlannerScreen parent;
    private Holder.Reference<SoundEvent> pressSound = SoundEvents.UI_BUTTON_CLICK;
    private Color color = Color.WHITE;

    public IconButton(int x, int y, Icon icon, Component tooltip, PlannerScreen parent, Button.OnPress action) {
        super(x, y, 12, 12, tooltip, action, DEFAULT_NARRATION);
        this.icon = icon;
        this.parent = parent;
    }

    public IconButton withSound(Holder.Reference<SoundEvent> sound){
        this.pressSound = sound;
        return this;
    }

    public IconButton withColor(Color color){
        this.color = color;
        return this;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        this.isHovered = mouseX >= getX() && mouseY >= getY()
                && mouseX < getX() + width && mouseY < getY() + height;

        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                isHovered ? 1.0F : 0.8F
        );
        icon.render( guiGraphics, getX(), getY());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);


        if (isHovered) {
            parent.postRenderTasks.add(() ->
                    guiGraphics.renderTooltip(
                            Minecraft.getInstance().font,
                            getMessage(),
                            mouseX,
                            mouseY
                    )
            );
        }

    }



    @Override
    public void playDownSound(@NotNull SoundManager handler) {
        if(pressSound != null)handler.play(SimpleSoundInstance.forUI(pressSound, 1.0F));
    }
}