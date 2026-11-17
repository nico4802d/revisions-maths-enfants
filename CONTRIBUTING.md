# Guide de contribution

Merci de votre intérêt pour « Révisions Maths Enfants » ! 🎉
Toute contribution est bienvenue : corrections de bugs, améliorations, idées de
fonctionnalités, traductions ou documentation.

## Comment contribuer

1. **Forkez** le dépôt puis créez votre branche de travail :
   ```bash
   git checkout -b feat/ma-nouvelle-fonctionnalite
   ```
2. **Développez** votre modification en suivant les conventions du projet (voir ci-dessous).
3. **Testez** : les tests unitaires doivent passer.
   ```bash
   ./gradlew testDebugUnitTest
   ```
4. **Compilez** l'APK pour vérifier que tout build :
   ```bash
   ./gradlew assembleDebug
   ```
5. Ouvrez une **Pull Request** vers `main` avec une description claire de votre
   changement et pourquoi il améliore l'application.

Le workflow GitHub Actions (« Build APK ») valide automatiquement chaque PR :
compilation, tests unitaires, production de l'APK.

## Conventions du projet

- **Langue** : le code (identifiants, commentaires) et l'interface sont en
  **français** ; les messages de commit sont en français.
- **Style Kotlin** : [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
  officielles (le projet suit `kotlin.code.style=official`).
- **Architecture** : respecter la séparation en couches :
  - `core/` : logique métier pure (testable sans Android),
  - `data/` : persistance (Room) et préférences,
  - `voice/` : synthèse vocale et clients IA,
  - `ui/` : écrans Compose et ViewModels.
- **Principe clé** : la vérification des réponses est **toujours locale** ;
  l'IA ne fait qu'animer la conversation et ne doit jamais être source de vérité.
- **Tests** : toute nouvelle logique métier (`core/`) doit être couverte par des
  tests unitaires dans `app/src/test/`.
- **Sécurité** : jamais de clé API, de mot de passe ou de secret dans le code
  ou l'historique (voir « Sécurité » ci-dessous).

## Signaler un bug

Ouvrez une **Issue** avec :
1. La version de l'application (voir Réglages ou release),
2. Le modèle de téléphone et la version d'Android,
3. Les étapes pour reproduire le problème,
4. Le comportement attendu vs observé.

## Sécurité

- 🔒 Ne commitez **jamais** de clé API, de token ou de mot de passe.
- Les clés API des utilisateurs sont saisies dans l'application et stockées
  chiffrées sur l'appareil — elles n'ont rien à faire dans le dépôt.
- Une vulnérabilité ? Merci de préférer une Issue privée au propriétaire du
  dépôt plutôt qu'une Issue publique détaillée.

## Licence

En contribuant, vous acceptez que vos contributions soient publiées sous la
[licence MIT](LICENSE) du projet.
