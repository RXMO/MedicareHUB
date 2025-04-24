package edu.ezip.ing1.pds.frontend;

import java.awt.BorderLayout;
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

    private DefaultListModel<String> modeleListe;
    private JList<String> listeSymptomes;
    private JTextArea resultatDiagnostic;

    private JButton boutonAjouter, boutonAfficher, boutonModifier, boutonSupprimer, boutonDiagnostiquer;

    private ServiceSymptome serviceSymptome;
    
    // Liste pour stocker les symptômes ajoutés 
    private List<Symptomes> symptomesAjoutes = new ArrayList<>();

    public FenetreDiagnostic() {
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setIpaddress("172.31.252.216");
        networkConfig.setTcpport(45065);

        serviceSymptome = new ServiceSymptome(networkConfig);

        setTitle("Diagnostic Médical");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelHaut = new JPanel();
        champSymptome = new JTextField(20);
        boutonAjouter = new JButton("Ajouter");
        boutonAfficher = new JButton("Afficher");

        panelHaut.add(new JLabel("Symptôme :"));
        panelHaut.add(champSymptome);
        panelHaut.add(boutonAjouter);
        panelHaut.add(boutonAfficher);
        add(panelHaut, BorderLayout.NORTH);

        modeleListe = new DefaultListModel<>();
        listeSymptomes = new JList<>(modeleListe);
        JScrollPane scrollPane = new JScrollPane(listeSymptomes);
        add(scrollPane, BorderLayout.CENTER);

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
    }

    private void ajouterSymptome() {
        String symptomeTexte = champSymptome.getText().trim();
        
        if (symptomeTexte.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Champ vide !", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Symptomes symptome = new Symptomes(0, symptomeTexte);
            Symptomes symptomeAvecId = serviceSymptome.insertSymptome(symptome);
            champSymptome.setText("");
            
            // Ajouter à la liste locale avec l'ID
            symptomesAjoutes.add(symptomeAvecId);
            
            JOptionPane.showMessageDialog(this, 
                "Symptôme ajouté avec succès ! ID: " + symptomeAvecId.getId(), 
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
            // Trouver l'ID du symptôme sélectionné
            int idSymptome = 0;
            for (Symptomes s : symptomesAjoutes) {
                if (s.getNom().equals(symptomeSelectionne)) {
                    idSymptome = s.getId();
                    break;
                }
            }
            
            if (idSymptome == 0) {
                JOptionPane.showMessageDialog(this, "Impossible de trouver l'ID du symptôme sélectionné.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Symptomes symptome = new Symptomes(idSymptome, symptomeSelectionne);
            serviceSymptome.updateSymptome(symptome, nouveauNom);
            champModification.setText("");
            
            // Mettre à jour la liste locale
            for (int i = 0; i < symptomesAjoutes.size(); i++) {
                if (symptomesAjoutes.get(i).getId() == idSymptome) {
                    symptomesAjoutes.get(i).setNom(nouveauNom);
                    break;
                }
            }
            
            // affichage
            afficherSymptomes();
    
            JOptionPane.showMessageDialog(this, "Symptôme modifié avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerSymptome() {
        String symptomeSelectionne = listeSymptomes.getSelectedValue();
        if (symptomeSelectionne != null) {
            try {
                // Trouver l'ID du symptôme sélectionné
                Symptomes symptomeASupprimer = null;
                for (Symptomes s : symptomesAjoutes) {
                    if (s.getNom().equals(symptomeSelectionne)) {
                        symptomeASupprimer = s;
                        break;
                    }
                }
                
                if (symptomeASupprimer == null || symptomeASupprimer.getId() == 0) {
                    JOptionPane.showMessageDialog(this, "Impossible de trouver l'ID du symptôme sélectionné.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Supprimer de la BD avec l'ID correct
                serviceSymptome.deleteSymptome(symptomeASupprimer);
                
                // Supprimer de la liste locale
                symptomesAjoutes.remove(symptomeASupprimer);
                
                // Mettre à jour l'affichage automatiquement après suppression
                afficherSymptomes();
    
                JOptionPane.showMessageDialog(this, "Symptôme supprimé avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erreur lors de la suppression: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un symptôme à supprimer.", "Attention", JOptionPane.WARNING_MESSAGE);
        }
    }


    private void diagnostiquer() {
        String symptomeSelectionne = listeSymptomes.getSelectedValue();
        if (symptomeSelectionne == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un symptôme à diagnostiquer.", "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<String> maladies = serviceSymptome.rechercherMaladiesParSymptome(symptomeSelectionne);
            if (maladies == null || maladies.isEmpty()) {
                resultatDiagnostic.setText("Aucune maladie trouvée pour ce symptôme.");
            } else {
                StringBuilder sb = new StringBuilder("Maladies associées à \"" + symptomeSelectionne + "\" :\n");
                for (String maladie : maladies) {
                    sb.append("- ").append(maladie).append("\n");
                }
                resultatDiagnostic.setText(sb.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            resultatDiagnostic.setText("Erreur lors du diagnostic.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FenetreDiagnostic().setVisible(true));
    }
}