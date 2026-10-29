OCEAN LIGHTS - Forge 1.20.1 (47.3.0)
Hecho por ClearGoat (@BadEnding01)

Luces estaticas de color con un shader SUAVE: solo suma un reflejo de luz de color sobre el terreno
y sobre las entidades (jugadores, mobs) que estan cerca de la luz. Las paredes y bloques tapan la
luz (no se cuela al otro lado), pero nunca del todo: queda un resto para que siga viendose. No oscurece nada
y no cambia la luz del juego. La luz solo se nota cerca de la fuente y se apaga suave con la distancia.

Compilar:
1. Instala Java 17.
2. Descarga el MDK de Forge 1.20.1 y copia su carpeta gradle/ y gradlew(.bat) a esta carpeta.
3. Ejecuta: gradlew build   (el .jar queda en build/libs/)  o  gradlew runClient para probar.
El mod tiene que estar en el cliente Y en el servidor (en un mundo de un jugador ya basta).
No es compatible con shaders de Optifine/Oculus.

LUCES ESTATICAS
Cualquier armor stand con el tag "Luz" emite luz de color. No sale de ningun lado, solo del punto donde esta.

Con comandos (necesita OP / cheats):
/oceanlights yellow            crea una luz amarilla donde estas parado (radio 16)
/oceanlights blue 30           luz azul con radio 30 (2 a 48)
/oceanlights ff8800            cualquier color en hexadecimal
/oceanlights intensidad 0.5    fuerza del reflejo (0.2 a 3, por defecto 1). Sube el valor si lo quieres mas fuerte
/oceanlights sombras 0.85      cuanto tapan las paredes la luz (0 = la atraviesan, 1 = la bloquean del todo, por defecto 0.85)
/oceanlights remove [radio]    borra las luces cerca de ti (por defecto 5 bloques)
/oceanlights clear             borra todas las luces de la dimension
Colores: yellow orange red blue pink white green lime cyan aqua purple violet magenta gold
         lightblue turquoise brown / amarillo naranja rojo azul rosa blanco verde celeste morado dorado

A mano (por ejemplo con command blocks), el armor stand que crea el comando es:
/summon armor_stand ~ ~ ~ {Tags:["Luz","Luz_blue","Luz_r20"],Invisible:1b,Invulnerable:1b,NoGravity:1b,Marker:1b}
  Luz          obligatorio
  Luz_<color>  color (nombre o hex de 6 digitos). Si no lo pones, es blanca
  Luz_r<N>     radio en bloques (2 a 48). Si no lo pones, 16

La luz queda 0.8 bloques arriba de la posicion del armor stand.
Se muestran hasta 8 luces a la vez (las mas cercanas a ti).
Con Marker:1b el armor stand no tiene hitbox: no se puede golpear ni empujar.
