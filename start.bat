@echo off
setlocal
echo ===================================================
echo Starting CampusConnect (CGC University Mohali)...
echo ===================================================

cd /d "%~dp0"

REM Refresh PATH from Windows Registry so newly installed Maven is picked up immediately
set "PATH=%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%SystemRoot%\System32\WindowsPowerShell\v1.0\"
for /f "tokens=2*" %%A in ('reg query "HKLM\System\CurrentControlSet\Control\Session Manager\Environment" /v Path 2^>nul') do set "SYS_PATH=%%B"
for /f "tokens=2*" %%A in ('reg query "HKCU\Environment" /v Path 2^>nul') do set "USER_PATH=%%B"
set "PATH=%SYS_PATH%;%USER_PATH%;%PATH%"

REM Check if mvn is available
where mvn >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo Maven detected successfully!
    mvn spring-boot:run -Dspring-boot.run.profiles=h2
    goto done
)

REM Check default Program Files locations
if exist "C:\Program Files\apache-maven-3.9.15\bin\mvn.cmd" (
    echo Running via C:\Program Files\apache-maven-3.9.15...
    "C:\Program Files\apache-maven-3.9.15\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.profiles=h2
    goto done
)

REM Fallback: Search for any installed mvn.cmd
for /d %%D in ("C:\Program Files\apache-maven*") do (
    if exist "%%D\bin\mvn.cmd" (
        echo Running via %%D...
        "%%D\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.profiles=h2
        goto done
    )
)

echo.
echo [NOTE] Maven path not yet recognized in this session.
echo Please run directly from your IDE by clicking the green Run button
echo on CampusConnectApplication.java (which doesn't require terminal Maven).
echo.
pause

:done
pause

