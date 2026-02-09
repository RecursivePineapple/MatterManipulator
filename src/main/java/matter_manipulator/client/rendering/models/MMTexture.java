package matter_manipulator.client.rendering.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import matter_manipulator.Tags;
import matter_manipulator.common.utils.enums.Flip;
import matter_manipulator.common.utils.enums.Rotation;

@EventBusSubscriber(Side.CLIENT)
public enum MMTexture implements QuadFactory {
    UPLINK_CASING,
    UPLINK_FRONT_OFF,
    UPLINK_FRONT_IDLE_GLOW,
    UPLINK_FRONT_ACTIVE_GLOW,
    ;

    public TextureAtlasSprite sprite;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void register(TextureMap textureMap) {
        for (MMTexture texture : values()) {
            texture.sprite = textureMap.registerSprite(new ResourceLocation(Tags.MODID, "blocks/" + texture.name()));
        }
    }

    private static final Vector3f[][] QUADS = {
        { new Vector3f() }, // DOWN
        { new Vector3f() }, // UP
        { new Vector3f() }, // NORTH
        { new Vector3f() }, // SOUTH
        { new Vector3f() }, // WEST
        { new Vector3f() }, // EAST
    };

    private static final FaceBakery bakery = new FaceBakery();

    @Override
    public @Nullable BakedQuad getQuad(@Nullable EnumFacing facing, long rand, Rotation rotation, Flip flip) {
        Vector3f a = new Vector3f(0f, 0f);
        Vector3f a = new Vector3f(0f, 0f);

        bakery.rotateVertex()

        return null;
    }
}
