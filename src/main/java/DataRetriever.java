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
        Connection conn = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        try {
            conn = dbConnection.getDBConnection();
            ps1 = conn.prepareStatement(dishQuery);
            ps2 = conn.prepareStatement(ingredientsQuery);
            ps1.setInt(1, id);
            ps2.setInt(1, id);
            rs1 = ps1.executeQuery();
            if (rs1.next()) {
                dish.setId(rs1.getInt(1));
                dish.setName(rs1.getString(2));
                dish.setDishType(DishTypeEnum.valueOf(rs1.getString(3)));
            } else {
                throw new RuntimeException("Dish with id: " + id + " not found");
            }
            rs2 = ps2.executeQuery();
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
        } finally {
            if (conn != null) dbConnection.closeConnection(rs1, rs2, ps1, ps2, conn);
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
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dbConnection.getDBConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, size);
            ps.setInt(2, offset);
            rs = ps.executeQuery();
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
        } finally {
            if (conn != null) dbConnection.closeConnection(rs, ps, conn);
        }
        return ingredientList;
    }

    private List<String> findAllIngredientsName() {
        List<String> ingredientsName = new ArrayList<>();
        String sql = """
                SELECT name FROM ingredient;
                """;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dbConnection.getDBConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                ingredientsName.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) dbConnection.closeConnection(rs, ps, conn);
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
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dbConnection.getDBConnection();
            conn.setAutoCommit(false);
            for (Ingredient ingredient : singleIngredientsList) {
                if (storedIngredients.contains(ingredient.getName())) {
                    conn.rollback();
                    throw new RuntimeException("The ingredient named \"" + ingredient.getName() + "\" already exists. No other ingredients have been added.");
                }
                ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) ingredient.setId(rs.getInt(1));
                }
                createdIngredients.add(ingredient);
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) dbConnection.closeConnection(rs, ps, conn);
        }
        return createdIngredients;
    }

    public Dish saveDish(Dish dishToSave) {
        String insertDishSql =
                """
                        INSERT INTO dish(name, dish_type) VALUES (?, ?::dish_type);
                        """;
        String updateDishSql =
                """
                        UPDATE dish SET name = ?, dish_type = ?::dish_type WHERE id = ?;
                        """;
        String associateIngredientSql = """
                UPDATE ingredient SET id_dish = ? WHERE id = ?;
                """;
        String dissociateIngredientSql = """
                UPDATE ingredient SET id_dish = null WHERE id = ?
                """;
        Connection conn = null;

        boolean inDB = dishToSave.getId() != 0 && findDishById(dishToSave.getId()) != null;
        if (inDB) {
            try {
                conn = dbConnection.getDBConnection();
                conn.setAutoCommit(false);
                Dish dishInDB = findDishById(dishToSave.getId());
                // Update dish
                if (dishInDB != dishToSave) {
                    updateDish(dishToSave, conn, updateDishSql);
                    // Update ingredients list
                    if (!(dishInDB.getIngredients().equals(dishToSave.getIngredients()))) {
                        // If dishInDB doesn't contain an ingredient in dishToSave, associate ingredient
                        for (Ingredient ingredient : dishToSave.getIngredients()) {
                            if (!(dishInDB.getIngredients().contains(ingredient))) {
                                associateIngredient(dishToSave, associateIngredientSql, conn, ingredient);
                            }
                        }
                        //If dishInDB contains an ingredient that is not in dishToSave ingredients, dissociate ingredient
                        for (Ingredient ingredient : dishInDB.getIngredients()) {
                            if (!(dishToSave.getIngredients().contains(ingredient))) {
                                dissociateIngredient(dishToSave, ingredient, conn, dissociateIngredientSql);
                            }
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                if (conn != null) dbConnection.closeConnection(conn);
            }

        } else {
            try {
                conn = dbConnection.getDBConnection();
                conn.setAutoCommit(false);
                PreparedStatement psDishInsert = conn.prepareStatement(insertDishSql, Statement.RETURN_GENERATED_KEYS);
                psDishInsert.setString(1, dishToSave.getName());
                psDishInsert.setString(2, dishToSave.getDishType().name());
                int i = psDishInsert.executeUpdate();
                if (i > 0) {
                    ResultSet generatedKeys = psDishInsert.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        dishToSave.setId(generatedKeys.getInt(1));
                    }
                }
                for (Ingredient ingredient : dishToSave.getIngredients()) {
                    associateIngredient(dishToSave, associateIngredientSql, conn, ingredient);
                }
                conn.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                if (conn != null) {
                    dbConnection.closeConnection(conn);
                }
            }
        }
        return findDishById(dishToSave.getId());
    }

    private static void updateDish(Dish dishToSave, Connection conn, String updateDishSql) throws SQLException {
        PreparedStatement updateDishPs = conn.prepareStatement(updateDishSql);
        updateDishPs.setString(1, dishToSave.getName());
        updateDishPs.setString(2, dishToSave.getDishType().name());
        updateDishPs.setInt(3, dishToSave.getId());
        int i = updateDishPs.executeUpdate();
        if (i <= 0) {
            conn.rollback();
            throw new RuntimeException("Error while updating dish \"" + dishToSave.getName() + "\"");
        }
    }

    private static void dissociateIngredient(Dish dishToSave, Ingredient ingredient, Connection conn, String dissociateIngredientSql) throws SQLException {
        PreparedStatement dissociateIngredientPs = conn.prepareStatement(dissociateIngredientSql);
        dissociateIngredientPs.setInt(1, ingredient.getId());
        int k = dissociateIngredientPs.executeUpdate();
        if (k <= 0) {
            conn.rollback();
            throw new RuntimeException("Error while dissociating ingredient \"" + ingredient.getName() + "\" to dish \"" + dishToSave.getName() + "\"");
        }
    }

    private void associateIngredient(Dish dishToSave, String associateIngredientSql, Connection conn, Ingredient ingredient) throws SQLException {
        PreparedStatement associateIng = conn.prepareStatement(associateIngredientSql);
        associateIng.setInt(1, dishToSave.getId());
        associateIng.setInt(2, ingredient.getId());
        int l = associateIng.executeUpdate();
        if (l <= 0) {
            conn.rollback();
            throw new RuntimeException("Error while associating ingredient \"" + ingredient.getName() + "\" to dish \"" + dishToSave.getName() + "\"");
        }
    }

    public List<Dish> findDishesByIngredientName(String IngredientName) {
        List<Dish> dishes = new ArrayList<>();
        String sql = """
                SELECT d.id FROM dish d JOIN ingredient i ON d.id = i.id_dish WHERE i.name ILIKE ?;
                """;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dbConnection.getDBConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + IngredientName + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                dishes.add(findDishById(rs.getInt(1)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) dbConnection.closeConnection(rs, ps, conn);
        }
        return dishes;
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) {
        List<Ingredient> ingredientList = new ArrayList<>();
        StringBuilder baseSql = new StringBuilder("SELECT i.id, i.name, i.price, i.category, i.id_dish, d.name FROM ingredient i LEFT JOIN dish d ON i.id_dish = d.id WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        if (ingredientName != null) {
            baseSql.append(" AND i.name ILIKE ?");
            parameters.add("%" + ingredientName + "%");
        }
        if (category != null) {
            baseSql.append(" AND i.category = ?::category");
            parameters.add(category.name());
        }
        if (dishName != null) {
            baseSql.append(" AND d.name ILIKE ?");
            parameters.add("%" + dishName + "%");
        }
        baseSql.append(" LIMIT ? OFFSET ?");
        parameters.add(size);
        int offset = (page - 1) * size;
        parameters.add(offset);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String finalSql = baseSql.toString();
            conn = dbConnection.getDBConnection();
            ps = conn.prepareStatement(finalSql);
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            System.out.println(ps);
            rs = ps.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt(1));
                ingredient.setName(rs.getString(2));
                ingredient.setPrice(rs.getDouble(3));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString(4)));
                if (rs.getInt(5) != 0) {
                    ingredient.setDish(findDishById(rs.getInt(5)));
                }
                ingredientList.add(ingredient);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) dbConnection.closeConnection(rs, ps, conn);
        }
        return ingredientList;
    }
}
