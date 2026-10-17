package com.darkspace.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtOps;

import java.util.ArrayList;
import java.util.List;

public final class ShipBlueprintItem extends Item {
    public ShipBlueprintItem(Settings settings) { super(settings); }

    public static ItemStack createBlueprint(String name, BlockPos min, BlockPos max, World world) {
        int sx = max.getX() - min.getX() + 1;
        int sy = max.getY() - min.getY() + 1;
        int sz = max.getZ() - min.getZ() + 1;
        if (sx > 100 || sy > 100 || sz > 100) throw new IllegalArgumentException("Blueprint dimensions cannot exceed 100 blocks on any axis.");
        NbtCompound data = new NbtCompound();
        data.putString("BlueprintName", name == null || name.isBlank() ? "My Ship" : name.substring(0, Math.min(48, name.length())));
        data.putInt("SizeX", sx); data.putInt("SizeY", sy); data.putInt("SizeZ", sz);
        NbtList blocks = new NbtList();
        RegistryOps<NbtElement> ops = world.getRegistryManager().getOps(NbtOps.INSTANCE);
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int y=min.getY(); y<=max.getY(); y++) {
            for (int z=min.getZ(); z<=max.getZ(); z++) {
                for (int x=min.getX(); x<=max.getX(); x++) {
                    pos.set(x,y,z);
                    BlockState state = world.getBlockState(pos);
                    if (state.isAir()) continue;
                    NbtElement encoded = BlockState.CODEC.encodeStart(ops, state).result().orElse(null);
                    if (encoded == null) continue;
                    NbtCompound entry = new NbtCompound();
                    entry.putInt("x", x-min.getX()); entry.putInt("y", y-min.getY()); entry.putInt("z", z-min.getZ());
                    entry.put("state", encoded);
                    blocks.add(entry);
                }
            }
        }
        data.put("Blocks", blocks);
        ItemStack stack = new ItemStack(PortalItems.SHIP_BLUEPRINT);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                net.minecraft.text.Text.literal(data.getString("BlueprintName")));
        return stack;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        ItemStack stack = context.getStack();
        NbtComponent custom = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (custom == null || !custom.contains("Blocks")) {
            if (context.getPlayer() != null && world.isClient) context.getPlayer().sendMessage(net.minecraft.text.Text.literal("This blueprint is empty."), true);
            return ActionResult.FAIL;
        }
        NbtCompound data = custom.copyNbt();
        BlockPos origin = context.getBlockPos().offset(context.getSide());
        NbtList blocks = data.getList("Blocks", NbtElement.COMPOUND_TYPE);
        List<BlockPos> positions = new ArrayList<>(blocks.size());
        List<BlockState> states = new ArrayList<>(blocks.size());
        RegistryOps<NbtElement> ops = world.getRegistryManager().getOps(NbtOps.INSTANCE);
        for (int i=0; i<blocks.size(); i++) {
            NbtCompound entry = blocks.getCompound(i);
            BlockState state = BlockState.CODEC.parse(ops, entry.get("state")).result().orElse(Blocks.AIR.getDefaultState());
            if (state.isAir()) continue;
            BlockPos target = origin.add(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"));
            positions.add(target); states.add(state);
        }
        for (BlockPos target : positions) {
            if (!world.getBlockState(target).isReplaceable()) {
                if (context.getPlayer() != null && world.isClient) context.getPlayer().sendMessage(net.minecraft.text.Text.literal("Blueprint placement blocked: clear the entire ship area first."), true);
                return ActionResult.FAIL;
            }
        }
        if (!world.isClient) {
            for (int i=0; i<positions.size(); i++) world.setBlockState(positions.get(i), states.get(i), Block.NOTIFY_ALL);
            PlayerEntity player = context.getPlayer();
            if (player != null) player.sendMessage(net.minecraft.text.Text.literal("Placed blueprint: " + data.getString("BlueprintName")), true);
        }
        return ActionResult.SUCCESS;
    }
}
