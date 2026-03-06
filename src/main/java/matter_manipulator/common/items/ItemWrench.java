package matter_manipulator.common.items;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import matter_manipulator.Tags;
import matter_manipulator.common.structure.IAlignment;
import matter_manipulator.common.structure.IAlignmentProvider;
import matter_manipulator.common.structure.StructureUtils;
import matter_manipulator.common.utils.MCUtils;
import matter_manipulator.common.utils.MathUtils;
import mcp.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemWrench extends Item {

    public ItemWrench() {
        setTranslationKey("wrench");
        setRegistryName(Tags.MODID, "wrench");
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, EnumHand hand) {
        RayTraceResult hit = MathUtils.getHitResult(player);

        if (hit != null) {
            if (world.getTileEntity(hit.getBlockPos()) instanceof IAlignmentProvider provider) {
                IAlignment alignment = provider.getAlignment();

                if (alignment != null) {
                    EnumFacing wrenchSide = StructureUtils.determineWrenchingSide(hit.sideHit, hitX, hitY, hitZ);

                    if (alignment.getDirection() == wrenchSide) {
                        if (player.isSneaking()) {
                            MCUtils.sendChatToPlayer(player, "flip");
                            return alignment.toolSetFlip(null) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
                        } else {
                            MCUtils.sendChatToPlayer(player, "rotate");
                            return alignment.toolSetRotation(null) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
                        }
                    } else {
                        MCUtils.sendChatToPlayer(player, "wrench");
                        return alignment.toolSetDirection(wrenchSide) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
                    }
                }
            }
        }

        return EnumActionResult.PASS;
    }
}
