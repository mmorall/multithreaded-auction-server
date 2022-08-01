package controller;

import model.Bid;
import model.DB;

import java.sql.*;
import java.util.ArrayList;

public class BidController {

    public static void setBid(Bid bid) {
        String sqlString = "INSERT INTO pujar (fecha_y_hora, id_usuario, id_articulo, fecha_inicio, fecha_fin, cantidad_pujada) VALUES (?,?,?,?,?,?)";
        try {
            DB.sem.acquire();
            Connection con = DB.getConnection();
            PreparedStatement statement = con.prepareStatement(sqlString);
            statement.setTimestamp(1, bid.getDate());
            statement.setInt(2, bid.getUserId());
            statement.setInt(3, bid.getItemId());
            statement.setTimestamp(4, bid.getStartDate());
            statement.setTimestamp(5, bid.getEndDate());
            statement.setDouble(6, bid.getBidAmount());
            statement.execute();
            con.close();
            DB.sem.release();
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Bid> getBidsByBidder(int bidderId) {
        ArrayList<Bid> bids = null;
        String sqlString = "SELECT * from pujar WHERE id_usuario=?";
        try {
            DB.sem.acquire();
            Connection con = DB.getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement(sqlString);
            statement.setInt(1,  bidderId);
            ResultSet rs = statement.executeQuery();
            bids = new ArrayList<>();
            while (rs.next()) {
                Bid bid = getBid(rs);
                bids.add(bid);
            }
            con.close();
            DB.sem.release();
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return bids;
    }

    public static int getMaxBid(int itemId, Timestamp starDate, Timestamp endDate) {
        int maxBid = 0;
        try {
            DB.sem.acquire();
            Connection con = DB.getConnection();
            String slqQuery = "SELECT MAX(cantidad_pujada) AS maxi FROM pujar WHERE id_articulo=? AND fecha_inicio=? AND fecha_fin=?";
            PreparedStatement statement = con.prepareStatement(slqQuery);
            statement.setInt(1, itemId);
            statement.setTimestamp(2, starDate);
            statement.setTimestamp(3, endDate);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) maxBid = rs.getInt("maxi");
            con.close();
            DB.sem.release();
        } catch (InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
        return maxBid;
    }

    private static Bid getBid(ResultSet rs) throws SQLException {
        Bid bid = new Bid();
        bid.setDate(rs.getTimestamp("fecha_y_hora"));
        bid.setUserId(rs.getInt("id_usuario"));
        bid.setItemId(rs.getInt("id_articulo"));
        bid.setStartDate(rs.getTimestamp("fecha_inicio"));
        bid.setEndDate(rs.getTimestamp("fecha_fin"));
        bid.setBidAmount(rs.getDouble("cantidad_pujada"));
        return bid;
    }
}
