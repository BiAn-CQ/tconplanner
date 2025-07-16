package net.tiffit.tconplanner.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.tiffit.tconplanner.TConPlanner;


public class Icon {
    private static final ResourceLocation ICONS = ResourceLocation.bySeparator(TConPlanner.MODID + ":textures/gui/icons.png", ':');

    private final int x,y;


    public Icon(int x, int y) {
        this.x = x;
        this.y = y;
    }


    public void render(GuiGraphics guiGraphics, int x, int y){
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, ICONS);

        guiGraphics.blit(ICONS, x, y, this.x * 12, this.y * 12, 12, 12);
    }
}