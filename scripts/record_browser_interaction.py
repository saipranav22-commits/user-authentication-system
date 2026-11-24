import time
import os
import shutil
from playwright.sync_api import sync_playwright

BASE_URL = "https://user-authentication-system-y5l5.onrender.com"
RECORDINGS_DIR = os.path.join(os.getcwd(), "docs", "demo", "recordings")
os.makedirs(RECORDINGS_DIR, exist_ok=True)

MASK_SCRIPT = """
() => {
    const maskString = (str) => {
        if (!str || str.length < 12) return str;
        return str.substring(0, 8) + '...' + '[MASKED]' + '...' + str.substring(str.length - 6);
    };

    const verif = document.getElementById('demoTokenDisplay');
    if (verif && verif.textContent) verif.textContent = maskString(verif.textContent);

    const ref = document.getElementById('currentRefreshTokenDisplay');
    if (ref && ref.value) ref.value = maskString(ref.value);

    const tokenInput = document.getElementById('resetTokenInput');
    if (tokenInput && tokenInput.value) tokenInput.value = maskString(tokenInput.value);

    ['regPassword', 'loginPassword', 'resetNewPassword'].forEach(id => {
        const el = document.getElementById(id);
        if (el && el.value) el.value = '••••••••••••';
    });

    const consoleEl = document.getElementById('apiConsoleOutput');
    if (consoleEl && consoleEl.textContent) {
        let text = consoleEl.textContent;
        text = text.replace(/eyJ[a-zA-Z0-9_\\-\\.]+/g, (m) => m.substring(0, 10) + '...[MASKED_JWT]...' + m.substring(m.length - 8));
        text = text.replace(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/gi, (m) => m.substring(0, 8) + '-****-****-****-********' + m.substring(m.length - 4));
        consoleEl.textContent = text;
    }
}
"""

def record_demo():
    print("Starting Playwright screen recording of live application...")
    with sync_playwright() as p:
        browser = p.chromium.launch(channel="msedge", headless=True)
        context = browser.new_context(
            record_video_dir=RECORDINGS_DIR,
            record_video_size={"width": 1920, "height": 1080},
            viewport={"width": 1920, "height": 1080}
        )
        page = context.new_page()

        # ==========================================
        # SCENE 1: Project Introduction (~36s)
        # ==========================================
        print("Scene 1: Project Introduction...")
        page.goto(BASE_URL, wait_until="networkidle")
        page.mouse.move(960, 540)
        time.sleep(5)
        # Smooth hover over brand and header badges
        page.mouse.move(300, 45)
        time.sleep(6)
        page.mouse.move(800, 265)
        time.sleep(8)
        # Hover over session banner
        page.mouse.move(600, 140)
        time.sleep(10)
        page.mouse.move(960, 500)
        time.sleep(7)

        # ==========================================
        # SCENE 2: System Architecture (~37s)
        # ==========================================
        print("Scene 2: System Architecture...")
        page.evaluate("switchTab('overview')")
        time.sleep(3)
        # Highlight architecture flow box
        page.mouse.move(500, 320)
        time.sleep(8)
        # Highlight Authentication and RBAC boxes
        page.mouse.move(400, 430)
        time.sleep(8)
        page.mouse.move(700, 430)
        time.sleep(8)
        # Move down to 'Start with Registration' button
        page.mouse.move(400, 520)
        time.sleep(10)

        # ==========================================
        # SCENE 3: Registration and Verification (~44s)
        # ==========================================
        print("Scene 3: Registration and Verification...")
        page.evaluate("switchTab('register')")
        time.sleep(3)
        timestamp = int(time.time())
        demo_email = f"demo.dev.{timestamp}@example.com"
        demo_pass = "SecureP@ss2026!"

        # Smooth typing
        page.click("#regEmail")
        page.fill("#regEmail", "")
        for char in demo_email:
            page.type("#regEmail", char, delay=35)
        time.sleep(2)

        page.click("#regPassword")
        page.fill("#regPassword", "")
        for char in demo_pass:
            page.type("#regPassword", char, delay=35)
        time.sleep(3)

        # Submit registration
        page.click("button:has-text('Register Account')")
        time.sleep(4)
        page.evaluate(MASK_SCRIPT)

        # Observe the demo verification step box
        page.mouse.move(500, 530)
        time.sleep(6)

        # Click verification button
        page.click("#verifyTokenBtn")
        time.sleep(3)
        page.evaluate(MASK_SCRIPT)
        # Hover over inspector showing HTTP 200 verification response
        page.mouse.move(1400, 350)
        time.sleep(8)

        # ==========================================
        # SCENE 4: Login & JWT Generation (~44s)
        # ==========================================
        print("Scene 4: Login and JWT Generation...")
        page.evaluate("switchTab('login')")
        time.sleep(3)

        page.click("#loginEmail")
        page.fill("#loginEmail", "")
        for char in demo_email:
            page.type("#loginEmail", char, delay=35)
        time.sleep(2)

        page.click("#loginPassword")
        page.fill("#loginPassword", "")
        for char in demo_pass:
            page.type("#loginPassword", char, delay=35)
        time.sleep(3)

        # Submit login
        page.click("button:has-text('Log In & Generate JWT')")
        time.sleep(4)
        page.evaluate(MASK_SCRIPT)

        # Inspect claims card
        page.mouse.move(500, 550)
        time.sleep(10)

        # Hover over session banner showing Authenticated
        page.mouse.move(500, 140)
        time.sleep(8)

        # Hover over Inspector console with JWT payload
        page.mouse.move(1400, 400)
        time.sleep(6)

        # ==========================================
        # SCENE 5: Role-Based Access Control (~36s)
        # ==========================================
        print("Scene 5: Role-Based Access Control...")
        page.evaluate("switchTab('protected')")
        time.sleep(4)

        # Test User Route (HTTP 200)
        page.mouse.move(500, 430)
        time.sleep(2)
        page.click("button:has-text('Test User Route')")
        time.sleep(3)
        page.evaluate(MASK_SCRIPT)
        page.mouse.move(1400, 320)
        time.sleep(8)

        # Test Admin Route (HTTP 403)
        page.mouse.move(500, 540)
        time.sleep(2)
        page.click("button:has-text('Test Admin Route')")
        time.sleep(3)
        page.evaluate(MASK_SCRIPT)
        # Point to 403 Forbidden in console
        page.mouse.move(1400, 240)
        time.sleep(8)

        # ==========================================
        # SCENE 6: Refresh-Token Rotation (~42s)
        # ==========================================
        print("Scene 6: Refresh-Token Rotation...")
        page.evaluate("switchTab('refresh')")
        time.sleep(4)
        page.evaluate(MASK_SCRIPT)

        # Hover over current refresh token display
        page.mouse.move(500, 420)
        time.sleep(6)

        # Click rotate & refresh token
        page.click("button:has-text('Rotate & Refresh Token')")
        time.sleep(4)
        page.evaluate(MASK_SCRIPT)

        # Point to rotated token and inspector response
        page.mouse.move(500, 420)
        time.sleep(8)
        page.mouse.move(1400, 380)
        time.sleep(12)

        # ==========================================
        # SCENE 7: Password Reset & Logout (~40s)
        # ==========================================
        print("Scene 7: Password Reset and Logout...")
        page.evaluate("switchTab('reset')")
        time.sleep(3)

        page.click("#resetEmail")
        page.fill("#resetEmail", demo_email)
        time.sleep(2)

        # Submit reset link request
        page.click("button:has-text('Request Reset Link')")
        time.sleep(4)
        page.evaluate(MASK_SCRIPT)
        page.mouse.move(1400, 350)
        time.sleep(8)

        # Trigger logout from session banner
        print("Invoking Logout...")
        page.mouse.move(880, 140)
        time.sleep(2)
        page.click("#logoutBtn")
        time.sleep(3)
        page.evaluate(MASK_SCRIPT)
        page.mouse.move(1400, 320)
        time.sleep(10)

        # ==========================================
        # SCENE 8: Production Deployment (~33s)
        # ==========================================
        print("Scene 8: Production Deployment...")
        # Check system health
        page.mouse.move(1350, 680)
        time.sleep(2)
        page.click("button:has-text('Check Health (/api/health)')")
        time.sleep(4)
        page.evaluate(MASK_SCRIPT)

        # Hover over UP badge and GitHub link
        page.mouse.move(1050, 45)
        time.sleep(8)
        page.mouse.move(1140, 45)
        time.sleep(10)

        # ==========================================
        # SCENE 9: Conclusion (~20s)
        # ==========================================
        print("Scene 9: Conclusion...")
        page.evaluate("switchTab('overview')")
        time.sleep(2)
        page.mouse.move(960, 400)
        time.sleep(12)

        # Close page and context to finalize video
        video_path = page.video.path()
        page.close()
        context.close()
        browser.close()

        print(f"Playwright video recorded successfully to: {video_path}")
        target_raw = os.path.join(RECORDINGS_DIR, "raw_demo.webm")
        shutil.copy2(video_path, target_raw)
        print(f"Copied to {target_raw}")

if __name__ == "__main__":
    record_demo()
