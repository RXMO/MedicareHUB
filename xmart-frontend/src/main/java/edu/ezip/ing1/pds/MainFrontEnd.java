package edu.ezip.ing1.pds;

import de.vandermeer.asciitable.AsciiTable;
import edu.ezip.ing1.pds.business.dto.Patient;
import edu.ezip.ing1.pds.business.dto.Patients;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class MainFrontEnd {

    private final static String LoggingLabel = "FrontEnd";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final static String networkConfigFile = "network.yaml";
    private static final Deque<ClientRequest> clientRequests = new ArrayDeque<ClientRequest>();

    public static void main(String[] args) throws IOException, InterruptedException {
        // Charger la configuration du réseau à partir du fichier YAML
        final NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);
        logger.debug("Load Network config file : {}", networkConfig.toString());

        // Création d'un service pour la gestion des patients
        final PatientService patientService = new PatientService(networkConfig);

        // Appel de la méthode pour récupérer les patients
        Patients patients = patientService.selectPatients();

        // Vérification de la validité de la réponse avant d'utiliser la collection de
        // patients
        if (patients != null && patients.getPatients() != null && !patients.getPatients().isEmpty()) {
            final AsciiTable asciiTable = new AsciiTable();

            // Parcourir et afficher les patients dans un tableau ASCII
            for (final Patient patient : patients.getPatients()) {
                asciiTable.addRule();
                asciiTable.addRow(patient.getNom(), patient.getPrenom(), patient.getAge());
            }
            asciiTable.addRule();
            logger.debug("\n{}\n", asciiTable.render());
        } else {
            // Message si aucun patient n'est trouvé ou si la collection est vide
            logger.debug("Aucun patient trouvé.");
        }
    }
}
