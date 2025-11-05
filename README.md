# 🚕 TPE2 - POD - Grupo 5
**Trabajo Práctico Especial 2 - Viajes en Autos de Aplicación**

Este proyecto corresponde al **Trabajo Práctico Especial 2** de la materia  
**Programación Orientada a la Distribución (POD)** del ITBA.

Implementa el procesamiento de datos reales de viajes en autos de aplicación  
de la ciudad de Nueva York, utilizando el modelo **MapReduce** con **Hazelcast 3.8.6**  
para ejecutar consultas distribuidas sobre grandes volúmenes de datos.

---

## 🚀 Instrucciones para levantar el sistema

### 1.️ Subir el proyecto a Pampero

```bash
scp -r ~/tpe2-g5/ usuario@pampero.itba.edu.ar:/home/usuario
```

### 2 Conectarse a Pampero
```bash
ssh usuario@pampero.itba.edu.ar
```

### 3. Compilar y empaquetar el proyecto

```bash
cd tpe2-g5/
mvn clean
mvn package
```

### 4. Levantar un nodo Hazelcast
```bash

cd server/target/
tar -xzf tpe2-g5-server-1.0-SNAPSHOT-bin.tar.gz
cd tpe2-g5-server-1.0-SNAPSHOT/
chmod u+x *.sh
./run-server.sh
```

### 5. Levantar otro nodo (desde otra terminal o máquina)
```bash
cd tpe2-g5/server/target/tpe2-g5-server-1.0-SNAPSHOT/
./run-server.sh
```

### 6. Levantar el cliente
```bash
cd client/target/
tar -xzf tpe2-g5-client-1.0-SNAPSHOT-bin.tar.gz
cd tpe2-g5-client-1.0-SNAPSHOT/
chmod u+x *.sh
```

---
## 📊 Ejecución de las Queries

Cada query se ejecuta por separado mediante su script correspondiente (`query1.sh` a `query5.sh`).  
El cliente leerá los archivos `.csv` desde el directorio indicado por `-DinPath` y escribirá la salida en `-DoutPath`.

---

### 1️⃣ Query 1 - Total de viajes por zona de inicio y finalización
```bash
./query1.sh -Daddresses='10.6.0.1:5701' -DinPath=. -DoutPath=.
```
---
### 2️⃣ Query 2 - Viaje más largo dentro de la ciudad por zona de inicio
```bash
./query2.sh -Daddresses='10.6.0.1:5701' -DinPath=. -DoutPath=.
```
---
### 3️⃣ Query 3 - Precio promedio por barrio de inicio y compañía
```bash
./query3.sh -Daddresses='10.6.0.1:5701' -DinPath=. -DoutPath=.
```

---

### 4️⃣ Query 4 - Viaje con mayor demora (en segundos) por zona de inicio para un barrio
```bash
./query4.sh -Daddresses='10.6.0.1:5701' -DinPath=. -DoutPath=. -Dborough=Manhattan
```

---
### 5️⃣ Query 5 - Total de millas YTD por compañía
```bash
./query5.sh -Daddresses='10.6.0.1:5701' -DinPath=. -DoutPath=.
```