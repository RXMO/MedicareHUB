package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "interaction_medicament")
public class Interaction_medicament {
    private int idMedicament1;
    private int idMedicament2;
    private String description;

    public Interaction_medicament() {
    }

    public Interaction_medicament(int idMedicament1, int idMedicament2, String description) {
        this.idMedicament1 = idMedicament1;
        this.idMedicament2 = idMedicament2;
        this.description = description;
    }

    @JsonProperty("id_medicament1")
    public int getIdMedicament1() {
        return idMedicament1;
    }

    public void setIdMedicament1(int idMedicament1) {
        this.idMedicament1 = idMedicament1;
    }

    @JsonProperty("id_medicament2")
    public int getIdMedicament2() {
        return idMedicament2;
    }

    public void setIdMedicament2(int idMedicament2) {
        this.idMedicament2 = idMedicament2;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}