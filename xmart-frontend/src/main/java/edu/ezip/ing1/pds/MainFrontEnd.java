package edu.ezip.ing1.pds;

import java.awt.Color;
import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ezip.ing1.pds.business.dto.Patients;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.frontend.OrdonnanceFrontEnd;
import edu.ezip.ing1.pds.services.MedicamentService;
import edu.ezip.ing1.pds.services.OrdonnanceService;
import edu.ezip.ing1.pds.services.PatientService;

public class MainFrontEnd {
    private final static String LoggingLabel = "FrontEnd";
    private final static Logger logger = LoggerFactory.getLogger(LoggingLabel);
    private final static String networkConfigFile = "network.yaml";
    
    public static void main(String[] args) throws IOException, InterruptedException {
        
        try {
        
            Color titleBarColor = new Color(173, 216, 230); 
            UIManager.put("activeCaption", titleBarColor);
            UIManager.put("activeCaptionText", Color.BLACK);
            UIManager.put("activeCaptionBorder", titleBarColor);
        } catch (Exception e) {
            logger.error("Erreur lors de la personnalisation de l'interface : {}", e.getMessage());
        }
        
        // Charger la configuration du réseau
        final NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, networkConfigFile);
        logger.debug("Load Network config file : {}", networkConfig.toString());
        
        // Récupération des patients
        final PatientService patientService = new PatientService(networkConfig);
        Patients patients = patientService.selectPatients();
        
        // Lancer l'interface graphique avec les boutons
        createUI(patients, patientService);
    }
    
    private static void createUI(Patients patients, PatientService patientService) {
        JFrame frame = new JFrame("Système de Gestion Médicale");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        
        // Centrer la fenêtre
        frame.setLocationRelativeTo(null);
        
        // Configuration supplémentaire pour améliorer l'apparence
        JRootPane rootPane = frame.getRootPane();
        rootPane.setBackground(new Color(240, 240, 240)); // Couleur de fond légère
        rootPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JButton btnOmar = new JButton("Patients");
        JButton btnAfrah = new JButton("Diagnostic");
        JButton btnEmna = new JButton("Ordonnances"); // Corrigé l'orthographe "Odonnances" → "Ordonnances"
        
        btnEmna.addActionListener(e -> {
            try {
                // Charger la configuration réseau à partir du fichier "network.yaml"
                NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml");
                
                // Initialiser les services avec la configuration réseau
                OrdonnanceService ordonnanceService = new OrdonnanceService(networkConfig);
                MedicamentService medicamentService = new MedicamentService(networkConfig);
                
                // Créer et afficher l'interface Ordonnance avec les services passés en paramètres
                new OrdonnanceFrontEnd(ordonnanceService, medicamentService);
            } catch (InterruptedException | IOException ex) {
                // Afficher un message d'erreur si une exception se produit
                JOptionPane.showMessageDialog(null, "Erreur lors de l'ouverture de l'interface ordonnance : " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        // ActionListener pour le bouton Omar
        btnOmar.addActionListener(e -> new PatientUI(patients, patientService));
        
        // ActionListener pour le bouton Afrah (ouvre l'interface FenetreDiagnostic)
        btnAfrah.addActionListener(e -> {
            // FenetreDiagnostic fenetreDiagnostic = new FenetreDiagnostic();
            //fenetreDiagnostic.setVisible(true);  // Affiche la fenêtre directement ici
            //fenetreDiagnostic.setLocationRelativeTo(null); // Centre la fenêtre
        });
        
        // Ajouter les boutons à l'interface
        frame.add(btnOmar);
        frame.add(btnAfrah);
        frame.add(btnEmna);
        
        frame.setVisible(true);
    }
}