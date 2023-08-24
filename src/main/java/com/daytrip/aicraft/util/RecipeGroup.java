package com.daytrip.aicraft.util;

import java.util.List;

public record RecipeGroup(List<IngredientInstance> ingredients) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeGroup that)) return false;

        return ingredients.equals(that.ingredients);
    }

    @Override
    public int hashCode() {
        return ingredients.hashCode();
    }
}
