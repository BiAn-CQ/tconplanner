package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.gui.components.Button;
import net.tiffit.tconplanner.screen.PlannerScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class MatPageButton extends Button {
    private final boolean right;
    private final PlannerScreen parent;


    public MatPageButton(int x, int y, int change, PlannerScreen parent) {
        super(
                x, y, 38, 20,
                Component.empty(),
                button -> {
                    parent.materialPage += change;
                    parent.refresh();
                },
                DEFAULT_NARRATION
        );
        this.right = change > 0;
        this.parent = parent;
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;


        this.isHovered = mouseX >= getX() && mouseY >= getY()
                && mouseX < getX() + width && mouseY < getY() + height;


        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();


        int u = right ? 176 : 214;
        int v = this.active ? 20 : 0;
        guiGraphics.blit(
                PlannerScreen.TEXTURE,
                getX(), getY(),
                u, v,
                width, height
        );


        if (isHovered) {
            guiGraphics.fill(
                    getX(), getY(),
                    getX() + width, getY() + height,
                    0x33FFFFFF
            );
        }
    }
}