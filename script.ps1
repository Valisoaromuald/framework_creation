# ==========================================
# Configuration
# ==========================================

$SrcPath = "src/java"
$LibPath = "lib"
$BuildPath = "bin"

$JarName = "frontServlet.jar"

# Modifier ce chemin
$DeployPath = "C:\Users\WINDOWS 10\Documents\Projets\framework\framework_test\lib"

# ==========================================
# Couleurs
# ==========================================

function Info($msg) {
    Write-Host $msg -ForegroundColor Cyan
}

function Success($msg) {
    Write-Host $msg -ForegroundColor Green
}

function Warning($msg) {
    Write-Host $msg -ForegroundColor Yellow
}

function ErrorExit($msg) {
    Write-Host $msg -ForegroundColor Red
    exit 1
}

# ==========================================
# Vérification Java
# ==========================================

Info "Vérification de Java..."

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    ErrorExit "javac est introuvable. Vérifiez JAVA_HOME."
}

if (-not (Get-Command jar -ErrorAction SilentlyContinue)) {
    ErrorExit "jar est introuvable."
}

# ==========================================
# Nettoyage
# ==========================================

Info "Nettoyage du dossier build..."

if (Test-Path $BuildPath) {
    Remove-Item $BuildPath -Recurse -Force
}

New-Item -ItemType Directory -Path $BuildPath | Out-Null

# ==========================================
# Compilation
# ==========================================

Info "Compilation des sources Java..."

$Sources = Get-ChildItem $SrcPath -Filter *.java -Recurse |
           ForEach-Object { $_.FullName }

& javac `
    -d $BuildPath `
    -cp "$LibPath/*" `
    $Sources

if ($LASTEXITCODE -ne 0) {
    ErrorExit "Compilation échouée."
}

Success "Compilation réussie."

# ==========================================
# Création du JAR
# ==========================================

Info "Création du JAR..."

Push-Location $BuildPath

& jar -cvf "../$JarName" * | Out-Null

if ($LASTEXITCODE -ne 0) {
    Pop-Location
    ErrorExit "Impossible de créer le JAR."
}

Pop-Location

Success "$JarName créé."

# ==========================================
# Déploiement
# ==========================================

Info "Déploiement..."

if (!(Test-Path $DeployPath)) {
    New-Item -ItemType Directory -Path $DeployPath | Out-Null
}

$Destination = Join-Path $DeployPath $JarName

if (Test-Path $Destination) {
    Warning "Suppression de l'ancien JAR..."
    Remove-Item $Destination -Force
}

Copy-Item $JarName $DeployPath -Force

Success "Déploiement réussi."

# ==========================================
# Nettoyage
# ==========================================

Info "Nettoyage..."

Remove-Item $BuildPath -Recurse -Force

Success "Nettoyage terminé."

Write-Host ""
Success "Script terminé."