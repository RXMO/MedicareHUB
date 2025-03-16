package edu.ezip.ing1.pds.business.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("Medicaments")
public class Medicaments {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("medicaments")
    private Set<Medicament> medicaments;

    // Constructeur pour garantir l'initialisation de la collection
    public Medicaments() {
        this.medicaments = new LinkedHashSet<>();
    }

    // Getter
    public Set<Medicament> getMedicaments() {
        return medicaments;
    }

    // Setter
    public void setMedicaments(Set<Medicament> medicaments) {
        if (medicaments != null) {
            this.medicaments = medicaments;
        } else {
            this.medicaments = new LinkedHashSet<>(); // Initialisation par défaut si null
        }
    }

    // Méthode pour ajouter un médicament
    public Medicaments add(Medicament medicament) {
        if (medicament != null) {
            medicaments.add(medicament);
        }
        return this;
    }

    


    @Override
    public String toString() {
        return "Medicaments{" +
                "medicaments=" + medicaments +
                '}';
    }
}
