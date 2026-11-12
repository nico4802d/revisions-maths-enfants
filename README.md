# Révisions Maths Enfants

Application Android (installation directe par APK, sans Play Store) pour réviser les
**tables d'addition** et de **multiplication**, avec un **assistant vocal** optionnel
propulsé par une IA (OpenAI ou Mistral AI).

## Fonctionnalités

- **Profils enfants** : prénom, âge, un profil par enfant.
- **Niveaux et XP** : 10 XP par bonne réponse en multiplication, 5 XP en addition,
  100 XP par niveau, barre de progression visible.
- **Historique** : mémoire des scores des derniers quiz (date, type, score, durée, XP).
- **Quiz adaptés à l'âge** :
  - ≤ 6 ans : additions jusqu'à 10, petites multiplications
  - 7 ans : additions jusqu'à 20, tables 2 à 5
  - 8 ans : additions jusqu'à 100, tables 2 à 9
  - ≥ 9 ans : additions à deux chiffres, tables 2 à 10
- **Types de quiz** : addition, multiplication ou **mixte**, 5 à 20 questions.
- **Conversation vocale complète** (fonctionne **sans IA ni clé API**) :
  - questions posées à voix haute (TTS natif Android),
  - réponses de l'enfant captées au micro (reconnaissance vocale native),
  - vérification **toujours locale** (l'IA ne juge jamais la justesse),
  - parseur de nombres français (« quatre-vingt-dix-neuf » → 99).
- **Saisie tactile toujours disponible** en parallèle du micro.
- **Assistant IA optionnel** (OpenAI ou Mistral AI) : feedbacks personnalisés générés
  à partir d'un pré-prompt strict (jamais donner la réponse, encourager, phrases courtes).
  En cas d'absence, de lenteur ou d'échec de l'IA : repli immédiat sur les feedbacks
  natifs — l'enfant n'attend jamais.

## Architecture

```
app/src/main/java/com/nico/revisionsmaths/
├── core/        Générateur de questions (par âge), parseur de nombres français
├── data/
│   ├── db/      Room : profils, résultats de quiz (profilDao, quizResultDao)
│   ├── model/   Types d'opérations, Question, QuizConfig
│   └── SettingsStore (clé API chiffrée via EncryptedSharedPreferences, fournisseur IA)
├── voice/
│   ├── AiClient          API chat-completions multi-fournisseurs + TTS OpenAI
│   ├── AssistantPrompts  Pré-prompt système (règles strictes, verdict local)
│   ├── VoiceController   Reconnaissance vocale native Android (STT)
│   ├── NativeTts         Synthèse vocale native Android (secours par défaut)
│   └── AudioPlayer       Lecture des MP3 renvoyés par le TTS OpenAI
└── ui/          Jetpack Compose : accueil, profils, config, quiz, résultats,
                 historique, réglages + ViewModels
```

**Principe clé** : la vérification des réponses est **toujours locale et pré-calculée**.
L'IA ne fait qu'animer la conversation à partir du verdict transmis dans le pré-prompt.

## Build de l'APK

### Via GitHub Actions (recommandé)
1. Poussez le code ou lancez `Build APK` depuis l'onglet **Actions** du dépôt.
2. À la fin du workflow, téléchargez l'artifact `revisions-maths-debug-apk`.
3. Le fichier `app-debug.apk` est installable directement sur le téléphone
   (autoriser « Installer des applications inconnues » dans les réglages Android).

### En local
```bash
./gradlew assembleDebug
# APK : app/build/outputs/apk/debug/app-debug.apk
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Configuration de l'assistant IA (optionnel)

Dans l'app : **Réglages** → choisir le fournisseur (OpenAI ou Mistral AI) → coller la
clé API. La clé est stockée chiffrée sur l'appareil (EncryptedSharedPreferences) et
n'est **jamais** versionnée dans le dépôt.

- OpenAI : API payante à l'usage, `gpt-4o-mini` très économique (< 1 centime par quiz).
- Mistral AI : palier gratuit disponible sur console.mistral.ai (après vérification
  par téléphone), largement suffisant pour des quizzes.

Sans clé, l'application reste pleinement utilisable en mode vocal (voix native Android).

## Tests

```bash
./gradlew testDebugUnitTest
```

Couvrent le générateur de questions (plages par âge, cohérence des réponses) et le
parseur de nombres français (pièges : soixante-dix, quatre-vingt-dix-neuf, cent quatre…).
