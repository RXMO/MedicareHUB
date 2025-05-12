package edu.ezip.ing1.pds.business.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DiagnosticResult implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String nomMaladie;
    private double score;
    private int symptomesCorrespondants;
    private int totalSymptomesMaladie;
    private int totalSymptomesPatient;
    private String specialite;
    private int idSpecialite;
    private double tauxCouverture;
    private double tauxCorrespondance;
    private List<Map<String, Object>> creneauxDisponibles; 
    private int id_maladie; 
    public DiagnosticResult() {}

    public DiagnosticResult(String nomMaladie, double score, int symptomesCorrespondants, 
                            int totalSymptomesMaladie, int totalSymptomesPatient,
                            String specialite, int idSpecialite, double tauxCouverture, 
                            double tauxCorrespondance) {
        this.nomMaladie = nomMaladie;
        this.score = score;
        this.symptomesCorrespondants = symptomesCorrespondants;
        this.totalSymptomesMaladie = totalSymptomesMaladie;
        this.totalSymptomesPatient = totalSymptomesPatient;
        this.specialite = specialite;
        this.idSpecialite = idSpecialite;
        this.tauxCouverture = tauxCouverture;
        this.tauxCorrespondance = tauxCorrespondance;
        this.creneauxDisponibles = null; 
    }

    //récupère le nom de la maladie
    public String getNomMaladie() { return nomMaladie; }
    public void setNomMaladie(String nomMaladie) { this.nomMaladie = nomMaladie; }
    //récupère le score du diagnostic
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    //récupère le nombre de symptômes qui correspondent
    public int getSymptomesCorrespondants() { return symptomesCorrespondants; }
    public void setSymptomesCorrespondants(int symptomesCorrespondants) { this.symptomesCorrespondants = symptomesCorrespondants; }
    //récupère le total des symptômes de la maladie
    public int getTotalSymptomesMaladie() { return totalSymptomesMaladie; }
    public void setTotalSymptomesMaladie(int totalSymptomesMaladie) { this.totalSymptomesMaladie = totalSymptomesMaladie; }
    //récupère le total des symptômes du patient
    public int getTotalSymptomesPatient() { return totalSymptomesPatient; }
    public void setTotalSymptomesPatient(int totalSymptomesPatient) { this.totalSymptomesPatient = totalSymptomesPatient; }
    // récupère la spécialité liée à la maladie
    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }
    //récupère l'ID de la spécialité
    public int getIdSpecialite() { return idSpecialite; }
    public void setIdSpecialite(int idSpecialite) { this.idSpecialite = idSpecialite; }
    //récupère le taux de couverture
    public double getTauxCouverture() { return tauxCouverture; }
    public void setTauxCouverture(double tauxCouverture) { this.tauxCouverture = tauxCouverture; }
    //récupère le taux de correspondance
    public double getTauxCorrespondance() { return tauxCorrespondance; }
    public void setTauxCorrespondance(double tauxCorrespondance) { this.tauxCorrespondance = tauxCorrespondance; }
    //récupère les créneaux disponibles pour un rendez-vous
    public List<Map<String, Object>> getCreneauxDisponibles() { return creneauxDisponibles; }
    public void setCreneauxDisponibles(List<Map<String, Object>> creneauxDisponibles) { this.creneauxDisponibles = creneauxDisponibles; }
    //récupère l'ID de la maladie
    public int getId_maladie() { return id_maladie; }
    public void setId_maladie(int id_maladie) { this.id_maladie = id_maladie; }

    //afficher le diagnostic sous forme de texte
    @Override
    public String toString() {
        return String.format("%s (Probabilité: %.1f%%, Symptômes: %d/%d, Spécialité: %s)", 
                nomMaladie, score, symptomesCorrespondants, totalSymptomesMaladie, specialite);
    }
}