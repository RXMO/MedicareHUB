package edu.ezip.ing1.pds.business.dto;

public class Symptome {
    private int id;
    private String nom;

    public Symptome(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public int getId() { return id; }
    public void setId(int id) { 
        this.id = id; 
    }
    public String getNom() { return nom; }

    @Override
    public String toString() {
        return nom;
    }
}
