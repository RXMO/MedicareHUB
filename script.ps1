# Définition des variables
$SSH_USER = "medicare"
$SSH_HOST = "172.31.249.24"
$SSH_PASSWORD = "medicare"

$SCP_USER = "medicareback"
$SCP_HOST = "172.31.252.216"

$SOURCE_FILE = "xmart-city-backend/target/xmart-zity-backend-1.0-SNAPSHOT-jar-with-dependencies.jar"
$DEST_PATH = "/home/medicareback"

$SCP_PASSWORD = "medicareback"

# Chemin de PuTTY (adapter selon l'installation)
$PUTTY_PATH = "C:\Program Files\PuTTY" 

Write-Host "-------------------- Connexion SSH --------------------"
& "$PUTTY_PATH\plink.exe" -ssh -l $SSH_USER -pw $SSH_PASSWORD $SSH_HOST "echoe 'Connexion SSH établie sur DATABASE'"

Write-Host "-------------------- Compilation du code --------------------"

# Compilation Maven
mvn clean install
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERREUR] Compilation Maven échouée !" -ForegroundColor Red
    exit 1
}
Write-Host "[SUCCÈS] Compilation réussie."

# Vérifier si le port 45065 est occupé
$PORT = 45065
$ProcessID = & "$PUTTY_PATH\plink.exe" -ssh -l $SCP_USER -pw $SCP_PASSWORD $SCP_HOST "netstat -tulnp | grep ':$PORT' | awk '{print `$7}' | cut -d'/' -f1'"

if ($ProcessID -match "^[0-9]+$") {
    Write-Host "[INFO] Le port $PORT est occupé par le processus PID: $ProcessID."
    Write-Host "[INFO] Arrêt du processus en cours..."
    & "$PUTTY_PATH\putty.exe" -ssh -l $SCP_USER -pw $SCP_PASSWORD $SCP_HOST "kill -9 $ProcessID"
    Write-Host "[SUCCÈS] Processus arrêté."
} else {
    Write-Host "[INFO] Le port $PORT est libre."
}

Start-Sleep -Seconds 3

Write-Host "-------------------- Transfert du fichier via SCP --------------------"
& "$PUTTY_PATH\pscp.exe" -pw $SCP_PASSWORD $SOURCE_FILE "$($SCP_USER)@$($SCP_HOST):$($DEST_PATH)"
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERREUR] Envoi du fichier échoué !" -ForegroundColor Red
    exit 1
}
Write-Host "[SUCCÈS] Fichier envoyé avec succès."

# Lancement du backend avec `nohup` en arrière-plan
Write-Host "-------------------- Lancement du Backend --------------------"
& "$PUTTY_PATH\plink.exe" -ssh -l $SCP_USER -pw $SCP_PASSWORD $SCP_HOST "nohup java -jar $DEST_PATH/xmart-zity-backend-1.0-SNAPSHOT-jar-with-dependencies.jar > backend.log 2>&1 & echo \$!"
Write-Host "[SUCCÈS] Backend démarré."

Start-Sleep -Seconds 5

Write-Host "-------------------- Lancement du Frontend --------------------"
Start-Process "java" -ArgumentList "-jar xmart-frontend/target/xmart-frontend-1.0-SNAPSHOT-jar-with-dependencies.jar"
Write-Host "[SUCCÈS] Frontend démarré."
