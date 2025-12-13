# ✅ Modifications effectuées pour l'intégration FCM dans GitHub Actions

## 📝 Fichiers modifiés

### 1. `.github/workflows/android.yml`
**Ajouts :**
- Installation du plugin `@capacitor/push-notifications`
- Décodage automatique de `google-services.json` depuis le secret `GOOGLE_SERVICES_JSON`
- Placement du fichier dans `android/app/google-services.json` avant le build

**Ordre d'exécution :**
```
Install Dependencies
  ↓
Install Capacitor Push Notifications Plugin
  ↓
Create google-services.json from secret ← NOUVEAU
  ↓
Create assets directory
  ↓
Sync Capacitor
  ↓
Build Android APK
```

---

### 2. `.github/workflows/ios.yml`
**Ajouts :**
- Installation du plugin `@capacitor/push-notifications`
- Décodage automatique de `GoogleService-Info.plist` depuis le secret `GOOGLE_SERVICE_INFO_PLIST`
- Placement du fichier dans `ios/App/App/GoogleService-Info.plist`
- Installation des CocoaPods (Firebase/Messaging) avant le build

**Ordre d'exécution :**
```
Install Dependencies
  ↓
Install Capacitor Push Notifications Plugin
  ↓
Create GoogleService-Info.plist from secret ← NOUVEAU
  ↓
Install CocoaPods dependencies ← NOUVEAU
  ↓
Sync Capacitor
  ↓
Build iOS App
```

---

### 3. `android/.gitignore`
**Modification :**
```diff
# Google Services (e.g. APIs or Firebase)
- # google-services.json
+ google-services.json
```
✅ Le fichier `google-services.json` est maintenant **exclu** du versioning Git

---

### 4. `ios/.gitignore`
**Ajout :**
```diff
# Generated Config files
App/App/capacitor.config.json
App/App/config.xml

+ # Firebase
+ App/App/GoogleService-Info.plist
```
✅ Le fichier `GoogleService-Info.plist` est maintenant **exclu** du versioning Git

---

### 5. `.github/GITHUB_SECRETS_SETUP.md` ← NOUVEAU FICHIER
Guide complet pour configurer les secrets GitHub :
- Création des secrets `GOOGLE_SERVICES_JSON` et `GOOGLE_SERVICE_INFO_PLIST`
- Instructions d'encodage Base64 pour Linux/macOS/Windows
- Procédures de dépannage
- Consignes de sécurité

---

## 🔐 Actions requises

### Étape 1 : Télécharger les fichiers Firebase

1. **Android :**
   - Console Firebase → Paramètres → Android → Télécharger `google-services.json`

2. **iOS :**
   - Console Firebase → Paramètres → iOS → Télécharger `GoogleService-Info.plist`

---

### Étape 2 : Encoder en Base64

**Linux/macOS :**
```bash
# Android
base64 -i google-services.json | tr -d '\n' > google-services-base64.txt

# iOS
base64 -i GoogleService-Info.plist | tr -d '\n' > google-service-info-base64.txt
```

**Windows PowerShell :**
```powershell
# Android
[Convert]::ToBase64String([IO.File]::ReadAllBytes("google-services.json")) | Out-File -Encoding ASCII google-services-base64.txt

# iOS
[Convert]::ToBase64String([IO.File]::ReadAllBytes("GoogleService-Info.plist")) | Out-File -Encoding ASCII google-service-info-base64.txt
```

---

### Étape 3 : Créer les secrets GitHub

1. Allez sur : `https://github.com/celya44/CelyaVox-Mobile/settings/secrets/actions`

2. Créez **2 secrets** :

   **Secret 1 :**
   - Name : `GOOGLE_SERVICES_JSON`
   - Value : Contenu de `google-services-base64.txt`

   **Secret 2 :**
   - Name : `GOOGLE_SERVICE_INFO_PLIST`
   - Value : Contenu de `google-service-info-base64.txt`

---

## 🚀 Résultat

Une fois les secrets configurés, **chaque push sur `main`** :

✅ **Android** : Build automatique avec Firebase intégré
- Token FCM récupéré automatiquement
- Notifications push fonctionnelles
- Fichier `google-services.json` injecté à la volée

✅ **iOS** : Build automatique avec Firebase intégré
- Token FCM récupéré automatiquement
- Notifications push fonctionnelles
- Fichier `GoogleService-Info.plist` injecté à la volée
- Pods Firebase installés automatiquement

---

## 🔒 Sécurité

✅ Les fichiers Firebase ne sont **jamais** commités dans Git
✅ Les secrets sont **chiffrés** par GitHub
✅ Les fichiers sont **générés dynamiquement** lors du build
✅ Accès aux secrets limité aux workflows autorisés

---

## 📚 Documentation complète

- [Guide d'implémentation FCM](../FCM_IMPLEMENTATION_GUIDE.md) - Configuration mobile complète
- [Setup des secrets GitHub](../GITHUB_SECRETS_SETUP.md) - Instructions détaillées
- [Documentation GitHub Actions](https://docs.github.com/en/actions)

---

**Prêt à builder ?** Suivez le guide `.github/GITHUB_SECRETS_SETUP.md` ! 🚀
