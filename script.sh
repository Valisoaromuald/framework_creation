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
    echo -e "${GREEN}\tCompilation reussie${NC}\n"
else
    echo -e "${RED}\tCompilation echouée${NC}\n"
    rm source.txt
    exit 1
fi

rm source.txt

echo -e "${BLUE}...Création du JAR${NC}\n"

cd "$BUILD_PATH" || exit 1

if jar -cvf "../$JAR_NAME.jar" ./* ; then
    echo -e "${GREEN}\tArchivage Reussi${NC}\n"
else
    echo -e "${RED}\tErreur de l'archivage${NC}\n"
    exit 1
fi

cd ..

echo -e "${BLUE}...Déploiement dans Tomcat/lib${NC}\n"

if mv -f "$JAR_NAME.jar" "$TOMCAT_PATH/lib/" ; then
    echo -e "${GREEN}\tDéploiement Reussi${NC}\n"
else
    echo -e "${RED}\tDéploiement échoué${NC}\n"
    exit 1
fi

if pgrep -f tomcat > /dev/null; then
    if [ -n "$one" ]; then
        if [ "$one" = 0 ]; then 
            echo -e "$RED\t...Fermeture du Tomcat$NC\n"
            "${TOMCAT_PATH}/bin/shutdown.sh" ; echo ""
        else
            echo -e "${RED}Parametre inconnu${NC}\n"
        fi 
    fi
else
    echo -e "$RED\t...Lancement du Tomcat\n$NC"
    "${TOMCAT_PATH}/bin/startup.sh" ; echo ""
fi

echo -e "$ORANGE\t...Fin du script$NC"
