@echo off
setlocal
cd /d "%~dp0.."

echo === Menjalankan 34 unit test JUnit ===
call mvnw.cmd test

echo.
if errorlevel 1 (
    echo TEST GAGAL - periksa pesan di atas.
) else (
    echo TEST LULOS - 34 test, 0 gagal, 0 error.
)
pause
endlocal
