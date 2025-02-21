package edu.ezip.ing1.pds.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.ezip.ing1.pds.commons.database.GestionBD;

public class ServiceDiagnostic {
public static boolean ajouterSymptome(int idPatient, String nomSymptome) {
    
    try (Connection conn = GestionBD.obtenirConnexion()) {
  String checkQuery = "SELECT id_symptome FROM symptomes WHERE description = ?";
 PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
 checkStmt.setString(1, nomSymptome);
 ResultSet rs = checkStmt.executeQuery();
int idSymptome;

if (rs.next()) {
 idSymptome = rs.getInt("id_symptome");
    } else {
   String insertQuery = "INSERT INTO symptomes (description) VALUES (?)";
   PreparedStatement insertStmt = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
 insertStmt.setString(1, nomSymptome);
                
 int rowsInserted = insertStmt.executeUpdate();
 if (rowsInserted == 0) {
                    return false;
         }
    
         ResultSet generatedKeys = insertStmt.getGeneratedKeys();
         if (generatedKeys.next()) {
                    idSymptome = generatedKeys.getInt(1);
                } else {
                    return false;
                }
            }
    
          String checkPatientSymptomeQuery = "SELECT * FROM symptomes_patients WHERE id_patient = ? AND id_symptome = ?";
            PreparedStatement checkPatientStmt = conn.prepareStatement(checkPatientSymptomeQuery);
             checkPatientStmt.setInt(1, idPatient);
             checkPatientStmt.setInt(2, idSymptome);
               ResultSet rsPatient = checkPatientStmt.executeQuery();
    
            if (rsPatient.next()) {
                throw new IllegalStateException(" Vous avez déjà saisi ce symptôme.");
            }
    
            String insertPatientSymptomeQuery = "INSERT INTO symptomes_patients (id_patient, id_symptome) VALUES (?, ?)";
            PreparedStatement insertPatientStmt = conn.prepareStatement(insertPatientSymptomeQuery);
               insertPatientStmt.setInt(1, idPatient);
              insertPatientStmt.setInt(2, idSymptome);
               insertPatientStmt.executeUpdate();
    
            return true;
        } catch (SQLException e) {
            return false;
        } catch (IllegalStateException e) {
            return false;
        }
    }
         
    public static List<String> recupererSymptomes(int idPatient) {
        List<String> symptomes = new ArrayList<>();
           try (Connection conn = GestionBD.obtenirConnexion()) {
            String query = 
            "SELECT s.description FROM symptomes s " +
            "JOIN symptomes_patients sp ON s.id_symptome = sp.id_symptome " +
            "WHERE sp.id_patient = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, idPatient);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
                symptomes.add(rs.getString("description"));
            }
        } catch (SQLException e) {
        }
        return symptomes;
    }

    public static boolean modifierSymptome(int idPatient, String ancienNom, String nouveauNom) {
        try (Connection conn = GestionBD.obtenirConnexion()) {
            String checkQuery = "SELECT id_symptome FROM symptomes WHERE description = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             checkStmt.setString(1, nouveauNom);
                ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return false;
            }

            String updateQuery = "UPDATE symptomes s " +
                                 "JOIN symptomes_patients sp ON s.id_symptome = sp.id_symptome " +
                                 "SET s.description = ? " +
                                 "WHERE sp.id_patient = ? AND s.description = ?";
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
              updateStmt.setString(1, nouveauNom);
            updateStmt.setInt(2, idPatient);
               updateStmt.setString(3, ancienNom);
            int rowsAffected = updateStmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            return false;
        }
    }

     public static boolean supprimerSymptome(int idPatient, String nomSymptome) {
   try (Connection conn = GestionBD.obtenirConnexion()) {
  String getIdQuery = "SELECT id_symptome FROM symptomes WHERE description = ?";
 PreparedStatement getIdStmt = conn.prepareStatement(getIdQuery);
  getIdStmt.setString(1, nomSymptome);
  ResultSet rs = getIdStmt.executeQuery();

if (!rs.next()) {
return false;
}

int idSymptome = rs.getInt("id_symptome");

String deletePatientQuery = "DELETE FROM symptomes_patients WHERE id_patient = ? AND id_symptome = ?";
 PreparedStatement deletePatientStmt = conn.prepareStatement(deletePatientQuery);
    deletePatientStmt.setInt(1, idPatient);
      deletePatientStmt.setInt(2, idSymptome);
    int rowsAffected = deletePatientStmt.executeUpdate();

return rowsAffected > 0;
} catch (SQLException e) {
return false;
}
}

public static List<String> diagnostiquer(int idPatient) {
List<String> maladiesPossibles = new ArrayList<>();
   try (Connection conn = GestionBD.obtenirConnexion()) {
   String query = "SELECT DISTINCT m.nom_maladie " +
"FROM maladies m " +
"JOIN symp_maladie sm ON m.id_maladie = sm.id_maladie " +
"JOIN symptomes s ON sm.id_symptome = s.id_symptome " +
"JOIN symptomes_patients sp ON s.id_symptome = sp.id_symptome " +
"WHERE sp.id_patient = ?";
    
PreparedStatement stmt = conn.prepareStatement(query);
  stmt.setInt(1, idPatient);
    ResultSet rs = stmt.executeQuery();
    
while (rs.next()) {
maladiesPossibles.add(rs.getString("nom_maladie"));
}
} catch (SQLException e) {
}
return maladiesPossibles;
}
}
