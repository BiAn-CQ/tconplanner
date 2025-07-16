package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.util.Mth;
import net.tiffit.tconplanner.Config;
import net.tiffit.tconplanner.screen.PlannerPanel;
import net.tiffit.tconplanner.screen.PlannerScreen;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PaginatedPanel<T extends AbstractWidget> extends PlannerPanel {

    private final List<T> allChildren = new ArrayList<>();
    private final String cachePrefix;
    private final int childWidth, childHeight, spacing, columns, rows, pageSize;
    private int totalRows;
    private int totalPages;
    private float scrollPageHeight;

    public PaginatedPanel(int x, int y, int childWidth, int childHeight, int columns, int rows, int spacing, String cachePrefix, PlannerScreen parent) {
        super(x, y,
                (childWidth + spacing) * columns - spacing + 4,
                (childHeight + spacing) * rows - spacing,
                parent);
        this.childWidth = childWidth;
        this.childHeight = childHeight;
        this.spacing = spacing;
        this.columns = columns;
        this.rows = rows;
        this.pageSize = columns * rows;
        this.cachePrefix = cachePrefix;
    }


    public void addChild(AbstractWidget widget) {
        allChildren.add((T) widget);
    }

    public void sort(Comparator<T> comparator) {
        allChildren.sort(comparator);
    }

    public void refresh() {
        refresh(parent.getCacheValue(cachePrefix + ".page", 0));
    }

    public void refresh(int page) {
        totalRows = (int) Math.ceil(allChildren.size() / (double) columns);
        totalPages = Math.max(1, allChildren.size() > pageSize ? totalRows - rows + 1 : 1);


        page = Mth.clamp(page, 0, totalPages - 1);
        parent.setCacheValue(cachePrefix + ".page", page);


        children.clear();
        int start = page * columns;
        int end = Math.min(allChildren.size(), start + pageSize);
        children.addAll(allChildren.subList(start, end));

        scrollPageHeight = height / (float) Math.max(totalPages, 1);


        for (int i = 0; i < children.size(); i++) {
            AbstractWidget widget = children.get(i);
            int col = i % columns;
            int row = i / columns;
            widget.setX(getX() + col * (childWidth + spacing));
            widget.setY(getY() + row * (childHeight + spacing));
        }
    }

    private void setPage(int page) {
        parent.setCacheValue(cachePrefix + ".page", page);
        refresh(page);
    }

    public void makeVisible(int index, boolean refresh) {
        if (index < 0 || index >= allChildren.size()) return;

        int row = index / columns;
        int currentPage = parent.getCacheValue(cachePrefix + ".page", 0);
        int targetPage = Math.max(0, row - rows + 1);

        if (currentPage != targetPage) {
            parent.setCacheValue(cachePrefix + ".page", targetPage);
            if (refresh) this.refresh(targetPage);
        }
    }


    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick); // 调用父类渲染


        if (totalPages > 1) {
            int scrollX = getX() + width - 3;
            int currentPage = parent.getCacheValue(cachePrefix + ".page", 0);


            guiGraphics.fill(
                    scrollX, getY(),
                    scrollX + 3, getY() + height,
                    0x0F_FFFFFF | (isHovered() ? 0x0A_000000 : 0)
            );


            int sliderYStart = getY() + (int) (scrollPageHeight * currentPage);
            int sliderYEnd = getY() + (int) (scrollPageHeight * (currentPage + rows));
            guiGraphics.fill(
                    scrollX, sliderYStart,
                    scrollX + 3, sliderYEnd,
                    0x0F_FFFFFF | (isHovered() ? 0x0F_000000 : 0)
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (totalPages > 1) {
            int scrollX = getX() + width - 3;

            if (mouseX >= scrollX && mouseX <= scrollX + 3
                    && mouseY >= getY() && mouseY <= getY() + height) {

                float relativeY = (float) (mouseY - getY()) / height;
                int clickedPage = (int) Math.min(relativeY * totalPages, totalPages - 1);
                setPage(clickedPage);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        boolean result = false;
        int currentPage = parent.getCacheValue(cachePrefix + ".page", 0);


        double scrollAmount = scroll * Config.CONFIG.scrollDirection.get().mult;
        if (scrollAmount > 0 && currentPage < totalPages - 1) {
            setPage(currentPage + 1);
            result = true;
        } else if (scrollAmount < 0 && currentPage > 0) {
            setPage(currentPage - 1);
            result = true;
        }


        return result || super.mouseScrolled(mouseX, mouseY, scroll);
    }


    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {

    }
}
