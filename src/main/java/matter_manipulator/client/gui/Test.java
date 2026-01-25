package matter_manipulator.client.gui;

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.IParentWidget;
import com.cleanroommc.modularui.api.widget.IPositioned;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widget.sizer.Flex;
import com.cleanroommc.modularui.widget.sizer.Unit;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Row;

@SuppressWarnings("unchecked")
public class Test {

    public static ModularPanel init() {
        return panel(
            "foo",
            row(
                coverChildren(),
                text(
                    IKey.str("foo"),
                    alignment(Alignment.BottomCenter),
                    size(50, 50)
                )
            )
        );
    }

    public static ModularPanel panel(String name, Property<? super ModularPanel>... props) {
        ModularPanel panel = new ModularPanel(name);

        for (var p : props) p.modify(panel);

        return panel;
    }

    public static Property<IParentWidget<? super Row, ?>> row(Property<? super Row>... props) {
        return parent -> {
            Row row = new Row();

            for (var p : props) {
                p.modify(row);
            }

            parent.child(row);
        };
    }

    public static Property<IParentWidget<? super TextWidget<?>, ?>> text(IKey key, Property<? super TextWidget<?>>... props) {
        return parent -> {
            TextWidget<?> text = new TextWidget<>(key);

            for (var p : props) {
                p.modify(text);
            }

            parent.child(text);
        };
    }

    public static Property<TextWidget<?>> alignment(Alignment a) {
        return w -> w.alignment(a);
    }

    public static Property<? super IPositioned<?>> coverChildrenWidth() {
        return IPositioned::coverChildrenWidth;
    }

    public static Property<? super IPositioned<?>> coverChildrenHeight() {
        return IPositioned::coverChildrenHeight;
    }

    public static Property<? super IPositioned<?>> coverChildren() {
        return IPositioned::coverChildren;
    }

    public static Property<? super IPositioned<?>> expanded() {
        return IPositioned::expanded;
    }

    public static Property<? super IPositioned<?>> relativeToScreen() {
        return IPositioned::relativeToScreen;
    }

    public static Property<? super IPositioned<?>> relativeToParent() {
        return IPositioned::relativeToParent;
    }

    public static Property<? super IPositioned<?>> bypassLayerRestriction() {
        return IPositioned::bypassLayerRestriction;
    }

    public static Property<? super IPositioned<?>> relative(IGuiElement guiElement) {
        return w -> w.relative(guiElement);
    }
    public static Property<? super IPositioned<?>> relative(Area guiElement) {
        return w -> w.relative(guiElement);
    }
    public static Property<? super IPositioned<?>> left(int val) {
        return w -> w.left(val);
    }
    public static Property<? super IPositioned<?>> leftRel(float val) {
        return w -> w.leftRel(val);
    }
    public static Property<? super IPositioned<?>> leftRelOffset(float val, int offset) {
        return w -> w.leftRelOffset(val, offset);
    }
    public static Property<? super IPositioned<?>> leftRelAnchor(float val, float anchor) {
        return w -> w.leftRelAnchor(val, anchor);
    }
    public static Property<? super IPositioned<?>> leftRel(float val, int offset, float anchor) {
        return w -> w.leftRel(val, offset, anchor);
    }
    public static Property<? super IPositioned<?>> left(float val, int offset, float anchor, Unit.Measure measure) {
        return w -> w.left(val, offset, anchor, measure);
    }
    public static Property<? super IPositioned<?>> left(DoubleSupplier val, com.cleanroommc.modularui.widget.sizer.Unit.Measure measure) {
        return w -> w.left(val, measure);
    }
    public static Property<? super IPositioned<?>> leftRelOffset(DoubleSupplier val, int offset) {
        return w -> w.leftRelOffset(val, offset);
    }
    public static Property<? super IPositioned<?>> leftRelAnchor(DoubleSupplier val, float anchor) {
        return w -> w.leftRelAnchor(val, anchor);
    }
    public static Property<? super IPositioned<?>> leftRel(DoubleSupplier val, int offset, float anchor) {
        return w -> w.leftRel(val, offset, anchor);
    }
    public static Property<? super IPositioned<?>> right(int val) {
        return w -> w.right(val);
    }
    public static Property<? super IPositioned<?>> rightRel(float val) {
        return w -> w.rightRel(val);
    }
    public static Property<? super IPositioned<?>> rightRelOffset(float val, int offset) {
        return w -> w.rightRelOffset(val, offset);
    }
    public static Property<? super IPositioned<?>> rightRelAnchor(float val, float anchor) {
        return w -> w.rightRelAnchor(val, anchor);
    }
    public static Property<? super IPositioned<?>> rightRel(float val, int offset, float anchor) {
        return w -> w.rightRel(val, offset, anchor);
    }
    public static Property<? super IPositioned<?>> right(float val, int offset, float anchor, Unit.Measure measure) {
        return w -> w.right(val, offset, anchor, measure);
    }
    public static Property<? super IPositioned<?>> right(DoubleSupplier val, com.cleanroommc.modularui.widget.sizer.Unit.Measure measure) {
        return w -> w.right(val, measure);
    }
    public static Property<? super IPositioned<?>> rightRelOffset(DoubleSupplier val, int offset) {
        return w -> w.rightRelOffset(val, offset);
    }
    public static Property<? super IPositioned<?>> rightRelAnchor(DoubleSupplier val, float anchor) {
        return w -> w.rightRelAnchor(val, anchor);
    }
    public static Property<? super IPositioned<?>> rightRel(DoubleSupplier val, int offset, float anchor) {
        return w -> w.rightRel(val, offset, anchor);
    }
    public static Property<? super IPositioned<?>> top(int val) {
        return w -> w.top(val);
    }
    public static Property<? super IPositioned<?>> topRel(float val) {
        return w -> w.topRel(val);
    }
    public static Property<? super IPositioned<?>> topRelOffset(float val, int offset) {
        return w -> w.topRelOffset(val, offset);
    }
    public static Property<? super IPositioned<?>> topRelAnchor(float val, float anchor) {
        return w -> w.topRelAnchor(val, anchor);
    }
    public static Property<? super IPositioned<?>> topRel(float val, int offset, float anchor) {
        return w -> w.topRel(val, offset, anchor);
    }
    public static Property<? super IPositioned<?>> top(float val, int offset, float anchor, Unit.Measure measure) {
        return w -> w.top(val, offset, anchor, measure);
    }
    public static Property<? super IPositioned<?>> top(DoubleSupplier val, com.cleanroommc.modularui.widget.sizer.Unit.Measure measure) {
        return w -> w.top(val, measure);
    }
    public static Property<? super IPositioned<?>> topRelOffset(DoubleSupplier val, int offset) {
        return w -> w.topRelOffset(val, offset);
    }
    public static Property<? super IPositioned<?>> topRelAnchor(DoubleSupplier val, float anchor) {
        return w -> w.topRelAnchor(val, anchor);
    }
    public static Property<? super IPositioned<?>> topRel(DoubleSupplier val, int offset, float anchor) {
        return w -> w.topRel(val, offset, anchor);
    }
    public static Property<? super IPositioned<?>> bottom(int val) {
        return w -> w.bottom(val);
    }
    public static Property<? super IPositioned<?>> bottomRel(float val) {
        return w -> w.bottomRel(val);
    }
    public static Property<? super IPositioned<?>> bottomRelOffset(float val, int offset) {
        return w -> w.bottomRelOffset(val, offset);
    }
    public static Property<? super IPositioned<?>> bottomRelAnchor(float val, float anchor) {
        return w -> w.bottomRelAnchor(val, anchor);
    }
    public static Property<? super IPositioned<?>> bottomRel(float val, int offset, float anchor) {
        return w -> w.bottomRel(val, offset, anchor);
    }
    public static Property<? super IPositioned<?>> bottom(float val, int offset, float anchor, Unit.Measure measure) {
        return w -> w.bottom(val, offset, anchor, measure);
    }
    public static Property<? super IPositioned<?>> bottom(DoubleSupplier val, com.cleanroommc.modularui.widget.sizer.Unit.Measure measure) {
        return w -> w.bottom(val, measure);
    }
    public static Property<? super IPositioned<?>> bottomRelOffset(DoubleSupplier val, int offset) {
        return w -> w.bottomRelOffset(val, offset);
    }
    public static Property<? super IPositioned<?>> bottomRelAnchor(DoubleSupplier val, float anchor) {
        return w -> w.bottomRelAnchor(val, anchor);
    }
    public static Property<? super IPositioned<?>> bottomRel(DoubleSupplier val, int offset, float anchor) {
        return w -> w.bottomRel(val, offset, anchor);
    }
    public static Property<? super IPositioned<?>> width(int val) {
        return w -> w.width(val);
    }
    public static Property<? super IPositioned<?>> widthRel(float val) {
        return w -> w.widthRel(val);
    }
    public static Property<? super IPositioned<?>> widthRelOffset(float val, int offset) {
        return w -> w.widthRelOffset(val, offset);
    }
    public static Property<? super IPositioned<?>> width(float val, Unit.Measure measure) {
        return w -> w.width(val, measure);
    }
    public static Property<? super IPositioned<?>> width(DoubleSupplier val, com.cleanroommc.modularui.widget.sizer.Unit.Measure measure) {
        return w -> w.width(val, measure);
    }
    public static Property<? super IPositioned<?>> widthRelOffset(DoubleSupplier val, int offset) {
        return w -> w.widthRelOffset(val, offset);
    }
    public static Property<? super IPositioned<?>> height(int val) {
        return w -> w.height(val);
    }
    public static Property<? super IPositioned<?>> heightRel(float val) {
        return w -> w.heightRel(val);
    }
    public static Property<? super IPositioned<?>> heightRelOffset(float val, int offset) {
        return w -> w.heightRelOffset(val, offset);
    }
    public static Property<? super IPositioned<?>> height(float val, Unit.Measure measure) {
        return w -> w.height(val, measure);
    }
    public static Property<? super IPositioned<?>> height(DoubleSupplier val, com.cleanroommc.modularui.widget.sizer.Unit.Measure measure) {
        return w -> w.height(val, measure);
    }
    public static Property<? super IPositioned<?>> heightRelOffset(DoubleSupplier val, int offset) {
        return w -> w.heightRelOffset(val, offset);
    }
    public static Property<? super IPositioned<?>> pos(int x, int y) {
        return w -> w.pos(x, y);
    }
    public static Property<? super IPositioned<?>> posRel(float x, float y) {
        return w -> w.posRel(x, y);
    }
    public static Property<? super IPositioned<?>> size(int w, int h) {
        return widget -> widget.size(w, h);
    }
    public static Property<? super IPositioned<?>> sizeRel(float w, float h) {
        return widget -> widget.sizeRel(w, h);
    }
    public static Property<? super IPositioned<?>> size(int val) {
        return w -> w.size(val);
    }
    public static Property<? super IPositioned<?>> sizeRel(float val) {
        return w -> w.sizeRel(val);
    }
    public static Property<? super IPositioned<?>> fullWidth() {
        return IPositioned::fullWidth;
    }
    public static Property<? super IPositioned<?>> fullHeight() {
        return IPositioned::fullHeight;
    }
    public static Property<? super IPositioned<?>> full() {
        return IPositioned::full;
    }
    public static Property<? super IPositioned<?>> anchorLeft(float val) {
        return w -> w.anchorLeft(val);
    }
    public static Property<? super IPositioned<?>> anchorRight(float val) {
        return w -> w.anchorRight(val);
    }
    public static Property<? super IPositioned<?>> anchorTop(float val) {
        return w -> w.anchorTop(val);
    }
    public static Property<? super IPositioned<?>> anchorBottom(float val) {
        return w -> w.anchorBottom(val);
    }
    public static Property<? super IPositioned<?>> anchor(Alignment alignment) {
        return w -> w.anchor(alignment);
    }
    public static Property<? super IPositioned<?>> alignX(float val) {
        return w -> w.alignX(val);
    }
    public static Property<? super IPositioned<?>> alignX(Alignment alignment) {
        return w -> w.alignX(alignment);
    }
    public static Property<? super IPositioned<?>> alignY(float val) {
        return w -> w.alignY(val);
    }
    public static Property<? super IPositioned<?>> alignY(Alignment alignment) {
        return w -> w.alignY(alignment);
    }
    public static Property<? super IPositioned<?>> align(Alignment alignment) {
        return w -> w.align(alignment);
    }
    public static Property<? super IPositioned<?>> horizontalCenter() {
        return IPositioned::horizontalCenter;
    }
    public static Property<? super IPositioned<?>> verticalCenter() {
        return IPositioned::verticalCenter;
    }
    public static Property<? super IPositioned<?>> center() {
        return IPositioned::center;
    }
    public static Property<? super IPositioned<?>> flex(Consumer<Flex> flexConsumer) {
        return w -> w.flex(flexConsumer);
    }
    public static Property<? super IPositioned<?>> padding(int left, int right, int top, int bottom) {
        return w -> w.padding(left, right, top, bottom);
    }
    public static Property<? super IPositioned<?>> padding(int horizontal, int vertical) {
        return w -> w.padding(horizontal, vertical);
    }
    public static Property<? super IPositioned<?>> padding(int all) {
        return w -> w.padding(all);
    }
    public static Property<? super IPositioned<?>> paddingLeft(int val) {
        return w -> w.paddingLeft(val);
    }
    public static Property<? super IPositioned<?>> paddingRight(int val) {
        return w -> w.paddingRight(val);
    }
    public static Property<? super IPositioned<?>> paddingTop(int val) {
        return w -> w.paddingTop(val);
    }
    public static Property<? super IPositioned<?>> paddingBottom(int val) {
        return w -> w.paddingBottom(val);
    }
    public static Property<? super IPositioned<?>> margin(int left, int right, int top, int bottom) {
        return w -> w.margin(left, right, top, bottom);
    }
    public static Property<? super IPositioned<?>> margin(int horizontal, int vertical) {
        return w -> w.margin(horizontal, vertical);
    }
    public static Property<? super IPositioned<?>> margin(int all) {
        return w -> w.margin(all);
    }
    public static Property<? super IPositioned<?>> marginLeft(int val) {
        return w -> w.marginLeft(val);
    }
    public static Property<? super IPositioned<?>> marginRight(int val) {
        return w -> w.marginRight(val);
    }
    public static Property<? super IPositioned<?>> marginTop(int val) {
        return w -> w.marginTop(val);
    }
    public static Property<? super IPositioned<?>> marginBottom(int val) {
        return w -> w.marginBottom(val);
    }

    public interface Property<T> {
        void modify(T widget);
    }

}
