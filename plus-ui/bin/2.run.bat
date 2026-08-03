@echo off
echo.
echo [信息] 使用 Vite 命令启动项目。
echo.

%~d0
cd %~dp0

cd ..
pnpm run dev

pause
