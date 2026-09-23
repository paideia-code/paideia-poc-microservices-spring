-- Datos de prueba para desarrollo local.
-- Este script puede excluirse en producción con spring.flyway.target=1.

INSERT INTO courses (id, title, description, price, status) VALUES
    ('11111111-0000-0000-0000-000000000001', 'Introducción a Java', 'Fundamentos del lenguaje Java y programación orientada a objetos.', 50, 'PUBLISHED'),
    ('11111111-0000-0000-0000-000000000002', 'Spring Boot desde cero', 'Construcción de APIs REST con Spring Boot, JPA y PostgreSQL.', 80, 'PUBLISHED'),
    ('11111111-0000-0000-0000-000000000003', 'Microservicios con Spring', 'Patrones y herramientas para arquitecturas de microservicios.', 100, 'PUBLISHED'),
    ('11111111-0000-0000-0000-000000000004', 'Docker y contenedores', 'Uso de Docker y Docker Compose en proyectos reales.', 60, 'DRAFT'),
    ('11111111-0000-0000-0000-000000000005', 'Kubernetes avanzado', 'Orquestación de contenedores a escala en producción.', 130, 'ARCHIVED');
