package com.daytrip.aicraft.util;

import net.minecraft.world.item.crafting.Ingredient;

public record IngredientInstance(Ingredient ingredient, int count) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IngredientInstance that)) return false;

        return ingredient.getStackingIds().equals(that.ingredient.getStackingIds());
    }

    @Override
    public int hashCode() {
        int result = ingredient.getStackingIds().hashCode();
        return result;
    }

    public static IngredientInstance fromIngredient(Ingredient ingredient) {
        return new IngredientInstance(ingredient, 1);
    }
}
