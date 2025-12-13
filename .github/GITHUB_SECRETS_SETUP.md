# Configuration des Secrets GitHub pour FCM

Ce guide explique comment configurer les secrets GitHub nécessaires pour le build automatique avec Firebase Cloud Messaging.

## 📋 Secrets requis

Vous devez créer **2 secrets** dans votre dépôt GitHub :

1. `GOOGLE_SERVICES_JSON` - Pour Android
2. `GOOGLE_SERVICE_INFO_PLIST` - Pour iOS

---

## 🔐 Création des secrets GitHub

### Étape 1 : Accéder aux paramètres du dépôt

1. Allez sur votre dépôt GitHub : `https://github.com/celya44/CelyaVox-Mobile`
2. Cliquez sur **Settings** (Paramètres)
3. Dans le menu de gauche, cliquez sur **Secrets and variables** → **Actions**
4. Cliquez sur **New repository secret**

---

## 🤖 Secret Android : `GOOGLE_SERVICES_JSON`

### Préparation du fichier

1. **Téléchargez** `google-services.json` depuis la console Firebase :
   - Console Firebase → Paramètres du projet → Vos applications → Android
   - Cliquez sur "Télécharger google-services.json"

2. **Encodez le fichier en Base64** :

   **Sur Linux/macOS :**
   ```bash
   base64 -i google-services.json | tr -d '\n' > google-services-base64.txt
   ```

   **Sur Windows (PowerShell) :**
   ```powershell
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("google-services.json")) | Out-File -Encoding ASCII google-services-base64.txt
   ```

   **Ou en ligne :**
   - Allez sur https://www.base64encode.org/
   - Uploadez votre fichier `google-services.json`
   - Copiez le résultat encodé

3. **Copiez le contenu** du fichier `google-services-base64.txt`

### Création du secret

1. Dans GitHub, cliquez sur **New repository secret**
2. Name : `GOOGLE_SERVICES_JSON`
3. Value : Collez le contenu encodé en Base64
4. Cliquez sur **Add secret**

---

## 🍎 Secret iOS : `GOOGLE_SERVICE_INFO_PLIST`

### Préparation du fichier

1. **Téléchargez** `GoogleService-Info.plist` depuis la console Firebase :
   - Console Firebase → Paramètres du projet → Vos applications → iOS
   - Cliquez sur "Télécharger GoogleService-Info.plist"

2. **Encodez le fichier en Base64** :

   **Sur Linux/macOS :**
   ```bash
   base64 -i GoogleService-Info.plist | tr -d '\n' > google-service-info-base64.txt
   ```

   **Sur Windows (PowerShell) :**
   ```powershell
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("GoogleService-Info.plist")) | Out-File -Encoding ASCII google-service-info-base64.txt
   ```

   **Ou en ligne :**
   - Allez sur https://www.base64encode.org/
   - Uploadez votre fichier `GoogleService-Info.plist`
   - Copiez le résultat encodé

3. **Copiez le contenu** du fichier `google-service-info-base64.txt`

### Création du secret

1. Dans GitHub, cliquez sur **New repository secret**
2. Name : `GOOGLE_SERVICE_INFO_PLIST`
3. Value : Collez le contenu encodé en Base64
4. Cliquez sur **Add secret**

---

## ✅ Vérification

Une fois les secrets créés, vous devriez voir :

```
GOOGLE_SERVICES_JSON          Updated X minutes ago
GOOGLE_SERVICE_INFO_PLIST     Updated X minutes ago
```

---

## 🚀 Fonctionnement

Lorsque vous poussez du code sur la branche `main` :

1. **Workflow Android** (`.github/workflows/android.yml`)
   - Décode `GOOGLE_SERVICES_JSON`
   - Place le fichier dans `android/app/google-services.json`
   - Build l'APK avec FCM intégré

2. **Workflow iOS** (`.github/workflows/ios.yml`)
   - Décode `GOOGLE_SERVICE_INFO_PLIST`
   - Place le fichier dans `ios/App/App/GoogleService-Info.plist`
   - Installe les pods Firebase
   - Build l'app avec FCM intégré

---

## 🔒 Sécurité

- ⚠️ **Ne commitez JAMAIS** les fichiers `google-services.json` ou `GoogleService-Info.plist` dans Git
- Ajoutez-les au `.gitignore` :
  ```gitignore
  # Firebase
  android/app/google-services.json
  ios/App/App/GoogleService-Info.plist
  ```
- Les secrets GitHub sont chiffrés et ne sont accessibles que pendant l'exécution des workflows
- Seuls les collaborateurs avec accès "Write" peuvent voir ou modifier les secrets

---

## 🐛 Dépannage

### Erreur "base64: invalid input"

Le fichier Base64 contient des caractères invalides (retours à la ligne). Utilisez la commande avec `tr -d '\n'` pour les supprimer.

### Erreur "google-services.json not found"

Le décodage Base64 a échoué. Vérifiez que :
1. Le secret existe bien dans GitHub
2. Le contenu est correctement encodé en Base64
3. Le nom du secret correspond exactement (sensible à la casse)

### Le build échoue après l'ajout des fichiers

Vérifiez que :
1. Votre fichier Firebase correspond bien à votre Package Name (Android) / Bundle ID (iOS)
2. Les dépendances Gradle sont à jour (`build.gradle`)
3. Le Podfile inclut bien `pod 'Firebase/Messaging'`

---

## 📚 Ressources

- [Documentation GitHub Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [Firebase Console](https://console.firebase.google.com/)
- [Guide d'implémentation FCM complet](../FCM_IMPLEMENTATION_GUIDE.md)

---

**Dernière mise à jour** : 13 décembre 2025
