package edu.ezip.ing1.pds.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.ezip.ing1.pds.business.dto.Maladie;
import edu.ezip.ing1.pds.commons.database.GestionBD;

public class ServiceMaladie {

    public static void ajouterMaladie(Maladie maladie) {
        try (Connection conn = GestionBD.obtenirConnexion()) {
            String requete = "INSERT INTO maladies (nom) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, maladie.getNom());
            stmt.executeUpdate();

    
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                maladie.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Maladie> obtenirToutesLesMaladies() {
        List<Maladie> maladies = new ArrayList<>();
        try (Connection conn = GestionBD.obtenirConnexion()) {
            String requete = "SELECT id, nom FROM maladies";
            PreparedStatement stmt = conn.prepareStatement(requete);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                maladies.add(new Maladie(rs.getInt("id"), rs.getString("nom"), new ArrayList<>()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maladies;
    }

    public static void mettreAJourMaladie(Maladie maladie) {
        try (Connection conn = GestionBD.obtenirConnexion()) {
            String requete = "UPDATE maladies SET nom = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(requete);
            stmt.setString(1, maladie.getNom());
            stmt.setInt(2, maladie.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void supprimerMaladie(int id) {
        try (Connection conn = GestionBD.obtenirConnexion()) {
            String requete = "DELETE FROM maladies WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(requete);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}