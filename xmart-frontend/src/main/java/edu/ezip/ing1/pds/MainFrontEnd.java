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

        // Créer le modèle de la table
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Nom");
        tableModel.addColumn("Prénom");
        tableModel.addColumn("Âge");

        // Ajouter les patients existants au modèle de table
        if (patients != null && patients.getPatients() != null && !patients.getPatients().isEmpty()) {
            for (Patient patient : patients.getPatients()) {
                tableModel.addRow(new Object[] { patient.getNom(), patient.getPrenom(), patient.getAge() });
            }
        } else {
            logger.debug("Aucun patient trouvé.");
        }

        // Créer une JTable pour afficher les patients
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        // Création de l'interface d'insertion
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2));

        JLabel nameLabel = new JLabel("Nom:");
        JTextField nameField = new JTextField();
        JLabel surnameLabel = new JLabel("Prénom:");
        JTextField surnameField = new JTextField();
        JLabel ageLabel = new JLabel("Âge:");
        JTextField ageField = new JTextField();

        JButton addButton = new JButton("Ajouter");
        JButton showButton = new JButton("Afficher la liste des patients");

        // Action à effectuer lors de l'ajout
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String surname = surnameField.getText();
            String ageText = ageField.getText();

            if (name.isEmpty() || surname.isEmpty() || ageText.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Veuillez remplir tous les champs.");
            } else {
                try {
                    int age = Integer.parseInt(ageText);

                    // Créer un nouveau patient et ajouter à la table
                    Patient newPatient = new Patient();
                    newPatient.setNom(name);
                    newPatient.setPrenom(surname);
                    newPatient.setAge(age);

                    // Ajouter le patient au modèle de table
                    tableModel
                            .addRow(new Object[] { newPatient.getNom(), newPatient.getPrenom(), newPatient.getAge() });

                    // Vider les champs de saisie
                    nameField.setText("");
                    surnameField.setText("");
                    ageField.setText("");

                    JOptionPane.showMessageDialog(null, "Patient ajouté avec succès.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "L'âge doit être un nombre valide.");
                }
            }
        });

        // Action à effectuer lors du clic sur le bouton "Afficher"
        showButton.addActionListener(e -> {
            // Afficher la table si elle n'est pas déjà affichée
            JFrame frame = new JFrame("Liste des Patients");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 300);
            frame.setLocationRelativeTo(null); // Centrer la fenêtre
            frame.setLayout(new BorderLayout());
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.setVisible(true);
        });

        // Ajouter les éléments à l'interface
        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(surnameLabel);
        inputPanel.add(surnameField);
        inputPanel.add(ageLabel);
        inputPanel.add(ageField);
        inputPanel.add(addButton);
        inputPanel.add(showButton);

        // Créer une fenêtre JFrame pour afficher la table et le formulaire
        JFrame frame = new JFrame("Gestion des Patients");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null); // Centrer la fenêtre
        frame.setLayout(new BorderLayout());
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.setVisible(true);
    }
}
