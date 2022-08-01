package controller;

import model.DB;
import model.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemController {

    public static Item getItemById(int id) {
        Item item = null;
        try {
            DB.sem.acquire();
            Connection con = DB.getConnection();
            String sqlString = "SELECT * from articulo WHERE id_articulo=?";
            PreparedStatement statement = con.prepareStatement(sqlString);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                item = new Item();
                item.setId(rs.getInt("id_articulo"));
                item.setName(rs.getString("nombre"));
                item.setDescription(rs.getString("descripcion"));
                item.setPicture(rs.getString("imagen"));
            }
            con.close();
            DB.sem.release();
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return item;
    }
}
