package com.daytrip.aicraft.util;

import com.daytrip.aicraft.graph.RecipeNode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemInstance(Item item, int count) implements RecipeNode {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemInstance that)) return false;

        return BuiltInRegistries.ITEM.getId(item) == BuiltInRegistries.ITEM.getId(that.item);
    }

    @Override
    public int hashCode() {
        int result = BuiltInRegistries.ITEM.getId(item);
        return result;
    }

    public static ItemInstance fromStack(ItemStack stack) {
        return new ItemInstance(stack.getItem(), stack.getCount());
    }

    public static ItemInstance fromIngredientInstance(IngredientInstance instance, int index) {
        return new ItemInstance(instance.ingredient().getItems()[index].getItem(), instance.count());
    }
}
