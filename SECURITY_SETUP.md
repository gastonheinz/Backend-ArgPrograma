# Configuración de seguridad

Antes del despliegue, rotar la contraseña SQL que estuvo publicada y la clave JWT. No basta con quitar sus valores del repositorio: el historial los conserva. Invalidar tokens existentes. No se reescribe el historial ni se intenta usar las credenciales antiguas.

Configurar DB_URL (jdbc:mysql://host:3306/base), DB_USERNAME, DB_PASSWORD y JWT_SECRET en el gestor de secretos del despliegue. JWT_SECRET debe ser Base64 de al menos 64 bytes criptográficamente aleatorios; no usar palabras, UUID ni valores de ejemplo. La aplicación falla al iniciar si falta la configuración. JWT_EXPIRATION es opcional, en segundos (3600 por defecto).

La base debe tener el esquema existente: Hibernate valida y no altera automáticamente el esquema de producción. Crear o migrar el esquema por un procedimiento administrativo antes del primer arranque. El despliegue deja de cargar el JAR antiguo versionado: Docker compila desde el código actual con Java 21.

El portfolio conserva lecturas públicas; todas las escrituras y la creación de usuarios requieren un administrador existente. El cuerpo de registro ya no concede roles administrativos. Aprovisionar el primer administrador mediante un procedimiento administrativo de la base (contraseña BCrypt), nunca mediante registro público. Revisar cuentas y roles ya creados durante el período de exposición. CORS no sustituye a la autorización.

Compilar y probar con Java 21 y Maven 3.9: `mvn -B -ntp verify`. Las pruebas usan H2, no la base de producción. La actualización migra a Jakarta y Spring Boot 4.0.9 y utiliza las versiones administradas por su BOM. Revisar periódicamente las actualizaciones de la rama y la siguiente migración soportada.
