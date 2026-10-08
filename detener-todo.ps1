# Detiene los procesos Java que escuchan en los puertos 8080-8090.

$puertos = 8080..8090
$conexiones = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue |
    Where-Object { $puertos -contains $_.LocalPort }

$detenidos = 0
foreach ($c in $conexiones) {
    $proceso = Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue
    if ($proceso -and $proceso.ProcessName -eq 'java') {
        Write-Host "Deteniendo puerto $($c.LocalPort) (pid $($proceso.Id))"
        Stop-Process -Id $proceso.Id -Force -ErrorAction SilentlyContinue
        $detenidos++
    }
}

if ($detenidos -eq 0) {
    Write-Host 'No había microservicios corriendo.'
} else {
    Write-Host "$detenidos servicio(s) detenido(s)." -ForegroundColor Green
}
