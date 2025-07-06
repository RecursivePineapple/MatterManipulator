package com.recursive_pineapple.matter_manipulator.server;

import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.sendChatToPlayer;
import static com.recursive_pineapple.matter_manipulator.common.utils.MMUtils.sendErrorToPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;

import com.recursive_pineapple.matter_manipulator.common.compat.BlockProperty;
import com.recursive_pineapple.matter_manipulator.common.compat.BlockPropertyRegistry;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class BlockStateCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "state";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/state <get|set> [property name] [property value]";
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<>();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer player)) return;

        String action = MMUtils.getIndexSafe(args, 0);
        String name = MMUtils.getIndexSafe(args, 1);
        String value = MMUtils.getIndexSafe(args, 2);

        if (action == null || "set".equals(action) && name != null && value == null) {
            sendErrorToPlayer(player, getCommandUsage(sender));
            return;
        }

        var hit = MMUtils.getHitResult(player, true);

        if (hit == null || hit.typeOfHit != MovingObjectType.BLOCK) {
            sendErrorToPlayer(player, StatCollector.translateToLocal("mm.info.error.must_look_at_block"));
            return;
        }

        Map<String, BlockProperty<?>> properties = new Object2ObjectOpenHashMap<>();
        BlockPropertyRegistry.getProperties(player.worldObj, hit.blockX, hit.blockY, hit.blockZ, properties);

        if ("get".equals(action)) {
            if (name != null) {
                var prop = properties.get(name);

                if (prop == null) {
                    sendErrorToPlayer(player, StatCollector.translateToLocal("mm.info.error.property_not_found"));
                    return;
                }

                sendChatToPlayer(player, prop.getName() + ": " + prop.getValueAsString(player.worldObj, hit.blockX, hit.blockY, hit.blockZ));
                return;
            } else {
                sendChatToPlayer(player, StatCollector.translateToLocal("mm.info.properties"));

                if (properties.isEmpty()) {
                    sendChatToPlayer(player, StatCollector.translateToLocal("mm.info.none"));
                } else {
                    for (var e : properties.entrySet()) {
                        sendChatToPlayer(
                            player,
                            e.getValue().getName() + ": " + e.getValue().getValueAsString(player.worldObj, hit.blockX, hit.blockY, hit.blockZ)
                        );
                    }
                }

                return;
            }
        } else {
            var prop = properties.get(name);

            if (prop == null) {
                sendErrorToPlayer(player, StatCollector.translateToLocal("mm.info.error.property_not_found"));
                return;
            }

            try {
                prop.setValueFromText(player.worldObj, hit.blockX, hit.blockY, hit.blockZ, value);
            } catch (Exception e) {
                sendErrorToPlayer(
                    player,
                    StatCollector.translateToLocalFormatted(
                        "mm.info.error.error_setting_property",
                        e.getMessage()
                    )
                );
            }

            return;
        }
    }
}
