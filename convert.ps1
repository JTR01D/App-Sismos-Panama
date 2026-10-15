$content = Get-Content 'panama.geo.json' -Raw | ConvertFrom-Json
$coords = $content.features[0].geometry.coordinates[0]
$sb = [System.Text.StringBuilder]::new()
foreach ($c in $coords) {
    [void]$sb.AppendLine(('Pair({0:F4}, {1:F4}),' -f $c[1], $c[0]))
}
Set-Content -Path 'coords.txt' -Value $sb.ToString()
Write-Host "Points written: $($coords.Count)"
