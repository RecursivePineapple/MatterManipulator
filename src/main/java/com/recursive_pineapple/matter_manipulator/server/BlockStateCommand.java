package com.recursive_pineapple.matter_manipulator.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

import com.recursive_pineapple.matter_manipulator.common.compat.BlockProperty;
import com.recursive_pineapple.matter_manipulator.common.compat.BlockPropertyRegistry;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

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
            MMUtils.sendErrorToPlayer(player, getCommandUsage(sender));
            return;
        }

        var hit = MMUtils.getHitResult(player, true);

        if (hit == null || hit.typeOfHit != MovingObjectType.BLOCK) {
            MMUtils.sendErrorToPlayer(player, "You must be looking at a block to use this command.");
            return;
        }

        HashMap<String, BlockProperty<?>> properties = new HashMap<>();
        BlockPropertyRegistry.getProperties(player.worldObj, hit.blockX, hit.blockY, hit.blockZ, properties);

        if ("get".equals(action)) {
            if (name != null) {
                var prop = properties.get(name);

                if (prop == null) {
                    MMUtils.sendErrorToPlayer(player, "Property not found.");
                    return;
                }

                MMUtils.sendChatToPlayer(player, prop.getName() + ": " + prop.getValueAsString(player.worldObj, hit.blockX, hit.blockY, hit.blockZ));
                return;
            } else {
                MMUtils.sendChatToPlayer(player, "Properties:");

                if (properties.isEmpty()) {
                    MMUtils.sendChatToPlayer(player, "None");
                } else {
                    for (var e : properties.entrySet()) {
                        MMUtils.sendChatToPlayer(
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
                MMUtils.sendErrorToPlayer(player, "Property not found.");
                return;
            }

            try {
                prop.setValueFromText(player.worldObj, hit.blockX, hit.blockY, hit.blockZ, value);
            } catch (Exception e) {
                MMUtils.sendErrorToPlayer(player, "Error setting property: " + e.getMessage());
            }

            return;
        }
    }
}
