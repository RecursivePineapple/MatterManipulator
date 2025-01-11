## Introduction

This mod adds the matter manipulator, which is meant to be a survival-usable version of world edit. The goal of this tool isn't to replace world edit completely, rather its goal is to help with building large or repetitive structures without forcing the player to resort to world edit.

## Current Status (Important, read me!)

This tool is in beta, meaning that it can be used in survival worlds if you're fine with loading backups. There are certainly still bugs that aren't known, but it seems stable enough to be used.

If you're using this in GTNH 2.7.x, you must upgrade your GT5u jar to the latest custom release found [here](https://github.com/RecursivePineapple/GT5-Unofficial/releases) that matches your modpack version. This patched GT5u contains several patches that are required for the matter manipulator to properly interact with GT machines. Your game will crash if you try to use a matter manipulator in GTNH without this jar.

The compat PR has been merged and the mod has been added to the nightlies. Any nightly from 813 onward has the mod in it already and can be used without further steps, but as always expect nightlies to have potentially game-breaking problems. Nightlies from 800 to 812 can be used if you put the mod in them. Any prior nightlies won't work properly.

You can find the releases [here](https://github.com/RecursivePineapple/MatterManipulator/releases).

See [here](https://github.com/users/RecursivePineapple/projects/1/views/1) for more information on what's being worked on. I'll use this github project to track what I need to work on and their priorities.

## For Non-GTNH Modpacks

This tool can be used as a standalone mod with a few dependencies, but it doesn't come with any default recipes since vanilla just doesn't have enough items or mechanics to support it. It's highly recommended to add your own recipes if using this in a non-GTNH pack. The manipulator crafting components will always be present, so it's also recommended to follow the existing pattern of making components, then combining the components into the final tool.

### Required Dependencies

- StructureLib
- ModularUI 1
- GTNHLib

## Contributing

This project is at the point that it can take contributions without constant merge conflicts. I'll gladly take any help, but please DM me first (discord: recursive_pineapple). Some things can't be added for balance reasons, and I don't want people to waste their time.

## Behaviour Overview

The tool has several modes which change its behaviour in various ways. They mainly affect which blocks the manipulator changes and how it changes them.

The first mode is a basic geometry mode. It can only place standard blocks and it does not configure them in any way. It can place cuboids/flat planes, ellipsoids/spheres, cylinders, and lines. The shape dimensions are completely configurable, and the blocks that make them up can be changed.
The mode has 4 block 'slots', but not every shape uses each slot. There are corners, edges, faces, and volumes. The block slots are used as expected. For example, if you wanted to make a hollow cuboid room, you could set the volumes to nothing, set the faces to glass, and set the edges + corners to stone bricks.

The second mode is the cable mode, which allows you to place fluix cables and GT cables or pipes in lines. It will automatically connect GT cables/pipes together properly. If you replace an existing GT cable, the new cable will inherit the old cable's connections along with its new ones. It does not automatically colour cables (use querns' infinite spray can), though it will use the correct fluix cable if the original was coloured.

The third mode allows you to replace large swaths of blocks, like the equal trade focus. You can set one or more blocks to replace, then set the block to replace with. The mode will replace all valid blocks within a marked region.

The fourth mode allows you to move a section of blocks from one area to another. It can move anything that can be broken, since it effectively just saves the tile entity and loads it in another location. The code for this was taken from blood magic's teleposers.

The fifth mode allows you to copy + paste areas of machines. Currently, it only supports basic blocks, GT machines, and AE machines. It's possible to add more integrations, but I didn't think it was worth the effort. I'll take contributions if anyone wants to add support for a mod. Additionally, you can rotate, flip, or stack the copied blocks. You can edit the stack size by either clicking 'Edit Stack' then clicking 'Mark', or you can edit it in the edit transform screen.

The final mode (although this is more of a global setting), is the remove mode. This will let you configure which existing blocks should be removed, if any. You can remove no blocks, replaceable blocks only (grass, leaves, etc), or all blocks.

## Block Placing & Removing

This tool will try to replicate blocks as accurately as possible. It does this by analyzing the block's item and metadata along with its tile entity. It does not do anything with raw NBT (except for the moving mode, which doesn't save it), so the only duplication or deletion glitches will be ones where the tool doesn't give or take items or fluids properly.

If a block has subtypes, it will save its metadata as is and try to find an item with that meta when placing it. If a block doesn't have subtypes, minecraft will reset its metadata when broken, which means the tool can set its metadata to whatever it was when analyzed without changing the block.

When the tool breaks a block, it will try to reset it in the world by checking for GT and AE interfaces, then calling a handful of methods on the tile entity. It will remove any covers, clear any fluid tanks or inventories, remove any AE patterns, cells, or upgrades, reset the AE name, and try to convert p2p tunnels to their default version. It will also remove any config on a storage cell. In general, if the tool can configure something, it will try its best to undo that configuration. Once it's done everything it can to reset a block, it will remove the block.

When modifying a tile entity, the tool will generate a TileAnalysisResult. This object contains a few dozen fields which represent all copyable state. When a tile is analyzed, this object will scan for various interfaces and call various methods to retrieve the tile's state. When applying, it will do the reverse, consuming or returning items when applicable. It will never directly modify the NBT state, even if it's safe to do so (ie for AE configuration). It will only hack into internals when absolutely necessary, and even then it only call private methods.

For non-moving modes, the tool will only place similar blocks at the same time. It groups blocks based on their item and meta (if the item has subtypes). Most builds will be limited by the build tick time, not the block place limit, so the block place limit is only relevant for geometry.

Each tier has a range limit, beyond which the tool will do nothing. The tool will always let the player mark a region outside of this limit, but the tool won't interact with blocks outside of its range. There is a preview which correctly shows which blocks can be placed depending on the player's current position. If the player wants to build any blocks outside of that range, they can move and build again.

For balance reasons, the tool will always void ore regardless of the tier. This is to prevent the player from avoiding the existing mining mechanics.

## Which traits are copied?

The goal is to completely support AE and GT, though some niche machine-specific settings in GT have been intentionally skipped. If there's anything critical missing, feel free to open an issue.

### GregTech

- Pipe/Cable connections
- Machine colour
- Machine front & output directions
- Basic machine settings (auto outputs, etc)
- Hatch settings (sorting, stack limiting, etc)
- Multi machine settings (void protection, batch mode, etc)
- Multi rotation
- Covers
- Strong redstone output
- Custom names
- Ghost circuits
- Bus/hatch item/fluid lock
- Modes (for machines that use the new mode system)
- Data stick data (via IDataCopyable)
- ME output bus/hatch capacity
- TecTech machine input parameters

### Applied Energistics 2

For singleblock machines (such as the IO Port):
- The colour
- The orientation
- The configuration
- The upgrades
- The custom name
- Any stored cells (will also automatically configure cells & install their upgrades)
- Any patterns (will encode but not decode patterns)

For parts:
- The configuration
- The custom name
- The oredict filter (if the card is present)
- P2P fields
- The priority

Note: attunable P2Ps (non-interface P2Ps) will also be attuned automatically from a blank P2P.

### Miscellaneous

- Inventories
- Block rotations (vanilla mainly, with a handful of supported mods)

Note: certain blocks may need extra logic for their rotations. If you find a block that can't be rotated but should be, please open an issue or DM me.

## How to use the tool

Most of the tool's configuration is done through a radial menu which can be accessed by right clicking in the air. There are a few keybinds and context actions for common actions - specifically cutting (ctrl + x), copying (ctrl + c), pasting (ctrl + v), and block picking (middle mouse button). The exchange mode extends block picking since it has two configurable blocks - holding shift while picking a block will set the replace whitelist instead of the block to replace with.

Areas are configured by right clicking on a block, then moving the player's reticle to another location and right clicking. The tool will render structure hints wherever it would place a block, even while moving the coordinates (it uses structurelib for the hints). By default, the target location is whatever face the player is looking at, but the player can select an existing block by sneaking.

Some geometry shapes require three coordinates to be configured, such as the cylinder.
The cylinder will pin its second and third (B & C) coordinates to specific locations since it has special requirements for the algorithm. B will be pinned to one of the axis planes around A and configures the cylinder's size. C will be pinned to the plane's normal and configures the cylinder's height.

To start building, the player has to hold shift + right click for 10 ticks (as a precaution, since there isn't an undo button). After that, the tool will place blocks in several batches. The amount placed and the interval between batches depends on the tool's tier. The tool will play a sound wherever it places or removes blocks; it won't spam several sounds in one, it'll calculate the centre point before playing anything.

## AE Integration

The higher tiers can directly interface with an ME system. The tool acts exactly like a terminal, and must be place into a security terminal to connect it to an ME system. Once connected, the player must be in range of a wireless access point on the network and the network must be powered. The tool will be able to insert and extract fluids and items from the ME network when needed. The tooltip states whether the tool can interact with the connected ME system.

Additionally, there is a new UV multi called a quantum uplink that allows the tool to connect to an ME system over an infinite range and across dimensions. It also allows the tool to create fake ME patterns for the required items when copying blocks, so that the player can easily craft whatever items are needed. The multi has a special hatch that connects directly to an ME system. The multi can create patterns for free, but to transfer items or fluids it requires 16k EU of plasma per item or per 1000 L. While active, it consumes 1 amp of ZPM regardless of its activity. The tool can be bound to an uplink by placing the tool into the controller while the uplink is active. The tooltip also states whether the tool can interact with its bound uplink.

The tool will try to insert and extract items in a relatively sane manner. Fluids and items follow the same priorities, though fluids  can only be removed from the world and never placed. If the player's inventory is full, the tool will spawn in an item entity that can hold up to 2.1 billion items (int 32 max). For fluids, the tool will try to fill any fluid cells when trying to insert a fluid into the player's inventory. If there are no cells, the fluid will be voided.

When something is placed into the world, the tool will pulls from inventories in this order:
1. It pulls from the ME system
2. It pulls from the player's inventory
3. It pulls from the uplink

When something is removed from the world, the tool will insert into inventories in this order:
1. It inserts into the ME system
2. It inserts into the uplink
3. It inserts into the player's inventory
4. (for items) It drops the items on the ground

## The various tiers & their requirements

This tool comes in several tiers. Each tier is a numerical upgrade over the previous (faster, a higher range, and a bigger EU buffer) along with new capabilities.

The first is a 'prototype' that has very few features and is unlocked in late HV (since it requires IV circuits). It requires thaumcraft and ender io progression to craft. It can only use the geometry mode and cannot remove blocks. It has a range of 32 blocks and can only place 16 blocks per second, so it's only useful for building minor structures.

The second is the 'first' version (called a MKI) and is unlocked in IV. It only requires GT progression though it's not easy to make in early IV since it requires ZPM circuits, lots of IV superconductors & tungstensteel, and a lapotronic energy orb. It has a range of 64 blocks and can place 64 blocks per second. It can connect to AE and remove blocks. It supports the geometry, cable, and exchanging modes.

The MKII is unlocked in LuV. It requires an assembly line, a MKI fusion reactor (for duranium), quantium, red zircon (ross128b gem), a lapotronic energy cluster, UV circuits, along with many other expensive progression materials (superconductor, HSS-S, etc). It has the capabilities of the MKI along with copying and moving. It can place 256 blocks per second and has a range of 128 blocks.

The MKIII is the final version and is unlocked in ZPM. It requires a MKII fusion reactor (for tritanium), mysterious crystal, gravitons (from a cyclotron), lots of naquadah alloy, several UV circuits, gravi stars (neutronium), trinium, and an energy module. It has an unlimited range and can place up to 1,024 blocks per second.

The uplink requires several meme items (various AE and tectech components), UHV circuits, hyper-acceleration cards, and a research station. Additionally, the ME connector hatch requires a 16384k storage component along with several more meme items. This either requires ultra bio mutated circuit boards, or several 4096k components.
