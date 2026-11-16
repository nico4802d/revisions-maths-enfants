# Changelog

Toutes les évolutions notables de l'application « Révisions Maths Enfants ».

## v0.5.0 — Feedbacks variés et ambiance classeur

- **Feedbacks variés** : prompt IA enrichi (interdiction de répéter une formulation), 12 encouragements et 8 messages d'erreur types tirés au hasard en mode natif.
- **Emojis interdits dans les messages vocaux** : règle ajoutée au prompt + filtre de sécurité qui retire tout emoji avant la lecture vocale (le TTS Google les décrit mot à mot).
- **Fond feuille de classeur** : lignes bleues et marge rouge sur l'écran de quiz.
- **Surprise finale** : célébration spéciale (étoiles, félicitations) pour une note de 16/20 (80 %) ou plus.

## v0.4.4 — Unicité des questions

- **Historique des questions** : plus jamais la même question exacte deux fois dans un quiz ; les inversions restent autorisées (8×9 et 9×8 peuvent coexister).
- **Mode table fixe sans remise** : table des 8 sur 10 questions = chacun des 8×1 à 8×10 exactement une fois.
- Pastilles de tables 1-12 réalignées (largeur standardisée).

## v0.4.3 — Pause et ergonomie clavier

- **Pause automatique** : à la sortie de l'application ou au verrouillage de l'écran, la voix s'arrête et le chronomètre gèle ; bannière « ⏸️ Quiz en pause » avec bouton Reprendre au retour.
- **Padding clavier (imePadding)** : les messages d'encouragement ne passent plus sous le clavier.
- Pastilles de tables standardisées (première itération).

## v0.4.2 — Tables jusqu'à 12 et compaction

- **Tables fixes jusqu'à 12** (1-6 et 7-12 sur deux lignes).
- **Écran quiz compacté** : marges et tailles réduites pour tout afficher clavier ouvert.
- **Champ effacé au timeout** : la saisie non validée est vidée au dépassement du temps.

## v0.4.1 — Sélecteur de table sur 2 lignes

- Les pastilles 1 à 10 réparties sur deux lignes pour être toutes visibles.

## v0.4.0 — Mode table fixe

- **Nouveau mode « table fixe »** : réviser uniquement la table de multiplication ou d'addition d'un nombre choisi (1 à 10) — ex. toutes les questions dans la table des 8.
- **Suppression des +0** pour les enfants de 7 ans et plus (trop enfantin) ; les opérandes sont désormais ≥ 1.

## v0.3.3 — Bouton Répéter visible

- Bouton « Répéter la question » déplacé au-dessus du champ de réponse pour rester visible clavier ouvert.

## v0.3.2 — Réponse pendant l'énoncé

- **Validation possible pendant l'énoncé oral** : la réponse soumise interrompt la voix et passe directement au feedback.

## v0.3.1 — Touche OK du clavier

- La touche ✓/OK du clavier valide la réponse (action « Terminé »), au même titre que le bouton OK à l'écran.

## v0.3.0 — V1 : sans capture micro

- **Décision v1** : retrait de la capture de réponse au micro (SpeechRecognizer), trop instable sur certains appareils (Samsung A54 notamment).
- Les questions et les feedbacks restent lus à voix haute ; les réponses se saisissent **au clavier, activé par défaut** dès la fin de la question.
- Suppression de la permission micro et de tout le pipeline STT.

## v0.2.4 — Corrections micro (dernière itération STT)

- Erreurs micro obsolètes ignorées (identifiant de session), transcription vide relancée automatiquement, silence de fin d'écoute allongé.

## v0.2.3 — Machine d'état micro

- Relance unique (plus de boucle de redémarrage), compteur d'échecs par question, durcissement Samsung (cancel avant destroy).

## v0.2.2 — APK en release GitHub

- L'APK est publié en **release GitHub** (au lieu d'un artifact d'actions) : lien permanent, toujours dernière version.
- Fin des actions Node 20 dans le CI.

## v0.2.1 — Décompte réglable

- **Temps de réponse réglable** dans les paramètres (5 à 60 s, défaut 10 s), pastille colorée vert/orange/rouge.
- **Écoute automatique** du micro dès la fin de la question posée.
- Relances micro sur erreurs réseau (codes 1, 2, 11).

## v0.2.0 — Interface colorée

- Palette vive (violet, rose, teal), avatars animaux par profil, cartes pastel, dégradés sur la question, boutons colorés.
- Thème sombre assorti.

## v0.1.0 — Version initiale

- Application Android (Kotlin + Jetpack Compose), installation directe par APK sans Play Store.
- **Profils enfants** (prénom, âge), niveaux et XP (10 XP multiplication, 5 addition, 100 XP par niveau).
- **Quiz adaptés à l'âge** : addition, multiplication ou mixte, 5 à 20 questions.
- **Questions et feedbacks oraux** (TTS natif Android), saisie clavier en parallèle.
- **Assistant IA optionnel** : OpenAI ou Mistral AI, pré-prompt strict (l'IA ne juge jamais, ne révèle jamais la réponse), clé API chiffrée sur l'appareil.
- **Historique des scores** par profil (Room).
- Parseur de nombres français (« quatre-vingt-dix-neuf » → 99).
- CI GitHub Actions : build APK + tests unitaires à chaque push.
