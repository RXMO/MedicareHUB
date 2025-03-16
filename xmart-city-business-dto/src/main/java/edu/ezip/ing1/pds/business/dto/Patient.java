package edu.ezip.ing1.pds.business.dto;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "patient")

public class Patient {
    private int idPatient;
    private String nomPatient;
    private String prenomPatient;
    private String numTel;
    private String allergies;

    public Patient() {
    }

    public final Patient build(final ResultSet resultSet)
            throws SQLException, NoSuchFieldException, IllegalAccessException {
        setFieldsFromResulset(resultSet, "id_patient", "nom_patient", "prenom_patient", "num_tel", "allergies");
        return this;
    }

    public final PreparedStatement build(PreparedStatement preparedStatement)
            throws SQLException, NoSuchFieldException, IllegalAccessException {
        return buildPreparedStatement(preparedStatement, String.valueOf(idPatient), nomPatient, prenomPatient, numTel,
                allergies);
    }

    public Patient(String nomPatient, String prenomPatient, String numTel, String allergies) {
        this.nomPatient = nomPatient;
        this.prenomPatient = prenomPatient;
        this.numTel = numTel;
        this.allergies = allergies;
        // L'ID patient sera généré par la base de données
    }

    public Patient(int idPatient, String nomPatient, String prenomPatient, String numTel, String allergies) {
        this.idPatient = idPatient;
        this.nomPatient = nomPatient;
        this.prenomPatient = prenomPatient;
        this.numTel = numTel;
        this.allergies = allergies;
    }

    public int getIdPatient() {
        return idPatient;
    }

    public String getNomPatient() {
        return nomPatient;
    }

    public String getPrenomPatient() {
        return prenomPatient;
    }

    public String getNumTel() {
        return numTel;
    }

    public String getAllergies() {
        return allergies;
    }

    // Pour maintenir la compatibilité avec le code existant
    public String getNom() {
        return nomPatient;
    }

    public String getPrenom() {
        return prenomPatient;
    }

    @JsonProperty("id_patient")
    public void setIdPatient(int idPatient) {
        this.idPatient = idPatient;
    }

    @JsonProperty("nom_patient")
    public void setNomPatient(String nomPatient) {
        this.nomPatient = nomPatient;
    }

    @JsonProperty("prenom_patient")
    public void setPrenomPatient(String prenomPatient) {
        this.prenomPatient = prenomPatient;
    }

    @JsonProperty("num_tel")
    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    @JsonProperty("allergies")
    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    // Pour maintenir la compatibilité avec le code existant
    @JsonProperty("nom")
    public void setNom(String nom) {
        this.nomPatient = nom;
    }

    @JsonProperty("prenom")
    public void setPrenom(String prenom) {
        this.prenomPatient = prenom;
    }

    private void setFieldsFromResulset(final ResultSet resultSet, final String... fieldNames)
            throws NoSuchFieldException, SQLException, IllegalAccessException {
        for (final String fieldName : fieldNames) {
            String javaFieldName = convertToJavaFieldName(fieldName);
            final Field field = this.getClass().getDeclaredField(javaFieldName);
            if (field.getType() == int.class) {
                field.set(this, resultSet.getInt(fieldName));
            } else {
                field.set(this, resultSet.getObject(fieldName));
            }
        }
    }

    private String convertToJavaFieldName(String dbFieldName) {
        // Convertit id_patient en idPatient, nom_patient en nomPatient, etc.
        String[] parts = dbFieldName.split("_");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            result.append(Character.toUpperCase(parts[i].charAt(0)))
                    .append(parts[i].substring(1));
        }
        return result.toString();
    }

    private final PreparedStatement buildPreparedStatement(PreparedStatement preparedStatement,
            final String... values)
            throws NoSuchFieldException, SQLException, IllegalAccessException {
        int ix = 0;
        for (final String value : values) {
            preparedStatement.setString(++ix, value);
        }
        return preparedStatement;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "idPatient=" + idPatient +
                ", nomPatient='" + nomPatient + '\'' +
                ", prenomPatient='" + prenomPatient + '\'' +
                ", numTel='" + numTel + '\'' +
                ", allergies='" + allergies + '\'' +
                '}';
    }
}