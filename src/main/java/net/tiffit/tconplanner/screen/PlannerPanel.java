package net.tiffit.tconplanner.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;

public class PlannerPanel extends AbstractWidget {

    protected final List<AbstractWidget> children = new ArrayList<>();
    protected final PlannerScreen parent;

    public PlannerPanel(int x, int y, int width, int height, PlannerScreen parent) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;
    }

    public void addChild(AbstractWidget widget) {

        widget.setX(widget.getX() + getX());
        widget.setY(widget.getY() + getY());
        children.add(widget);
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;


        this.isHovered = isMouseOver(mouseX, mouseY);


        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        for (AbstractWidget child : children) {
            child.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        poseStack.popPose();
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible || !this.active) return false;


        for (int i = children.size() - 1; i >= 0; i--) {
            AbstractWidget child = children.get(i);
            if (child.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean result = false;
        for (AbstractWidget child : children) {
            if (child.mouseReleased(mouseX, mouseY, button)) {
                result = true;
            }
        }
        return result || super.mouseReleased(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean result = false;
        for (AbstractWidget child : children) {
            if (child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                result = true;
            }
        }
        return result || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if (!this.visible || !this.active) return false;


        for (int i = children.size() - 1; i >= 0; i--) {
            AbstractWidget child = children.get(i);
            if (child.isMouseOver(mouseX, mouseY) && child.mouseScrolled(mouseX, mouseY, scrollDelta)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result = false;
        for (AbstractWidget child : children) {
            if (child.keyPressed(keyCode, scanCode, modifiers)) {
                result = true;
            }
        }
        return result || super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        boolean result = false;
        for (AbstractWidget child : children) {
            if (child.keyReleased(keyCode, scanCode, modifiers)) {
                result = true;
            }
        }
        return result || super.keyReleased(keyCode, scanCode, modifiers);
    }


    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean result = false;
        for (AbstractWidget child : children) {
            if (child.charTyped(codePoint, modifiers)) {
                result = true;
            }
        }
        return result || super.charTyped(codePoint, modifiers);
    }


    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}
