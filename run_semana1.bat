@echo off
echo ==========================================
echo  Build e Execucao - Semana 1 (Ingnuo)
echo ==========================================

if not exist out mkdir out

echo Compilando...
javac --release 17 -encoding UTF-8 -d out src\semana1\Pedido.java src\semana1\FilaDePedidos.java src\semana1\Garcom.java src\semana1\Cozinheiro.java src\semana1\Restaurante.java

if %errorlevel% neq 0 (
    echo ERRO na compilacao!
    exit /b 1
)

echo Compilacao OK.
echo.
echo Executando simulacao...
echo.
java -cp out semana1.Restaurante
