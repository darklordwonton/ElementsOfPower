package gigaherz.elementsofpower.database.recipes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gigaherz.elementsofpower.ElementsOfPower;
import gigaherz.elementsofpower.database.Utils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class RecipeTools
{
    public static List<RecipeEnumerator> recipeEnumerators = Lists.newArrayList();

    static
    {
        recipeEnumerators.add(new RecipeEnumerator.Crafting());
        recipeEnumerators.add(new RecipeEnumerator.Furnace());
    }

    public static Map<ItemStack, List<ItemStack>> gatherRecipes()
    {
        Processor p = new Processor();
        for (RecipeEnumerator re : recipeEnumerators)
        {
            re.enumerate(p::processRecipe);
        }
        return p.itemSources;
    }

    private static class Processor
    {
        public Map<ItemStack, List<ItemStack>> itemSources = Maps.newHashMap();

        private void processRecipe(@Nonnull IRecipeInfoProvider recipe)
        {
            ItemStack output = recipe.getRecipeOutput();

            if (output.getItem() == null)
            {
                ElementsOfPower.logger.warn("A Recipe has non-null itemstack but the item is NULL! This recipe can NOT be processed!");
                return;
            }

            if (output.stackSize == 0)
            {
                ElementsOfPower.logger.warn("Recipe with output '" + output + "' has stack size 0. This recipe will be ignored.");
                return;
            }

            for (ItemStack s : recipe.getRecipeInputs())
            {
                if (s != null)
                {
                    if (s.getItem() == null)
                    {
                        ElementsOfPower.logger.warn("Recipe with output '" + output + "' has invalid input stack. This recipe will be ignored.");
                        return;
                    }

                    if (s.stackSize == 0)
                    {
                        ElementsOfPower.logger.warn("Recipe with output '" + output + "' has input stack of size 0. This recipe will be ignored.");
                        return;
                    }
                }
            }

            if (Utils.stackMapContainsKey(itemSources, output))
            {
                return;
            }

            List<ItemStack> inputs = reduceItemsList(recipe.getRecipeInputs());
            List<ItemStack> expandedInputs = Lists.newArrayList();

            output = applyExistingRecipes(output, inputs, expandedInputs);

            if (!isRecipeAggregating(expandedInputs, output))
                replaceExistingSources(output, expandedInputs);

            itemSources.put(output, expandedInputs);
        }

        private boolean isRecipeAggregating(@Nonnull List<ItemStack> inputs, @Nonnull ItemStack output)
        {
            if (inputs.size() == 1)
            {
                if (inputs.get(0).stackSize < output.stackSize)
                {
                    return true;
                }
            }

            return false;
        }

        private void replaceExistingSources(@Nonnull ItemStack output, @Nonnull List<ItemStack> items)
        {
            List<ItemStack> stacksToRemove = Lists.newArrayList();
            Map<ItemStack, List<ItemStack>> stacksToAdd = Maps.newHashMap();

            for (Map.Entry<ItemStack, List<ItemStack>> entry : itemSources.entrySet())
            {
                ItemStack result = entry.getKey().copy();
                List<ItemStack> stacks = Lists.newArrayList();
                int totalMult = 1;
                boolean anythingChanged = false;

                for (ItemStack s : entry.getValue())
                {

                    if (OreDictionary.itemMatches(s, output, false))
                    {
						if (s.stackSize != 0 && output.stackSize != 0) {
                        	int numNeeded = s.stackSize;
                        	int numProduced = output.stackSize;
                        	int num = Utils.lcm(numNeeded, numProduced);

                        	int mult = num / numNeeded;

                        	result.stackSize *= mult;
                        	for (ItemStack t : stacks)
                        	{
                            	t.stackSize *= mult;
                        	}

                        	totalMult *= mult;

                        	int mult2 = num / numProduced;
                        	for (ItemStack t : items)
                        	{
                            	ItemStack r = t.copy();
                            	r.stackSize *= mult2;
                            	stacks.add(r);
                        	}

                        	anythingChanged = true;
						}
                    }
                    else
                    {
                        ItemStack r = s.copy();
                        r.stackSize *= totalMult;
                        stacks.add(r);
                    }
                }

                if (anythingChanged)
                {
                    stacksToRemove.add(entry.getKey());
                    stacksToAdd.put(result, stacks);
                }
            }

            for (ItemStack s : stacksToRemove)
            {
                itemSources.remove(s);
            }

            itemSources.putAll(stacksToAdd);
        }

        @Nonnull
        public ItemStack applyExistingRecipes(@Nonnull ItemStack output, @Nonnull List<ItemStack> items, @Nonnull List<ItemStack> applied)
        {
            ItemStack result = output.copy();
            int numProduced = output.stackSize;
            int totalMult = 1;

            for (ItemStack is : items)
            {
                Map.Entry<ItemStack, List<ItemStack>> found = null;

                for (Map.Entry<ItemStack, List<ItemStack>> entry : itemSources.entrySet())
                {
                    if (OreDictionary.itemMatches(is, entry.getKey(), true))
                    {
                        List<ItemStack> ss = entry.getValue();

                        if (ss.size() == 1)
                        {
                            if (ss.get(0).stackSize < entry.getKey().stackSize)
                            {
                                continue;
                            }
                        }

                        found = entry;
                        break;
                    }
                }

                if (found != null)
                {
                    int numNeeded = is.stackSize;
                    int num = Utils.lcm(numNeeded, numProduced);

                    int mult = num / numNeeded;

                    result.stackSize *= mult;
                    for (ItemStack t : applied)
                    {
                        t.stackSize *= mult;
                    }

                    totalMult *= mult;

                    int mult2 = num / numProduced;
                    for (ItemStack t : found.getValue())
                    {
                        ItemStack q = t.copy();
                        q.stackSize *= mult2;
                        addCompacting(applied, q);
                    }
                }
                else
                {
                    ItemStack q = is.copy();
                    q.stackSize *= totalMult;
                    addCompacting(applied, q);
                }
            }

            if (result.stackSize > 1)
            {
                int cd = result.stackSize;
                for (ItemStack is : applied)
                {
                    cd = Utils.gcd(cd, is.stackSize);
                }

                if (cd > 1)
                {
                    for (ItemStack is : applied)
                    {
                        is.stackSize /= cd;
                    }
                    result.stackSize /= cd;
                }
            }

            return result;
        }

        @Nonnull
        public List<ItemStack> reduceItemsList(@Nonnull List<ItemStack> items)
        {
            List<ItemStack> itemsResolved = Lists.newArrayList();

            for (ItemStack is : items)
            {
                if (is == null)
                    continue;

                addCompacting(itemsResolved, is);
            }

            return itemsResolved;
        }

        private void addCompacting(@Nonnull List<ItemStack> aggregate, @Nonnull ItemStack input)
        {
            boolean found = false;

            for (ItemStack k : aggregate)
            {
                if (OreDictionary.itemMatches(input, k, false))
                {
                    if (k != input)
                    {
                        k.stackSize += input.stackSize;
                    }
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                aggregate.add(input.copy());
            }
        }
    }
}