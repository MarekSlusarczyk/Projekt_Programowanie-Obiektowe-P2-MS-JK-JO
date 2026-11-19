@echo off
rem Buduje AutoSerwis.jar. Uruchomienie: java -jar AutoSerwis.jar
rem (z katalogu projektu, aby program widzial folder "dane")
setlocal
cd /d "%~dp0"

set "JAVAC=javac"
set "JAR=jar"
if defined JAVA_HOME (
    set "JAVAC=%JAVA_HOME%\bin\javac"
    set "JAR=%JAVA_HOME%\bin\jar"
)

if exist build rmdir /s /q build
mkdir build\classes

"%JAVAC%" -encoding UTF-8 --release 17 -d build\classes src\*.java || goto :blad
"%JAR%" --create --file AutoSerwis.jar --main-class Main -C build\classes . || goto :blad

echo.
echo Gotowe: AutoSerwis.jar
echo Uruchom: java -jar AutoSerwis.jar
exit /b 0

:blad
echo Budowanie nie powiodlo sie.
exit /b 1
