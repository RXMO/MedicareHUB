package edu.ezip.ing1.pds;

import edu.ezip.ing1.pds.business.dto.Patients;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class MainFrontEnd {

    private final static String LoggingLabel = "FrontEnd";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final static String networkConfigFile = "network.yaml";

    public static void main(String[] args) throws IOException, InterruptedException {
        // Charger la configuration du réseau
        final NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);
        logger.debug("Load Network config file : {}", networkConfig.toString());

        // Récupération des patients
        final PatientService patientService = new PatientService(networkConfig);
        Patients patients = patientService.selectPatients();

        // Lancer l'interface graphique
        new PatientUI(patients, patientService); // Ajout de patientService
    }
}
