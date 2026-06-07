@REM @echo off
@REM setlocal

@REM set "MVN_HOME=C:\Users\2494470\.m2\wrapper\dists\apache-maven-3.9.10-bin\53h08a94dg6djh6umvruv7q564\apache-maven-3.9.10"
@REM set "PATH=%MVN_HOME%\bin;%PATH%"

@REM call "%MVN_HOME%\bin\mvn.cmd" %*


@echo off
setlocal
set "DIR=%~dp0"
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "MVN_HOME=%DIR%.m2\wrapper\dists\apache-maven-3.9.10-bin\53h08a94dg6djh6umvruv7q564\apache-maven-3.9.10"

if not exist "%MVN_HOME%\bin\mvn.cmd" (
    echo ============================================================
    echo           Downloading Maven local wrapper...
    echo ============================================================
    powershell -Command "New-Item -ItemType Directory -Force -Path '%DIR%.m2\wrapper\dists' | Out-Null"
    powershell -Command "Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.10/binaries/apache-maven-3.9.10-bin.zip' -OutFile '%DIR%maven.zip'"
    powershell -Command "Expand-Archive -Path '%DIR%maven.zip' -DestinationPath '%DIR%.m2\wrapper\dists\apache-maven-3.9.10-bin\53h08a94dg6djh6umvruv7q564' -Force"
    powershell -Command "Remove-Item -Force '%DIR%maven.zip'"
)

set "PATH=%MVN_HOME%\bin;%PATH%"
call "%MVN_HOME%\bin\mvn.cmd" %*