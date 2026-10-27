$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
$oldBaselineFlag = $env:FLYWAY_BASELINE_ON_MIGRATE
try {
    docker compose up -d --wait mysql
    if ($LASTEXITCODE -ne 0) { throw 'MySQL startup failed' }
    $tables = 'MYSQL_PWD="$MYSQL_PASSWORD" mysql -N -u"$MYSQL_USER" "$MYSQL_DATABASE" -e "SHOW TABLES"' | docker compose exec -T mysql sh -c 'tr -d ''\r'' | sh'
    if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect the database' }
    $tables = @($tables | ForEach-Object { $_.Trim() })
    $legacy = $tables.Count -gt 0 -and 'flyway_schema_history' -notin $tables
    if ($legacy) {
        if ('users' -notin $tables -or 'posts' -notin $tables -or 'comments' -notin $tables) {
            throw 'Existing database does not match the Lab 3 blog schema. Baseline cancelled.'
        }
        docker compose stop app
        if ($LASTEXITCODE -ne 0) { throw 'Cannot stop the old application before backup' }
        New-Item -ItemType Directory -Force output/backups | Out-Null
        $backupPath = "output/backups/before-flyway-$(Get-Date -Format 'yyyyMMdd-HHmmss').sql"
        'MYSQL_PWD="$MYSQL_PASSWORD" mysqldump --single-transaction --no-tablespaces --skip-lock-tables -u"$MYSQL_USER" "$MYSQL_DATABASE" > /tmp/blog-before-flyway.sql' | docker compose exec -T mysql sh -c 'tr -d ''\r'' | sh'
        if ($LASTEXITCODE -ne 0) { throw 'Database backup failed; upgrade cancelled' }
        docker compose cp mysql:/tmp/blog-before-flyway.sql $backupPath
        if ($LASTEXITCODE -ne 0) { throw 'Cannot copy the backup; upgrade cancelled' }
        Write-Host "Existing database backed up to $backupPath"
        # Only the known pre-Flyway schema is adopted as version 1.
        $env:FLYWAY_BASELINE_ON_MIGRATE = 'true'
    } else {
        $env:FLYWAY_BASELINE_ON_MIGRATE = 'false'
    }
    docker compose up --build -d app
    if ($LASTEXITCODE -ne 0) { throw 'Application build/start failed' }
    $appPort = '8080'
    if (Test-Path .env) {
        $portLine = Get-Content .env | Where-Object { $_ -match '^APP_PORT=\d+$' } | Select-Object -Last 1
        if ($portLine) { $appPort = $portLine.Split('=')[1] }
    }
    if ($env:APP_PORT) { $appPort = $env:APP_PORT }
    $ready = $false
    for ($attempt = 0; $attempt -lt 90; $attempt++) {
        try {
            $null = Invoke-RestMethod "http://localhost:$appPort/api/posts" -TimeoutSec 5
            $ready = $true
            break
        } catch { Start-Sleep -Seconds 2 }
    }
    if (-not $ready) { throw 'Application did not become ready. Check docker compose logs app.' }
    # Disable automatic adoption for future starts after the one-time upgrade.
    $env:FLYWAY_BASELINE_ON_MIGRATE = 'false'
    docker compose up -d app
    if ($LASTEXITCODE -ne 0) { throw 'Cannot restore normal Flyway startup mode' }
    'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u"$MYSQL_USER" "$MYSQL_DATABASE" -e "SELECT installed_rank, version, description, type, success FROM flyway_schema_history ORDER BY installed_rank; SELECT id, slug, name FROM posts;"' | docker compose exec -T mysql sh -c 'tr -d ''\r'' | sh'
    if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect migration results' }
    Write-Host 'Flyway upgrade completed. Baseline-on-migrate is disabled for normal starts.'
} finally {
    if ($null -eq $oldBaselineFlag) { Remove-Item Env:FLYWAY_BASELINE_ON_MIGRATE -ErrorAction SilentlyContinue }
    else { $env:FLYWAY_BASELINE_ON_MIGRATE = $oldBaselineFlag }
    Pop-Location
}
