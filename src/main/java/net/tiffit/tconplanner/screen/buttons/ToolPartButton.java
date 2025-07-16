package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.tiffit.tconplanner.screen.PlannerScreen;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.tools.part.IToolPart;

public class ToolPartButton extends Button {

    private final ItemStack stack;
    private final IMaterial material;
    public final IToolPart part;
    private final PlannerScreen parent;
    public final int index;


    public ToolPartButton(int index, int x, int y, IToolPart part, IMaterial material, PlannerScreen parent) {
        super(
                x, y, 16, 16,
                Component.empty(),
                button -> parent.setSelectedPart(index),
                DEFAULT_NARRATION
        );
        this.index = index;
        this.part = part;
        this.parent = parent;
        this.material = material;

        this.stack = material == null
                ? new ItemStack(part.asItem())
                : part.withMaterialForDisplay(material.getIdentifier());
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;


        this.isHovered = mouseX >= getX() && mouseY >= getY()
                && mouseX < getX() + width && mouseY < getY() + height;


        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        boolean selected = parent.selectedPart == index;


        PoseStack modelStack = RenderSystem.getModelViewStack();
        modelStack.pushPose();
        modelStack.translate(0, 0, 1);
        RenderSystem.applyModelViewMatrix();


        PlannerScreen.bindTexture();
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.7f);
        RenderSystem.enableBlend();


        int u = 176 + (material == null ? 18 : 0);
        int v = 41 + (selected ? 18 : 0);
        guiGraphics.blit(
                PlannerScreen.TEXTURE,
                getX() - 1, getY() - 1,
                u, v,
                18, 18
        );


        modelStack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f); // 重置颜色



        guiGraphics.renderItem(stack, getX(), getY());
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, getX(), getY());

        if (isHovered) {
            parent.postRenderTasks.add(() -> renderTooltip(guiGraphics, mouseX, mouseY));
        }
    }


    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() ->
                guiGraphics.renderTooltip(
                        Minecraft.getInstance().font,
                        stack,
                        mouseX,
                        mouseY
                )
        );
    }


    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}
