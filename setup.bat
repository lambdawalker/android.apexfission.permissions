@echo off
setlocal DisableDelayedExpansion
set "reference="
for /f "tokens=2" %%R in ('findstr /b "tooling_ref:" "%~dp0.project\setup\config.yaml"') do set "reference=%%R"
set "reference=%reference:'=%"
set "reference=%reference:"=%"
if not defined reference goto invalid
set "option=--tag"
echo(%reference%| findstr /r /x "v[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*" >nul
if not errorlevel 1 goto install
set "option=--rev"
if "%reference:~39,1%"=="" goto invalid
if not "%reference:~40,1%"=="" goto invalid
echo(%reference%| findstr /r /x "[0-9a-f][0-9a-f]*" >nul
if errorlevel 1 goto invalid
:install
call cargo install --locked --git https://github.com/lambdawalker/gradle.android-repo-setup.git %option% %reference% android-repo-setup
if errorlevel 1 exit /b %ERRORLEVEL%
call android-repo-setup --project "%~dp0." %*
exit /b %ERRORLEVEL%
:invalid
echo Invalid tooling_ref in .project/setup/config.yaml >&2
exit /b 1
