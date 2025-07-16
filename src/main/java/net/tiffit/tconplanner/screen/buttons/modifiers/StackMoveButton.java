package net.tiffit.tconplanner.screen.buttons.modifiers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.screen.buttons.PaginatedPanel;
import net.tiffit.tconplanner.util.TranslationUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.NotNull;

public class StackMoveButton extends Button {
    private static final Component MOVE_UP = TranslationUtil.createComponent("modifierstack.moveup");
    private static final Component MOVE_DOWN = TranslationUtil.createComponent("modifierstack.movedown");
    private final PaginatedPanel<ModifierStackButton> scrollPanel;
    private final PlannerScreen parent;
    private final boolean moveUp;

    public StackMoveButton(int x, int y, boolean moveUp, PaginatedPanel<ModifierStackButton> scrollPanel, PlannerScreen parent) {
        super(
                x, y, 18, 10,
                Component.empty(),

                button -> {
                    if (moveUp) {
                        // 上移逻辑
                        if (parent.selectedModifierStackIndex > 0) {
                            parent.modifierStack.moveDown(parent.selectedModifierStackIndex - 1);
                            parent.selectedModifierStackIndex--;
                            scrollPanel.makeVisible(parent.selectedModifierStackIndex, false);
                            parent.refresh();
                        }
                    } else {
                        // 下移逻辑
                        if (parent.selectedModifierStackIndex < parent.modifierStack.getStack().size() - 1) {
                            parent.modifierStack.moveDown(parent.selectedModifierStackIndex);
                            parent.selectedModifierStackIndex++;
                            scrollPanel.makeVisible(parent.selectedModifierStackIndex, false);
                            parent.refresh();
                        }
                    }
                },
                DEFAULT_NARRATION
        );
        this.parent = parent;
        this.moveUp = moveUp;
        this.scrollPanel = scrollPanel;
    }






    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;


        this.isHovered = isMouseOver(mouseX, mouseY);


        PlannerScreen.bindTexture();
        RenderSystem.enableBlend();
        int vOffset = moveUp ? 0 : height;
        guiGraphics.blit(PlannerScreen.TEXTURE, getX(), getY(), 214, 145 + vOffset, width, height);


        if (isHovered) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }


        RenderSystem.disableBlend();
    }


    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> {
            guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    moveUp ? MOVE_UP : MOVE_DOWN,
                    mouseX,
                    mouseY
            );
        });
    }


    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }


    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {

    }
}
