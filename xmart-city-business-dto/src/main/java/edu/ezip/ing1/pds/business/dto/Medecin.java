package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "medecin")
public class Medecin {
    private int idMedecin;
    private String nomMedecin;
    private String prenomMedecin;
    private String specialite;
    private String numTel;

    public Medecin() {
    }

    public Medecin(int idMedecin, String nomMedecin, String prenomMedecin, String specialite, String numTel) {
        this.idMedecin = idMedecin;
        this.nomMedecin = nomMedecin;
        this.prenomMedecin = prenomMedecin;
        this.specialite = specialite;
        this.numTel = numTel;
    }

    @JsonProperty("id_medecin")
    public int getIdMedecin() {
        return idMedecin;
    }

    public void setIdMedecin(int idMedecin) {
        this.idMedecin = idMedecin;
    }

    @JsonProperty("nom_medecin")
    public String getNomMedecin() {
        return nomMedecin;
    }

    public void setNomMedecin(String nomMedecin) {
        this.nomMedecin = nomMedecin;
    }

    @JsonProperty("prenom_medecin")
    public String getPrenomMedecin() {
        return prenomMedecin;
    }

    public void setPrenomMedecin(String prenomMedecin) {
        this.prenomMedecin = prenomMedecin;
    }

    @JsonProperty("id_specialite")
    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    @JsonProperty("num_tel")
    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }
}
