import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
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
            } else {
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
        try (Connection conn = dbConnection.getDBConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, size);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt(1));
                ingredient.setName(rs.getString(2));
                ingredient.setPrice(rs.getDouble(3));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString(4)));
                ingredient.setDish(findDishById(rs.getInt(5)));
                ingredientList.add(ingredient);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ingredientList;
    }

    private List<String> findAllIngredientsName() {
        List<String> ingredientsName = new ArrayList<>();
        String sql = """
                SELECT name FROM ingredient;
                """;
        try {
            Connection conn = dbConnection.getDBConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ingredientsName.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ingredientsName;
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        List<Ingredient> createdIngredients = new ArrayList<>();
        List<Ingredient> singleIngredientsList = new HashSet<>(newIngredients).stream().toList();
        List<String> storedIngredients = findAllIngredientsName();
        String sql =
                """
                        INSERT INTO ingredient(name, price, category, id_dish) VALUES (? ,?, ?::category,?)
                        """;

        try (Connection conn = dbConnection.getDBConnection()) {
            conn.setAutoCommit(false);
            for (Ingredient ingredient : singleIngredientsList) {
                if (storedIngredients.contains(ingredient.getName())) {
                    conn.rollback();
                    throw new RuntimeException("The ingredient named \"" + ingredient.getName() + "\" already exists. No other ingredients have been added.");
                }
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, ingredient.getName());
                ps.setDouble(2, ingredient.getPrice());
                ps.setString(3, ingredient.getCategory().name());
                if (ingredient.getDish() != null) {
                    ps.setInt(4, ingredient.getDish().getId());
                } else {
                    ps.setNull(4, ingredient.getId());
                }
                int i = ps.executeUpdate();
                if (i > 0) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) ingredient.setId(rs.getInt(1));
                }
                createdIngredients.add(ingredient);
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return createdIngredients;
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
