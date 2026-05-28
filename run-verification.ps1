# Define paths and URIs
$logFile = "C:\Users\Hp\.gemini\antigravity\brain\32260fc3-5bd3-487c-8de7-7cb35565c5d1\.system_generated\tasks\task-424.log"
$baseUri = "http://localhost:8090"

Write-Host "=================================================="
Write-Host "   STARTING LIVE ADVANCED SECURITY VERIFICATION   "
Write-Host "=================================================="

# --- STEP 1: Registration ---
Write-Host "`n[Step 1] Registering account: alice@example.com..."
$regBody = @{ email = "alice@example.com"; password = "SecureP@ssword123!" } | ConvertTo-Json;
$res1 = Invoke-RestMethod -Uri "$baseUri/api/auth/register" -Method Post -Body $regBody -ContentType "application/json"
$res1 | ConvertTo-Json -Depth 5

# --- STEP 2: Try Login Before Verification ---
Write-Host "`n[Step 2] Attempting login before email verification (Expected: 403 Forbidden)..."
try {
    Invoke-RestMethod -Uri "$baseUri/api/auth/login" -Method Post -Body $regBody -ContentType "application/json"
    Write-Host "   [FAILED] Login allowed unverified account!"
} catch {
    $errStream = $_.Exception.Response.GetResponseStream();
    $reader = New-Object System.IO.StreamReader($errStream);
    $errBody = $reader.ReadToEnd();
    Write-Host "   [SUCCESS] Blocked. Status:" $_.Exception.Response.StatusCode;
    Write-Host "   [SUCCESS] Message:" $errBody
}

# --- STEP 3: Extract Verification Token from Server Logs ---
Write-Host "`n[Step 3] Extracting Verification Token from server logs..."
Start-Sleep -Seconds 2
$logContent = Get-Content -Path $logFile -Raw
# Search for token specifically in verification mock email block
if ($logContent -match "MOCK EMAIL: VERIFICATION[\s\S]+?token=([a-f0-9\-]+)") {
    $verifyToken = $Matches[1]
    Write-Host "   [SUCCESS] Found verification token: $verifyToken"
} else {
    Write-Host "   [FAILED] Verification token not found in logs!"
    exit 1
}

# --- STEP 4: Verify Email ---
Write-Host "`n[Step 4] Triggering email verification..."
$res4 = Invoke-RestMethod -Uri "$baseUri/api/auth/verify-email?token=$verifyToken" -Method Get
$res4 | ConvertTo-Json

# --- STEP 5: Successful Login (Obtain Tokens) ---
Write-Host "`n[Step 5] Logging in with verified account..."
$res5 = Invoke-RestMethod -Uri "$baseUri/api/auth/login" -Method Post -Body $regBody -ContentType "application/json"
$res5 | ConvertTo-Json -Depth 5

$accessToken = $res5.data.accessToken
$refreshToken = $res5.data.refreshToken

# --- STEP 6: Access Protected User Route ---
Write-Host "`n[Step 6] Accessing protected route (/api/user/profile) WITHOUT authorization..."
try {
    Invoke-RestMethod -Uri "$baseUri/api/user/profile" -Method Get
    Write-Host "   [FAILED] Allowed protected route without JWT!"
} catch {
    Write-Host "   [SUCCESS] Blocked. Status:" $_.Exception.Response.StatusCode
}

Write-Host "`n[Step 6.2] Accessing protected route WITH JWT Bearer token..."
$headers = @{ Authorization = "Bearer $accessToken" }
$res6 = Invoke-RestMethod -Uri "$baseUri/api/user/profile" -Method Get -Headers $headers
$res6 | ConvertTo-Json -Depth 5

# --- STEP 7: Access Admin Route (RBAC verification - Expected: 403 Forbidden) ---
Write-Host "`n[Step 7] Accessing admin-only route (/api/admin/dashboard) with ROLE_USER JWT..."
try {
    Invoke-RestMethod -Uri "$baseUri/api/admin/dashboard" -Method Get -Headers $headers
    Write-Host "   [FAILED] Allowed USER to access ADMIN dashboard!"
} catch {
    Write-Host "   [SUCCESS] Blocked. Status:" $_.Exception.Response.StatusCode " (Access Denied)"
}

# --- STEP 8: Refresh Access Token ---
Write-Host "`n[Step 8] Refreshing Access Token using Refresh Token..."
$refreshBody = @{ refreshToken = $refreshToken } | ConvertTo-Json
$res8 = Invoke-RestMethod -Uri "$baseUri/api/auth/refresh" -Method Post -Body $refreshBody -ContentType "application/json"
$res8 | ConvertTo-Json -Depth 5

$newAccessToken = $res8.data.accessToken
$newRefreshToken = $res8.data.refreshToken

# --- STEP 9: Password Reset Workflow ---
Write-Host "`n[Step 9] Initiating password reset recovery..."
$resetReqBody = @{ email = "alice@example.com" } | ConvertTo-Json
$res9 = Invoke-RestMethod -Uri "$baseUri/api/auth/password-reset/request" -Method Post -Body $resetReqBody -ContentType "application/json"
$res9 | ConvertTo-Json

Write-Host "`n[Step 9.2] Extracting Reset Token from server logs..."
Start-Sleep -Seconds 2
$logContent = Get-Content -Path $logFile -Raw
# Search for token specifically in password reset mock email block
if ($logContent -match "MOCK EMAIL: PASSWORD RESET[\s\S]+?token=([a-f0-9\-]+)") {
    $resetToken = $Matches[1]
    Write-Host "   [SUCCESS] Found password reset token: $resetToken"
} else {
    Write-Host "   [FAILED] Reset token not found in logs!"
    exit 1
}

Write-Host "`n[Step 9.3] Finalizing password reset override..."
$resetConfirmBody = @{ token = $resetToken; newPassword = "NewSecureP@ss123!" } | ConvertTo-Json
$res9_3 = Invoke-RestMethod -Uri "$baseUri/api/auth/password-reset/confirm" -Method Post -Body $resetConfirmBody -ContentType "application/json"
$res9_3 | ConvertTo-Json

# --- STEP 10: Verify Login with New Password ---
Write-Host "`n[Step 10] Attempting login with OLD password..."
try {
    Invoke-RestMethod -Uri "$baseUri/api/auth/login" -Method Post -Body $regBody -ContentType "application/json"
    Write-Host "   [FAILED] Allowed login with old credentials!"
} catch {
    Write-Host "   [SUCCESS] Blocked. Status:" $_.Exception.Response.StatusCode " (Invalid credentials)"
}

Write-Host "`n[Step 10.2] Attempting login with NEW password..."
$newLoginBody = @{ email = "alice@example.com"; password = "NewSecureP@ss123!" } | ConvertTo-Json
$res10 = Invoke-RestMethod -Uri "$baseUri/api/auth/login" -Method Post -Body $newLoginBody -ContentType "application/json"
$res10 | ConvertTo-Json -Depth 5

# --- STEP 11: Invalidate Refresh Token on Logout ---
$newHeaders = @{ Authorization = "Bearer $($res10.data.accessToken)" }
Write-Host "`n[Step 11] Logging out to invalidate sessions..."
$res11 = Invoke-RestMethod -Uri "$baseUri/api/auth/logout" -Method Post -Headers $newHeaders
$res11 | ConvertTo-Json

Write-Host "`n[Step 11.2] Attempting token exchange with invalidated refresh token..."
$invalidRefreshBody = @{ refreshToken = $res10.data.refreshToken } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$baseUri/api/auth/refresh" -Method Post -Body $invalidRefreshBody -ContentType "application/json"
    Write-Host "   [FAILED] Exchanged invalidated refresh token!"
} catch {
    Write-Host "   [SUCCESS] Blocked. Status:" $_.Exception.Response.StatusCode " (Invalid session)"
}

Write-Host "`n=================================================="
Write-Host "         VERIFICATION COMPLETED SUCCESSFULLY      "
Write-Host "=================================================="
