package net.tiffit.tconplanner;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.PlannerData;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.screen.buttons.BookmarkedButton;
import net.tiffit.tconplanner.screen.ext.ExtIconButton;
import net.tiffit.tconplanner.screen.ext.ExtItemStackButton;
import net.tiffit.tconplanner.util.Icon;
import net.tiffit.tconplanner.util.TranslationUtil;

import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.tools.layout.LayoutSlot;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayout;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.tables.block.ScorchedAnvilBlock;
import slimeknights.tconstruct.tables.block.TinkersAnvilBlock;
import slimeknights.tconstruct.tables.client.inventory.TinkerStationScreen;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.ScreenEvent.Init;
import net.minecraftforge.client.event.ScreenEvent.Render;
import slimeknights.tconstruct.tables.client.inventory.widget.SlotButtonItem;
import slimeknights.tconstruct.tables.client.inventory.widget.TinkerStationButtonsWidget;


@Mod.EventBusSubscriber(Dist.CLIENT)
public class EventListener {
    private static final Icon plannerIcon = new Icon(0, 0);
    private static final Icon importIcon = new Icon(8, 0);

    public static final Queue<Runnable> postRenderQueue = new LinkedBlockingQueue<>();

    private static StationSlotLayout layout = null;
    private static boolean starredLayout = false;
    private static final Field currentLayoutField;
    private static SlotButtonItem starredButton = null;
    private static boolean forceNextUpdate = false;
    private static TinkerStationButtonsWidget buttonScreen;
    private static final Field buttonsScreenField;

    static {
        try {

            currentLayoutField = TinkerStationScreen.class.getDeclaredField("currentLayout");
            currentLayoutField.setAccessible(true);

            buttonsScreenField = TinkerStationScreen.class.getDeclaredField("buttonsScreen");
            buttonsScreenField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void onScreenInit(Init.Post e) {

        if (e.getScreen() instanceof TinkerStationScreen) {

            TinkerStationScreen screen = (TinkerStationScreen) e.getScreen();
            Minecraft mc = screen.getMinecraft();
            PlannerData data = TConPlanner.DATA;
            try {
                data.firstLoad();
                buttonScreen = (TinkerStationButtonsWidget) buttonsScreenField.get(screen);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            updateLayout(screen, true);
            forceNextUpdate = true;


            int x = screen.cornerX + Config.CONFIG.buttonX.get();
            int y = screen.cornerY + Config.CONFIG.buttonY.get();
            e.addListener(new ExtIconButton(
                    x, y,
                    plannerIcon,
                    TranslationUtil.createComponent("plannerbutton"),
                    action -> mc.setScreen(new PlannerScreen(screen)),
                    screen
            ));


            BlockEntity tile = screen.getTileEntity();
            if (tile == null) return;
            Block stationBlock = tile.getBlockState().getBlock();
            boolean isAnvil = stationBlock instanceof ScorchedAnvilBlock || stationBlock instanceof TinkersAnvilBlock;

            int importX = screen.cornerX + (isAnvil ? Config.CONFIG.importButtonXAnvil : Config.CONFIG.importButtonXStation).get();
            int importY = screen.cornerY + (isAnvil ? Config.CONFIG.importButtonYAnvil : Config.CONFIG.importButtonYStation).get();

            e.addListener(new ExtIconButton(
                    importX, importY,
                    importIcon,
                    TranslationUtil.createComponent("importtool"),
                    action -> {
                        Slot slot = screen.getMenu().getSlot(0);
                        if (!slot.getItem().isEmpty()) {
                            mc.setScreen(new PlannerScreen(screen, ToolStack.from(slot.getItem())));
                        }
                    },
                    screen
            ).withEnabledFunc(() -> {
                if (!layout.isMain()) return false;
                Slot slot = screen.getMenu().getSlot(0);
                return !slot.getItem().isEmpty() && ToolStack.isInitialized(slot.getItem());
            }));


            if (data.starred != null) {
                List<Component> tooltip = new ArrayList<>();

                MutableComponent line = Component.literal("---------");
                tooltip.add(line.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA))));


                MutableComponent moveText = (MutableComponent) TranslationUtil.createComponent("star.move");
                tooltip.add(moveText.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF00))));

                MutableComponent removeText = (MutableComponent) TranslationUtil.createComponent("star.ext_remove");
                tooltip.add(removeText.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF0000))));

                e.addListener(new ExtItemStackButton(
                        screen.cornerX + 83, screen.cornerY + 58,
                        data.starred.createOutput(),
                        tooltip,
                        btn -> {
                            if (Screen.hasShiftDown()) {
                                btn.visible = false;
                                btn.active = false;
                                starredLayout = false;
                                data.starred = null;
                                try {
                                    data.save();
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                movePartsToSlots(screen, mc, data.starred);
                            }
                        },
                        screen
                ));
            }
        }
    }

    @SubscribeEvent
    public static void onScreenDraw(Render.Post e) {
        if (e.getScreen() instanceof TinkerStationScreen screen) {
            GuiGraphics guiGraphics = e.getGuiGraphics();
            PoseStack poseStack = guiGraphics.pose();

            if (starredLayout) {
                Blueprint starred = TConPlanner.DATA.starred;
                if (starred == null) return;

                ItemStack carried = screen.getMenu().getCarried();
                for (int i = 0; i < layout.getInputSlots().size(); i++) {
                    LayoutSlot slotCoords = layout.getInputSlots().get(i);
                    int slotX = slotCoords.getX() + screen.cornerX;
                    int slotY = slotCoords.getY() + screen.cornerY;


                    IToolPart part = starred.parts[i];

                    boolean isHovered = e.getMouseX() > slotX && e.getMouseY() > slotY &&
                            e.getMouseX() < slotX + 16 && e.getMouseY() < slotY + 16;

                    Slot tconSlot = screen.getMenu().getSlot(i + 1);
                    ItemStack stack = tconSlot.getItem();
                    MaterialId material = starred.materials[i].getIdentifier();


                    if (stack.isEmpty()) {
                        poseStack.pushPose();
                        poseStack.translate(0, 0, 101);
                        int color = carried.isEmpty()
                                ? 0x5A000050
                                : isValidToolPart(carried, part, material)
                                ? 0x5AE8B641
                                : 0x5AFF0000;
                        guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, color);
                        poseStack.popPose();


                        if (isHovered) {
                            List<Component> tooltips = new ArrayList<>();
                            MutableComponent missingText = (MutableComponent) TranslationUtil.createComponent("star.slot.missing");
                            tooltips.add(missingText.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAA0000))));
                            tooltips.add(part.withMaterialForDisplay(material).getDisplayName());
                            guiGraphics.renderTooltip(
                                    screen.getMinecraft().font,
                                    (Component) tooltips,
                                    e.getMouseX(),
                                    e.getMouseY()
                            );
                        }
                    } else if (!material.equals(part.getMaterial(stack).getId())) {
                        poseStack.pushPose();
                        poseStack.translate(0, 0, 101);
                        guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x7AFF0000);
                        poseStack.popPose();


                        if (isHovered) {
                            List<Component> tooltips = new ArrayList<>();
                            MutableComponent incorrectText = (MutableComponent) TranslationUtil.createComponent("star.slot.incorrect");
                            tooltips.add(incorrectText.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAA0000))));
                            tooltips.add(part.withMaterialForDisplay(material).getDisplayName());
                            guiGraphics.renderTooltip(
                                    screen.getMinecraft().font,
                                    (Component) tooltips,
                                    e.getMouseX(),
                                    e.getMouseY() - 30
                            );
                        }
                    }
                }
            }


            if (starredButton != null) {
                poseStack.pushPose();
                poseStack.translate(starredButton.getX() + 10, starredButton.getY() + 10, 105);
                poseStack.scale(0.5f, 0.5f, 1);

                BookmarkedButton.STAR_ICON.render(guiGraphics, 0, 0);
                poseStack.popPose();
            }


            while (!postRenderQueue.isEmpty()) {
                postRenderQueue.poll().run();
            }
        }
    }

    @SubscribeEvent
    public static void onScreenDrawPre(ScreenEvent.Render.Pre e) {
        if (e.getScreen() instanceof TinkerStationScreen screen) {
            postRenderQueue.clear();
            updateLayout(screen, forceNextUpdate);
        }
    }

    private static void updateLayout(TinkerStationScreen screen, boolean force) {
        try {
            StationSlotLayout newLayout = (StationSlotLayout) currentLayoutField.get(screen);
            if (!force && newLayout == layout) return;

            forceNextUpdate = false;
            layout = newLayout;
            PlannerData data = TConPlanner.DATA;
            boolean foundButton = false;

            if (data.starred != null) {
                StationSlotLayout starredSlotLayout = data.starred.tool.getLayout();
                starredLayout = layout == starredSlotLayout;

                for (Object obj : buttonScreen.getButtons()) {
                    if (obj instanceof SlotButtonItem button) {
                        if (starredSlotLayout == button.getLayout()) {
                            starredButton = button;
                            foundButton = true;
                            break;
                        }
                    }
                }

                if (!foundButton) {
                    starredLayout = false;
                    starredButton = null;
                }

            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void movePartsToSlots(TinkerStationScreen screen, Minecraft mc, Blueprint starred) {

        if (starred.tool.getLayout() != layout) {
            screen.onToolSelection(starred.tool.getLayout());
            updateLayout(screen, true);
        }

        Player player = mc.player;
        MultiPlayerGameMode gameMode = mc.gameMode;
        if (player == null || gameMode == null) return;

        AbstractContainerMenu container = screen.getMenu();
        for (int i = 0; i < layout.getInputSlots().size(); i++) {
            MaterialId material = starred.materials[i].getIdentifier();
            Slot tconSlot = container.getSlot(i + 1);


            if (tconSlot.getItem().isEmpty()) {
                for (int j = 0; j < container.slots.size(); j++) {
                    Slot invSlot = container.slots.get(j);
                    if (!(invSlot.container instanceof Inventory)) continue;

                    ItemStack stack = invSlot.getItem();
                    if (isValidToolPart(stack, starred.parts[i], material)) {

                        gameMode.handleInventoryMouseClick(
                                container.containerId,
                                j,
                                0,
                                ClickType.PICKUP,
                                player
                        );
                        gameMode.handleInventoryMouseClick(
                                container.containerId,
                                i + 1,
                                1,
                                ClickType.PICKUP,
                                player
                        );
                        break;
                    }
                }
            }
        }
    }

    private static boolean isValidToolPart(ItemStack stack, IToolPart part, MaterialId material) {
        if (stack.getItem() instanceof ToolPartItem toolPartItem) {
            return part.asItem() == toolPartItem && material.equals(toolPartItem.getMaterial(stack).getId());
        }
        return false;
    }
}