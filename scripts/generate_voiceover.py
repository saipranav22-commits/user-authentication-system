import asyncio
import os
import re
import subprocess
import edge_tts
import imageio_ffmpeg

SCRIPT_PATH = os.path.join("docs", "demo", "scripts", "narration.txt")
TEMP_MP3 = os.path.join("docs", "demo", "voiceover", "narration_temp.mp3")
OUTPUT_WAV = os.path.join("docs", "demo", "voiceover", "narration.wav")
OUTPUT_SRT = os.path.join("docs", "demo", "final", "jwt-authentication-system-subtitles.srt")
FFMPEG_EXE = imageio_ffmpeg.get_ffmpeg_exe()

VOICE = "en-IN-PrabhatNeural"

CONCISE_SCRIPT = """[Scene 1 - Project Introduction]
Welcome to the technical demonstration of the User Authentication and Role-Based Access Control System. Securing user identities while maintaining stateless session scalability is a core engineering challenge. Traditional server-side sessions create clustering bottlenecks, while naive JSON Web Token setups often lack token revocation and replay defense. This project delivers an enterprise-grade authentication architecture built with Spring Boot 3.4.1, Spring Security 6, JJWT, and PostgreSQL, deployed at zero cost on Render and Neon.

[Scene 2 - System Architecture]
Incoming HTTP requests pass through a strict Cross-Origin filter before reaching the JwtAuthenticationFilter. This component inspects the Authorization header for an HMAC-SHA256 Bearer token. Upon validating signature integrity and expiration, it sets the authentication principal in Spring Security's SecurityContextHolder. Downstream authorization gates enforce role-based access control, routing verified traffic through service and repository layers into a dedicated serverless Neon PostgreSQL database.

[Scene 3 - Registration and Verification]
In the interactive dashboard, a new user registers with an email and password. The service layer verifies payload constraints and email uniqueness, then hashes the password using BCrypt with a workload factor of 10. The account is created with verification locked, preventing unauthorized login. A 24-hour verification token is issued. In this portfolio demo mode, the token is provided directly to demonstrate verification without requiring external transactional SMTP infrastructure. Triggering verification unlocks the account and clears the token to prevent reuse.

[Scene 4 - Login and JWT Generation]
Next, we authenticate using our registered credentials. The backend queries PostgreSQL, returning a uniform InvalidCredentialsException if lookup fails, mitigating user enumeration attacks. Once BCrypt confirms the password hash and verifies activation, the JwtService generates a short-lived 15-minute access token signed with HMAC-SHA256. Simultaneously, a persistent 30-day UUID refresh token is saved to PostgreSQL. Inspecting the live claims, we see the user subject, granted ROLE_USER authority, and expiration timestamp.

[Scene 5 - Role-Based Access Control]
Now we demonstrate Role-Based Access Control. Invoking the protected user endpoint at GET /api/user/profile with our Bearer token returns HTTP 200 OK with authorized profile details. However, when we attempt to access GET /api/admin/dashboard using this same token, Spring Security blocks the request, returning an immediate HTTP 403 Forbidden. This confirms strict privilege boundary separation between standard users and administrators.

[Scene 6 - Refresh-Token Rotation]
To balance security with usability, short-lived access tokens are refreshed using persistent refresh tokens. When we invoke POST /api/auth/refresh, the backend executes strict token rotation: the submitted refresh token is permanently deleted from PostgreSQL, and a brand new refresh token is issued alongside a fresh JWT. If an attacker replays the old token, the backend detects it is missing and rejects the request with HTTP 401 Unauthorized, defeating token replay attacks.

[Scene 7 - Password Reset and Logout]
Password recovery enforces anti-enumeration by returning a uniform success message regardless of account existence. Validating the 1-hour reset token updates the BCrypt password hash and nullifies the token. Crucially, as verified in commit 1e27b90, password reset instantly purges all active refresh tokens from PostgreSQL, terminating sessions across all devices. Similarly, invoking Logout purges the user's refresh token from the database, preventing subsequent token renewals while stateless JWTs expire naturally.

[Scene 8 - Production Deployment]
The application is deployed on Render's Free web service, containerized with multi-stage Docker on Java 21 LTS and Spring Boot 3.4.1. Persistence is managed by a separate serverless Neon PostgreSQL database with SSL enforcement. All secrets, including the 256-bit JWT key and database credentials, are injected via cloud environment variables with zero hardcoded fallbacks. The system is protected by 25 automated unit and integration tests passing cleanly.

[Scene 9 - Conclusion]
In summary, this project demonstrates end-to-end authentication engineering: cryptographic security, role-based authorization, dynamic token rotation, replay defense, and production cloud containerization on a strict zero-rupee budget. The complete source code is available on GitHub at github.com/saipranav22-commits/user-authentication-system, and the live application can be accessed online. Thank you for watching."""

def format_timestamp(seconds):
    millis = int((seconds - int(seconds)) * 1000)
    secs = int(seconds) % 60
    mins = (int(seconds) // 60) % 60
    hours = int(seconds) // 3600
    return f"{hours:02d}:{mins:02d}:{secs:02d},{millis:03d}"

async def main():
    # Write the script
    with open(SCRIPT_PATH, "w", encoding="utf-8") as f:
        f.write(CONCISE_SCRIPT.strip())

    # Clean text for speech
    clean_text = re.sub(r"\[.*?\]", "", CONCISE_SCRIPT).strip()
    words = clean_text.split()
    print(f"Calibrated word count: {len(words)}")

    communicate = edge_tts.Communicate(clean_text, VOICE, rate="+8%", pitch="+0Hz")

    word_timings = []
    with open(TEMP_MP3, "wb") as f:
        async for chunk in communicate.stream():
            if chunk["type"] == "audio":
                f.write(chunk["data"])
            elif chunk["type"] == "WordBoundary":
                start_sec = chunk["offset"] / 10_000_000.0
                dur_sec = chunk["duration"] / 10_000_000.0
                word_timings.append({
                    "word": chunk["text"],
                    "start": start_sec,
                    "end": start_sec + dur_sec
                })

    print("Converting to 48kHz WAV...")
    cmd = [
        FFMPEG_EXE, "-y",
        "-i", TEMP_MP3,
        "-ar", "48000",
        "-ac", "2",
        OUTPUT_WAV
    ]
    subprocess.run(cmd, check=True)

    # Check duration
    probe = subprocess.run([FFMPEG_EXE, "-i", OUTPUT_WAV], stderr=subprocess.PIPE, text=True)
    dur_match = re.search(r"Duration:\s*(\d+:\d+:\d+\.\d+)", probe.stderr)
    dur_str = dur_match.group(1) if dur_match else "unknown"
    print(f"Generated Voiceover Duration: {dur_str}")

    # Generate synchronized SRT
    srt_entries = []
    chunk_size = 7
    for i in range(0, len(word_timings), chunk_size):
        slice_words = word_timings[i:i + chunk_size]
        if not slice_words:
            continue
        start_time = slice_words[0]["start"]
        end_time = slice_words[-1]["end"]
        end_time = max(end_time, start_time + 1.2)
        text = " ".join([w["word"] for w in slice_words])
        srt_entries.append((start_time, end_time, text))

    with open(OUTPUT_SRT, "w", encoding="utf-8") as srt_file:
        for idx, (st, et, txt) in enumerate(srt_entries, 1):
            srt_file.write(f"{idx}\n")
            srt_file.write(f"{format_timestamp(st)} --> {format_timestamp(et)}\n")
            srt_file.write(f"{txt}\n\n")

    print(f"Subtitles written to {OUTPUT_SRT}")

if __name__ == "__main__":
    asyncio.run(main())
