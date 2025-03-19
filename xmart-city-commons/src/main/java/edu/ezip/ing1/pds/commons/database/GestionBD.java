package edu.ezip.ing1.pds.commons.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GestionBD {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/pds";
    private static final String UTILISATEUR = "root";
    private static final String MOT_DE_PASSE = "";

    public static Connection obtenirConnexion() throws SQLException {
        return DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE);
    }
}