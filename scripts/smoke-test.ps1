param([string]$BaseUrl = 'http://localhost:8080')
$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
$userId = $null
$postId = $null
$commentId = $null

function Invoke-Api([string]$Method, [string]$Path, $Body = $null) {
    $params = @{ Method = $Method; Uri = "$BaseUrl/api$Path"; TimeoutSec = 15 }
    if ($null -ne $Body) {
        $params.ContentType = 'application/json; charset=utf-8'
        $params.Body = [Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Compress))
    }
    Invoke-RestMethod @params
}

function Assert-Status([int]$Expected, [string]$Method, [string]$Path, $Body = $null) {
    try {
        $null = Invoke-Api $Method $Path $Body
    } catch {
        if ($null -ne $_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq $Expected) {
            return
        }
        throw
    }
    throw "Expected HTTP $Expected for $Method $Path"
}

function Wait-Api {
    for ($attempt = 0; $attempt -lt 90; $attempt++) {
        try { $null = Invoke-Api GET '/posts'; return } catch { Start-Sleep -Seconds 2 }
    }
    throw 'API did not become ready. Check docker compose logs app mysql.'
}

try {
    Wait-Api
    $suffix = [guid]::NewGuid().ToString('N')
    $userBody = @{ firstName = 'Lab'; lastName = 'Tester'; email = "lab-$suffix@example.com"; password = 'local-test' }
    $user = Invoke-Api POST '/users' $userBody
    $userId = $user.id
    $postBody = @{ authorId = $userId; slug = "lab-$suffix"; name = 'Lab 3'; body = 'Docker MySQL test' }
    $post = Invoke-Api POST '/posts' $postBody
    $postId = $post.id
    $comment = Invoke-Api POST '/comments' @{ authorId = $userId; postId = $postId; body = 'Test comment' }
    $commentId = $comment.id
    $updated = Invoke-Api PUT "/posts/$postId" @{ name = 'Updated Lab 3' }
    if ($updated.name -ne 'Updated Lab 3' -or $updated.body -ne $postBody.body) { throw 'Partial update failed' }
    $list = @(Invoke-Api GET "/posts/$postId/comments")
    if ($commentId -notin $list.id) { throw 'Comment not found in post comments' }
    Assert-Status 400 PUT "/posts/$postId" @{ name = $null }
    Assert-Status 400 PUT "/posts/$postId" @{ body = '   ' }
    Assert-Status 404 POST '/posts' @{ authorId = [long]::MaxValue; slug = "missing-$suffix"; name = 'Missing'; body = 'Text' }
    Assert-Status 409 POST '/users' $userBody
    Assert-Status 409 POST '/posts' $postBody
    Assert-Status 409 DELETE "/posts/$postId"
    Assert-Status 409 DELETE "/users/$userId"
    Write-Host 'CRUD and HTTP 400/404/409 checks passed.'

    # Recreate both containers while preserving the named database volume.
    docker compose down
    if ($LASTEXITCODE -ne 0) { throw 'docker compose down failed' }
    docker compose up -d
    if ($LASTEXITCODE -ne 0) { throw 'docker compose up failed' }
    Wait-Api
    $saved = Invoke-Api GET "/posts/$postId"
    if ($saved.name -ne 'Updated Lab 3') { throw 'Post was not preserved after container recreation' }
    $null = Invoke-Api GET "/users/$userId"
    $null = Invoke-Api GET "/comments/$commentId"
    Write-Host 'Data persisted after container recreation.'
} finally {
    try {
        if ($commentId) { $null = Invoke-Api DELETE "/comments/$commentId" }
        if ($postId) { $null = Invoke-Api DELETE "/posts/$postId" }
        if ($userId) { $null = Invoke-Api DELETE "/users/$userId" }
    } finally { Pop-Location }
}
Write-Host 'All checks passed. Test records removed.'
