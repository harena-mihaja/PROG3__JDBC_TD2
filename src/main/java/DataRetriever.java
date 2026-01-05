import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {
    private final DBConnection dbConnection = new DBConnection();

    public Dish findDishById(Integer id) {
        String dishQuery = "SELECT id, name, dish_type FROM dish d WHERE d.id = ?;";
        String ingredientsQuery = "SELECT id, name, price, category FROM ingredient i WHERE id_dish = ?;";
        Dish dish = new Dish();
        List<Ingredient> ingredients = new ArrayList<>();
        try (Connection conn = dbConnection.getDBConnection()) {
            PreparedStatement ps1 = conn.prepareStatement(dishQuery);
            PreparedStatement ps2 = conn.prepareStatement(ingredientsQuery);
            ps1.setInt(1, id);
            ps2.setInt(1, id);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                dish.setId(rs1.getInt(1));
                dish.setName(rs1.getString(2));
                dish.setDishType(DishTypeEnum.valueOf(rs1.getString(3)));
            }else{
                throw new RuntimeException("Dish with id: " + id + ", not found");
            }
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs2.getInt(1));
                ingredient.setName(rs2.getString(2));
                ingredient.setPrice(rs2.getDouble(3));
                ingredient.setCategory(CategoryEnum.valueOf(rs2.getString(4)));
                ingredients.add(ingredient);
            }
            dish.setIngredients(ingredients);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dish;
    }

    public List<Ingredient> findIngredients(int page, int size) {
        List<Ingredient> ingredientList = new ArrayList<>();
        int offset = (page - 1) * size;
        String sql = """
                        SELECT i.id, i.name, i.price, i.category, d.id FROM ingredient i
                        LEFT JOIN dish d ON d.id = i.id_dish
                        LIMIT ? OFFSET ?
                     """;
        try (Connection conn = dbConnection.getDBConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, size);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt(1));
                ingredient.setName(rs.getString(2));
                ingredient.setPrice(rs.getDouble(3));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString(4)));
                ingredient.setDish(findDishById(rs.getInt(5)));
                ingredientList.add(ingredient);
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
        return  ingredientList;
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Dish saveDish(Dish dishToSave) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<Dish> findDishesByIngredientName(String IngredientName) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
