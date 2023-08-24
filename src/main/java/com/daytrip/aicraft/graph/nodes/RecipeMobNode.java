package com.daytrip.aicraft.graph.nodes;

import com.daytrip.aicraft.graph.RecipeNode;
import net.minecraft.world.entity.Entity;

public record RecipeMobNode(Entity source) implements RecipeNode {
}
