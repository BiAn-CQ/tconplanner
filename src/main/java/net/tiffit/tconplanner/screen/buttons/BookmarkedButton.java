package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.Icon;
import org.jetbrains.annotations.NotNull;

public class BookmarkedButton extends Button {

    public static final Icon STAR_ICON = new Icon(6, 0);

    private final PlannerScreen parent;
    private final ItemStack stack;
    private final int index;
    private final Blueprint blueprint;
    private final boolean starred;
    private boolean selected;

    public BookmarkedButton(int index, Blueprint blueprint, boolean starred, PlannerScreen parent) {

        super (0, 0, 18, 18, Component.empty (),
                button -> parent.setBlueprint (blueprint.clone ()),
                Button.DEFAULT_NARRATION);
        this.index = index;
        this.blueprint = blueprint;
        this.starred = starred;
        this.parent = parent;
        stack = blueprint.createOutput();
        this.selected = parent.blueprint != null && parent.blueprint.equals(blueprint);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() &&
                mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();


        guiGraphics.blit(PlannerScreen.TEXTURE, getX(), getY(), 213, 41 + (selected ? 18 : 0), 18, 18);



        guiGraphics.renderItem(stack, getX() + 1, getY() + 1);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, getX() + 1, getY() + 1);

        if (starred) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(getX() + 11, getY() + 11, 105);
            poseStack.scale(0.5f, 0.5f, 0.5f);


            STAR_ICON.render(guiGraphics, 0, 0);
            poseStack.popPose();
        }

        if (isHovered) {

            parent.postRenderTasks.add(() ->
                    guiGraphics.renderTooltip(Minecraft.getInstance().font, stack, mouseX, mouseY));
        }
    }
}