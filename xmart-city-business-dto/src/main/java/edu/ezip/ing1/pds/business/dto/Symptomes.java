package edu.ezip.ing1.pds.business.dto;

public class Symptomes {
    private int id_symptome;
    private String description;

    public Symptomes() {
    }

    public Symptomes(int id, String nom) {
        this.id_symptome = id;
        this.description = nom;
    }

    public int getId() { return id_symptome; }
    public void setId(int id) { this.id_symptome = id; }

    public String getNom() { return description; }
    public void setNom(String nom) { this.description = nom; }

    @Override
    public String toString() {
        return description;
    }
}