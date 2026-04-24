@echo off
dir /s /b src\*.java > sources.txt
javac -cp "lib/*" -d out @sources.txt
start java -cp "out;lib/*" palantier.Main
