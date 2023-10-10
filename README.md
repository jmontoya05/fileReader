# File Reader

Se requiere crear una solución la cual permita validar un archivo que puede ser (Excel, csv).
por lo tanto, se deberá generar un POST REQUEST (en el body se le enviará la ruta del archivo)
para el servicio de procesar.


* Objetivo del servicio de procesamiento:

Cargar el documento, para ser leído, una vez leído, por cada registro se debe enviar una
petición POST al servicio de validación para validar si el registro está bien o no, el servicio de
validación retornara un booleano indicando si el registro esta valido o no, una vez procesados
todos los registros se debe retornar al usuario el número de líneas validas e invalidas.
Nota: la lectura y carga de un archivo csv, Excel son diferentes, tiene que hacer una
implementación para cada una, tenga en cuenta que en el futuro se pueden añadir más tipos
de archivos para validar, entonces debe de ser escalable la solución.
Objetivo del servicio de validación:
Si el archivo es CSV las validaciones deben de ser las siguientes.

1) Validar si el email tiene la estructura correcta de un correo electrónico Ejemplo:
mateo@gmail.com -> correo valido
juan@example.com -> correo valido
asdasda878__ -> correo invalido
Juan””#@@..co -> correo invalido.
Para validar esto usar una expresión regular.
2) Validar el Date of birth , se cuenta como linea valida si es mayor al 1980-01-01
3) Validar el Job Title , si es Haematologist, Phytotherapist, Building surveyor, Insurance
account manager, Educational psychologist, si es alguno de estas , se tomara como
valida.
Si no cumple con alguna de estas validaciones no se tomará la línea como valida, y retornara
false al servicio.


Si el archivo es Excel las validaciones deben de ser las siguientes.
1) Validar el Injury Location que sea diferente de N/A
2) Validar el Report Type solo se deben aceptar (Near Miss, Lost Time, First Aid)
Si no cumple con alguna de estas validaciones la línea queda marcada cono invalida, y retornara
false al servicio

## Servicio de validación

https://github.com/jmontoya05/fileValidator
