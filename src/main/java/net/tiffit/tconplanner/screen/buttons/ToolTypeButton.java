package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.tiffit.tconplanner.api.TCTool;
import net.tiffit.tconplanner.screen.PlannerScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ToolTypeButton extends Button {

    private final TCTool tool;
    private final PlannerScreen parent;
    public final int index;

    public ToolTypeButton(int index, TCTool tool, PlannerScreen parent) {
        super(
                0, 0, 18, 18,
                tool.getDescription(),
                button -> parent.setSelectedTool(index),
                DEFAULT_NARRATION
        );
        this.tool = tool;
        this.index = index;
        this.parent = parent;
    }


    private boolean isToolSelected() {
        return parent.blueprint != null && tool == parent.blueprint.tool;
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;


        this.isHovered = mouseX >= getX() && mouseY >= getY()
                && mouseX < getX() + width && mouseY < getY() + height;


        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();


        int vOffset = isToolSelected() ? 18 : 0;
        guiGraphics.blit(
                PlannerScreen.TEXTURE,
                getX(), getY(),
                213, 41 + vOffset,
                width, height
        );


        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        ItemStack renderStack = tool.getRenderTool();
        guiGraphics.renderItem(renderStack, getX() + 1, getY() + 1);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, renderStack, getX() + 1, getY() + 1);


        if (isHovered) {
            parent.postRenderTasks.add(() -> renderTooltip(guiGraphics, mouseX, mouseY));
        }
    }


    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() ->
                guiGraphics.renderTooltip(
                        Minecraft.getInstance().font,
                        tool.getRenderTool(),
                        mouseX,
                        mouseY
                )
        );
    }


    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}