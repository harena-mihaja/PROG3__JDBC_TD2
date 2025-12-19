public class Main {

    public static void main(String[] args) {
        DataRetriever dao = new DataRetriever();

        System.out.println(dao.findDishById(1).getDishCost());
        System.out.println(dao.findDishById(999));
    }
}
