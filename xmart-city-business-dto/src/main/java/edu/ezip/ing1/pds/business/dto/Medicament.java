package edu.ezip.ing1.pds.business.dto;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Medicament {
    @JsonProperty("idMedicament")
    private int idMedicament;

    @JsonProperty("nomMedicament")
    private String nomMedicament;

    @JsonProperty("principeActif")
    private String principeActif;

    public Medicament() {
    }

    public Medicament(int idMedicament, String nomMedicament, String principeActif) {
        this.idMedicament = idMedicament;
        this.nomMedicament = nomMedicament;
        this.principeActif = principeActif;
    }
    public Medicament(int idMedicament) {
        this.idMedicament = idMedicament;
        this.nomMedicament = "";
    }
    public final Medicament build(final ResultSet resultSet)
            throws SQLException, NoSuchFieldException, IllegalAccessException {
        setFieldsFromResultSet(resultSet, "idMedicament", "nomMedicament", "principeActif");
        return this;
    }

    public final PreparedStatement build(PreparedStatement preparedStatement)
            throws SQLException, NoSuchFieldException, IllegalAccessException {
        return buildPreparedStatement(preparedStatement, String.valueOf(idMedicament), nomMedicament, principeActif);
    }

    public int getId() {
        return idMedicament;
    }

    public String getNomMedicament() {
        return nomMedicament;
    }

    public String getPrincipeActif() {
        return principeActif;
    }

    public void setIdMedicament(int idMedicament) {
        this.idMedicament = idMedicament;
    }

    public void setNomMedicament(String nomMedicament) {
        this.nomMedicament = nomMedicament;
    }

    public void setPrincipeActif(String principeActif) {
        this.principeActif = principeActif;
    }
    private boolean selected;
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    private void setFieldsFromResultSet(final ResultSet resultSet, final String... fieldNames)
        throws NoSuchFieldException, SQLException, IllegalAccessException {
    for (final String fieldName : fieldNames) {
        final Field field = this.getClass().getDeclaredField(fieldName);
        String columnName;
        
        // Mappez les noms de champs Java aux noms de colonnes SQL
        if (fieldName.equals("idMedicament")) {
            columnName = "id_medicament";
        } else if (fieldName.equals("nomMedicament")) {
            columnName = "nom_medicament";
        } else if (fieldName.equals("principeActif")) {
            columnName = "principe_actif";
        } else {
            columnName = fieldName; // Fallback pour les autres champs
        }
        
        if (field.getType() == int.class) {
            field.set(this, resultSet.getInt(columnName));
        } else {
            field.set(this, resultSet.getObject(columnName));
        }
    }
}

    private final PreparedStatement buildPreparedStatement(PreparedStatement preparedStatement,
            final String... fieldValues)
            throws SQLException {
        int ix = 0;
        for (final String fieldValue : fieldValues) {
            preparedStatement.setString(++ix, fieldValue);
        }
        return preparedStatement;
    }

    @Override
    public String toString() {
        return "Medicament{" +
                "idMedicament=" + idMedicament +
                ", nomMedicament='" + nomMedicament + '\'' +
                ", principeActif='" + principeActif + '\'' +
                '}';
    }
}
