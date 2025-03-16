package edu.ezip.ing1.pds.business.dto;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("Ordonnances")
@JsonIgnoreProperties(ignoreUnknown = true)

public class Ordonnances {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("ordonnances")
    private Set<Ordonnance> ordonnances;

    // Constructeur pour garantir l'initialisation de la collection
    public Ordonnances() {
        this.ordonnances = new LinkedHashSet<>();
    }

    // Getter
    public Set<Ordonnance> getOrdonnances() {
        return ordonnances;
    }

    // Setter
    public void setOrdonnances(Set<Ordonnance> ordonnances) {
        if (ordonnances != null) {
            this.ordonnances = ordonnances;
        } else {
            this.ordonnances = new LinkedHashSet<>(); // Initialisation par défaut si null
        }
    }

    // Méthode pour ajouter une ordonnance
    public Ordonnances add(Ordonnance ordonnance) {
        if (ordonnance != null) {
            ordonnances.add(ordonnance);
        }
        return this; // Retourne l'instance pour permettre le chaînage
    }

    // Méthode pour enlever une ordonnance
    public void removeOrdonnance(Ordonnance ordonnance) {
        this.ordonnances.remove(ordonnance);
    }

    // Méthode pour vider la collection d'ordonnances
    public void clear() {
        this.ordonnances.clear();
    }

    @Override
    public String toString() {
        return "Ordonnances{" +
                "ordonnances=" + ordonnances +
                '}';
    }
}
