package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class InsertSymptomeClientRequest extends ClientRequest<Symptomes, Symptomes> {

    public InsertSymptomeClientRequest(
            NetworkConfig networkConfig, int myBirthDate, Request request, Symptomes symptome, byte[] bytes)
            throws IOException {
        super(networkConfig, myBirthDate, request, symptome, bytes);
    }

    @Override
    public Symptomes readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(body, Symptomes.class);
        } catch (Exception e) {
            try {
                Map<String, Object> map = mapper.readValue(body, Map.class);
                Symptomes symptome = new Symptomes();
                
                if (map.containsKey("id")) {
                    symptome.setId(Integer.parseInt(map.get("id").toString()));
                }
                
                if (map.containsKey("nom")) {
                    symptome.setNom(map.get("nom").toString());
                }
                
                return symptome;
            } catch (Exception ex) {
                System.err.println("Impossible de parser la réponse: " + body);
                return new Symptomes();
            }
        }
    }
}