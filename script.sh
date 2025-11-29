#!/bin/bash

one=$1

GREEN='\033[0;32m'
RED='\033[0;31m'
ORANGE='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BUILD_PATH="bin/"
LIBRARY_PATH="lib/"
TOMCAT_PATH="$HOME/Documents/S5/apache-tomcat-10.1.28"
JAR_NAME="frontServlet"
DEPLOY_PATH2="$HOME/Documents/S5/MR_Naina/framework/framework_test/lib"   # <-- Dossier secondaire

if [ "$one" = -1 ]; then
    echo -e "$RED\t...Fermeture du Tomcat$NC\n"
    "${TOMCAT_PATH}/bin/shutdown.sh" ; echo ""
    exit 1
fi

echo -e "$ORANGE\t...Lancement du Script\n$NC"

echo -e "$BLUE...Compilation du code Java$NC\n"

rm -rf "$BUILD_PATH"*

find src/java -name "*.java" > source.txt

if javac -d "$BUILD_PATH" -cp "$LIBRARY_PATH/*" @source.txt; then
    echo -e "${GREEN}\tCompilation réussie${NC}\n"
else
    echo -e "${RED}\tCompilation échouée${NC}\n"
    rm source.txt
    exit 1
fi

rm source.txt

echo -e "${BLUE}...Création du JAR${NC}\n"

cd "$BUILD_PATH" || exit 1

if jar -cvf "../$JAR_NAME.jar" ./* ; then
    echo -e "${GREEN}\tArchivage Réussi${NC}\n"
else
    echo -e "${RED}\tErreur de l'archivage${NC}\n"
    exit 1
fi

cd ..

echo -e "${BLUE}...Déploiement dans Tomcat/lib${NC}\n"

# --- Suppression ancien jar dans Tomcat/lib
if [ -f "$TOMCAT_PATH/lib/$JAR_NAME.jar" ]; then
    echo -e "${ORANGE}\tSuppression de l'ancien JAR dans Tomcat/lib${NC}"
    rm -f "$TOMCAT_PATH/lib/$JAR_NAME.jar"
fi

# --- Copie du nouveau jar
if mv -f "$JAR_NAME.jar" "$TOMCAT_PATH/lib/" ; then
    echo -e "${GREEN}\tDéploiement Réussi dans Tomcat/lib${NC}\n"
else
    echo -e "${RED}\tDéploiement échoué dans Tomcat/lib${NC}\n"
    exit 1
fi

# --- Déploiement secondaire
echo -e "${BLUE}...Déploiement secondaire dans $DEPLOY_PATH2${NC}\n"

# Création du dossier s’il n’existe pas
mkdir -p "$DEPLOY_PATH2"

# Suppression de l'ancien jar
if [ -f "$DEPLOY_PATH2/$JAR_NAME.jar" ]; then
    echo -e "${ORANGE}\tSuppression de l'ancien JAR dans le déploiement secondaire${NC}"
    rm -f "$DEPLOY_PATH2/$JAR_NAME.jar"
fi

# Copie du nouveau jar
if cp "$TOMCAT_PATH/lib/$JAR_NAME.jar" "$DEPLOY_PATH2/" ; then
    echo -e "${GREEN}\tDéploiement secondaire réussi${NC}\n"
else
    echo -e "${RED}\tDéploiement secondaire échoué${NC}\n"
    exit 1
fi

# --- Gestion du Tomcat
if pgrep -f tomcat > /dev/null; then
    if [ -n "$one" ]; then
        if [ "$one" = 0 ]; then 
            echo -e "$RED\t...Fermeture du Tomcat$NC\n"
            "${TOMCAT_PATH}/bin/shutdown.sh" ; echo ""
        else
            echo -e "${RED}Paramètre inconnu${NC}\n"
        fi 
    fi
else
    echo -e "$RED\t...Lancement du Tomcat\n$NC"
    "${TOMCAT_PATH}/bin/startup.sh" ; echo ""
fi

echo -e "$ORANGE\t...Fin du script$NC"
