@echo off
echo.
echo [信息] 安装依赖，生成node_modules文件。
echo.

%~d0
cd %~dp0

cd ..
pnpm i

pause
