# Configurer la signature iOS pour GitHub Actions

Actuellement, le workflow `ios.yml` génère une version non signée (pour verification). Pour générer des `.ipa` installables (TestFlight / App Store), vous devrez configurer la signature.

## 1. Prérequis Apple Developer
- Un compte Apple Developer actif.
- Un certificat de distribution (`.p12`).
- Un profil de provisionnement (`.mobileprovision`).

## 2. Ajouter les Secrets GitHub
Dans votre repo GitHub -> **Settings** -> **Secrets and variables** -> **Actions**, ajoutez :

- `BUILD_CERTIFICATE_BASE64`: Le contenu de votre fichier `.p12` converti en base64.
  ```bash
  base64 -i certificat.p12 | pbcopy
  ```
- `P12_PASSWORD`: Le mot de passe du fichier `.p12`.
- `BUILD_PROVISION_PROFILE_BASE64`: Le contenu du `.mobileprovision` en base64.
- `KEYCHAIN_PASSWORD`: Un mot de passe aléatoire pour le trousseau temporaire du runner.

## 3. Mettre à jour le workflow
Décommentez ou ajoutez les étapes de signature dans `ios.yml` (utilisation de `apple-actions/import-codesign-certs` par exemple) et retirez `CODE_SIGNING_ALLOWED=NO`.
