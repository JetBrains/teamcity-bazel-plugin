@echo off
rem This is a script to checkout bundled plugins

set BASEDIR=%~dp0
set BASEDIR=%~dp0

set LOCAL_GIT_REPO=rx
echo Updating %LOCAL_GIT_REPO%
if not exist %LOCAL_GIT_REPO%/.git (
	git clone git@github.com:DevTeam/rx.git -b master %LOCAL_GIT_REPO%
) else (
	cd %LOCAL_GIT_REPO%
	rem Add "pull --tags" or "pull origin master" if you need to.
	git pull
	cd %BASEDIR%
)