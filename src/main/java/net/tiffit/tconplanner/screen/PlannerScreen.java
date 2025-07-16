package net.tiffit.tconplanner.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.tiffit.tconplanner.TConPlanner;
import net.tiffit.tconplanner.api.TCTool;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.data.PlannerData;
import net.tiffit.tconplanner.util.MaterialSort;
import net.tiffit.tconplanner.util.ModifierStack;
import net.tiffit.tconplanner.util.TranslationUtil;
import org.jetbrains.annotations.NotNull;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.tconstruct.library.json.IntRange;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tables.client.inventory.TinkerStationScreen;

import java.io.IOException;
import java.util.*;


import net.minecraft.client.gui.GuiGraphics;

public class PlannerScreen extends Screen {

    public static final ResourceLocation TEXTURE = ResourceLocation.bySeparator(TConPlanner.MODID + ":textures/gui/planner.png", ':'); // 替换弃用构造方法
    private final Map<String, Object> cache = new HashMap<>();
    public Deque<Runnable> postRenderTasks = new ArrayDeque<>();
    private final TinkerStationScreen child;
    private final List<TCTool> tools = TCTool.getTools();
    private final List<IDisplayModifierRecipe> modifiers;
    private final PlannerData data;

    public Blueprint blueprint;

    public int selectedPart = 0;
    public int materialPage = 0;
    public MaterialSort<?> sorter;

    public ModifierInfo selectedModifier;

    public int selectedModifierStackIndex = -1;
    public ModifierStack modifierStack;

    public int left, top, guiWidth, guiHeight;
    private Component titleText;

    public Blueprint getCurrentBlueprint() {
        return blueprint;
    }


    public PlannerScreen(TinkerStationScreen child) {
        super(TranslationUtil.createComponent("name"));
        this.child = child;
        this.data = TConPlanner.DATA;
        try {
            data.load();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.modifiers = getModifierRecipes();
    }

    public PlannerScreen(TinkerStationScreen child, ToolStack stack) {
        this(child);
        Optional<TCTool> optionalTCTool = tools.stream()
                .filter(tool -> tool.getModifiable().getToolDefinition().getId().equals(stack.getDefinition().getId()))
                .findAny();
        if (optionalTCTool.isPresent()) {
            blueprint = new Blueprint(optionalTCTool.get());
            for (int i = 0; i < blueprint.materials.length; i++) {

                blueprint.materials[i] = (IMaterial) stack.getMaterial(i);
            }
            selectedPart = -1;
        }
    }


    @Override
    protected void init() {
        super.init();
        guiWidth = 175;
        guiHeight = 204;
        left = (width - guiWidth) / 2;
        top = (height - guiHeight) / 2;
        refresh();
    }

    public void refresh() {
        clearWidgets();

        int toolSpace = 20;
        titleText = blueprint == null ? TranslationUtil.createComponent("notool") : blueprint.tool.getName();


        addRenderableWidget(new ToolSelectPanel(
                left - toolSpace * 5 - 4,
                top,
                toolSpace * 5,
                toolSpace * 3 + 23 + 4,
                tools,
                this
        ));


        if (!data.saved.isEmpty()) {
            addRenderableWidget(new BookmarkSelectPanel(
                    left - toolSpace * 5 - 4,
                    top + 15 + 18 * 4,
                    toolSpace * 5,
                    toolSpace * 5 + 23 + 4,
                    data,
                    this
            ));
        }


        if (blueprint != null) {
            int topPanelSize = 115;
            ItemStack result = blueprint.createOutput();
            ToolStack resultStack = !result.isEmpty() ? ToolStack.from(result) : null;


            addRenderableWidget(new ToolTopPanel(
                    left, top, guiWidth, topPanelSize,
                    result, resultStack, data, this
            ));


            if (selectedPart != -1) {
                addRenderableWidget(new MaterialSelectPanel(
                        left, top + topPanelSize,
                        guiWidth, guiHeight - topPanelSize,
                        this
                ));
            }


            if (resultStack != null) {
                addRenderableWidget(new ModifierPanel(
                        left + guiWidth, top,
                        115, guiHeight,
                        result, resultStack, modifiers, this
                ));
            }
        }
    }


    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        renderBackground(guiGraphics);


        bindTexture();
        guiGraphics.blit(TEXTURE, left, top, 0, 0, guiWidth, guiHeight);


        guiGraphics.drawCenteredString(
                font, titleText,
                left + guiWidth / 2, top + 7,
                0xFFFFFFFF
        );


        super.render(guiGraphics, mouseX, mouseY, partialTick);


        Runnable task;
        while ((task = postRenderTasks.poll()) != null) {
            task.run();
        }
    }


    public static void bindTexture() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }


    public void setSelectedTool(int index) {
        setBlueprint(new Blueprint(tools.get(index)));
    }


    public void setBlueprint(Blueprint bp) {
        blueprint = bp;
        this.materialPage = 0;
        sorter = null;
        selectedModifier = null;
        modifierStack = null;
        selectedModifierStackIndex = -1;
        setSelectedPart(-1);
    }


    public void setSelectedPart(int index) {
        this.selectedPart = index;
        this.materialPage = 0;
        sorter = null;
        refresh();
    }


    public void setPart(IMaterial material) {
        blueprint.materials[selectedPart] = material;
        selectedModifier = null;
        selectedModifierStackIndex = -1;
        modifierStack = null;
        refresh();
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        if (minecraft != null && minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            this.onClose();
            return true;
        }


        if (keyCode == InputConstants.KEY_B && blueprint != null && blueprint.isComplete()) {
            if (data.isBookmarked(blueprint)) {
                unbookmarkCurrent();
            } else {
                bookmarkCurrent();
            }
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    public void renderItemTooltip(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        guiGraphics.renderTooltip(font, stack, x, y);
    }


    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(child);
        }
    }


    public void bookmarkCurrent() {
        if (blueprint.isComplete()) {
            data.saved.add(blueprint);
            try {
                data.refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        refresh();
    }


    public void starCurrent() {
        if (blueprint.isComplete()) {
            data.starred = blueprint;
            try {
                data.refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        refresh();
    }

    public void unbookmarkCurrent() {
        if (blueprint.isComplete()) {
            data.saved.removeIf(blueprint1 -> blueprint1.equals(blueprint));
            if (blueprint.equals(data.starred)) {
                data.starred = null;
            }
            try {
                data.refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        refresh();
    }


    public void unstarCurrent() {
        if (blueprint.isComplete()) {
            data.starred = null;
            try {
                data.refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        refresh();
    }


    public void randomize() {
        if (blueprint != null) {
            setBlueprint(new Blueprint(blueprint.tool));
            Random r = new Random();
            List<IMaterial> materials = new ArrayList<>(MaterialRegistry.getMaterials());
            for (int i = 0; i < blueprint.parts.length; i++) {
                IToolPart part = blueprint.parts[i];
                List<IMaterial> usable = materials.stream()
                        .filter(part::canUseMaterial)
                        .toList();
                if (!usable.isEmpty()) {
                    blueprint.materials[i] = usable.get(r.nextInt(usable.size()));
                }
            }
            selectedModifier = null;
            refresh();
        }
    }


    public void giveItemstack(ItemStack stack) {
        if (minecraft == null || minecraft.player == null) return;

        Inventory inventory = minecraft.player.getInventory();
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack current = inventory.items.get(i);
            if (current.isEmpty()) {
                int slot = i < 9 ? i + 36 : i;
                minecraft.gameMode.handleCreativeModeItemAdd(stack, slot);
                return;
            }
        }
    }


    public void sort(MaterialSort<?> sort) {
        if (sorter == sort) {
            sorter = null;
        } else {
            sorter = sort;
        }
        refresh();
    }


    public <T> T getCacheValue(String key, T defaultVal) {
        //noinspection unchecked
        return (T) cache.getOrDefault(key, defaultVal);
    }


    public void setCacheValue(String key, Object value) {
        cache.put(key, value);
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }


    public static List<IDisplayModifierRecipe> getModifierRecipes() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return new ArrayList<>();

        RecipeManager recipeManager = mc.level.getRecipeManager();

        net.minecraft.core.RegistryAccess registryAccess = mc.level.registryAccess();

        java.util.stream.Stream<? extends net.minecraft.world.item.crafting.Recipe<?>> recipeStream =
                recipeManager.getAllRecipesFor(TinkerRecipeTypes.TINKER_STATION.get())
                        .stream();

        List<IDisplayModifierRecipe> jeiRecipes = RecipeHelper.getJEIRecipes(
                registryAccess, recipeStream, IDisplayModifierRecipe.class
        );
        List<IDisplayModifierRecipe> cleanedList = new ArrayList<>();

        for (IDisplayModifierRecipe recipe : jeiRecipes) {
            if (recipe instanceof ITinkerStationRecipe tinkerRecipe) {
                boolean contains = cleanedList.stream().anyMatch(recipe1 ->
                        recipe1.getDisplayResult().getModifier().equals(recipe.getDisplayResult().getModifier()) &&
                                Objects.equals(recipe1.getSlots(), recipe.getSlots()) &&
                                getMaxLevel(recipe1) == getMaxLevel(recipe)
                );
                if (!contains) {
                    cleanedList.add(recipe);
                }
            }
        }
        return cleanedList;
    }

    private static int getMaxLevel(IDisplayModifierRecipe recipe) {

        IntRange levelRange = recipe.getLevel();

        return levelRange.max();
    }
}