# Production Setup and Deployment Guide

## 1. Neon Database Setup (Zero Cost, No Card)
1. Sign in to your Neon console at https://neon.tech.
2. Click **Create Project** and name it `auth-system-db`.
3. In the project dashboard, copy the PostgreSQL connection details:
   - Host
   - Database name
   - Username
   - Password
4. Assemble your JDBC connection URL:
   `jdbc:postgresql://<host>/<database>?sslmode=require`

## 2. Render Web Service Deployment (Zero Cost, No Card)
1. Sign in to Render at https://render.com.
2. Click **New +** > **Web Service**.
3. Connect your repository: `https://github.com/saipranav22-commits/user-authentication-system`.
4. Choose **Docker** as the runtime environment and select the **Free** instance type.
5. In the **Environment Variables** section, configure:
   - `SPRING_DATASOURCE_URL`: your Neon JDBC connection string
   - `SPRING_DATASOURCE_USERNAME`: your Neon database username
   - `SPRING_DATASOURCE_PASSWORD`: your Neon database password
   - `SPRING_DATASOURCE_DRIVER_CLASS_NAME`: `org.postgresql.Driver`
   - `SPRING_JPA_HIBERNATE_DDL_AUTO`: `update`
   - `JWT_SECRET`: a secure random 32+ character key
   - `APP_DEMO_MODE`: `true`
6. Click **Create Web Service**.
7. Once deployment finishes, navigate to your public Render URL to access the interactive demo console.
