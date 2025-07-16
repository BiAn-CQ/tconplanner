package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.tiffit.tconplanner.screen.PlannerScreen;
import org.jetbrains.annotations.NotNull;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class BannerWidget extends AbstractWidget {

    private static final ResourceLocation TEXTURE = ResourceLocation.parse("tconplanner:textures/gui/planner.png");
    private final PlannerScreen parent;

    public BannerWidget(int x, int y, Component text, PlannerScreen parent) {
        super(x, y, 90, 19, text);
        this.parent = parent;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);


        guiGraphics.blit(TEXTURE, getX(), getY(), 0, 205, width, height);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                getX() + width/2, getY() + 5, getTextColor());
    }

    private int getTextColor() {
        return this.isHovered ? 0xFF_D0_D0_FF : 0xFF_90_90_FF;
    }

    @Override
    public void playDownSound(net.minecraft.client.sounds.@NotNull SoundManager soundManager) {

    }


    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }


}