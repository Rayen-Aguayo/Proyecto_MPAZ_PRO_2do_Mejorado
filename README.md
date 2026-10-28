# MPAZ PRO 2.º – Aplicación móvil de apoyo didáctico (Segundo Básico)

Proyecto de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles** (Evaluación Parcial 2).
Contexto: **Escuela Escritora Marcela Paz** (San Bernardo). MVP Android que reemplaza guías impresas
con unidades interactivas para las 4 asignaturas del nivel.

## Integrantes

- Rayén Aguayo Morales
- Armando Rosselot

## Cómo funciona

La UTP crea **unidades** por objetivo de aprendizaje (OA) y las autoriza. El docente asigna las unidades
autorizadas a un curso y monitorea el avance. El estudiante solo ve lo que se asignó a su curso.

```
UTP crea unidad → agrega desafíos y prueba final → envía a revisión → aprueba (Autorizada)
   → Docente la asigna a un curso (fechas e intentos) → Estudiante lee, escucha, resuelve
   desafíos y rinde la prueba final → Docente revisa respuestas, rendimiento por OA y observaciones
```

## Funcionalidades por perfil

### Jefatura UTP
- Crear unidades por asignatura y objetivo de aprendizaje: código OA, objetivo, explicación escrita,
  ejemplo guiado, instrucciones para audio e imagen de apoyo.
- Editar unidades mientras no estén autorizadas.
- Agregar **desafíos de práctica** y **preguntas de la prueba final** (4 alternativas).
- Flujo de estados: **Borrador → En revisión → Autorizada → Deshabilitada** (aprobar y deshabilitar).

### Docente
- Consultar solo las unidades **autorizadas por UTP**.
- Asignar una unidad a un curso: rango de disponibilidad, **máximo de intentos** de la prueba final (1 a 3),
  aviso al curso y si el estudiante puede revisar sus respuestas.
- **Monitorear el avance** de cada estudiante del curso.
- **Revisar las respuestas** de la prueba final (correctas e incorrectas).
- Ver el **rendimiento agrupado por objetivo de aprendizaje**, filtrable por curso.
- Registrar **observaciones pedagógicas** sobre el informe del estudiante.

### Estudiante
- Ve solo las asignaturas y objetivos asignados a su curso y vigentes por fecha.
- Lee textos breves, ve imágenes, **escucha** las instrucciones, explicaciones y ejemplos guiados.
- Resuelve **desafíos progresivos** (cada uno se desbloquea al completar el anterior) con confirmación de avance.
- Rinde la **prueba final** según los intentos permitidos, con nota de 1,0 a 7,0 (60 % de exigencia),
  y revisa sus respuestas correctas e incorrectas si el docente lo habilitó.

## Cuentas de prueba

Se crean solas la primera vez que se abre la app. También se pueden registrar usuarios nuevos
(el estudiante elige su curso).

| Perfil | Usuario | Clave |
|---|---|---|
| Jefatura UTP | `utp` | `utp123` |
| Docente | `docente` | `docente123` |
| Estudiante (2° Básico A) | `alumno` | `alumno123` |

## Reglas principales

- Código OA con formato `OA 3` (las letras OA y un número).
- Una unidad solo pasa a revisión si tiene al menos un desafío y una pregunta de prueba final.
- Una unidad autorizada no se edita; hay que deshabilitarla primero.
- El docente solo asigna unidades autorizadas, una vez por curso.
- La fecha de término no puede ser anterior a hoy ni a la fecha de inicio.
- Los intentos de la prueba final se validan al entregar y no se pueden superar.
- Si la UTP deshabilita una unidad, deja de verse en los cursos que la tenían asignada.

## Seguridad y datos

- Registro e inicio de sesión con 3 perfiles. La clave se guarda **cifrada (PBKDF2 + sal)** en la base de datos local (Room).
- La sesión se recuerda con **DataStore**.
- Solo se usan **datos ficticios**; todo se guarda en el teléfono.

## Validaciones y animaciones

- Validaciones visuales en cada campo (borde de error, ícono y mensaje), con la lógica separada de la
  interfaz en `util/Validadores.kt`.
- Animaciones: botón con efecto al presionar, mensajes que aparecen y desaparecen, color animado al elegir
  alternativas, y barras de progreso animadas (desafíos y rendimiento por OA).
- Formularios con campos de texto, numéricos, fechas con calendario, selección de opciones e interruptores.

## Recursos nativos

1. **Lectura en voz alta** (TextToSpeech): instrucciones, explicaciones, ejemplos y preguntas.
2. **Notificaciones locales** (permiso `POST_NOTIFICATIONS`): registro, unidad creada o asignada y prueba terminada.
3. **Selector de imágenes** de la galería para la imagen de apoyo de cada unidad.

## Arquitectura (MVVM)

```
data/
  local/            AppDatabase (Room) y dao/ con los DAOs
entity/             Usuario, Unidad, Pregunta, Asignacion, IntentoPrueba, Observacion, ...
repository/         UsuarioRepository, UnidadRepository, AprendizajeRepository
session/            SesionDataStore (DataStore)
util/               Validadores, EstadoUnidad, Calificacion, Fechas, Cursos, Roles,
                    PasswordHasher, LectorVoz, Notificaciones, Imagenes
viewmodel/          AuthViewModel, UtpViewModel, DocenteViewModel, EstudianteViewModel
ui/
  components/       botones, campos, selector de fecha, audio, revisión de respuestas
  navigation/       AppNavGraph y Rutas (Navigation Compose)
  screens/          auth, utp, docente, estudiante
  theme/            colores por asignatura
```

## Modelo de datos (Room)

| Tabla | Para qué sirve |
|---|---|
| `usuarios` | Cuentas de UTP, docente y estudiante (con curso) |
| `unidades` | Contenido por OA y su estado |
| `preguntas` | Desafíos y preguntas de la prueba final |
| `asignaciones` | Unidad asignada a un curso (fechas, intentos) |
| `intentos_prueba` / `respuestas_prueba` | Resultado y respuestas de cada intento |
| `avance_desafios` | Desafíos ya resueltos por cada estudiante |
| `observaciones` | Observaciones pedagógicas del docente |

## Pasos para ejecutar

1. Clonar el repositorio y abrirlo con Android Studio (versión reciente).
2. Esperar la sincronización de Gradle (**Sync Now**).
3. Ejecutar con ▶ en un emulador o dispositivo Android (minSdk 24).
4. Entrar con las cuentas de prueba y recorrer el flujo:
   `utp` crea, completa y autoriza una unidad → `docente` la asigna a "2° Básico A" →
   `alumno` resuelve los desafíos y rinde la prueba → `docente` revisa el informe y escribe una observación.

> Si cambias las tablas de la base de datos, desinstala la app del emulador para que se cree de nuevo.

## Herramientas

Kotlin · Jetpack Compose · Material 3 · Room · DataStore · Navigation Compose · GitHub · Trello

## Flujo de trabajo en Git

```
git add .
git commit -m "feat: descripción técnica de lo que cambiaste"
git push
```
