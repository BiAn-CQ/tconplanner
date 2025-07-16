package net.tiffit.tconplanner.screen.buttons.modifiers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.ModifierStack;
import net.tiffit.tconplanner.util.TranslationUtil;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public class ModLevelButton extends Button {

    private final PlannerScreen parent;
    private final int change;
    private boolean disabled = false;
    private Component tooltip;

    public void disable(Component tooltip) {
        this.tooltip = tooltip;
        disabled = true;
    }
    public boolean isDisabled() {
        return disabled;
    }


    public ModLevelButton(int x, int y, int change, PlannerScreen parent) {
        super(
                x, y, 18, 17,
                Component.empty(),

                button -> {
                    if (!((ModLevelButton) button).isDisabled()) {
                        ModifierStack stack = parent.blueprint.modStack;
                        stack.setIncrementalDiff(parent.selectedModifier.modifier, 0);
                        if (change > 0) {
                            stack.push(parent.selectedModifier);
                        } else {
                            stack.pop(parent.selectedModifier);
                        }
                        parent.refresh();
                    }
                },
                DEFAULT_NARRATION
        );
        this.parent = parent;
        this.change = change;



    }




    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;


        this.isHovered = isMouseOver(mouseX, mouseY);


        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, disabled ? 0.5f : 1f);


        int u = change > 0 ? 176 : 194;
        int v = disabled ? 146 : 163;
        guiGraphics.blit(PlannerScreen.TEXTURE, getX(), getY(), u, v, width, height);


        if (isHoveredOrFocused()) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }


        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }


    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        
        Style greenStyle = Style.EMPTY.withColor(TextColor.fromRgb(0x00FF00));

        MutableComponent tooltipText;
        if (isDisabled()) {
            
            tooltipText = (tooltip instanceof MutableComponent)
                    ? (MutableComponent) tooltip
                    : Component.literal(tooltip.getString()).withStyle(tooltip.getStyle());
        } else {
            
            tooltipText = TranslationUtil.createComponent(
                    change < 0 ? "modifiers.removelevel" : "modifiers.addlevel"
            ).copy().withStyle(greenStyle);
        }


        parent.postRenderTasks.add(() ->
                guiGraphics.renderTooltip(
                        Minecraft.getInstance().font,
                        tooltipText,
                        mouseX,
                        mouseY
                )
        );
    }


    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        if (!disabled) {
            super.playDownSound(soundManager);
        }
    }


    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}