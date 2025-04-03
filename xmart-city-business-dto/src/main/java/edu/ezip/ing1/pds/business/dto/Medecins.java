package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.LinkedHashSet;
import java.util.Set;

@JsonRootName("Medecins")
public class Medecins {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("medecins")
    private Set<Medecin> medecins;

    // Constructeur pour garantir l'initialisation de la collection
    public Medecins() {
        this.medecins = new LinkedHashSet<>();
    }

    // Getter
    public Set<Medecin> getMedecins() {
        return medecins;
    }

    // Setter
    public void setMedecins(Set<Medecin> medecins) {
        if (medecins != null) {
            this.medecins = medecins;
        } else {
            this.medecins = new LinkedHashSet<>(); // Initialisation par défaut si null
        }
    }

    // Méthode pour ajouter un médecin
    public Medecins add(Medecin medecin) {
        if (medecin != null) {
            medecins.add(medecin);
        }
        return this;
    }

    @Override
    public String toString() {
        return "Medecins{" +
                "medecins=" + medecins +
                '}';
    }
}
