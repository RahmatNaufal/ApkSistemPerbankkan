@echo off
setlocal
set ROOT=%~dp0..
set OUT=%ROOT%\out

if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%" || exit /b 1

echo === Menyusun domain OOP dengan javac ===
pushd "%ROOT%\domain-reference\src\com\intbank\domain" || exit /b 1
javac -encoding UTF-8 -d "%OUT%" *.java
if errorlevel 1 (popd & exit /b 1)
popd

javac -encoding UTF-8 -cp "%OUT%" -d "%OUT%" "%ROOT%\domain-reference\DomainDemo.java" || exit /b 1

echo.
echo === Menjalankan 22 pemeriksaan domain ===
java -cp "%OUT%" DomainDemo
endlocal
