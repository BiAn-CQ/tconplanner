package net.tiffit.tconplanner.screen.ext;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.tiffit.tconplanner.screen.buttons.BookmarkedButton;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.entity.ItemRenderer;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class ExtItemStackButton extends Button {
    public static final ResourceLocation BACKGROUND = ResourceLocation.bySeparator(
            "tconstruct:textures/gui/tinker_station.png",
            ResourceLocation.NAMESPACE_SEPARATOR
    );

    private final ItemStack stack;
    private final Screen screen;
    private final List<Component> tooltips;


    public ExtItemStackButton(int x, int y, ItemStack stack, List<Component> tooltips, OnPress action, Screen screen) {
        super(
                x, y, 16, 16,
                Component.empty(),
                action,
                DEFAULT_NARRATION
        );
        this.stack = stack.copy();
        this.screen = screen;
        this.tooltips = tooltips == null ? Collections.emptyList() : new ArrayList<>(tooltips);
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;


        this.isHovered = isMouseOver(mouseX, mouseY);

        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();


        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKGROUND);


        guiGraphics.blit(BACKGROUND, getX() - 1, getY() - 1, 194, 0, 18, 18);


        if (!isHoveredOrFocused()) {
            guiGraphics.fill(getX(), getY(), getX() + 16, getY() + 16, 0xFFA29B81); // ARGB 格式
        }


        guiGraphics.renderItem(stack, getX(), getY());
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, getX(), getY());



        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.6f);

        BookmarkedButton.STAR_ICON.render( guiGraphics, getX() + 2, getY() + 2);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        poseStack.popPose();


        if (isHoveredOrFocused()) {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }


    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();

        List<Component> combinedTooltips = Stream.concat(
                Screen.getTooltipFromItem(Minecraft.getInstance(), stack).stream(),
                tooltips.stream()
        ).collect(Collectors.toList());


        mc.submit(() -> guiGraphics.renderTooltip(
                mc.font,
                (Component) combinedTooltips,
                mouseX,
                mouseY
        ));
    }


    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {


    }
}
