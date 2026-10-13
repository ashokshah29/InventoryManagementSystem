$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path $PSScriptRoot -Parent
Push-Location $repositoryRoot
try {
    $configuredHooks = git config --get core.hooksPath
    if ($configuredHooks) {
        throw 'A custom hooks directory is configured. Install the database hook there manually.'
    }
    $hookPath = git rev-parse --git-path hooks/pre-commit
    if ($LASTEXITCODE -ne 0) { throw 'Cannot locate the repository hooks directory.' }
    $sourcePath = Join-Path $PSScriptRoot 'pre-commit'
    if (Test-Path -LiteralPath $hookPath) {
        if ((Get-Content -LiteralPath $hookPath -Raw) -ne (Get-Content -LiteralPath $sourcePath -Raw)) {
            throw 'An existing pre-commit hook is installed. Combine the hooks manually.'
        }
    } else {
        Copy-Item -LiteralPath $sourcePath -Destination $hookPath
    }
    Write-Output 'Database export hook installed. Each commit will refresh and stage inventory_db.'
} finally {
    Pop-Location
}
