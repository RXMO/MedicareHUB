package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.DiagnosticResult;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

//demander les diagnostics d'un patient au serveur
public class SelectPatientDiagnosticsClientRequest extends ClientRequest<Integer, List<DiagnosticResult>> {

    public SelectPatientDiagnosticsClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Integer idPatient, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, idPatient, bytes);
    }

    @Override
    public List<DiagnosticResult> readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        // renvoie une liste vide
        if (body.contains("Aucun diagnostic trouvé")) {
            return new ArrayList<>();
        }
        // transforme la réponse JSON en une liste de diagnostics
        List<Map<String, Object>> diagnosticsData = mapper.readValue(body, List.class);
        List<DiagnosticResult> diagnostics = new ArrayList<>();
        
        // Pour chaque diagnostic, créer un objet DiagnosticResult
        for (Map<String, Object> data : diagnosticsData) {
            DiagnosticResult result = new DiagnosticResult();
            result.setIdSpecialite((Integer) data.get("id_maladie")); //utiliser id_maladie comme idSpecialite 
            result.setScore((Double) data.get("score"));
            diagnostics.add(result);
        }
        return diagnostics;
    }
}