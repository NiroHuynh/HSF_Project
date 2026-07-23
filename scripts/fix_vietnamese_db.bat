@echo off
chcp 65001 >nul
title CineMax - Fix Vietnamese DB
color 0E

echo ============================================
echo  CineMax - Fix tieng Viet trong Database
echo ============================================
echo.
set /p server="SQL Server [localhost]: " || set server=localhost
set /p db="Database [HSF_PROJECT]: " || set db=HSF_PROJECT
set /p user="Username [sa]: " || set user=sa
set /p pass="Password: "

echo.
echo Dang chay script...
echo.

sqlcmd -S %server% -d %db% -U %user% -P %pass% -i "%~dp0fix_vietnamese_db.sql" -f 65001

if %errorlevel% equ 0 (
    echo.
    color 0A
    echo ============================================
    echo  THANH CONG! D lieu da duoc khOi phuc.
    echo ============================================
) else (
    echo.
    color 0C
    echo ============================================
    echo  THAT BAI! Kiem tra lai thong tin ket noi.
    echo ============================================
)

pause
