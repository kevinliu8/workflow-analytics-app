@echo off
set MAVEN_CMD=mvn

where %MAVEN_CMD% >nul 2>nul
if errorlevel 1 (
  echo mvn not found. Please install Maven to run this project. 1>&2
  exit /b 1
)

%MAVEN_CMD% %*
