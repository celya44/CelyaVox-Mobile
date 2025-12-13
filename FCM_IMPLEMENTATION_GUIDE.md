# Guide d'implémentation FCM (Firebase Cloud Messaging)

## 📱 Configuration Mobile (Android & iOS)

### 1. Création du projet Firebase

1. **Accéder à la console Firebase**
   - Rendez-vous sur https://console.firebase.google.com/
   - Cliquez sur "Ajouter un projet"
   - Donnez un nom à votre projet (ex: "CelyaVox-Mobile")
   - Acceptez les conditions et créez le projet

2. **Activer Cloud Messaging**
   - Dans votre projet, accédez à "Build" → "Cloud Messaging"
   - Activez l'API Cloud Messaging si nécessaire

---

## 🤖 Configuration Android

### Étape 1 : Ajouter l'application Android

1. Dans la console Firebase, cliquez sur l'icône Android
2. Renseignez le **Package Name** (identique à celui dans `capacitor.config.json`)
   - Exemple : `com.voip.celyavox`
3. Téléchargez le fichier **`google-services.json`**

### Étape 2 : Intégrer le fichier de configuration

1. Placez `google-services.json` dans le dossier :
   ```
   android/app/google-services.json
   ```

### Étape 3 : Modifier les fichiers Gradle

**`android/build.gradle`** (projet) :
```gradle
buildscript {
    dependencies {
        // Ajouter cette ligne
        classpath 'com.google.gms:google-services:4.3.15'
    }
}
```

**`android/app/build.gradle`** (module) :
```gradle
// En haut du fichier, après les autres plugins
apply plugin: 'com.google.gms.google-services'

dependencies {
    // Ajouter les dépendances Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-messaging'
}
```

### Étape 4 : Vérifier les permissions

**`android/app/src/main/AndroidManifest.xml`** :
```xml
<manifest>
    <!-- Permissions FCM -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    
    <application>
        <!-- Service Firebase Messaging (optionnel pour personnalisation) -->
        <service
            android:name=".MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

### Étape 5 : Build et test

```bash
cd android
./gradlew clean
./gradlew assembleDebug
```

---

## 🍎 Configuration iOS

### Étape 1 : Ajouter l'application iOS

1. Dans la console Firebase, cliquez sur l'icône iOS
2. Renseignez le **Bundle ID** (identique à celui dans `capacitor.config.json`)
   - Exemple : `com.voip.celyavox`
3. Téléchargez le fichier **`GoogleService-Info.plist`**

### Étape 2 : Intégrer le fichier de configuration

1. Placez `GoogleService-Info.plist` dans le dossier :
   ```
   ios/App/App/GoogleService-Info.plist
   ```

2. Dans Xcode :
   - Ouvrez `ios/App/App.xcworkspace`
   - Glissez-déposez `GoogleService-Info.plist` dans le projet
   - ✅ Cochez "Copy items if needed"
   - ✅ Sélectionnez la cible "App"

### Étape 3 : Configurer les Capabilities

Dans Xcode :
1. Sélectionnez le projet → Target "App" → "Signing & Capabilities"
2. Cliquez sur "+ Capability"
3. Ajoutez **"Push Notifications"**
4. Ajoutez **"Background Modes"** et cochez :
   - ✅ Remote notifications

### Étape 4 : Créer une clé APNs (Apple Push Notification service)

1. Accédez à https://developer.apple.com/account/resources/authkeys/list
2. Cliquez sur "+" pour créer une nouvelle clé
3. Donnez-lui un nom (ex: "CelyaVox APNs")
4. ✅ Cochez **"Apple Push Notifications service (APNs)"**
5. Téléchargez la clé (fichier `.p8`) - **⚠️ Une seule fois !**
6. Notez le **Key ID** et le **Team ID**

### Étape 5 : Uploader la clé APNs dans Firebase

1. Dans la console Firebase → Paramètres du projet → Cloud Messaging
2. Onglet "iOS"
3. Cliquez sur "Télécharger une clé APNs"
4. Uploadez le fichier `.p8`
5. Renseignez le **Key ID** et le **Team ID**

### Étape 6 : Modifier le Podfile

**`ios/App/Podfile`** :
```ruby
platform :ios, '13.0'
use_frameworks!

target 'App' do
  capacitor_pods
  
  # Ajouter Firebase
  pod 'Firebase/Messaging'
end
```

Puis exécutez :
```bash
cd ios/App
pod install
```

### Étape 7 : Modifier AppDelegate.swift

**`ios/App/App/AppDelegate.swift`** :
```swift
import UIKit
import Capacitor
import Firebase
import FirebaseMessaging

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        // Initialiser Firebase
        FirebaseApp.configure()
        
        // Demander les permissions de notification
        UNUserNotificationCenter.current().delegate = self
        let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
        UNUserNotificationCenter.current().requestAuthorization(
            options: authOptions,
            completionHandler: { _, _ in }
        )
        
        application.registerForRemoteNotifications()
        
        return true
    }
    
    // Récupération du token FCM
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
}

// Extension pour gérer les notifications
extension AppDelegate: UNUserNotificationCenterDelegate {
    
    // Notification reçue en foreground
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                              willPresent notification: UNNotification,
                              withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([[.banner, .sound, .badge]])
    }
    
    // Action sur notification
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                              didReceive response: UNNotificationResponse,
                              withCompletionHandler completionHandler: @escaping () -> Void) {
        completionHandler()
    }
}
```

### Étape 8 : Build et test

```bash
# Depuis la racine du projet
npx cap sync ios
npx cap open ios
```

Puis compilez depuis Xcode (⌘ + R)

---

## 🔧 Installation du plugin Capacitor

Le plugin `@capacitor/push-notifications` est déjà inclus dans le code. Pour l'installer explicitement :

```bash
npm install @capacitor/push-notifications
npx cap sync
```

---

## 🖥️ Implémentation côté serveur

### API à implémenter

Votre serveur doit exposer un endpoint pour recevoir et stocker les tokens FCM :

**Endpoint existant (déjà implémenté dans le code client)** :
```
GET /sipapp-api/fmc/settoken
```

**Paramètres** :
- `extension` : Numéro de l'extension (ex: `9944105`) - **AVEC le préfixe 99**
- `token_fmc` : Token FCM reçu du client
- `api_key` : Clé d'authentification API (optionnel)

**Exemple d'URL** :
```
https://votre-domaine.com/sipapp-api/fmc/settoken?extension=9944105&token_fmc=abc123def456...&api_key=votre_cle
```

### Exemple d'implémentation serveur (PHP)

**`/sipapp-api/fmc/settoken.php`** :
```php
<?php
header('Content-Type: application/json');

// Récupérer les paramètres
$extension = $_GET['extension'] ?? null;
$token_fcm = $_GET['token_fmc'] ?? null;
$api_key = $_GET['api_key'] ?? null;

// Validation
if (!$extension || !$token_fcm) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Missing parameters']);
    exit;
}

// Vérifier l'API key (optionnel)
if ($api_key !== 'VOTRE_CLE_SECRETE') {
    http_response_code(401);
    echo json_encode(['status' => 'error', 'message' => 'Unauthorized']);
    exit;
}

// Connexion à la base de données
$pdo = new PDO('mysql:host=localhost;dbname=votre_base', 'user', 'password');

// Vérifier si un token existe déjà pour cette extension
$stmt = $pdo->prepare('SELECT id FROM fcm_tokens WHERE extension = ?');
$stmt->execute([$extension]);
$existing = $stmt->fetch();

if ($existing) {
    // Mettre à jour le token existant
    $stmt = $pdo->prepare('UPDATE fcm_tokens SET token = ?, updated_at = NOW() WHERE extension = ?');
    $stmt->execute([$token_fcm, $extension]);
} else {
    // Insérer un nouveau token
    $stmt = $pdo->prepare('INSERT INTO fcm_tokens (extension, token, created_at, updated_at) VALUES (?, ?, NOW(), NOW())');
    $stmt->execute([$extension, $token_fcm]);
}

echo json_encode(['status' => 'success', 'message' => 'Token saved']);
```

### Structure de table SQL

```sql
CREATE TABLE fcm_tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    extension VARCHAR(20) NOT NULL UNIQUE,
    token VARCHAR(500) NOT NULL,
    platform ENUM('android', 'ios', 'web') DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_extension (extension)
);
```

---

## 📤 Envoi de notifications push

### Quand envoyer une notification ?

Envoyez une notification FCM lorsque :
- Un appel entrant arrive pour une extension
- L'utilisateur n'est pas actuellement enregistré SIP
- L'application mobile est en arrière-plan ou fermée

### Exemple d'envoi via l'API Firebase

**Script PHP d'envoi** :
```php
<?php
function sendFCMNotification($token, $caller, $callId) {
    $serverKey = 'VOTRE_SERVER_KEY_FIREBASE'; // Depuis Firebase Console
    
    $notification = [
        'token' => $token,
        'notification' => [
            'title' => 'Appel entrant',
            'body' => 'De: ' . $caller,
            'sound' => 'default'
        ],
        'data' => [
            'type' => 'incoming_call',
            'caller' => $caller,
            'callId' => $callId,
            'timestamp' => time()
        ],
        'android' => [
            'priority' => 'high',
            'notification' => [
                'channel_id' => 'incoming_calls',
                'click_action' => 'FLUTTER_NOTIFICATION_CLICK'
            ]
        ],
        'apns' => [
            'payload' => [
                'aps' => [
                    'alert' => [
                        'title' => 'Appel entrant',
                        'body' => 'De: ' . $caller
                    ],
                    'sound' => 'default',
                    'badge' => 1,
                    'content-available' => 1
                ]
            ]
        ]
    ];
    
    $ch = curl_init('https://fcm.googleapis.com/v1/projects/VOTRE_PROJECT_ID/messages:send');
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Authorization: Bearer ' . getAccessToken(),
        'Content-Type: application/json'
    ]);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode(['message' => $notification]));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);
    
    return ['code' => $httpCode, 'response' => $response];
}

// Fonction pour obtenir le token d'accès OAuth2 (nécessaire pour FCM v1)
function getAccessToken() {
    // Téléchargez le fichier JSON des credentials depuis Firebase Console
    // Paramètres du projet → Comptes de service → Générer une nouvelle clé privée
    
    $serviceAccountJson = file_get_contents('/path/to/service-account.json');
    $serviceAccount = json_decode($serviceAccountJson, true);
    
    // Utiliser la librairie google/auth ou générer le JWT manuellement
    // Voir: https://firebase.google.com/docs/cloud-messaging/auth-server
    
    // Exemple simplifié (utilise google/auth composer package)
    // require 'vendor/autoload.php';
    // $client = new Google_Client();
    // $client->setAuthConfig($serviceAccount);
    // $client->addScope('https://www.googleapis.com/auth/firebase.messaging');
    // return $client->fetchAccessTokenWithAssertion()['access_token'];
    
    return 'YOUR_ACCESS_TOKEN';
}
```

### Alternative : Legacy API (plus simple mais deprecated)

```php
<?php
function sendFCMLegacy($token, $caller, $callId) {
    $serverKey = 'VOTRE_SERVER_KEY'; // Depuis Firebase Console → Paramètres → Cloud Messaging
    
    $data = [
        'to' => $token,
        'notification' => [
            'title' => 'Appel entrant',
            'body' => 'De: ' . $caller,
            'sound' => 'default',
            'priority' => 'high'
        ],
        'data' => [
            'type' => 'incoming_call',
            'caller' => $caller,
            'callId' => $callId
        ],
        'priority' => 'high',
        'content_available' => true
    ];
    
    $ch = curl_init('https://fcm.googleapis.com/fcm/send');
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Authorization: key=' . $serverKey,
        'Content-Type: application/json'
    ]);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    
    $response = curl_exec($ch);
    curl_close($ch);
    
    return $response;
}
```

---

## 🔗 Intégration avec Asterisk

### Dialplan Asterisk

Ajoutez cette logique pour déclencher l'envoi FCM :

```ini
[from-internal]
exten => _99XXX,1,NoOp(Appel vers ${EXTEN})
 same => n,Set(DESTINATION=${EXTEN})
 same => n,GotoIf($[${DEVICE_STATE(PJSIP/${DESTINATION})} = NOT_INUSE]?send_fcm:normal_call)
 
 same => n(send_fcm),NoOp(Extension non enregistrée, envoi FCM)
 same => n,System(php /var/www/html/sipapp-api/fmc/send_notification.php ${DESTINATION} ${CALLERID(num)})
 same => n,Dial(PJSIP/${DESTINATION},30)
 same => n,Hangup()
 
 same => n(normal_call),Dial(PJSIP/${DESTINATION},30)
 same => n,Hangup()
```

---

## ✅ Checklist de vérification

### Android
- [ ] Projet Firebase créé
- [ ] Application Android ajoutée avec le bon Package Name
- [ ] `google-services.json` placé dans `android/app/`
- [ ] `build.gradle` modifié (projet et module)
- [ ] Permissions dans `AndroidManifest.xml`
- [ ] Build réussi sans erreur
- [ ] Token FCM reçu dans les logs

### iOS
- [ ] Application iOS ajoutée avec le bon Bundle ID
- [ ] `GoogleService-Info.plist` dans Xcode
- [ ] Capabilities : Push Notifications + Background Modes
- [ ] Clé APNs créée et uploadée dans Firebase
- [ ] Podfile mis à jour et `pod install` exécuté
- [ ] `AppDelegate.swift` configuré
- [ ] Build réussi sans erreur
- [ ] Token FCM reçu dans les logs

### Serveur
- [ ] Endpoint `/sipapp-api/fmc/settoken` implémenté
- [ ] Table `fcm_tokens` créée en base de données
- [ ] Tokens stockés correctement (vérifier en BDD)
- [ ] Script d'envoi FCM fonctionnel
- [ ] Test d'envoi de notification réussi

---

## 🧪 Tests

### Test du token côté client

Ouvrez la console du navigateur/logcat/Xcode et vérifiez :
```
[FCM] Token reçu: xxxxxxxxxxxxxx
[FCM] ✅ Token envoyé avec succès
```

### Test de l'envoi depuis le serveur

Utilisez un outil comme **Postman** ou **curl** pour envoyer une notification test :

```bash
curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: key=VOTRE_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "TOKEN_FCM_DU_CLIENT",
    "notification": {
      "title": "Test",
      "body": "Notification de test"
    },
    "data": {
      "type": "test"
    }
  }'
```

---

## 📚 Ressources officielles

- **Firebase Console** : https://console.firebase.google.com/
- **Documentation FCM** : https://firebase.google.com/docs/cloud-messaging
- **Capacitor Push Notifications** : https://capacitorjs.com/docs/apis/push-notifications
- **Apple Developer** : https://developer.apple.com/
- **Android Firebase Setup** : https://firebase.google.com/docs/android/setup

---

## ⚠️ Notes importantes

1. **Sécurité** : Ne jamais exposer votre Server Key Firebase dans le code client
2. **Token Management** : Les tokens FCM peuvent changer, d'où l'importance du système de mise à jour automatique
3. **iOS Production** : N'oubliez pas de tester avec un profil de provisioning de production, pas seulement en développement
4. **Taux de livraison** : FCM ne garantit pas 100% de délivrabilité, prévoir un fallback (ex: SMS)
5. **Batterie** : Les notifications push préservent la batterie par rapport à un WebSocket permanent

---

**Dernière mise à jour** : 13 décembre 2025
