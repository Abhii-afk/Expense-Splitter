@echo off
setlocal
if not exist out (
  mkdir out
)
echo Compiling Java sources...
javac -d out com\expensesplitter\*.java com\expensesplitter\model\*.java com\expensesplitter\service\*.java com\expensesplitter\ui\*.java com\expensesplitter\util\*.java
if %ERRORLEVEL% NEQ 0 (
  echo.
  echo Compilation failed.
  pause
  exit /b %ERRORLEVEL%
)
echo.
echo Running Expense Splitter...
java -cp out com.expensesplitter.Main
endlocal