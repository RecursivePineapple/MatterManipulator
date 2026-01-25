package matter_manipulator.common.modes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import lombok.EqualsAndHashCode;
import matter_manipulator.client.rendering.MMRenderUtils;
import matter_manipulator.common.building.StandardBuild;
import matter_manipulator.common.modes.shapes.GeometryBlockPalette;
import matter_manipulator.common.modes.shapes.GeometryModeCube;
import matter_manipulator.common.modes.shapes.GeometryModeCylinder;
import matter_manipulator.common.modes.shapes.GeometryModeLine;
import matter_manipulator.common.modes.shapes.GeometryModeSphere;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.common.utils.data.XSTR;
import matter_manipulator.common.utils.math.Location;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.building.PendingBlock;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.util.Coroutine;

@EqualsAndHashCode
public class GeometryConfig implements GeometryBlockPalette {

    public PendingAction action;
    public Shape shape = Shape.CUBE;
    public IBlockSpec faces, edges, corners, volumes;
    public Location a, b, c;

    @Override
    public IBlockSpec corners() {
        return corners == null ? IBlockSpec.AIR : corners;
    }

    @Override
    public IBlockSpec edges() {
        return edges == null ? IBlockSpec.AIR : edges;
    }

    @Override
    public IBlockSpec faces() {
        return faces == null ? IBlockSpec.AIR : faces;
    }

    @Override
    public IBlockSpec volumes() {
        return volumes == null ? IBlockSpec.AIR : volumes;
    }

    public enum PendingAction {
        MARK_A {
            @Override
            public Optional<GeometryConfig> process(GeometryConfig config, ManipulatorContext context,
                boolean forPreview) {
                config.a = context.getLookedAtBlock();
                config.action = PendingAction.MARK_B;

                return Optional.of(config);
            }

            @Override
            public ImmutableColor getRulerColor() {
                return MMRenderUtils.BLUE;
            }
        },
        MARK_B {
            @Override
            public Optional<GeometryConfig> process(GeometryConfig config, ManipulatorContext context,
                boolean forPreview) {
                config.b = context.getLookedAtBlock();
                config.action = null;

                if (config.shape != null && config.shape.needsC()) {
                    config.action = PendingAction.MARK_C;
                }

                return Optional.of(config);
            }

            @Override
            public ImmutableColor getRulerColor() {
                return MMRenderUtils.BLUE;
            }
        },
        MARK_C {
            @Override
            public Optional<GeometryConfig> process(GeometryConfig config, ManipulatorContext context,
                boolean forPreview) {
                config.c = context.getLookedAtBlock();
                config.action = null;

                return Optional.of(config);
            }

            @Override
            public ImmutableColor getRulerColor() {
                return MMRenderUtils.BLUE;
            }
        },
        //
        ;

        public Optional<GeometryConfig> process(GeometryConfig config, ManipulatorContext context, boolean forPreview) {
            throw new UnsupportedOperationException();
        }

        /// Gets the ruler color, or null if rulers should not be shown.
        @Nullable
        public ImmutableColor getRulerColor() {
            return null;
        }
    }

    public enum Shape {
        CUBE {
            @Override
            public Coroutine<StandardBuild> getBlocks(GeometryConfig config, ManipulatorContext context) {
                return ctx -> {
                    Vector3i a = config.a;
                    Vector3i b = config.b;

                    XSTR rng = new XSTR(config.hashCode());

                    ArrayList<PendingBlock> blocks = GeometryModeCube.iterateCube(config, context.getWorld(), a.x, a.y, a.z, b.x, b.y, b.z);

                    ctx.stop(new StandardBuild(new ArrayDeque<>(blocks)));
                };
            }
        },
        LINE {
            @Override
            public Coroutine<StandardBuild> getBlocks(GeometryConfig config, ManipulatorContext context) {
                return ctx -> {
                    Vector3i a = config.a;
                    Vector3i b = config.b;

                    XSTR rng = new XSTR(config.hashCode());

                    ArrayList<PendingBlock> blocks = GeometryModeLine.iterateLine(config, context.getWorld(), a.x, a.y, a.z, b.x, b.y, b.z);

                    ctx.stop(new StandardBuild(new ArrayDeque<>(blocks)));
                };
            }
        },
        SPHERE {
            @Override
            public Coroutine<StandardBuild> getBlocks(GeometryConfig config, ManipulatorContext context) {
                return ctx -> {
                    Vector3i a = config.a;
                    Vector3i b = config.b;

                    XSTR rng = new XSTR(config.hashCode());

                    ArrayList<PendingBlock> blocks = GeometryModeSphere.iterateSphere(config, context.getWorld(), a.x, a.y, a.z, b.x, b.y, b.z);

                    ctx.stop(new StandardBuild(new ArrayDeque<>(blocks)));
                };
            }
        },
        CYLINDER {
            @Override
            public Coroutine<StandardBuild> getBlocks(GeometryConfig config, ManipulatorContext context) {
                return ctx -> {
                    XSTR rng = new XSTR(config.hashCode());

                    ArrayList<PendingBlock> blocks = GeometryModeCylinder.iterateCylinder(config, context.getWorld(), config.a, config.b, config.c);

                    ctx.stop(new StandardBuild(new ArrayDeque<>(blocks)));
                };
            }

            @Override
            public void pinCoordinates(Vector3i a, Vector3i b, Vector3i c) {
                // B must lay on one of the axis planes
                b.set(MathUtils.pinToPlanes(a, b));
                // C must lay on the normal of the A,B plane
                c.set(MathUtils.pinToLine(a, b, c));
            }

            @Override
            public boolean needsC() {
                return true;
            }
        },
        //
        ;

        public void pinCoordinates(Vector3i a, Vector3i b, Vector3i c) {

        }

        public boolean canRender(Vector3i a, Vector3i b, Vector3i c) {
            return a != null && b != null && (!needsC() || c != null);
        }

        public boolean needsC() {
            return false;
        }

        public Coroutine<StandardBuild> getBlocks(GeometryConfig config, ManipulatorContext context) {
            throw new UnsupportedOperationException();
        }

        public String getLocalizedName() {
            return MCUtils.translate("mm.mode.geometry.shape." + name().toLowerCase());
        }
    }
}
