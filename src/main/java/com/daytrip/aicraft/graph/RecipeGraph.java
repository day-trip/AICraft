package com.daytrip.aicraft.graph;

import com.daytrip.aicraft.util.Edge;
import com.daytrip.aicraft.util.IngredientInstance;
import com.daytrip.aicraft.util.ItemInstance;
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

public class RecipeGraph {
    private static Multimap<ItemInstance, List<IngredientInstance>> REQUIREMENTS;
    private final MutableValueGraph<ItemInstance, Edge> graph;
    private final List<ItemInstance> baseRequirements;

    protected RecipeGraph(MutableValueGraph<ItemInstance, Edge> graph, List<ItemInstance> base) {
        this.graph = graph;
        this.baseRequirements = base;
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

    public static RecipeGraph create(Item item) {
        MutableValueGraph<ItemInstance, Edge> graph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
        List<ItemInstance> base = new ArrayList<>();

        handleItem(ItemInstance.fromStack(item.getDefaultInstance()), graph, base);

        return new RecipeGraph(graph, base);
    }

    private static void handleItem(ItemInstance stack, MutableValueGraph<ItemInstance, Edge> graph, List<ItemInstance> base) {
        // TODO: fix the fact that crafting recipes don't get pulled up because count doesn't match

        System.out.println("Handling: " + stack.item().getDefaultInstance().getDisplayName());
        graph.addNode(stack);
        REQUIREMENTS.get(stack).forEach(r -> r.forEach(requirement -> {
            ItemInstance instance = ItemInstance.fromIngredientInstance(requirement, 0);
            handleItem(instance, graph, base);
            graph.putEdgeValue(stack, instance, new Edge(Edge.EdgeType.CRAFT, 0d));
        }));
        if (REQUIREMENTS.get(stack).isEmpty()) {
            base.add(stack);
        }
    }

    public List<ItemInstance> getRemainingRequirements(Inventory inventory) {
        List<ItemInstance> requirements = new ArrayList<>(this.baseRequirements);

        inventory.items.forEach(stack -> requirements.remove(ItemInstance.fromStack(stack)));

        return requirements;
    }

    public MutableValueGraph<ItemInstance, Edge> getGraph() {
        return this.graph;
    }

    @Override
    public String toString() {
        return this.graph.toString();
    }
}
