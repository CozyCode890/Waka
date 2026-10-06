# Prints CLAUDE.md's STATE block at session start.
#
# Why this exists: CLAUDE.md is auto-loaded at the start of a session, but a long session
# under context compaction can summarise it away — precisely when a wrong write to memory\
# or a skipped wrap-up is most likely. This hook re-states the cursor on every session start,
# including resume and clear, so the position is never guessed.
#
# It prints nothing and exits 0 if anything is missing. A broken hook must never block a session.

$ErrorActionPreference = 'Stop'

try {
    $claudeMd = Join-Path $PSScriptRoot '..\..\CLAUDE.md'
    if (-not (Test-Path -LiteralPath $claudeMd)) { exit 0 }

    $lines = @(Get-Content -LiteralPath $claudeMd)

    # Match "## <sigil>STATE" without putting a non-ASCII character in this script.
    $hit = $lines | Select-String -Pattern '^##\s+\S*STATE\s*$' | Select-Object -First 1
    if (-not $hit) { exit 0 }

    # Select-String's LineNumber is 1-based, so it is already the index of the next line.
    $body = [System.Collections.Generic.List[string]]::new()
    for ($i = $hit.LineNumber; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match '^##\s') { break }
        $body.Add($lines[$i])
    }

    $text = ($body -join "`n").Trim()
    if (-not $text) { exit 0 }

    Write-Output 'Weka-GUI memory cursor, re-stated from CLAUDE.md:'
    Write-Output ''
    Write-Output $text
    Write-Output ''
    Write-Output 'Classify the request against CLAUDE.md ROUTER and print "row: <name> -> <files>" before your first read.'
}
catch {
    exit 0
}
