import os
from PIL import Image, ImageDraw, ImageFont

OUTPUT_PATH = os.path.join("docs", "demo", "final", "jwt-authentication-system-thumbnail.png")
os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)

WIDTH, HEIGHT = 1280, 720

def create_thumbnail():
    # Base image with deep slate / navy gradient
    img = Image.new("RGBA", (WIDTH, HEIGHT), (11, 15, 25, 255))
    draw = ImageDraw.Draw(img)

    # Draw subtle background grid & accents
    for x in range(0, WIDTH, 40):
        draw.line([(x, 0), (x, HEIGHT)], fill=(25, 33, 50, 120), width=1)
    for y in range(0, HEIGHT, 40):
        draw.line([(0, y), (WIDTH, y)], fill=(25, 33, 50, 120), width=1)

    # Decorative cyber accent glow on the right
    draw.ellipse([850, -100, 1400, 500], fill=(56, 189, 248, 25))
    draw.ellipse([950, 250, 1450, 750], fill=(16, 185, 129, 25))

    # Load system fonts or fallback
    try:
        font_title = ImageFont.truetype("arialbd.ttf", 62)
        font_sub = ImageFont.truetype("arialbd.ttf", 32)
        font_badge = ImageFont.truetype("arialbd.ttf", 22)
        font_desc = ImageFont.truetype("arial.ttf", 24)
    except:
        font_title = font_sub = font_badge = font_desc = ImageFont.load_default()

    # Top Tag
    draw.rounded_rectangle([80, 70, 360, 115], radius=8, fill=(14, 165, 233, 40), outline=(56, 189, 248, 200), width=2)
    draw.text((100, 80), "ENTERPRISE SECURITY", font=font_badge, fill=(56, 189, 248, 255))

    # Main Title
    draw.text((80, 145), "JWT AUTHENTICATION", font=font_title, fill=(255, 255, 255, 255))
    draw.text((80, 220), "& ROLE-BASED ACCESS CONTROL", font=font_title, fill=(56, 189, 248, 255))

    # Subtitle / Key Features
    draw.text((80, 315), "Dynamic Token Rotation  •  Replay Protection  •  Session Revocation", font=font_sub, fill=(203, 213, 225, 255))

    # Tech Stack Badges
    tech_tags = [
        ("Java 21 LTS", (234, 88, 12)),
        ("Spring Boot 3.4.1", (34, 197, 94)),
        ("Spring Security 6", (16, 185, 129)),
        ("Neon PostgreSQL", (14, 165, 233)),
        ("Multi-Stage Docker", (59, 130, 246)),
        ("Render Free Cloud", (168, 85, 247))
    ]

    bx, by = 80, 385
    for tag, color in tech_tags:
        # Draw pill badge
        text_bbox = draw.textbbox((0, 0), tag, font=font_badge)
        tw = text_bbox[2] - text_bbox[0]
        draw.rounded_rectangle([bx, by, bx + tw + 28, by + 42], radius=8, fill=(30, 41, 59, 220), outline=color, width=2)
        draw.text((bx + 14, by + 9), tag, font=font_badge, fill=(241, 245, 249, 255))
        bx += tw + 42
        if bx > 1150:
            bx = 80
            by += 55

    # Feature Highlights Card (Right / Bottom)
    draw.rounded_rectangle([80, 520, 1200, 640], radius=12, fill=(15, 23, 42, 230), outline=(51, 65, 85, 200), width=2)

    metrics = [
        ("25 / 25 TESTS PASSING", "15 Integration + 10 Unit Tests", (34, 197, 94)),
        ("15-MIN STATELESS JWT", "Cryptographically Signed HS256", (56, 189, 248)),
        ("ZERO CLOUD BILLING", "100% Free Hosting (Render + Neon)", (234, 179, 8))
    ]

    mx = 110
    for header, sub, col in metrics:
        draw.text((mx, 545), header, font=font_sub, fill=col)
        draw.text((mx, 590), sub, font=font_desc, fill=(148, 163, 184, 255))
        mx += 380

    # Save Thumbnail
    img.convert("RGB").save(OUTPUT_PATH, quality=95)
    print(f"Thumbnail successfully created at: {OUTPUT_PATH}")

if __name__ == "__main__":
    create_thumbnail()
