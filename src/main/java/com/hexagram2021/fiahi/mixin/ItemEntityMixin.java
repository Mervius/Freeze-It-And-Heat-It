package com.hexagram2021.fiahi.mixin;

import com.hexagram2021.fiahi.common.handler.ItemStackFoodHandler;
import net.minecraft.nbt.CompoundTag;
import com.hexagram2021.fiahi.common.ForgeEventHandler;
import com.hexagram2021.fiahi.register.FIAHICapabilities;
import com.hexagram2021.fiahi.register.FIAHIItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@SuppressWarnings("deprecation")
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V", shift = At.Shift.AFTER))
	public void fiahi$tickFood(CallbackInfo ci) {
		ItemEntity current = (ItemEntity) (Object) this;
		if(!current.level().isClientSide && ForgeEventHandler.isAvailableToTickFood()) {
			current.getItem().getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(c -> {
				Double temperature = c.getTemperature();
				int old_temp = (int)(temperature / 5) * 5;
				c.foodTick(temperature + 2.0D * WorldHelper.getTemperatureAt(current.level(), current.getOnPos()), current.getItem().getItem());
				temperature = c.getTemperature();
				int new_temp = (int)(temperature / 5) * 5;
				if (new_temp != old_temp) {
					CompoundTag nbt = current.getItem().getTag();
					if(nbt == null) {
						if(temperature == 0) {
							return;
						}
						nbt = new CompoundTag();
					}
					nbt.putInt(ItemStackFoodHandler.FIAHI_TAG_TEMPERATURE, new_temp);
					ItemStack new_stack = new ItemStack(current.getItem().getItem(),current.getItem().getCount());
					new_stack.setTag(nbt);
					current.setItem(new_stack);
					current.getItem().getCapability(FIAHICapabilities.FOOD_CAPABILITY).ifPresent(d -> {d.setTemperature(temperature);});
				}
				if(c.getTemperature() > 120) {
					FoodProperties foodProperties = current.getItem().getItem().getFoodProperties();
					if(foodProperties != null) {
						current.setItem(new ItemStack(foodProperties.isMeat() ? FIAHIItems.LEFTOVER_MEAT : FIAHIItems.LEFTOVER_VEGETABLE, current.getItem().getCount()));
					}
				}
			});
		}
	}
}
