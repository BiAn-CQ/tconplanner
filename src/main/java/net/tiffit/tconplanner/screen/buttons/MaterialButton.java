package net.tiffit.tconplanner.screen.buttons;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;

public class MaterialButton extends Button {

    public final IMaterial material;
    public final ItemStack stack;
    private final PlannerScreen parent;
    public boolean selected = false;
    public Component errorText;


    public MaterialButton(int index, IMaterial material, ItemStack stack, int x, int y, PlannerScreen parent) {
        super(x, y, 16, 16, stack.getHoverName(),
                button -> parent.setPart(material),
                DEFAULT_NARRATION);
        this.material = material;
        this.stack = stack;
        this.parent = parent;

        Blueprint baseBlueprint = parent.getCurrentBlueprint();
        Blueprint cloned = baseBlueprint.clone();


        IMaterial[] newMaterials = new IMaterial[cloned.materials.length];
        for (int i = 0; i < newMaterials.length; i++) {
            newMaterials[i] = material;
        }
        cloned.materials = newMaterials;

        ToolStack toolStack = ToolStack.from(cloned.createOutput());
        Component validationError = toolStack.tryValidate();
        if (validationError != null) {
            this.errorText = validationError.copy().withStyle(style ->
                    style.withColor(TextColor.fromRgb(0xFFCC0000))
            );
        }
    }


    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) return;


        this.isHovered = mouseX >= getX() && mouseY >= getY()
                && mouseX < getX() + width && mouseY < getY() + height;


        guiGraphics.renderItem(stack, getX(), getY());
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, getX(), getY());


        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        int x = getX();
        int y = getY();
        int right = x + width;
        int bottom = y + height;


        if (selected) {
            guiGraphics.fill(x, y, right, bottom, 0x5500FF00);
        }

        if (errorText != null) {
            guiGraphics.fill(x, y, right, bottom, 0x55FF0000);
        }

        if (isHovered) {
            guiGraphics.fill(x, y, right, bottom, 0x80FFEA00);
        }
        poseStack.popPose();


        if (isHovered) {
            parent.postRenderTasks.add(() -> renderTooltip(guiGraphics, mouseX, mouseY));
        }
    }


    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (errorText != null) {
            List<Component> singleTooltip = List.of(errorText);
            guiGraphics.renderComponentTooltip(
                    mc.font,
                    singleTooltip,
                    mouseX,
                    mouseY
            );
        } else {
            List<Component> tooltip = new ArrayList<>();


            tooltip.addAll(stack.getTooltipLines(mc.player, TooltipFlag.Default.NORMAL));


            if (!net.minecraft.client.gui.screens.Screen.hasControlDown()) {

                Style ctrlStyle = Style.EMPTY
                        .withColor(TextColor.fromRgb(0x00FFFF))
                        .withItalic(true);

                Component ctrlHint = TConstruct.makeTranslation("key", "ctrl")
                        .copy()
                        .withStyle(ctrlStyle);

                tooltip.add(TranslationUtil.createComponent(
                        "parts.modifier_descriptions",
                        ctrlHint
                ));
            } else if (stack.getItem() instanceof slimeknights.tconstruct.library.tools.part.ToolPartItem partItem) {

                Component partName = partItem.getName(stack).copy()
                        .withStyle(style -> style.withUnderlined(true));
                tooltip.add(partName);


                List<ModifierEntry> entries = MaterialRegistry.getInstance()
                        .getTraits(material.getIdentifier(), partItem.getStatType());
                for (ModifierEntry entry : entries) {
                    Modifier modifier = entry.getModifier();

                    Component modifierName = modifier.getDisplayName(entry.getLevel())
                            .copy()
                            .withStyle(partName.getStyle());
                    tooltip.add(modifierName);


                    TextColor color = TextColor.fromRgb(modifier.getColor());
                    Style descStyle = Style.EMPTY.withColor(color);
                    for (Component comp : modifier.getDescriptionList(entry.getLevel())) {
                        tooltip.add(comp.copy().withStyle(descStyle));
                    }
                }
            }


            guiGraphics.renderComponentTooltip(
                    mc.font,
                    tooltip,
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public void onPress() {
        if (errorText == null) {
            parent.setPart(material);
        }
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        if (errorText != null) {

            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_HIT, 1.0F, 1.0F));
        } else {
            super.playDownSound(soundManager);
        }
    }
}