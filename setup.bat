@echo off
setlocal


call mvn clean

echo Running Liquibase database migrations...
call mvn liquibase:update
if %errorlevel% neq 0 (
    echo Liquibase update failed!
    exit /b %errorlevel%
)

echo Generating Jooq code...
call mvn jooq-codegen:generate
if %errorlevel% neq 0 (
    echo Jooq code generation failed!
    exit /b %errorlevel%
)

echo Running Spotless formatter...
call mvn spotless:apply
if %errorlevel% neq 0 (
    echo Spotless formatting failed!
    exit /b %errorlevel%
)
echo Setup complete!
exit /b 0