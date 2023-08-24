package com.daytrip.aicraft.graph.nodes;

import com.daytrip.aicraft.graph.RecipeNode;
import com.daytrip.aicraft.util.IngredientInstance;

import java.util.List;

public record RecipeCraftNode(IngredientInstance ingredient) implements RecipeNode {
}
