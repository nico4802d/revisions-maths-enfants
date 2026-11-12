# Spécifications — Application « Révisions Maths Enfants »

Document de référence validé avec le propriétaire du produit.

## 1. Objectif

Application Android installable en sideload (APK, sans Play Store) permettant aux
enfants de réviser les tables d'addition et de multiplication, avec un assistant
vocal. L'IA est **optionnelle** : l'application fonctionne en mode vocal complet
sans aucune clé API.

## 2. Parcours utilisateur

1. **Accueil** : liste des profils enfants, création d'un profil (prénom, âge), niveau
   et XP visibles par profil.
2. **Configuration du quiz** : type d'opération (addition / multiplication / **mixte**),
   nombre de questions (5, 10, 15, 20), difficulté adaptée à l'âge affichée.
3. **Quiz** : question posée à voix haute ; l'enfant répond **au clavier**
   (activé par défaut) ; l'assistant lit le feedback (encouragement ou
   correction) à voix haute.
4. **Résultats** : score, XP gagnés, progression de niveau.
5. **Historique** : mémoire des scores des derniers quiz, par profil.
6. **Réglages** : fournisseur IA (OpenAI ou Mistral AI) et clé API (chiffrée sur
   l'appareil), suppression possible.

## 3. Profils et progression

- Profil par enfant : prénom, âge, date de création.
- XP : 10 par bonne réponse en multiplication, 5 en addition.
- Niveau : 1 + XP / 100, barre de progression, XP restants affichés.
- Chaque quiz est enregistré : date, type d'opération, nombre de questions, bonnes
  réponses, durée, XP gagnés.

## 4. Difficulté par âge (validé)

- ≤ 6 ans : additions jusqu'à 10, multiplications 1-3
- 7 ans : additions jusqu'à 20, tables 2-5
- 8 ans : additions jusqu'à 100, tables 2-9
- ≥ 9 ans : additions à deux chiffres, tables 2-10

## 5. Assistant vocal (v1 : questions et feedbacks oraux, réponse au clavier)

Pipeline par question :
1. **TTS** : question posée à voix haute (TTS OpenAI si configuré, sinon TTS natif Android).
2. **Saisie clavier** : le clavier s'ouvre automatiquement dès la fin de la question ;
   temps de réponse limité (réglable, défaut 10 s).
3. **Vérification locale** : comparaison avec la réponse pré-calculée ; un parseur de
   nombres français accepte aussi les nombres en lettres. **L'IA ne juge jamais.**

> Décision v1 : la capture de la réponse au micro (SpeechRecognizer) a été retirée —
> trop instable sur certains appareils (Samsung A54 notamment). Elle pourra être
> réintroduite dans une version ultérieure.
4. **Pré-prompt système à chaque requête IA** : rôle bienveillant, âge, opération en
   cours avec réponse correcte (usage d'évaluation uniquement), verdict officiel
   (CORRECT / INCORRECT / INCOMPRIS), règles strictes : jamais donner la réponse,
   1-2 phrases courtes en français, pas de nouvelle question.
5. Feedback lu par le TTS, puis question suivante.

**Latence** : la voix (TTS natif) et la vérification sont instantanées ; l'IA (petit
modèle, prompt court) est appelée avec un délai maximal de 8 s — au-delà, repli
immédiat sur les feedbacks fixes natifs. L'enfant n'attend jamais.

## 6. Fournisseurs IA (validé : IA optionnelle, multi-fournisseurs)

- **OpenAI** : `gpt-4o-mini` (chat) + `gpt-4o-mini-tts` (voix IA). Payant à l'usage,
  très économique pour ce cas.
- **Mistral AI** : `mistral-small-latest` (chat), palier gratuit disponible. Pas d'API
  TTS : la voix native Android lit les feedbacks.
- Sans clé API : quiz vocal complet avec feedbacks fixes natifs.

## 7. Architecture technique

- Kotlin + Jetpack Compose (APK direct via Gradle, pas de toolchain externe).
- Room (profils, historique), EncryptedSharedPreferences (clé API).
- Réseau : HttpURLConnection + org.json (zéro dépendance externe), appels sur thread IO.
- UI 100 % française.

## 8. Build et distribution

- APK debug signé, produit par le workflow GitHub Actions « Build APK » à chaque push
  (artifact `revisions-maths-debug-apk`), ou en local via `./gradlew assembleDebug`.
- Tests unitaires : générateur de questions et parseur de nombres français.
