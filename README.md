# Proyecto Final - Calculadora de Derivadas e Integrales

[![Java](https://img.shields.io/badge/Java-%23ED8B00.svg?logo=openjdk&logoColor=white)](#)

Calculadora gráfica que resuelve **derivadas e integrales** (indefinidas y definidas) de polinomios, con selección de la variable (`x` o `y`), desarrollada en Java con programación orientada a objetos.

Proyecto del cuarto semestre de Ingeniería en Sistemas
Universidad Mariano Gálvez de Guatemala - Programación II

## Funcionalidades

**Requeridas**
- Derivada de un polinomio respecto a `x` o `y`.
- Integral indefinida (`F(x) = ... + C`).
- Integral definida entre dos límites (regla de Barrow).
- Interfaz gráfica con manual de usuario integrado en la aplicación.
- Manejo de errores con mensajes claros (función mal escrita, límites inválidos).

**Extras**
- Pasos del procedimiento, término por término.
- Gráfica de la función y del área bajo la curva.
- Historial de operaciones, con respaldo opcional en MySQL.
- Botón para copiar el resultado.

## Estructura del proyecto

```
src/
├── modelo/        Termino, Polinomio y las interfaces Derivable e Integrable
├── logica/        ParserPolinomio, Calculadora e Historial
├── datos/         HistorialDAO (acceso opcional a MySQL)
├── excepciones/   CalculadoraException y sus 3 excepciones específicas
└── vista/         Main, VentanaPrincipal y VentanaManual
test/pruebas/      PruebasParser (no se incluye en el JAR)
docs/              UML, diagramas, documentación formal y script SQL
```

## Cómo funciona

```
VentanaPrincipal  ->  Calculadora  ->  ParserPolinomio  ->  Polinomio
     (vista)           (fachada)        (texto a objeto)    (derivar / integrar)
```

La interfaz solo se comunica con `Calculadora`. Esta convierte el texto en un `Polinomio`, aplica la operación del modelo y devuelve el resultado ya formateado. Todos los errores se informan mediante excepciones propias que heredan de `CalculadoraException`.

## Sintaxis aceptada

| Escribes | Se interpreta como |
|---|---|
| `3x^2 + 5x - 4` | Términos separados por `+` o `-` (espacios opcionales) |
| `x` / `-x` | `1x` / `-1x` |
| `7` | Constante |
| `2.5x^2` | Coeficientes decimales con punto |
| `x^-2` | Exponentes negativos (no se puede integrar `x^-1`) |

No se aceptan paréntesis, productos entre términos, exponentes decimales ni letras distintas de la variable elegida.

**Ejemplos**
- `3x^2 + 5x` derivada: `f'(x) = 6x + 5`
- `2x` integral definida de 0 a 2: `Resultado = 4`

## Requisitos

- JDK 17 o superior
- (Opcional) MySQL local, solo si se quiere guardar el historial en base de datos

## Cómo ejecutarlo

**Con el JAR**
```
java -jar entrega/CalculadoraCalculo.jar
```

**Desde el código fuente**

Linux / macOS:
```
mkdir out
javac -d out $(find src -name "*.java")
java -cp out vista.Main
```

Windows (PowerShell o CMD):
```
mkdir out
dir /s /b src\*.java > fuentes.txt
javac -d out @fuentes.txt
java -cp out vista.Main
```

## Autores

Proyecto realizado por:

| Integrante | Responsabilidad |
|---|---|
| [@Julio-szl](https://github.com/Julio-szl) | Parser, calculadora, excepciones e historial |
| [@alexvrro](https://github.com/alexvrro) | _completar_ |
| [@Esdras](https://github.com/esdrassamueltuyyax-netizen) | _completar_ |
