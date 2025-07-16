package net.tiffit.tconplanner.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.tiffit.tconplanner.api.TCSlotPos;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.PlannerData;
import net.tiffit.tconplanner.screen.buttons.IconButton;
import net.tiffit.tconplanner.screen.buttons.OutputToolWidget;
import net.tiffit.tconplanner.screen.buttons.ToolPartButton;
import net.tiffit.tconplanner.util.Icon;
import net.tiffit.tconplanner.util.TranslationUtil;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;

import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToolTopPanel extends PlannerPanel {

    public ToolTopPanel(int x, int y, int width, int height, ItemStack result, ToolStack tool, PlannerData data, PlannerScreen parent) {
        super(x, y, width, height, parent);
        Blueprint blueprint = parent.blueprint;
        List<TCSlotPos> positions = blueprint.tool.getSlotPos();

        for (int i = 0; i < blueprint.parts.length; i++) {
            TCSlotPos pos = positions.get(i);
            IToolPart part = blueprint.parts[i];
            addChild(new ToolPartButton(
                    i, pos.getX(), pos.getY(),
                    part, blueprint.materials[i],
                    parent
            ));
        }

        addChild(new IconButton(
                parent.guiWidth - 70, 88,
                new Icon(3, 0),
                TranslationUtil.createComponent("randomize"),
                parent, e -> parent.randomize()
        ).withSound((Holder.Reference<SoundEvent>) getSoundHolder(SoundEvents.ITEM_PICKUP)));


        if (tool != null) {
            addChild(new OutputToolWidget(
                    parent.guiWidth - 34, 58,
                    result, parent
            ));

            boolean bookmarked = data.isBookmarked(blueprint);
            boolean starred = blueprint.equals(data.starred);

            addChild(new IconButton(
                    parent.guiWidth - 33, 88,
                    new Icon(bookmarked ? 2 : 1, 0),
                    TranslationUtil.createComponent(bookmarked ? "bookmark.remove" : "bookmark.add"),
                    parent, e -> {
                if (bookmarked) parent.unbookmarkCurrent();
                else parent.bookmarkCurrent();
            }
            ).withSound((Holder.Reference<SoundEvent>) getSoundHolder(SoundEvents.UI_STONECUTTER_TAKE_RESULT)));


            if (bookmarked) {
                addChild(new IconButton(
                        parent.guiWidth - 18, 88,
                        new Icon(starred ? 7 : 6, 0),
                        TranslationUtil.createComponent(starred ? "star.remove" : "star.add"),
                        parent, e -> {
                    if (starred) parent.unstarCurrent();
                    else parent.starCurrent();
                }
                ).withSound((Holder.Reference<SoundEvent>) getSoundHolder(SoundEvents.BOOK_PAGE_TURN)));

            }

            if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
                addChild(new IconButton(
                        parent.guiWidth - 48, 88,
                        new Icon(4, 0),
                        TranslationUtil.createComponent("giveitem"),
                        parent, e -> parent.giveItemstack(result)
                ).withSound((Holder.Reference<SoundEvent>) getSoundHolder(SoundEvents.ITEM_PICKUP)));


            }
        }
    }

    private Holder<SoundEvent> getSoundHolder(SoundEvent sound) {
        if (Minecraft.getInstance().level != null) {
            return Minecraft.getInstance().level.registryAccess()
                    .registry(Registries.SOUND_EVENT)
                    .orElseThrow()
                    .getHolderOrThrow(ResourceKey.create(Registries.SOUND_EVENT, Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.getKey(sound))));
        }
        return null;
    }


    @Override
    public void renderWidget( @NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;

        PoseStack stack = guiGraphics.pose();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();


        stack.pushPose();
        stack.translate(this.getX() + TCSlotPos.partsOffsetX + 7, this.getY() + TCSlotPos.partsOffsetY + 22, -200);
        stack.scale(3.7F, 3.7F, 1.0F);
        guiGraphics.renderItem(new ItemStack(parent.blueprint.toolStack.getItem()), 0, 0);
        stack.popPose();


        PlannerScreen.bindTexture();
        int boxX = 13, boxY = 24, boxL = 81;
        boolean isHovered = mouseX > boxX + getX() && mouseY > boxY + getY() && mouseX < boxX + getX() + boxL && mouseY < boxY + getY() + boxL;


        RenderSystem.setShaderColor(1f, 1f, 1f, isHovered ? 0.75f : 0.5f);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableDepthTest();


        if (PlannerScreen.TEXTURE != null) {
            guiGraphics.blit(PlannerScreen.TEXTURE, getX() + boxX, getY() + boxY, boxX, boxY, boxL, boxL);
        }


        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableDepthTest();


        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }
}