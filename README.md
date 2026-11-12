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
- **Questions et feedbacks à voix haute** (fonctionne **sans IA ni clé API**,
  via la synthèse vocale native Android) :
  - la question est posée oralement, l'enfant répond **au clavier** (activé par
    défaut dès la fin de la question),
  - l'assistant lit ensuite l'encouragement ou la correction à voix haute,
  - vérification **toujours locale** (l'IA ne juge jamais la justesse),
  - parseur de nombres français (« quatre-vingt-dix-neuf » → 99).
- **Temps de réponse réglable** (défaut 10 s, de 5 à 60 s) dans les réglages.
- (La capture de réponse au micro a été retirée de la v1 : trop instable sur
  certains appareils ; elle pourra revenir dans une version ultérieure.)
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
│   ├── NativeTts         Synthèse vocale native Android (voix par défaut)
│   └── AudioPlayer       Lecture des MP3 renvoyés par le TTS OpenAI
└── ui/          Jetpack Compose : accueil, profils, config, quiz, résultats,
                 historique, réglages + ViewModels
```

**Principe clé** : la vérification des réponses est **toujours locale et pré-calculée**.
L'IA ne fait qu'animer la conversation à partir du verdict transmis dans le pré-prompt.

## Build de l'APK

### Via GitHub Releases (recommandé)
1. Poussez le code : le workflow « Build APK » compile et publie automatiquement l'APK.
2. Onglet **Releases** du dépôt → release **APK debug (dernier build)** → `app-debug.apk`.
3. Le fichier est installable directement sur le téléphone
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
