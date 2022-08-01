package controller;

import model.Auction;
import model.DB;

import java.sql.*;
import java.util.ArrayList;

public class AuctionController {

    public static ArrayList<Auction> getAuctionsByStatus(Auction.Status status) {
        ArrayList<Auction> auctions = null;
        String sqlString = "SELECT * from subastar";
        try {
            DB.sem.acquire();
            Connection con = DB.getConnection();
            PreparedStatement statement;
            if (status != Auction.Status.ALL) {
                sqlString += " WHERE estado=?";
                statement = con.prepareStatement(sqlString);
                statement.setString(1,  statusToEstado(status));
            } else {
                statement = con.prepareStatement(sqlString);
            }
            ResultSet rs = statement.executeQuery();
            auctions = new ArrayList<>();
            while (rs.next()) {
                Auction auction = getAuction(rs);
                auctions.add(auction);
            }
            con.close();
            DB.sem.release();
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return auctions;
    }

    public static Auction getAuctionByIdDate(int idItem, Timestamp startDate, Timestamp endDate) {
        Auction auction = null;
        try {
            DB.sem.acquire();
            Connection con = DB.getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * from subastar WHERE id_articulo=? AND fecha_inicio=? AND fecha_fin=?");
            statement.setInt(1, idItem);
            statement.setTimestamp(2, startDate);
            statement.setTimestamp(3, endDate);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) auction = getAuction(rs);
            con.close();
            DB.sem.release();
        } catch (InterruptedException | SQLException e) {
            throw new RuntimeException(e);
        }
        return auction;
    }
    private static Auction getAuction(ResultSet rs) throws SQLException {
        Auction auction = new Auction();
        auction.setUserId(rs.getInt("id_usuario"));
        auction.setItemId(rs.getInt("id_articulo"));
        auction.setStartDate(rs.getTimestamp("fecha_inicio"));
        auction.setEndDate(rs.getTimestamp("fecha_fin"));
        auction.setStatus(estadoToStatus(rs.getString("estado")));
        auction.setStartingPrice(rs.getInt("precio_salida"));
        return auction;
    }
    public static Auction.Status estadoToStatus(String value) {
        Auction.Status status;
        switch (value) {
            case "creada" -> status = Auction.Status.CREATED;
            case "abierta" -> status = Auction.Status.OPEN;
            case "cerrada por compra" -> status = Auction.Status.CLOSEDBYBUY;
            case "cerrada por eliminación" -> status = Auction.Status.CLOSEDBYDROP;
            case "'cerrada por tiempo" -> status = Auction.Status.CLOSEDBYTIME;
            default -> status = Auction.Status.ALL;
        }
        return status;
    }

    public static String statusToEstado(Auction.Status status) {
        String estado = null;
        switch (status) {
            case CREATED -> estado = "creada";
            case OPEN -> estado = "abierta";
            case CLOSEDBYBUY -> estado = "cerrada por compra";
            case CLOSEDBYDROP -> estado = "cerrada por eliminación";
            case CLOSEDBYTIME -> estado = "cerrada por tiempo'";
        }
        return estado;
    }

}
