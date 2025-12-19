import java.util.List;
import java.util.Objects;

public class Dish {
    private int id;
    private String name;
    private DishTypeEnum dishType;
    private List<Ingredient> ingredients;

    public Dish() {
    }

    public Dish(DishTypeEnum dishType, int id, List<Ingredient> ingredients, String name) {
        this.dishType = dishType;
        this.id = id;
        this.ingredients = ingredients;
        this.name = name;
    }

    public Double getDishCost(){
        return ingredients == null ? null :
                ingredients.stream()
                .mapToDouble(Ingredient::getPrice)
                .sum();
    }

    public DishTypeEnum getDishType() {
        return dishType;
    }

    public void setDishType(DishTypeEnum dishType) {
        this.dishType = dishType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return getId() == dish.getId() && Objects.equals(getName(), dish.getName()) && getDishType() == dish.getDishType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDishType());
    }

    @Override
    public String toString() {
        return "Dish{" +
                "dishType=" + dishType +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", ingredients=" + ingredients +
                '}';
    }
}
