package org.unnamedgroup.restapi.resources;

import org.apache.commons.dbutils.DbUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.unnamedgroup.restapi.security.DBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CanaliDB {
    public static String getNome(int id) throws SQLException {

        String result = "";
        Connection dbConnection = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        dbConnection = DBManager.getDBConenction();
        pst = dbConnection.prepareStatement("SELECT * FROM canale WHERE id_canale = ? ");


        pst.setInt(1, id);

        rs = pst.executeQuery();

        if (rs.next()) {
           result = rs.getString("nome");;

        }
        dbConnection.close();
        return result;
    }


    public static int getNumberOfCanali() throws SQLException {

        Connection dbConnection = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        dbConnection = DBManager.getDBConenction();
        pst = dbConnection.prepareStatement("SELECT COUNT(*) AS numero_canali FROM canale");

        int numero_canali = 0;

        rs = pst.executeQuery();

        if (rs.next()) {
            numero_canali = rs.getInt("numero_canali");;

        }
        dbConnection.close();
        return numero_canali;
    }



}