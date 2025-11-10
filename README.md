# 🧩 Test Technique – AxENR / Intégration Axelor ERP

## 🎯 Objectif du test

Ce test vise à évaluer :
- ta capacité à **analyser un problème fonctionnel** et à proposer une **solution technique claire** ;
- ta **rigueur dans le développement** et la **structure de ton code** ;
- ta **compréhension du framework Axelor** et ta capacité à t’y adapter.

⏱ Temps estimé : **2 à 3 heures**  
📅 Délai de rendu : **7 jours** à partir de la réception du test.

Tu peux utiliser **l’IA** pour t’aider, mais tu devras **comprendre et être capable d’expliquer** tes choix en entretien.  

---

## 🧠 Contexte du problème

AxENR intègre l’ERP **Axelor Open Suite** pour des entreprises du secteur **photovoltaïque**.  
Dans le cadre de la planification de projets, nous souhaitons automatiser la **gestion des dates des tâches**.

### Problème à résoudre

Nous voulons mettre en place un **système de calcul automatique des dates** pour un ensemble de tâches liées à un projet.

Chaque tâche comporte :
- une **durée** (en jours),
- un **délai avant démarrage** (en jours, après la fin de la tâche précédente),
- une éventuelle **dépendance à une autre tâche**.

### Objectifs

1. Calculer automatiquement la **date de début** et la **date de fin** de chaque tâche.  
2. Gérer l’**enchaînement des tâches** (exemple : la tâche B démarre après la tâche A + délai avant démarrage).  
3. *(Bonus)* Permettre un **rétroplanning** (calcul des dates à partir d’une date de fin globale).

---

## 🧩 Structure du test

Le test est découpé en **3 parties complémentaires** :

---

### 🧾 Partie 1 – Conception & Réflexion

🗂️ **Livrable attendu :** compléter la section ci-dessous dans ce fichier `README.md`.

Décris en quelques lignes :
- ta **compréhension du problème**,  
- ton **approche technique** (modèle de données, algorithme, logique générale),  
- et les **choix techniques** que tu feras dans la partie suivante.  

Tu peux utiliser des schémas, du pseudocode ou des diagrammes si cela t’aide.

#### ✍️ Section à compléter :

## Partie 1 – Analyse et proposition de solution

 ### Compréhension du problème
Le système doit calculer automatiquement les dates de début et de fin pour un ensemble de tâches liées à un projet photovoltaïque. Chaque tâche possède :

- Une durée (nombre de jours ouvrables)
- Un délai avant démarrage (nombre de jours d'attente après la fin de la tâche précédente)
- Une dépendance optionnelle vers une autre tâche

Le défi principal est de gérer correctement l'enchaînement des tâches en respectant les dépendances et les délais.
Approche technique
#### 1. Modèle de données
Les entités principales sont :

Project : contient une date de début globale et une liste de tâches
Task : contient durée, délai avant démarrage, référence vers la tâche dépendante, dates calculées

#### 2. Algorithme de calcul (approche topologique)
Principe : Parcourir les tâches dans l'ordre de leurs dépendances (tri topologique) pour calculer les dates séquentiellement.
Étapes :

Identifier les tâches sans dépendances (tâches racines)
Pour chaque tâche racine :

Date début = Date début du projet
Date fin = Date début + durée


Pour chaque tâche dépendante :

Date début = Date fin de la tâche parent + délai avant démarrage
Date fin = Date début + durée


Mettre à jour la date de fin du projet = max(dates de fin de toutes les tâches)

#### Gestion des cas particuliers :

Dépendances circulaires : détection et erreur
Tâches orphelines : démarrent à la date de début du projet
Calcul récursif pour gérer les chaînes de dépendances

---

### 💻 Partie 2 – Proof of Concept (POC)

🧪 **Objectif :** implémenter ta logique de calcul dans le langage de ton choix  
(Java, Python, JavaScript, etc.)

Dans le dossier `/poc`, ajoute :
- ton code source ;
- un petit jeu de données (3 à 5 tâches) ;
- et un fichier `README.md` expliquant :
  - comment exécuter ton POC ;
  - et quel résultat on doit obtenir.

L’objectif est de **montrer que ta logique fonctionne indépendamment d’Axelor**.

**Exemple de résultat attendu :**

| Tâche | Durée (J) | Délai avant (J) | Dépend de | Date début | Date fin |
|-------|------------|------------------|------------|-------------|-----------|
| Étude du site | 3 | 0 | - | 01/01 | 03/01 |
| Commande matériel | 2 | 1 | Étude du site | 05/01 | 06/01 |
| Installation | 4 | 0 | Commande matériel | 07/01 | 10/01 |

---

### ⚙️ Partie 3 – Intégration dans Axelor

🧱 **Objectif :** intégrer ta logique dans le framework Axelor à partir d’un code de base fourni.

Tu trouveras dans le dépôt un dossier `axelor/` contenant la base d’un projet fonctionnant avec le framework Axelor.  
Pour des raisons de simplicité, aucun module métier d’Axelor Open Suite (facturation, commandes, etc.) n’a été inclus.  
Seules quelques vues et modèles ont été ajoutés pour te permettre de réaliser cette partie :

- les entités `Task` et `Project` déjà définies (modèle de données et vues XML),  
- la vue des projets avec un **bouton “Calculer les dates”** prêt à être relié,  
- et une action vide (`action-task-compute-dates`).

Un fichier `compose.yml` a également été fourni pour te permettre de démarrer une base de données PostgreSQL si tu n’en as pas sur ta machine.

#### 🔧 Ta mission :
1. Compléter le service `TaskPlanningService` pour :
   - parcourir les tâches d’un projet,
   - calculer les dates (projet et tâches) en fonction de la durée, du délai et des dépendances.
2. Connecter ton service à l’action `action-task-compute-dates`.
3. Vérifier que le calcul fonctionne et que les bonnes dates s’affichent dans la vue du projet.

*(Bonus : implémenter le rétroplanning à partir de la date de fin du projet ou d’une tâche donnée.)*

---

📚 **Documentation utile :**  
Tu trouveras toute la documentation nécessaire pour comprendre et utiliser le framework Axelor ici :  
👉 [https://docs.axelor.com/adk/7.4/index.html](https://docs.axelor.com/adk/7.4/index.html)

---

## 📦 Structure attendue du dépôt

```
axenr-test-[prenom-nom]/
├── README.md                # ce document, avec ta partie 1 complétée
├── poc/                     # ton POC dans le langage de ton choix
│   ├── main.py / main.java / ...
│   └── README.md
└── axelor/                  # projet Axelor contenant le module AxENR modifié avec ta solution
```

---

## 📬 Modalités de rendu

1. Crée un **dépôt GitHub privé** nommé :  
   `axenr-test-[prenom-nom]`
2. Donne les droits de lecture à :  
   - WTFlay
   - ade-axenr
3. Fournis le lien du dépôt avant la date limite indiquée.

---

## 💡 Conseils

- N’hésite pas à modifier les fichiers XML de la partie 3 pour y apporter des améliorations.  
- Pense à **rafraîchir les vues** après tes modifications :  
  `Administration → View Management → All Views → Restore all (toolbar)`  
- Soigne la **clarté et la lisibilité du code** (objectif : *clean code*).  
- Les **commits Git** sont pris en compte : fais-les **clairs et réguliers**.  
- Concentre-toi sur la logique avant la perfection visuelle : la compréhension prime sur la finition.

---

Bonne chance 🍀  
L’équipe **AxENR**
