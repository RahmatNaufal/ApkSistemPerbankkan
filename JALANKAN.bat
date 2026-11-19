@echo off
setlocal
:menu
cls
echo ==================================================
echo    SISTEM PERBANKAN - INT BANK (Domain OOP)
echo ==================================================
echo.
echo    1. Demo 22 pemeriksaan (kompilasi + jalankan)
echo    2. Unit test JUnit (34 test)
echo    3. Buka proyek di VS Code
echo    4. Keluar
echo.
set /p pil="Pilih angka (1-4): "

if "%pil%"=="1" (
    call "%~dp0demo\jalankan-demo.bat"
    echo.
    pause
    goto menu
)
if "%pil%"=="2" (
    call "%~dp0demo\jalankan-test.bat"
    goto menu
)
if "%pil%"=="3" (
    start "" code "%~dp0"
    goto menu
)
if "%pil%"=="4" (
    endlocal
    exit /b 0
)

echo.
echo Pilihan tidak valid. Coba lagi.
pause
goto menu
