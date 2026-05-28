# PowerShell script to run the ManualVerification runner offline using local m2 jar paths
$m2Repo = "C:\Users\Hp\.m2\repository"
$libDir = "target/lib"

# 1. Re-create a local library directory
if (Test-Path $libDir) {
    Remove-Item $libDir -Recurse -Force | Out-Null
}
New-Item -ItemType Directory -Path $libDir | Out-Null

Write-Host "Copying essential dependencies to target/lib..."

# 2. Define essential package keywords (Including jjwt- and jackson)
$keywords = @("spring-", "jakarta.", "mysql-", "slf4j-", "mockito-", "junit-", "byte-buddy", "objenesis", "reactive-streams", "jjwt-", "jackson")

# 3. Search and copy essential dependencies (excluding duplicates of older versions)
Get-ChildItem -Path $m2Repo -Filter "*.jar" -Recurse | Where-Object {
    $fullName = $_.FullName
    $match = $false
    foreach ($keyword in $keywords) {
        if ($fullName.Contains($keyword)) { $match = $true; break }
    }
    $match
} | ForEach-Object {
    $dest = Join-Path $libDir $_.Name
    if (-not (Test-Path $dest)) {
        Copy-Item -Path $_.FullName -Destination $dest -Force
    }
}

# 4. Construct classpath utilizing Java 6+ wildcard loading
$classpath = "target/classes;target/test-classes;target/lib/*"

# 5. Run the java main class with assertion checking enabled (-ea)
Write-Host "Launching ManualVerification App..."
Write-Host "--------------------------------------------------"
java -ea -cp $classpath com.edu.authsystem.ManualVerification
Write-Host "--------------------------------------------------"
Write-Host "Execution finished."
