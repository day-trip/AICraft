package com.daytrip.aicraft.graph.nodes;

import com.daytrip.aicraft.graph.RecipeNode;
import net.minecraft.world.level.block.Block;

public record RecipeBlockNode(Block source) implements RecipeNode {
}
