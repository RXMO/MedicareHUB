#!/bin/bash

# Définir les variables de connexion SSH pour la machine distante
SSH_USER="medicare"
SSH_HOST="172.31.249.24"
SSH_PASSWORD="medicare"

# Définir les variables pour la commande SCP
SCP_USER="medicareback"
SCP_HOST="172.31.252.216"
SOURCE_FILE="xmart-city-backend/target/xmart-zity-backend-1.0-SNAPSHOT-jar-with-dependencies.jar"
DEST_PATH="/home/medicareback"
SCP_PASSWORD="medicareback"

# Connexion SSH à la machine distante
echo "--------------------DATABASE--------------------"
sshpass -p "$SSH_PASSWORD" ssh -o StrictHostKeyChecking=no "$SSH_USER"@"$SSH_HOST" << EOF
    echo "--------------------Connexion SSH établie sur la machine distante de DATABASE--------------------"
EOF

# Compilation du projet Maven
echo "--------------------Compilation de code--------------------"
mvn clean install
if [ $? -ne 0 ]; then
    echo "❌ Erreur : Compilation Maven échouée !"
    exit 1
fi
echo "✅ Compilation réussie."

# Gestion du port backend
PORT=45065
# Commande sans sudo qui fonctionne avec les permissions normales
PID=$(sshpass -p "$SCP_PASSWORD" ssh -o StrictHostKeyChecking=no "$SCP_USER@$SCP_HOST" "ss -ltnp 'sport = :$PORT' | grep -oP 'pid=\K\d+' | head -n 1")

if [ ! -z "$PID" ]; then
    echo "⚠️ Le port $PORT est occupé par le processus PID: $PID."
    echo "🔴 Arrêt du processus en cours..."
    sshpass -p "$SCP_PASSWORD" ssh "$SCP_USER@$SCP_HOST" "kill -9 $PID"
    if [ $? -eq 0 ]; then
        echo "✅ Processus arrêté avec succès."
    else
        echo "❌ Échec lors de l'arrêt du processus. Essayez avec sudo si nécessaire."
        # Alternative avec le mot de passe sudo si connu
        # SUDO_PASSWORD="votre_mot_de_passe_sudo"
        # sshpass -p "$SCP_PASSWORD" ssh "$SCP_USER@$SCP_HOST" "echo '$SUDO_PASSWORD' | sudo -S kill -9 $PID"
    fi
else
    echo "✅ Le port $PORT est libre."
fi

sleep 3

# Envoi du fichier via SCP
echo "--------------------Envoi du fichier via SCP au VM du Backend...--------------------"
sshpass -p "$SCP_PASSWORD" scp "$SOURCE_FILE" "$SCP_USER@$SCP_HOST:$DEST_PATH"

if [ $? -ne 0 ]; then
    echo "❌ Erreur : Envoi du fichier échoué !"
    exit 1
fi
echo "✅ Fichier envoyé avec succès."

# Lancer le backend dans la VM
echo "--------------------Lancement du Back-end--------------------"
sshpass -p "$SCP_PASSWORD" ssh -o StrictHostKeyChecking=no "$SCP_USER@$SCP_HOST" "nohup java -jar xmart-zity-backend-1.0-SNAPSHOT-jar-with-dependencies.jar > backend.log 2>&1 &"
echo "✅ Back-end démarré."

# Afficher les logs
gnome-terminal -- bash -c "sshpass -p '$SCP_PASSWORD' ssh -o StrictHostKeyChecking=no '$SCP_USER@$SCP_HOST' 'tail -f /home/medicareback/backend.log'; exec bash"

sleep 5

# Lancer le front-end (JavaFX)
echo "--------------------Lancement du Front-end (JavaFX)--------------------"
gnome-terminal -- bash -c "cd xmart-frontend && mvn javafx:run; exec bash"
echo "✅ Front-End (JavaFX) démarré."