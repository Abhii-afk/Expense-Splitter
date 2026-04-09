@echo off
setlocal
cd /d "D:\ONE DRIVE\OneDrive\Desktop\Java_PBL"
if not exist out mkdir out
echo Compiling Java sources...
javac -d out com\expensesplitter\*.java com\expensesplitter\model\*.java com\expensesplitter\service\*.java com\expensesplitter\ui\*.java com\expensesplitter\util\*.java
if %ERRORLEVEL% NEQ 0 (
  echo Compilation failed with error code %ERRORLEVEL%
  exit /b %ERRORLEVEL%
)
echo Compilation successful
echo Running main class...
java -cp out com.expensesplitter.Main
endlocal
