package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.tiffit.tconplanner.screen.PlannerScreen;
import org.jetbrains.annotations.NotNull;

public class TextButton extends Button {

    private final PlannerScreen parent;
    private final Runnable onPress;
    private int color = 0xFFFFFF;
    private Component tooltip = null;


    public TextButton(int x, int y, Component text, Runnable onPress, PlannerScreen parent) {
        super(
                x, y, 58, 18,
                text,
                button -> {},
                DEFAULT_NARRATION
        );
        this.parent = parent;
        this.onPress = onPress;
    }


    public TextButton withColor(int color) {
        this.color = color;
        return this;
    }


    public TextButton withTooltip(Component tooltip) {
        this.tooltip = tooltip;
        return this;
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;


        this.isHovered = mouseX >= getX() && mouseY >= getY()
                && mouseX < getX() + width && mouseY < getY() + height;


        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        PlannerScreen.bindTexture();


        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        RenderSystem.setShaderColor(red, green, blue, 1.0F);


        guiGraphics.blit(
                PlannerScreen.TEXTURE,
                getX(), getY(),
                176, 183,
                width, height
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);


        int textColor = isHovered ? 0xFFFFFFFF : 0xA0FFFFFF;
        guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                getMessage(),
                getX() + width / 2,
                getY() + 5,
                textColor
        );


        if (isHovered && tooltip != null) {
            parent.postRenderTasks.add(() ->
                    guiGraphics.renderTooltip(
                            Minecraft.getInstance().font,
                            tooltip,
                            mouseX,
                            mouseY
                    )
            );
        }
    }


    @Override
    public void onPress() {
        onPress.run();
    }


    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}
