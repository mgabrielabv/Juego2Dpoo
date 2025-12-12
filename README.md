Maze Girl es un juego 2D sencillo hecho en Java con Swing. La idea es recorrer un laberinto, esquivar enemigos y llegar a la salida. El juego tiene una estética rosa, con una pantalla de inicio animada y una celebración al ganar.

Características principales
Pantalla de título con animación Idle (1–16) y controles visibles (WASD/Flechas, Enter para empezar).

Dos niveles con enemigos que patrullan y persiguen al jugador usando un algoritmo BFS básico.

Sistema de 3 vidas: al recibir daño se reinicia el nivel; si se pierden todas, aparece la pantalla de Game Over con animación “Dead”.

Pantalla de victoria con animación Jump (1–20) y opción de volver al título.

Controles
Movimiento: WASD o Flechas.

Enter: Iniciar, continuar o volver al título.

Esc: Pausar y reanudar.

Organización del proyecto
src/: código fuente.

Main.java: punto de entrada.

controller/KeyHandler.java: manejo de teclas.

model/: clases de jugador, enemigo, entidad, tiles y gestor de tiles.

view/GamePanel.java: panel principal del juego.

res/: recursos gráficos (Idle, Dead, Jump, sprites del jugador y tiles).

Niveles y jugabilidad
Nivel 1: un enemigo con rango de persecución moderado.

Nivel 2: dos enemigos con mayor alcance; al llegar a la salida aparece la pantalla de victoria.

Al perder una vida, el nivel se reinicia de inmediato sin retraso.

En Game Over, basta con presionar Enter para reiniciar y volver a jugar.
