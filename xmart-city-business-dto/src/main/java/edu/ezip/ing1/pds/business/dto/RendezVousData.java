package edu.ezip.ing1.pds.business.dto;

public class RendezVousData {
    // ATTRIBUTS DE RENDEZ-VOUS
    private int idPatient;
    private String dateRendezVous;
    private int idSpecialite;
    private int idDisponibilite;
    private int idMedecin;

    public RendezVousData(int idPatient, String dateRendezVous, int idSpecialite, int idDisponibilite, int idMedecin) {
        this.idPatient = idPatient;
        this.dateRendezVous = dateRendezVous;
        this.idSpecialite = idSpecialite;
        this.idDisponibilite = idDisponibilite;
        this.idMedecin = idMedecin;
    }

    // GETTERS ET SETTERS
    public int getIdPatient() {
        return idPatient;
    }

    public void setIdPatient(int idPatient) {
        this.idPatient = idPatient;
    }

    public String getDateRendezVous() {
        return dateRendezVous;
    }

    public void setDateRendezVous(String dateRendezVous) {
        this.dateRendezVous = dateRendezVous;
    }

    public int getIdSpecialite() {
        return idSpecialite;
    }

    public void setIdSpecialite(int idSpecialite) {
        this.idSpecialite = idSpecialite;
    }

    public int getIdDisponibilite() {
        return idDisponibilite;
    }

    public void setIdDisponibilite(int idDisponibilite) {
        this.idDisponibilite = idDisponibilite;
    }

    public int getIdMedecin() {
        return idMedecin;
    }

    public void setIdMedecin(int idMedecin) {
        this.idMedecin = idMedecin;
    }
}