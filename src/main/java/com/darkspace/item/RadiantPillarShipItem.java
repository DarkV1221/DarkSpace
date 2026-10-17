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

        // Never overwrite terrain or player builds; require the complete footprint to be clear.
        for (BlockPos pos : build.keySet()) {
            if (!world.getBlockState(pos).isReplaceable()) return ActionResult.FAIL;
        }

        if (!world.isClient) {
            build.forEach((pos, state) -> world.setBlockState(pos, state, Block.NOTIFY_ALL));
            if (context.getPlayer() != null && !context.getPlayer().getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }
        }
        return ActionResult.SUCCESS;
    }

    /**
     * Builds a low, horizontal, red-and-white exploration fighter inspired by classic
     * space-sim starfighters. The nose points toward negative Z.
     */
    private static Map<BlockPos, BlockState> createShip(BlockPos o) {
        Map<BlockPos, BlockState> b = new LinkedHashMap<>();
        BlockState white = Blocks.QUARTZ_BLOCK.getDefaultState();
        BlockState bright = Blocks.SMOOTH_QUARTZ.getDefaultState();
        BlockState red = Blocks.RED_CONCRETE.getDefaultState();
        BlockState redAccent = Blocks.RED_TERRACOTTA.getDefaultState();
        BlockState dark = Blocks.POLISHED_BLACKSTONE.getDefaultState();
        BlockState canopy = Blocks.BLACK_STAINED_GLASS.getDefaultState();
        BlockState engine = Blocks.SEA_LANTERN.getDefaultState();
        BlockState trim = Blocks.IRON_BLOCK.getDefaultState();

        // Main fuselage: tapered nose, broad midsection, and engine housing.
        put(b,o,0,0,-5,bright);
        for (int x=-1;x<=1;x++) put(b,o,x,0,-4, x==0?white:bright);
        for (int x=-1;x<=1;x++) put(b,o,x,0,-3,white);
        for (int x=-1;x<=1;x++) put(b,o,x,0,-2,white);
        for (int x=-2;x<=2;x++) put(b,o,x,0,-1, x==0?bright:white);
        for (int x=-2;x<=2;x++) put(b,o,x,0,0, x==0?bright:white);
        for (int x=-2;x<=2;x++) put(b,o,x,0,1, x==0?bright:white);
        for (int x=-2;x<=2;x++) put(b,o,x,0,2,white);
        for (int x=-1;x<=1;x++) put(b,o,x,0,3,bright);
        for (int x=-1;x<=1;x++) put(b,o,x,0,4,trim);

        // Swept, delta-shaped wings. Red leading edges and tips make the silhouette read
        // clearly from above instead of looking like a vertical block sculpture.
        wingRow(b,o,-1,0, -2, 2, white, red);
        wingRow(b,o, 0,0, -3, 3, white, red);
        wingRow(b,o, 1,0, -4, 4, white, red);
        wingRow(b,o, 2,0, -4, 4, white, red);
        wingRow(b,o, 3,0, -3, 3, white, red);
        wingRow(b,o, 4,0, -2, 2, white, red);

        // Red outer panels, white inner panels, and small dark control-surface details.
        for (int x : new int[]{-4,4}) {
            put(b,o,x,0,1,redAccent);
            put(b,o,x,0,2,red);
        }
        for (int x : new int[]{-3,3}) {
            put(b,o,x,0,0,red);
            put(b,o,x,0,3,redAccent);
        }
        put(b,o,-2,0,1,redAccent); put(b,o,2,0,1,redAccent);
        put(b,o,-2,0,2,red); put(b,o,2,0,2,red);

        // Raised cockpit, canopy glazing, and white dorsal spine.
        put(b,o,0,1,-2,white);
        put(b,o,0,1,-1,canopy);
        put(b,o,0,1,0,canopy);
        put(b,o,0,1,1,white);
        put(b,o,0,1,2,bright);
        put(b,o,-1,1,-1,redAccent); put(b,o,1,1,-1,redAccent);

        // Twin rear engine bells with luminous cores and dark exhaust surrounds.
        for (int x : new int[]{-1,1}) {
            put(b,o,x,0,3,dark);
            put(b,o,x,0,4,engine);
            put(b,o,x,1,3,trim);
        }
        put(b,o,0,0,4,bright);
        return b;
    }

    private static void wingRow(Map<BlockPos, BlockState> b, BlockPos o, int z, int y,
                                int minX, int maxX, BlockState white, BlockState red) {
        for (int x=minX;x<=maxX;x++) {
            boolean edge = Math.abs(x) == Math.max(Math.abs(minX), Math.abs(maxX));
            put(b,o,x,y,z,edge ? red : white);
        }
    }

    private static void put(Map<BlockPos, BlockState> blocks, BlockPos origin,
                            int x, int y, int z, BlockState state) {
        blocks.put(origin.add(x,y,z),state);
    }
}
