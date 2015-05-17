package subsistence.common.config;

import com.google.gson.Gson;
import subsistence.common.lib.SubsistenceLogger;
import subsistence.common.lib.tool.ToolDefinition;
import subsistence.common.recipe.SubsistenceRecipes;
import subsistence.common.recipe.core.RecipeParser;
import subsistence.common.util.StackHelper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author lclc98
 */
public class TableConfig {


    public static class ParsedRecipe {
        public Recipe[] recipes;
        public Perishable[] perishable;
    }

    public static class Perishable {
        public String item;
        public int duration;
    }

    public static class Recipe {
        public String input;
        public String output;
        public float durability;
        public float duration;
        public String type = "both";
    }


    public static void parseFile(File file, String type) {
        try {
            SubsistenceLogger.info("Parsing " + file.getName());
            ParsedRecipe recipe = new Gson().fromJson(new FileReader(file), ParsedRecipe.class);
            verifyParse(file.getName(), recipe, type);
        } catch (IOException ex) {
            SubsistenceLogger.warn("Failed to parse " + file.getName());
        }
    }

    public static void makeNewFiles () {
        //TODO: make new files
    }

    public static void verifyParse(String name, ParsedRecipe recipe, String type) {
        for (Recipe recipe1 : recipe.recipes) {
            Object input = RecipeParser.getItem(recipe1.input);
            Object output = RecipeParser.getItem(recipe1.output);

            boolean hammerMill = recipe1.type.equalsIgnoreCase("mill") || recipe1.type.equalsIgnoreCase("both");
            boolean table = recipe1.type.equalsIgnoreCase("table") || recipe1.type.equalsIgnoreCase("both");

            if (input == null) {
                throw new NullPointerException(recipe1.input + " is not a valid item!");
            } else if (output == null) {
                throw new NullPointerException(recipe1.output + " is not a valid item!");
            } else if (!table && !hammerMill) {
                throw new NullPointerException("Please specify table or mill");
            }


            if (type.equals("hammer"))
                SubsistenceRecipes.TABLE.registerHammerRecipe(input, output, recipe1.durability, recipe1.duration, table, hammerMill);
            else if (type.equals("drying")) {
                SubsistenceRecipes.TABLE.registerDryingRecipe(input, output, recipe1.duration);
                if (recipe.perishable != null)
                    for (Perishable p : recipe.perishable)
                        SubsistenceRecipes.PERISHABLE.put(StackHelper.convert(RecipeParser.getItem(p.item))[0].getItem(), p.duration);
            } else if (type.equals("axe"))
                SubsistenceRecipes.TABLE.registerRecipe(input, output, ToolDefinition.AXE, recipe1.durability, recipe1.duration, table, hammerMill);
        }

        int length = recipe.recipes.length;
        SubsistenceLogger.info("Parsed " + name + ". Loaded " + length + (length > 1 ? " recipes" : " recipe"));
    }
}