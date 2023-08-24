package com.daytrip.aicraft.graph;

import com.daytrip.aicraft.graph.nodes.RecipeCraftNode;
import com.daytrip.aicraft.util.Edge;
import com.daytrip.aicraft.util.IngredientInstance;
import com.daytrip.aicraft.util.ItemInstance;
import com.daytrip.aicraft.util.RecipeGroup;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RecipeGraph {
    private static Multimap<ItemInstance, List<IngredientInstance>> REQUIREMENTS;
    private final MutableValueGraph<ItemInstance, Edge> graph;
    private final MutableValueGraph<RecipeNode, Edge> newGraph;
    private final ItemInstance item;

    public RecipeGraph(Item item) {
        this.graph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
        this.newGraph = ValueGraphBuilder.directed().build();
        this.item = ItemInstance.fromStack(item.getDefaultInstance());

        this.handleItem(this.item);
    }

    public static void buildCache(RecipeManager manager, RegistryAccess access) {
        REQUIREMENTS = ArrayListMultimap.create();

        manager.getRecipes().forEach(recipe -> {
            System.out.println("Got recipe: " + recipe.getId());
            List<IngredientInstance> items = new ArrayList<>();
            recipe.getIngredients().forEach(ingredient -> {
                var newInstance = IngredientInstance.fromIngredient(ingredient);
                if (!items.contains(newInstance)) {
                    items.add(newInstance);
                } else {
                    int index = items.indexOf(newInstance);
                    var instance = items.get(index);
                    items.set(index, new IngredientInstance(instance.ingredient(), instance.count() + 1));
                }
            });
            REQUIREMENTS.put(ItemInstance.fromStack(recipe.getResultItem(access)), items);
        });
    }

    private void handleItem(ItemInstance stack) {
        graph.addNode(stack);
        REQUIREMENTS.get(stack).forEach(r -> r.forEach(requirement -> {
            ItemInstance instance = ItemInstance.fromIngredientInstance(requirement, 0);
            this.handleItem(instance);
            graph.putEdgeValue(stack, instance, new Edge(Edge.EdgeType.CRAFT, 0d));
        }));
    }

    private void handleItemNew(RecipeNode node) {
        newGraph.addNode(node);

        if (node instanceof RecipeCraftNode) {
            ((RecipeCraftNode) node).ingredients().forEach(instance -> {

            });
        }
    }

    public List<ItemInstance> getRemainingRequirements(Inventory inventory) {
        List<ItemInstance> requirements = new ArrayList<>();

        if (inventory.contains(this.item.item().getDefaultInstance())) {
            return requirements;
        }

        x(this.item, inventory, requirements);

        return requirements;
    }

    private void x(ItemInstance item, Inventory inventory, List<ItemInstance> requirements) {
        Set<ItemInstance> a = graph.adjacentNodes(item);
        if (a.size() == 0) {
            requirements.add(item);
            return;
        }

        for (var i : a) {
            if (inventory.contains(i.item().getDefaultInstance())) {
                continue;
            }
            this.x(i, inventory, requirements);
        }
    }

    public MutableValueGraph<ItemInstance, Edge> getGraph() {
        return this.graph;
    }

    @Override
    public String toString() {
        return this.graph.toString();
    }
}
