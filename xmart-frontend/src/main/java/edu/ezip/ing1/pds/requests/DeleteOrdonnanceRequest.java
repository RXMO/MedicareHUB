package edu.ezip.ing1.pds.requests;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ezip.ing1.pds.business.dto.Ordonnance;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.commons.Request;

public class DeleteOrdonnanceRequest extends ClientRequest<Ordonnance, String> {

    public DeleteOrdonnanceRequest(NetworkConfig networkConfig, int myBirthDate, Request request, Ordonnance info, byte[] bytes) throws IOException {
        super(networkConfig, myBirthDate, request, info, bytes);
    }

    @Override
    public String readResult(String body) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, String> statusMap = mapper.readValue(body, Map.class);
        return statusMap.getOrDefault("status", "Statut inconnu");
    }

    
    /*public boolean deleteOrdonnanceFromDB(int ordonnanceId) {
        String sql = "DELETE FROM ordonnance WHERE id = ?";  

        try (Connection conn = ConnectionPoolImpl.get();  
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            
            stmt.setInt(1, ordonnanceId);

           
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;  
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;  
    }*/
}
