package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.client.screen.tooltip.TemplateData;
import com.direwolf20.buildinggadgets.common.component.BGComponent;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class TemplateItem extends Item {

    public TemplateItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        GadgetUtils.addTooltipNameAndAuthor(stack, worldIn, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (!playerIn.isShiftKeyDown())
            return super.use(worldIn, playerIn, handIn);

        if (worldIn.isClientSide) {
            return GuiMod.MATERIAL_LIST.openScreen(playerIn)
                    ? InteractionResultHolder.success(playerIn.getItemInHand(handIn))
                    : super.use(worldIn, playerIn, handIn);
        }

        return super.use(worldIn, playerIn, handIn);
    }

    public static ItemStack getTemplateItem(Player player) {
        ItemStack mainhand = player.getMainHandItem();
        if (BGComponent.TEMPLATE_KEY_COMPONENT.isProvidedBy(mainhand))
            return mainhand;

        ItemStack offhand = player.getOffhandItem();
        if (BGComponent.TEMPLATE_KEY_COMPONENT.isProvidedBy(offhand))
            return offhand;
        return ItemStack.EMPTY;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        return Optional.of(new TemplateData(itemStack));
    }
}
