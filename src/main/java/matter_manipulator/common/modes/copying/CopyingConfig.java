package matter_manipulator.common.modes.copying;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import lombok.EqualsAndHashCode;
import matter_manipulator.client.rendering.MMRenderUtils;
import matter_manipulator.common.utils.math.Location;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.common.utils.math.VoxelAABB;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.context.ManipulatorContext;

@EqualsAndHashCode
public class CopyingConfig {

    public PendingAction action;
    public Location copyA, copyB, paste;
    public Vector3i stack;
    @NotNull
    public Transform transform = new Transform();

    public Vector3i calculateStack(Vector3i lookingAt) {
        if (!Location.areCompatible(copyA, copyB)) return new Vector3i(1);

        Vector3i array = new Vector3i(lookingAt).sub(paste);

        Vector3i delta = copyB.clone()
            .sub(copyA);

        Vector3f v2 = new Vector3f(array).mulTransposeDirection(new Matrix4f(transform.getRotation()).invert());

        array.x = Math.round(v2.x);
        array.y = Math.round(v2.y);
        array.z = Math.round(v2.z);

        array.x = delta.x == 0 ? array.x : Math.floorDiv(array.x, delta.x + (delta.x < 0 ? -1 : 1));
        array.y = delta.y == 0 ? array.y : Math.floorDiv(array.y, delta.y + (delta.y < 0 ? -1 : 1));
        array.z = delta.z == 0 ? array.z : Math.floorDiv(array.z, delta.z + (delta.z < 0 ? -1 : 1));

        return array;
    }

    public VoxelAABB getPasteVisualDeltas() {
        if (!Location.areCompatible(copyA, copyB)) return null;

        VoxelAABB aabb = new VoxelAABB(copyA, copyB);

        aabb.moveOrigin(paste);

        if (stack != null) {
            aabb.scale(stack.x, stack.y, stack.z);
        }

        if (this.transform != null) {
            this.transform.apply(aabb);
        }

        return aabb;
    }

    public VoxelAABB getCopyVisualDeltas() {
        if (!Location.areCompatible(copyA, copyB)) return null;

        return new VoxelAABB(copyA, copyB);
    }

    public enum PendingAction {
        MARK_COPY_A {
            @Override
            public Optional<CopyingConfig> process(CopyingConfig config, ManipulatorContext context,
                boolean forPreview) {
                config.copyA = context.getLookedAtBlock();
                config.copyB = null;
                config.paste = null;
                config.stack = null;

                if (!forPreview) {
                    config.action = PendingAction.MARK_COPY_B;
                }

                return Optional.of(config);
            }

            @Override
            public ImmutableColor getRulerColor() {
                return MMRenderUtils.BLUE;
            }
        },
        MARK_COPY_B {
            @Override
            public Optional<CopyingConfig> process(CopyingConfig config, ManipulatorContext context,
                boolean forPreview) {
                config.copyB = context.getLookedAtBlock();
                config.paste = null;
                config.stack = null;

                if (!forPreview) {
                    config.action = null;
                }

                return Optional.of(config);
            }

            @Override
            public ImmutableColor getRulerColor() {
                return MMRenderUtils.BLUE;
            }
        },
        MARK_PASTE {
            @Override
            public Optional<CopyingConfig> process(CopyingConfig config, ManipulatorContext context,
                boolean forPreview) {
                config.paste = context.getLookedAtBlock();
                config.stack = null;

                if (!forPreview) {
                    config.action = null;
                }

                return Optional.of(config);
            }

            @Override
            public ImmutableColor getRulerColor() {
                return MMRenderUtils.ORANGE;
            }
        },
        MARK_STACK {
            @Override
            public Optional<CopyingConfig> process(CopyingConfig config, ManipulatorContext context,
                boolean forPreview) {
                config.stack = config.calculateStack(context.getLookedAtBlock());

                if (!forPreview) {
                    config.action = null;
                }

                return Optional.of(config);
            }

            @Override
            public ImmutableColor getRulerColor() {
                return MMRenderUtils.GREEN;
            }
        },
        //
        ;

        public Optional<CopyingConfig> process(CopyingConfig config, ManipulatorContext context, boolean forPreview) {
            throw new UnsupportedOperationException();
        }

        /// Gets the ruler color, or null if rulers should not be shown.
        @Nullable
        public ImmutableColor getRulerColor() {
            return null;
        }
    }
}
