package edu.ezip.ing1.pds;

import edu.ezip.ing1.pds.business.dto.Patient;
import edu.ezip.ing1.pds.business.dto.Patients;
import edu.ezip.ing1.pds.client.commons.ClientRequest;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
            // Créer un modèle de table pour afficher les patients
            DefaultTableModel tableModel = new DefaultTableModel();
            tableModel.addColumn("Nom");
            tableModel.addColumn("Prénom");
            tableModel.addColumn("Âge");

            // Ajouter les patients au modèle de table
            for (Patient patient : patients.getPatients()) {
                tableModel.addRow(new Object[] { patient.getNom(), patient.getPrenom(), patient.getAge() });
            }

            // Créer une JTable pour afficher les patients
            JTable table = new JTable(tableModel);

            // Créer un JScrollPane pour ajouter du défilement à la JTable
            JScrollPane scrollPane = new JScrollPane(table);
            table.setFillsViewportHeight(true);

            // Créer une fenêtre JFrame pour afficher la table
            JFrame frame = new JFrame("Liste des Patients");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 300);
            frame.setLocationRelativeTo(null); // Centrer la fenêtre
            frame.setLayout(new BorderLayout());
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.setVisible(true);

        } else {
            // Message si aucun patient n'est trouvé ou si la collection est vide
            logger.debug("Aucun patient trouvé.");
        }
    }
}
