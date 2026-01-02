#!/usr/bin/env php
<?php
/**
 * Script de réveil de l'application mobile CelyaVox
 * Usage: php wake_app.php <FCM_TOKEN>
 * 
 * Ce script envoie une notification silencieuse (data-only) pour réveiller l'app
 * en arrière-plan et forcer la reconnexion SIP.
 * 
 * Configuration requise:
 * 1. Télécharger le fichier Service Account JSON depuis Firebase Console
 * 2. Créer un fichier .env dans le même dossier avec:
 *    FCM_PROJECT_ID=votre-project-id
 *    FCM_SERVICE_ACCOUNT_JSON=/chemin/vers/service-account.json
 * 
 * Note: Ce fichier ne doit PAS être versionné dans Git!
 */

// Charger la configuration depuis .env
function loadEnv($filePath) {
    if (!file_exists($filePath)) {
        die("❌ Erreur: Fichier .env introuvable à $filePath\n");
    }
    
    $lines = file($filePath, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
    foreach ($lines as $line) {
        if (strpos(trim($line), '#') === 0) continue;
        
        list($name, $value) = explode('=', $line, 2);
        $name = trim($name);
        $value = trim($value);
        
        if (!array_key_exists($name, $_ENV)) {
            putenv("$name=$value");
            $_ENV[$name] = $value;
        }
    }
}

function sendWakeUpNotification($token) {
    
    $url = "https://celyavox.celya.fr/phone/wake_app.php";
    
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json'
    ]);
    $message = [ 'server' => 'freepbx17-dev.celya.fr', 'token' => $token ];
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($message));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $error = curl_error($ch);
    curl_close($ch);
    
    if ($error) {
        return [
            'success' => false,
            'error' => "Erreur cURL: $error"
        ];
    }
    
    $responseData = json_decode($response, true);
    
    return [
        'success' => ($httpCode == 200),
        'http_code' => $httpCode,
        'response' => $responseData,
        'raw_response' => $response
    ];
}

// Récupérer le token depuis la BDD
function getFcmTokenFromExtension($extension) {
    $host = getenv('DB_HOST');
    $dbname = getenv('DB_NAME');
    $user = getenv('DB_USER');
    $pass = getenv('DB_PASS');

    if (!$host || !$dbname || !$user || !$pass) {
        die("❌ Erreur: Configuration base de données incomplète dans .env (DB_HOST, DB_NAME, DB_USER, DB_PASS)\n");
    }

    try {
        $dsn = "mysql:host=$host;dbname=$dbname;charset=utf8mb4";
        $pdo = new PDO($dsn, $user, $pass, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC
        ]);

        $stmt = $pdo->prepare("SELECT token_fcm FROM token_fcm WHERE extension = ?");
        $stmt->execute([$extension]);
        $result = $stmt->fetch();

        if ($result) {
            return $result['token_fcm'];
        } else {
            return null;
        }
    } catch (PDOException $e) {
        die("❌ Erreur connexion BDD: " . $e->getMessage() . "\n");
    }
}

// --- MAIN ---

// Chargement de la configuration
$envPath = __DIR__ . '/.env';
loadEnv($envPath);

// Vérification des arguments
if ($argc < 2) {
    echo "❌ Usage: php " . basename(__FILE__) . " <EXTENSION>\n\n";
    echo "Exemple:\n";
    echo "  php wake_app.php 1001\n\n";
    echo "Configuration:\n";
    echo "  1. Téléchargez le Service Account JSON depuis Firebase\n";
    echo "  2. Créez un fichier .env avec:\n";
    echo "     FCM_PROJECT_ID=votre-project-id\n";
    echo "     FCM_SERVICE_ACCOUNT_JSON=/chemin/vers/service-account.json\n";
    echo "     DB_HOST=localhost\n";
    echo "     DB_NAME=ma_base\n";
    echo "     DB_USER=mon_user\n";
    echo "     DB_PASS=mon_pass\n\n";
    exit(1);
}

$extension = $argv[1];
echo "🔍 Recherche du token pour l'extension: $extension...\n";

$token = getFcmTokenFromExtension($extension);

if (!$token) {
    echo "❌ Erreur: Aucun token FCM trouvé pour l'extension $extension\n";
    exit(1);
}

echo "📱 Réveil de l'application mobile...\n";
echo "Token: " . substr($token, 0, 20) . "...\n\n";

// Envoi de la notification
$result = sendWakeUpNotification($token);

if ($result['success']) {
    echo "✅ Notification envoyée avec succès!\n\n";
    
    if (isset($result['response']['name'])) {
        echo "📊 Résultat FCM:\n";
        echo "   - Message: " . $result['response']['name'] . "\n";
    }
    
    exit(0);
} else {
    echo "❌ Échec de l'envoi\n\n";
    echo "Erreur: " . ($result['error'] ?? 'Inconnue') . "\n";
    
    if (isset($result['http_code'])) {
        echo "Code HTTP: " . $result['http_code'] . "\n";
    }
    
    if (isset($result['raw_response'])) {
        echo "\nRéponse brute:\n" . $result['raw_response'] . "\n";
    }
    
    exit(1);
}
