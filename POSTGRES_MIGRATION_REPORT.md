# PostgreSQL Migration Report

## Overview
Migration assessment and preparation from MySQL to Neon PostgreSQL.

## Entity & Schema Evaluation
- **User Entity**: Uses standard JPA annotations (`@Entity`, `@Table(name = "users")`). The `IDENTITY` generation strategy maps cleanly to PostgreSQL `BIGSERIAL` / `IDENTITY`.
- **User Roles**: Stored in `user_roles` via `@ElementCollection` with foreign key referencing `users(id)`.
- **Refresh Token Entity**: Stored in `refresh_tokens` via `@OneToOne` with unique constraint on `user_id`.
- **Timestamps**: Uses `java.time.LocalDateTime` and `java.time.Instant`, mapping cleanly to PostgreSQL timestamps.

## Database Driver & Configuration
- Added `org.postgresql:postgresql` runtime dependency to `pom.xml`.
- Configured dynamic Hibernate dialect resolution in `application.yml`.
- Created `schema-postgres.sql` with PostgreSQL DDL, indices, and foreign keys.

## Backward Compatibility
- Preserved existing H2 profile (`application-h2.yml`) and MySQL driver support for local testing.
