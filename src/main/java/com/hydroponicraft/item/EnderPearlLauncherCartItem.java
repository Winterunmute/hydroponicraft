package com.hydroponicraft.item;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.entity.EnderPearlLauncherCart;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EnderPearlLauncherCartItem extends Item {

    public EnderPearlLauncherCartItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!state.is(BlockTags.RAILS)) return InteractionResult.FAIL;

        if (!level.isClientSide()) {
            EnderPearlLauncherCart cart = new EnderPearlLauncherCart(
                    HydroponiCraftRegistry.ENDER_PEARL_LAUNCHER_CART_TYPE.get(), level);
            cart.setPos(pos.getX() + 0.5, pos.getY() + 0.0625, pos.getZ() + 0.5);
            level.addFreshEntity(cart);
            if (ctx.getPlayer() != null && !ctx.getPlayer().getAbilities().instabuild) {
                ctx.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
