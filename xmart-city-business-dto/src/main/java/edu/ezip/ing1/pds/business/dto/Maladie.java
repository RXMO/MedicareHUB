package edu.ezip.ing1.pds.business.dto;

import java.util.List;

public class Maladie {
    private int id_maladie;
    private String nom_maladie;
    private List<Symptomes> symptomes;

    public Maladie(int id, String nom, List<Symptomes> symptomes) {
        this.id_maladie = id;
        this.nom_maladie = nom;
        this.symptomes = symptomes;
    }

    public int getId() { return id_maladie; }
    public void setId(int id) { 
        this.id_maladie = id; 
    }
    public String getNom() { return nom_maladie; }
    public List<Symptomes> getSymptomes() { return symptomes; }

    @Override
    public String toString() {
        return nom_maladie;
    }
}