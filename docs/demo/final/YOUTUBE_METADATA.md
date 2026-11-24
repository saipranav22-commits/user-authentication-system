# YouTube Video Metadata & Upload Guide

## Video Title
```
JWT Authentication System | Spring Boot Security, JWT, RBAC, PostgreSQL & Docker | Full Project Demo
```

---

## Intended Video Visibility
**UNLISTED** (Direct link accessible, not publicly indexed on channel search unless changed by author).

> [!NOTE]
> Upload this video manually to your YouTube channel as an **Unlisted** video using YouTube Studio, or via YouTube Data API if authenticated. Once uploaded, copy the resulting video URL into your GitHub `README.md`.

---

## Video Description
```
A comprehensive technical demonstration and architecture walkthrough of a production-grade User Authentication and Role-Based Access Control (RBAC) System built with Java 21 LTS, Spring Boot 3.4.1, Spring Security 6, JJWT, and PostgreSQL.

🌐 Live Deployed Application:
https://user-authentication-system-y5l5.onrender.com/

📂 GitHub Source Code & Complete Test Suites:
https://github.com/saipranav22-commits/user-authentication-system

---

### ⏱️ Video Chapters & Timestamps
00:00 - Scene 1: Project Introduction & Stateless vs Stateful Authentication
00:36 - Scene 2: High-Level Architecture & Spring Security Filter Pipeline
01:16 - Scene 3: User Registration, BCrypt Hashing & Email Verification Workflow
01:58 - Scene 4: Authentication, BCrypt Verification & JWT Token Generation
02:44 - Scene 5: Role-Based Access Control (RBAC) Enforcement (HTTP 200 vs 403)
03:26 - Scene 6: Dynamic Refresh-Token Rotation & Replay Attack Defense
04:12 - Scene 7: Password Reset Recovery, Session Revocation & Logout Mechanics
04:54 - Scene 8: Production Cloud Architecture (Docker, Render Free & Neon PostgreSQL)
05:26 - Scene 9: Conclusion, Verification Results & Engineering Portfolio Links

---

### 🏗️ Technology Stack
- Language & Runtime: Java 21 LTS
- Framework: Spring Boot 3.4.1 (Web, Security, Data JPA, Validation)
- Security Engine: Spring Security 6 & JJWT (io.jsonwebtoken 0.11.5)
- Password Encryption: BCrypt (Workload Factor 10)
- Database: Neon Serverless PostgreSQL (SSL-enforced) & H2 (In-Memory for CI)
- Containerization: Multi-stage Docker (Eclipse Temurin 21 Alpine)
- Cloud Hosting: Render Free Web Service (Oregon)
- Testing: JUnit 5, Mockito, Spring Boot Test (25/25 automated tests passing)
- Operating Budget: Strictly ₹0 / Free Tier (Zero payment card exposure)

---

### 🛡️ Verified Security Highlights
✅ Stateless HS256 JWT Access Tokens (15-minute lifetime)
✅ Dynamic Refresh Token Rotation (30-day lifetime, rotated on each refresh)
✅ Single-Use Replay Protection (Replaying revoked tokens returns HTTP 401)
✅ Role-Based Access Control (Granular separation between ROLE_USER and ROLE_ADMIN)
✅ Anti-Enumeration Protections (Uniform responses on login and password reset)
✅ Session Revocation on Password Reset & Logout (Pre-existing refresh tokens purged)
✅ Ephemeral Portfolio Verification Mode (No card-requiring SMTP dependencies)
✅ Zero Hardcoded Credentials (100% cloud environment variable injection)

---

### 📌 Step-by-Step Manual Upload Instructions
1. Open YouTube Studio (https://studio.youtube.com).
2. Click **Create** > **Upload videos**.
3. Select the exported video file:
   `docs/demo/final/jwt-authentication-system-demo.mp4`
4. Paste the **Video Title** provided above.
5. Paste this **Video Description** (including timestamps and links).
6. Upload the custom thumbnail:
   `docs/demo/final/jwt-authentication-system-thumbnail.png`
7. Upload the synchronized subtitle track:
   `docs/demo/final/jwt-authentication-system-subtitles.srt` (Language: English).
8. Under **Visibility**, select **Unlisted**.
9. Click **Save** and copy the generated YouTube link into your portfolio and GitHub `README.md`.

---

### 🏷️ Relevant Hashtags & Tags
#SpringBoot #Java21 #SpringSecurity #JWT #WebSecurity #PostgreSQL #Docker #Render #BackendDevelopment #FullStack #SoftwareEngineering #PortfolioProject
```
