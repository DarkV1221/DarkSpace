package com.darkspace.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RadiantPillarShipItem extends Item {
    public RadiantPillarShipItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos origin = context.getBlockPos().offset(context.getSide());
        Map<BlockPos, BlockState> build = createShip(origin);

        // Refuse placement if the whole ship does not fit, so it never overwrites a build.
        for (BlockPos pos : build.keySet()) {
            if (!world.getBlockState(pos).isReplaceable()) {
                return ActionResult.FAIL;
            }
        }

        if (!world.isClient) {
            build.forEach((pos, state) -> world.setBlockState(pos, state, Block.NOTIFY_ALL));
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }
        }
        return ActionResult.SUCCESS;
    }

    private static Map<BlockPos, BlockState> createShip(BlockPos origin) {
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        BlockState hull = Blocks.SMOOTH_QUARTZ.getDefaultState();
        BlockState glass = Blocks.CYAN_STAINED_GLASS.getDefaultState();
        BlockState glow = Blocks.SEA_LANTERN.getDefaultState();
        BlockState fin = Blocks.END_ROD.getDefaultState();

        // A tall luminous spacecraft: central spine, radiant core, and swept side fins.
        for (int y = 0; y <= 5; y++) {
            put(blocks, origin, 0, y, 0, y == 2 || y == 4 ? glow : hull);
        }
        put(blocks, origin, 0, 6, 0, glow);
        put(blocks, origin, 0, 7, 0, fin);

        for (int x : new int[]{-2, -1, 1, 2}) {
            put(blocks, origin, x, 3, 0, Math.abs(x) == 2 ? hull : glass);
            put(blocks, origin, x, 2, 0, Math.abs(x) == 2 ? fin : glow);
            put(blocks, origin, x, 4, 0, Math.abs(x) == 2 ? fin : glass);
        }
        for (int z : new int[]{-1, 1}) {
            put(blocks, origin, 0, 3, z, glass);
            put(blocks, origin, 0, 4, z, glow);
        }
        for (int x : new int[]{-1, 1}) {
            put(blocks, origin, x, 1, 0, hull);
            put(blocks, origin, x, 5, 0, glass);
        }
        return blocks;
    }

    private static void put(Map<BlockPos, BlockState> blocks, BlockPos origin,
                            int x, int y, int z, BlockState state) {
        blocks.put(origin.add(x, y, z), state);
    }
}
