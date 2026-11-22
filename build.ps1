param([switch]$Test)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) { throw 'JDK 17 or newer is required. Install a JDK and put its bin folder on PATH.' }
New-Item -ItemType Directory -Force -Path 'build/classes','build/test-classes','dist' | Out-Null
$sources = @(Get-ChildItem -LiteralPath 'src/main/java' -Filter '*.java' -Recurse | ForEach-Object { $_.FullName })
& javac --release 17 -encoding UTF-8 -d build/classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
& jar --create --file dist/Structura.jar --main-class edu.rungta.dsv.Main -C build/classes .
if ($LASTEXITCODE -ne 0) { throw 'JAR packaging failed. Close any running Structura window before rebuilding.' }
Write-Host 'Built dist/Structura.jar (Java 17 compatible).'
if ($Test) {
    $testSources = @(Get-ChildItem -LiteralPath 'src/test/java' -Filter '*.java' -Recurse | Where-Object { $_.Name -ne 'ModelJUnitTest.java' } | ForEach-Object { $_.FullName })
    & javac --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes @testSources
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
    & java '-Djava.awt.headless=true' -cp 'build/classes;build/test-classes' edu.rungta.dsv.Verification
    if ($LASTEXITCODE -ne 0) { throw 'Verification failed.' }
}
