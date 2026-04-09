@echo off
g++ main.cpp scheduler.cpp graph.cpp -o SmartPrepScheduler.exe
if %errorlevel% neq 0 (
    echo Compilation failed.
) else (
    echo Compilation successful!
    echo Run "SmartPrepScheduler.exe" to start the program.
)
