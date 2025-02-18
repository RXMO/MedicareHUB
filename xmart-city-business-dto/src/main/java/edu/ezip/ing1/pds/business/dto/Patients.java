package edu.ezip.ing1.pds.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.LinkedHashSet;
import java.util.Set;

public class Patients {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("patients")
    private Set<Patient> Patients = new LinkedHashSet<Patient>();

    public Set<Patient> getPatients() {
        return Patients;
    }

    public void setPatients(Set<Patient> Patients) {
        this.Patients = Patients;
    }

    public final Patients add(final Patient Patient) {
        Patients.add(Patient);
        return this;
    }

    @Override
    public String toString() {
        return "Patients{" +
                "Patients=" + Patients +
                '}';
    }
}
