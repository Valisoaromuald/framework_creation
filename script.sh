#!/bin/bash

# === Couleurs ===
GREEN='\033[0;32m'
RED='\033[0;31m'
ORANGE='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# === Paramètres ===
BUILD_PATH="bin"
SRC_PATH="src/java"
LIB_PATH="lib"
DEPLOY_PATH2="$HOME/Documents/S5/MR_Naina/framework/framework_test/lib"
JAR_NAME="frontServlet.jar"

# === Nettoyage du build ===
echo -e "${BLUE}...Nettoyage du dossier build${NC}"
rm -rf "$BUILD_PATH"
mkdir -p "$BUILD_PATH"

# === Compilation des sources Java ===
echo -e "${BLUE}...Compilation des sources Java${NC}"

# Créer le dossier classes
mkdir -p "$BUILD_PATH"

# Récupérer tous les fichiers .java
find "$SRC_PATH" -name "*.java" > source.txt

# Compilation avec toutes les dépendances dans lib
javac -d "$BUILD_PATH" -cp "$LIB_PATH/*" @source.txt
if [ $? -ne 0 ]; then
    echo -e "${RED}\tCompilation échouée${NC}"
    rm source.txt
    exit 1
fi
rm source.txt
echo -e "${GREEN}\tCompilation réussie${NC}"

# === Création du JAR ===
echo -e "${BLUE}...Création du JAR $JAR_NAME${NC}"

cd "$BUILD_PATH" || exit 1
jar -cvf "../$JAR_NAME" ./* > /dev/null
cd ..
echo -e "${GREEN}\t$JAR_NAME créé avec succès${NC}"

# === Déploiement dans le dossier secondaire ===
echo -e "${BLUE}...Déploiement dans $DEPLOY_PATH2${NC}"

# Création du dossier s'il n'existe pas
mkdir -p "$DEPLOY_PATH2"

# Suppression de l'ancien JAR
if [ -f "$DEPLOY_PATH2/$JAR_NAME" ]; then
    echo -e "${ORANGE}\tSuppression de l'ancien JAR${NC}"
    rm -f "$DEPLOY_PATH2/$JAR_NAME"
fi

# Copier le nouveau JAR
cp "$JAR_NAME" "$DEPLOY_PATH2/"
if [ $? -eq 0 ]; then
    echo -e "${GREEN}\tDéploiement réussi dans $DEPLOY_PATH2${NC}"
else
    echo -e "${RED}\tErreur lors du déploiement${NC}"
    exit 1
fi

# === Nettoyage final ===
echo -e "${BLUE}...Nettoyage du dossier build${NC}"
rm -rf "$BUILD_PATH"
echo -e "${GREEN}\tNettoyage terminé${NC}"

echo -e "${ORANGE}...Script terminé${NC}"
