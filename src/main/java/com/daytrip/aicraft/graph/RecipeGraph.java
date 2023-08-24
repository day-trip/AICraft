package com.daytrip.aicraft.graph;

import com.daytrip.aicraft.graph.nodes.RecipeBlockNode;
import com.daytrip.aicraft.util.Edge;
import com.daytrip.aicraft.util.IngredientInstance;
import com.daytrip.aicraft.util.ItemInstance;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class RecipeGraph {
    private static Multimap<ItemInstance, Set<IngredientInstance>> CRAFTING;
    private static Multimap<ItemInstance, Block> BLOCKS;
    private final MutableValueGraph<RecipeNode, Edge> graph;
    private final ItemInstance item;

    public RecipeGraph(Item item) {
        this.graph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
        this.item = ItemInstance.fromStack(item.getDefaultInstance());

        this.handleItem(this.item);
    }

    public static void buildCache(Level level, LocalPlayer player) {
        // RECIPE PHASE

        CRAFTING = ArrayListMultimap.create();
        level.getRecipeManager().getRecipes().forEach(recipe -> {
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
            CRAFTING.put(ItemInstance.fromStack(recipe.getResultItem(level.registryAccess())), new HashSet<>(items));
        });

        // BLOCK PHASE

        // todo: multiplayer support (packets)
        /*BLOCKS = ArrayListMultimap.create();
        ServerLevel slevel = Minecraft.getInstance().getSingleplayerServer().overworld();
        BuiltInRegistries.BLOCK.forEach(block -> {
            LootParams.Builder builder = (new LootParams.Builder(slevel).withOptionalParameter(LootContextParams.ORIGIN, null).withOptionalParameter(LootContextParams.TOOL, null).withParameter(LootContextParams.THIS_ENTITY, player).withOptionalParameter(LootContextParams.BLOCK_ENTITY, null));
            block.defaultBlockState().getDrops(builder).forEach(stack -> {
                BLOCKS.put(ItemInstance.fromStack(stack), block);
            });
        });*/
    }

    private void handleItem(RecipeNode stack) {
        this.graph.addNode(stack);
        AtomicInteger i = new AtomicInteger();
        if (stack instanceof ItemInstance) {
            CRAFTING.get((ItemInstance) stack).forEach(r -> {
                r.forEach(requirement -> {
                    ItemInstance instance = ItemInstance.fromIngredientInstance(requirement, 0);
                    this.handleItem(instance);
                    this.graph.putEdgeValue(stack, instance, new Edge(Edge.EdgeType.CRAFT, i.get(), 0d));
                });
                i.getAndIncrement();
            });
            BLOCKS.get((ItemInstance) stack).forEach(b -> {
                RecipeBlockNode node = new RecipeBlockNode(b);
                this.graph.addNode(node);
                this.graph.putEdgeValue(stack, node, new Edge(Edge.EdgeType.BLOCK, i.get(), 0d));
                i.getAndIncrement();
            });
        }
    }

    public List<ItemInstance> getRemainingRequirements(Inventory inventory) {
        /*List<ItemInstance> requirements = new ArrayList<>();

        if (inventory.contains(this.item.item().getDefaultInstance())) {
            return requirements;
        }

        x(this.item, inventory, requirements);

        return requirements;*/
        return new ArrayList<>();
    }

    /*private void x(ItemInstance item, Inventory inventory, List<ItemInstance> requirements) {
        Set<RecipeNode> a = graph.adjacentNodes(item);
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
    }*/

    public MutableValueGraph<RecipeNode, Edge> getGraph() {
        return this.graph;
    }

    @Override
    public String toString() {
        return this.graph.toString();
    }
}
