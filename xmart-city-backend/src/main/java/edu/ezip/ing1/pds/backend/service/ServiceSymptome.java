package edu.ezip.ing1.pds.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.ezip.ing1.pds.business.dto.Symptome;
import edu.ezip.ing1.pds.commons.database.GestionBD;

public class ServiceSymptome {

    public static void ajouterSymptome(Symptome symptome) {
        try (Connection conn = GestionBD.obtenirConnexion()) {
            String requete = "INSERT INTO symptomes (nom) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, symptome.getNom());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                symptome.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Symptome> obtenirTousLesSymptomes() {
        List<Symptome> symptomes = new ArrayList<>();
        try (Connection conn = GestionBD.obtenirConnexion()) {
            String requete = "SELECT id, nom FROM symptomes";
            PreparedStatement stmt = conn.prepareStatement(requete);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                symptomes.add(new Symptome(rs.getInt("id"), rs.getString("nom")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return symptomes;
    }

    public static void mettreAJourSymptome(Symptome symptome) {
        try (Connection conn = GestionBD.obtenirConnexion()) {
            String requete = "UPDATE symptomes SET nom = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(requete);
            stmt.setString(1, symptome.getNom());
            stmt.setInt(2, symptome.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void supprimerSymptome(int id) {
        try (Connection conn = GestionBD.obtenirConnexion()) {
            String requete = "DELETE FROM symptomes WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(requete);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}