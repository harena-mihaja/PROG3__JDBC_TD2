public class Main {

    public static void main(String[] args) {
        DataRetriever dao = new DataRetriever();
        System.out.println("--- findDishByID() ---");
//        System.out.println(dao.findDishById(1).getDishCost());
//        System.out.println(dao.findDishById(999));
        System.out.println("--- findIngredients() ---");
        System.out.println(dao.findIngredients(2, 2));
        System.out.println(dao.findIngredients(3, 5));
    }
}
