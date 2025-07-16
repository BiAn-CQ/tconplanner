package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.TextPosEnum;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;


public class TooltipTextWidget extends AbstractWidget {

    private final PlannerScreen parent;
    private int color = 0xFFFFFFFF;
    private final List<Component> tooltip;
    private IOnTooltipTextWidgetClick onClick;
    private final TextPosEnum textPos;


    public TooltipTextWidget(int x, int y, Component text, Component tooltip, PlannerScreen parent) {
        this(x, y, TextPosEnum.LEFT, text, Collections.singletonList(tooltip), parent);
    }


    public TooltipTextWidget(int x, int y, TextPosEnum pos, Component text, Component tooltip, PlannerScreen parent) {
        this(x, y, pos, text, Collections.singletonList(tooltip), parent);
    }


    public TooltipTextWidget(int x, int y, TextPosEnum pos, Component text, List<Component> tooltip, PlannerScreen parent) {
        super(x, y, 0, 0, text);
        this.parent = parent;
        this.tooltip = tooltip;
        this.textPos = pos;

        setWidth(Minecraft.getInstance().font.width(text));
        setHeight(Minecraft.getInstance().font.lineHeight);

        adjustPositionByTextPos();
    }


    private void adjustPositionByTextPos() {
        switch (textPos) {
            case CENTER:

                setX(getX() - getWidth() / 2);
                break;
            case RIGHT:

                setX(getX() - getWidth());
                break;
            default:

                break;
        }
    }


    public TooltipTextWidget withColor(int color) {
        this.color = color;
        return this;
    }


    public TooltipTextWidget withClickHandler(IOnTooltipTextWidgetClick onClick) {
        this.onClick = onClick;
        return this;
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        this.isHovered = mouseX >= getX() && mouseY >= getY()
                && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();


        Component text = getMessage();
        switch (textPos) {
            case LEFT:
                guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        text,
                        getX(),
                        getY(),
                        color
                );
                break;
            case CENTER:
                guiGraphics.drawCenteredString(
                        Minecraft.getInstance().font,
                        text,
                        getX() + getWidth() / 2,
                        getY(),
                        color
                );
                break;
            case RIGHT:
                guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        text,
                        getX() + getWidth(),
                        getY(),
                        color
                );
                break;
        }


        if (isHoveredOrFocused()) {
            parent.postRenderTasks.add(() -> renderTooltip(guiGraphics, mouseX, mouseY));
        }
    }


    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        guiGraphics.renderComponentTooltip(
                Minecraft.getInstance().font,
                tooltip,
                mouseX,
                mouseY
        );
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.active && this.visible && clicked(mouseX, mouseY)) {
            return onClick != null && onClick.onClick(mouseX, mouseY, mouseButton);
        }
        return false;
    }


    public boolean clicked(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseY >= getY()
                && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
    }


    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }


    @FunctionalInterface
    public interface IOnTooltipTextWidgetClick {
        boolean onClick(double mouseX, double mouseY, int mouseButton);
    }
}
