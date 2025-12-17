import java.util.Objects;

public class Ingredient {
    private int id;
    private String name;
    private Double price;
    private CategoryEnum category;
    private Dish dish;

    public Ingredient() {
    }

    public Ingredient(CategoryEnum category, Dish dish, int id, String name, Double price) {
        this.category = category;
        this.dish = dish;
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return getId() == that.getId() && Objects.equals(getName(), that.getName()) && Objects.equals(getPrice(), that.getPrice()) && getCategory() == that.getCategory() && Objects.equals(getDish(), that.getDish());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getPrice(), getCategory(), getDish());
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "category=" + category +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", dish=" + dish +
                '}';
    }
}
