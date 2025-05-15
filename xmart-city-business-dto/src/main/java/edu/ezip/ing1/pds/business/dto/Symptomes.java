package edu.ezip.ing1.pds.business.dto;

public class Symptomes {
    private int id_symptome;
    private String description; 

    public Symptomes() {
    }

    public Symptomes(int id, String description) {
        this.id_symptome = id;
        this.description = description;
    }

    public int getId() { return id_symptome; }
    public void setId(int id) { this.id_symptome = id; }

    public String getDescription() { return description; } 
    public void setDescription(String description) { this.description = description; } 

    @Override
    public String toString() {
        return description;
    }
}