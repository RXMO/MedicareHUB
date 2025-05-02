package edu.ezip.ing1.pds.frontend;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.ezip.ing1.pds.business.dto.Symptomes;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.ServiceSymptome;

public class FenetreDiagnostic extends JFrame {

    private JTextField champSymptome;
    private JTextField champModification;
    private JTextField champPatientId;  

    private DefaultListModel<String> modeleListe;
    private JList<String> listeSymptomes;
    private JTextArea resultatDiagnostic;

    private JButton boutonAjouter, boutonAfficher, boutonModifier, boutonSupprimer, boutonDiagnostiquer;

    private ServiceSymptome serviceSymptome;
    
    private List<Symptomes> symptomesAjoutes = new ArrayList<>();
    
    private int idPatientActuel = 1; 

    public FenetreDiagnostic() {
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setIpaddress("172.31.252.216");
        networkConfig.setTcpport(45065);

        serviceSymptome = new ServiceSymptome(networkConfig);

        setTitle("Diagnostic Médical");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelPatient = new JPanel(new FlowLayout(FlowLayout.LEFT));
        champPatientId = new JTextField(5);
        champPatientId.setText(String.valueOf(idPatientActuel));
        JButton boutonChargerPatient = new JButton("Charger Patient");
        
        panelPatient.add(new JLabel("ID Patient :"));
        panelPatient.add(champPatientId);
        panelPatient.add(boutonChargerPatient);
        add(panelPatient, BorderLayout.NORTH);

        JPanel panelHaut = new JPanel();
        champSymptome = new JTextField(20);
        boutonAjouter = new JButton("Ajouter");
        boutonAfficher = new JButton("Afficher");

        panelHaut.add(new JLabel("Symptôme :"));
        panelHaut.add(champSymptome);
        panelHaut.add(boutonAjouter);
        panelHaut.add(boutonAfficher);
        
        JPanel panelCentre = new JPanel(new BorderLayout());
        panelCentre.add(panelHaut, BorderLayout.NORTH);
        
        modeleListe = new DefaultListModel<>();
        listeSymptomes = new JList<>(modeleListe);
        JScrollPane scrollPane = new JScrollPane(listeSymptomes);
        panelCentre.add(scrollPane, BorderLayout.CENTER);
        
        add(panelCentre, BorderLayout.CENTER);

        JPanel panelBas = new JPanel();
        champModification = new JTextField(15);
        boutonModifier = new JButton("Modifier");
        boutonSupprimer = new JButton("Supprimer");

        panelBas.add(new JLabel("Modifier le symptôme sélectionné :"));
        panelBas.add(champModification);
        panelBas.add(boutonModifier);
        panelBas.add(boutonSupprimer);
        add(panelBas, BorderLayout.SOUTH);

        JPanel panelDroite = new JPanel(new BorderLayout());
        boutonDiagnostiquer = new JButton("Diagnostiquer");
        resultatDiagnostic = new JTextArea(10, 20);
        resultatDiagnostic.setEditable(false);
        panelDroite.add(boutonDiagnostiquer, BorderLayout.NORTH);
        panelDroite.add(new JScrollPane(resultatDiagnostic), BorderLayout.CENTER);
        add(panelDroite, BorderLayout.EAST);

        boutonAjouter.addActionListener(e -> ajouterSymptome());
        boutonAfficher.addActionListener(e -> afficherSymptomes());
        boutonModifier.addActionListener(e -> modifierSymptome());
        boutonSupprimer.addActionListener(e -> supprimerSymptome());
        boutonDiagnostiquer.addActionListener(e -> diagnostiquer());
        boutonChargerPatient.addActionListener(e -> chargerPatient());
        
        chargerSymptomesPatient();
    }
    
    private void chargerPatient() {
        try {
            String idText = champPatientId.getText().trim();
            if (idText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez entrer un ID de patient valide", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int newPatientId = Integer.parseInt(idText);
            if (newPatientId <= 0) {
                JOptionPane.showMessageDialog(this, "L'ID du patient doit être un entier positif", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            idPatientActuel = newPatientId;
            
            chargerSymptomesPatient();
            
            JOptionPane.showMessageDialog(this, "Patient " + idPatientActuel + " chargé avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "L'ID du patient doit être un nombre entier", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void chargerSymptomesPatient() {
        try {
            symptomesAjoutes.clear(); 
            
            List<Symptomes> symptomesPatient = serviceSymptome.getSymptomesPatient(idPatientActuel);
            symptomesAjoutes.addAll(symptomesPatient);
            
            afficherSymptomes();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors du chargement des symptômes: " + ex.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouterSymptome() {
        String symptomeTexte = champSymptome.getText().trim();
        
        if (symptomeTexte.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Champ vide !", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Symptomes symptome = new Symptomes(0, symptomeTexte);
            
            Symptomes symptomeAvecId = serviceSymptome.associerSymptomePatient(idPatientActuel, symptome);
            champSymptome.setText("");
            
            boolean existe = false;
            for (Symptomes s : symptomesAjoutes) {
                if (s.getId() == symptomeAvecId.getId()) {
                    existe = true;
                    break;
                }
            }
            
            if (!existe) {
                symptomesAjoutes.add(symptomeAvecId);
            }
            
            JOptionPane.showMessageDialog(this, 
                "Symptôme associé au patient avec succès ! ID: " + symptomeAvecId.getId(), 
                "Succès", JOptionPane.INFORMATION_MESSAGE);
            
           //afficherSymptomes();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur lors de l'ajout du symptôme: " + ex.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void afficherSymptomes() {
        modeleListe.clear();
        for (Symptomes s : symptomesAjoutes) {
            modeleListe.addElement(s.getNom());
        }
    }

    private void modifierSymptome() {
    String symptomeSelectionne = listeSymptomes.getSelectedValue();
    String nouveauNom = champModification.getText().trim();

    if (symptomeSelectionne == null || nouveauNom.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Champ vide ou symptôme non sélectionné !", "Erreur", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        int idSymptome = 0;
        int index = -1;
        for (int i = 0; i < symptomesAjoutes.size(); i++) {
            Symptomes s = symptomesAjoutes.get(i);
            if (s.getNom().equals(symptomeSelectionne)) {
                idSymptome = s.getId();
                index = i;
                break;
            }
        }
        
        if (idSymptome == 0) {
            JOptionPane.showMessageDialog(this, "Impossible de trouver l'ID du symptôme sélectionné.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<Symptomes> tousLesSymptomes = serviceSymptome.selectSymptomes();
        boolean symptomeExiste = false;
        
        for (Symptomes s : tousLesSymptomes) {
            if (s.getNom().equals(nouveauNom)) {
                symptomeExiste = true;
                break;
            }
        }
        
        if (!symptomeExiste) {
            JOptionPane.showMessageDialog(this, 
                "Le symptôme '" + nouveauNom + "' n'existe pas dans la base de données.\n" +
                "Veuillez d'abord l'ajouter comme nouveau symptôme avant de l'utiliser pour la modification.",
                "Symptôme introuvable", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Symptomes nouveauSymptome = serviceSymptome.modifierSymptomePatient(idPatientActuel, idSymptome, nouveauNom);
        champModification.setText("");
        
        if (index >= 0 && nouveauSymptome != null) {
            symptomesAjoutes.remove(index);
            symptomesAjoutes.add(nouveauSymptome);
        } else {
            chargerSymptomesPatient();
        }
        
        afficherSymptomes();

        JOptionPane.showMessageDialog(this, "Symptôme modifié avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
private void supprimerSymptome() {
    String symptomeSelectionne = listeSymptomes.getSelectedValue();
    if (symptomeSelectionne == null) {
        JOptionPane.showMessageDialog(this, "Veuillez sélectionner un symptôme à supprimer.", "Attention", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    try {
        int idSymptome = 0;
        for (Symptomes s : symptomesAjoutes) {
            if (s.getNom().equals(symptomeSelectionne)) {
                idSymptome = s.getId();
                break;
            }
        }
        
        if (idSymptome == 0) {
            JOptionPane.showMessageDialog(this, 
                "Impossible de trouver l'ID du symptôme " + symptomeSelectionne, 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirmation = JOptionPane.showConfirmDialog(
            this,
            "Êtes-vous sûr de vouloir supprimer l'association avec le symptôme \"" + symptomeSelectionne + "\" ?",
            "Confirmation de suppression",
            JOptionPane.YES_NO_OPTION);
        
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }
        
        String message = serviceSymptome.supprimerSymptomePatient(idPatientActuel, idSymptome);
        
        boolean supprime = false;
        for (int i = 0; i < symptomesAjoutes.size(); i++) {
            if (symptomesAjoutes.get(i).getId() == idSymptome) {
                symptomesAjoutes.remove(i);
                supprime = true;
                break;
            }
        }
        
        if (!supprime) {
            chargerSymptomesPatient();
        } else {
            afficherSymptomes();
        }
        
        JOptionPane.showMessageDialog(this, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "Erreur lors de la suppression: " + ex.getMessage(), 
            "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}

    private void diagnostiquer() {
        try {
            if (symptomesAjoutes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucun symptôme ajouté pour ce patient.", "Attention", JOptionPane.WARNING_MESSAGE);
                return;
            }
    
            List<String> maladies = serviceSymptome.diagnostiquer(idPatientActuel);
            
            if (maladies == null || maladies.isEmpty()) {
                resultatDiagnostic.setText("Aucune maladie trouvée pour ces symptômes.");
            } else {
                StringBuilder sb = new StringBuilder("Maladies possibles selon les symptômes :\n");
                for (String maladie : maladies) {
                    sb.append("- ").append(maladie).append("\n");
                }
                resultatDiagnostic.setText(sb.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            resultatDiagnostic.setText("Erreur lors du diagnostic: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FenetreDiagnostic().setVisible(true));
    }
}
//
