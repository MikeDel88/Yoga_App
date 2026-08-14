# Yoga App

Application full stack Yoga App : API backend en Spring Boot (`back/`) et application frontend en Angular (`front/`).

Chaque partie dispose de son propre README détaillé :
- [back/README.md](back/README.md)
- [front/README.md](front/README.md)

Ce document donne un récapitulatif rapide pour installer, lancer et tester les deux parties.

## Back (`back/`)

### Prérequis
- JDK 21
- Docker + Docker Compose
- Maven 3.9.3 ou plus

### Installation et lancement
Depuis le dossier `back/` :
```
mvn spring-boot:run
```
Cette commande démarre le conteneur Docker de la base de données MySQL puis lance l'API sur le port `8080`.

Voir [back/README.md](back/README.md) pour la procédure détaillée (initialisation de la base, création de l'utilisateur admin par défaut, etc.).

### Tests et couverture
Depuis le dossier `back/` :
```
mvn clean test
```
Cette commande exécute les tests (unitaires et d'intégration) ainsi que les vérifications de couverture JaCoCo. Le build échoue si les seuils ne sont pas atteints.

Rapport de couverture HTML :
```
back/target/site/jacoco/index.html
```

## Front (`front/`)

### Prérequis
- Node.js et npm
- Angular CLI

### Installation et lancement
```
cd front
npm install
npm run start
```
L'application est servie sur `http://localhost:4200`.

### Tests et couverture

**Tests unitaires (Jest)**
```
npm run test
npm run test:coverage
```
Rapport de couverture HTML :
```
front/coverage/jest/lcov-report/index.html
```

**Tests end-to-end (Cypress)**
```
npm run e2e
npm run e2e:coverage
npm run e2e:coverage:check
```
Rapport de couverture HTML :
```
front/coverage/lcov-report/index.html
```

Voir [front/README.md](front/README.md) pour le détail complet (mode watch, mode interactif Cypress, etc.).

## Exigences qualité du projet

Le projet impose, sur le back comme sur le front :
- **80 % minimum** de couverture de code sur tous les indicateurs (instructions, branches, lignes, fonctions/méthodes, classes/complexité) :
  - Back : vérifié par JaCoCo lors de `mvn clean test`.
  - Front : vérifié par Jest (`npm run test:coverage`) et par Cypress/nyc (`npm run e2e:coverage:check`).
- **30 % minimum** des tests doivent être des tests d'intégration :
  - Back : fichiers nommés `*IntegrationTest.java`, ratio vérifié par `TestPyramidRatioTest`.
  - Front : blocs `describe("Tests d'intégration (...)")` dans les specs Jest, ratio vérifié par `src/test-pyramid-ratio.spec.ts`.
