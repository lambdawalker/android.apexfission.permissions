$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$config = Get-Content .project/setup/config.yaml -Raw
$match = [regex]::Match($config, '(?m)^tooling_ref: [''" ]*(v\d+\.\d+\.\d+|[0-9a-f]{40})[''" ]*$')
if (-not $match.Success) { throw 'Invalid tooling_ref in .project/setup/config.yaml' }
$reference = $match.Groups[1].Value
$option = if ($reference.StartsWith('v')) { '--tag' } else { '--rev' }
& cargo install --locked --git https://github.com/lambdawalker/gradle.android-repo-setup.git $option $reference android-repo-setup
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& android-repo-setup @args
exit $LASTEXITCODE
