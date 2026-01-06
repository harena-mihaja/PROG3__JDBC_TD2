import model.CategoryEnum;
import model.Dish;
import model.DishTypeEnum;
import model.Ingredient;
import repository.DataRetriever;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        DataRetriever dao = new DataRetriever();
        System.out.println("--- findDishByID() ---");
        System.out.println(dao.findDishById(1));
        System.out.println(dao.findDishById(999));
        System.out.println("--- findIngredients() ---");
        System.out.println(dao.findIngredients(2, 2));
        System.out.println(dao.findIngredients(3, 5));
        System.out.println("--- createIngredients() ---");
        Ingredient fromage = new Ingredient("Fromage", 1200D, CategoryEnum.DAIRY );
        Ingredient oignon = new Ingredient("Oignon", 500D, CategoryEnum.VEGETABLE );
        Ingredient laitue = new Ingredient("Laitue", 2000D, CategoryEnum.VEGETABLE );
        Ingredient carotte = new Ingredient("Carotte", 500D, CategoryEnum.VEGETABLE );
        List<Ingredient> ingredientList1 = List.of(fromage, oignon);
        List<Ingredient> ingredientList2 = List.of(carotte, laitue);
        System.out.println(dao.createIngredients(ingredientList1));
        System.out.println(dao.createIngredients(ingredientList2));
        System.out.println("--- saveDish() ---");
        Ingredient fromageWithId = new Ingredient(6, "Fromage", 1200D, CategoryEnum.DAIRY );
        Ingredient oignonWithId = new Ingredient(7, "Oignon", 500D, CategoryEnum.VEGETABLE);
        Ingredient laitueWithId = new Ingredient(1,"Laitue", 800D, CategoryEnum.VEGETABLE );
        Ingredient tomateWithId = new Ingredient(2, "Tomate", 600D, CategoryEnum.VEGETABLE);
        Dish soupeLegumes = new Dish("Soupe de légumes", DishTypeEnum.START, new ArrayList<>(List.of(oignonWithId)));
        System.out.println(dao.saveDish(soupeLegumes));
        Dish saladeFraiche = new Dish(1, "Salade fraiche", DishTypeEnum.START, new ArrayList<>(List.of(oignonWithId, laitueWithId, fromageWithId, tomateWithId)));
        System.out.println(dao.saveDish(saladeFraiche));
        Dish saladeFromage = new Dish(1, "Salade fraiche", DishTypeEnum.START, new ArrayList<>(List.of( fromageWithId)));
        System.out.println(dao.saveDish(saladeFromage));
        System.out.println("---findDishsByIngredientName()---");
        System.out.println(dao.findDishesByIngredientName("eur"));
        System.out.println("--- findIngredientsByCriteria() ---");
        System.out.println(dao.findIngredientsByCriteria(null, CategoryEnum.VEGETABLE, null, 1, 10));
        System.out.println(dao.findIngredientsByCriteria("cho", null, "Sal", 1, 10));
        System.out.println(dao.findIngredientsByCriteria("cho", null, "gâteau", 1, 10));
    }
}
