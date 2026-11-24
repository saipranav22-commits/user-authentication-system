import time
import os
import re
from playwright.sync_api import sync_playwright

BASE_URL = "https://user-authentication-system-y5l5.onrender.com"
OUTPUT_DIR = os.path.join(os.getcwd(), "docs", "screenshots")
os.makedirs(OUTPUT_DIR, exist_ok=True)

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
        // Mask JWT tokens
        text = text.replace(/eyJ[a-zA-Z0-9_\\-\\.]+/g, (m) => m.substring(0, 10) + '...[MASKED_JWT]...' + m.substring(m.length - 8));
        // Mask UUID refresh / verification tokens
        text = text.replace(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/gi, (m) => m.substring(0, 8) + '-****-****-****-********' + m.substring(m.length - 4));
        consoleEl.textContent = text;
    }
}
"""

def capture_all():
    with sync_playwright() as p:
        browser = p.chromium.launch(channel="msedge", headless=True)
        context = browser.new_context(viewport={"width": 1920, "height": 1080})
        page = context.new_page()

        print(f"Navigating to {BASE_URL}...")
        page.goto(BASE_URL, wait_until="networkidle")
        time.sleep(2)

        # 01 Architecture Overview
        page.evaluate("switchTab('overview')")
        time.sleep(1)
        page.evaluate(MASK_SCRIPT)
        page.screenshot(path=os.path.join(OUTPUT_DIR, "01_architecture_overview.png"), full_page=False)
        print("Captured 01_architecture_overview.png")

        # Synthetic user data for demo capture
        timestamp = int(time.time())
        demo_email = f"portfolio.engineer.{timestamp}@example.com"
        demo_pass = "SecureP@ss2026!"

        # 02 Registration & Verification
        print("Navigating to Registration Tab...")
        page.evaluate("switchTab('register')")
        page.fill("#regEmail", demo_email)
        page.fill("#regPassword", demo_pass)
        page.click("button:has-text('Register Account')")
        time.sleep(2.5)
        page.evaluate(MASK_SCRIPT)
        page.screenshot(path=os.path.join(OUTPUT_DIR, "02_registration_verification.png"), full_page=False)
        print("Captured 02_registration_verification.png")

        # Unlock account via demo verification button
        print("Triggering Account Verification...")
        page.click("#verifyTokenBtn")
        time.sleep(2)

        # 03 Login & JWT Claims
        print("Navigating to Login Tab...")
        page.evaluate("switchTab('login')")
        page.fill("#loginEmail", demo_email)
        page.fill("#loginPassword", demo_pass)
        page.click("button:has-text('Log In & Generate JWT')")
        time.sleep(2.5)
        page.evaluate(MASK_SCRIPT)
        page.screenshot(path=os.path.join(OUTPUT_DIR, "03_login_jwt_claims.png"), full_page=False)
        print("Captured 03_login_jwt_claims.png")

        # 04 User Route (HTTP 200)
        print("Navigating to Protected Routes Tab (User Route)...")
        page.evaluate("switchTab('protected')")
        time.sleep(1)
        page.click("button:has-text('Test User Route')")
        time.sleep(2)
        page.evaluate(MASK_SCRIPT)
        page.screenshot(path=os.path.join(OUTPUT_DIR, "04_user_route_success.png"), full_page=False)
        print("Captured 04_user_route_success.png")

        # 05 Admin Route Forbidden (HTTP 403)
        print("Testing Admin Route (HTTP 403)...")
        page.click("button:has-text('Test Admin Route')")
        time.sleep(2)
        page.evaluate(MASK_SCRIPT)
        page.screenshot(path=os.path.join(OUTPUT_DIR, "05_admin_route_forbidden.png"), full_page=False)
        print("Captured 05_admin_route_forbidden.png")

        # 06 Refresh Token Rotation
        print("Navigating to Refresh Token Rotation Tab...")
        page.evaluate("switchTab('refresh')")
        time.sleep(1)
        page.click("button:has-text('Rotate & Refresh Token')")
        time.sleep(2)
        page.evaluate(MASK_SCRIPT)
        page.screenshot(path=os.path.join(OUTPUT_DIR, "06_refresh_token_rotation.png"), full_page=False)
        print("Captured 06_refresh_token_rotation.png")

        # 07 Password Recovery
        print("Navigating to Password Recovery Tab...")
        page.evaluate("switchTab('reset')")
        time.sleep(1)
        page.fill("#resetEmail", demo_email)
        page.click("button:has-text('Request Reset Link')")
        time.sleep(2)
        page.evaluate(MASK_SCRIPT)
        page.screenshot(path=os.path.join(OUTPUT_DIR, "07_password_recovery.png"), full_page=False)
        print("Captured 07_password_recovery.png")

        # 08 System Health Check
        print("Testing Health Check Endpoint...")
        page.click("button:has-text('Check Health (/api/health)')")
        time.sleep(2)
        page.evaluate(MASK_SCRIPT)
        page.screenshot(path=os.path.join(OUTPUT_DIR, "08_system_health.png"), full_page=False)
        print("Captured 08_system_health.png")

        # Mobile responsive screenshot
        print("Capturing mobile responsive screenshot...")
        mobile_context = browser.new_context(viewport={"width": 390, "height": 844}, is_mobile=True)
        mobile_page = mobile_context.new_page()
        mobile_page.goto(BASE_URL, wait_until="networkidle")
        time.sleep(1.5)
        mobile_page.evaluate(MASK_SCRIPT)
        mobile_page.screenshot(path=os.path.join(OUTPUT_DIR, "09_mobile_responsive_overview.png"), full_page=False)
        print("Captured 09_mobile_responsive_overview.png")

        browser.close()
        print("All screenshots successfully captured!")

if __name__ == "__main__":
    capture_all()
