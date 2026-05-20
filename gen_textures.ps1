Add-Type -AssemblyName System.Drawing

$projectRoot = "C:\Users\leanv\IdeaProjects\BloodlineSAE"
$blockDir = Join-Path $projectRoot "src\main\resources\assets\bloodlinesae\textures\block"
$particleDir = Join-Path $projectRoot "src\main\resources\assets\bloodlinesae\textures\particle"
New-Item -ItemType Directory -Force -Path $blockDir | Out-Null
New-Item -ItemType Directory -Force -Path $particleDir | Out-Null

function Save-Png($bmp, $path) {
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

function New-Bitmap16 {
    return New-Object System.Drawing.Bitmap 16, 16, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
}

function Set-Px($bmp, $x, $y, $r, $g, $b, $a) {
    if ($x -ge 0 -and $x -lt $bmp.Width -and $y -ge 0 -and $y -lt $bmp.Height) {
        $color = [System.Drawing.Color]::FromArgb($a, $r, $g, $b)
        $bmp.SetPixel($x, $y, $color)
    }
}

$rand = New-Object System.Random 1337
function NoiseByte($base, $variance) {
    return [Math]::Max(0, [Math]::Min(255, $base + $rand.Next(-$variance, $variance + 1)))
}

# ---- bloodline_dirt ----
$bmp = New-Bitmap16
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $r = NoiseByte 28 8; $g = NoiseByte 10 5; $b = NoiseByte 10 5
        if ($rand.NextDouble() -lt 0.07) { $r = NoiseByte 80 20; $g = NoiseByte 12 6; $b = NoiseByte 14 6 }
        Set-Px $bmp $x $y $r $g $b 255
    }
}
Save-Png $bmp (Join-Path $blockDir 'bloodline_dirt.png')

# ---- bloodline_log ----
$bmp = New-Bitmap16
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $col = (($x + 3) % 5)
        $r = NoiseByte 55 12; $g = NoiseByte 16 6; $b = NoiseByte 18 6
        if ($col -eq 0) { $r = NoiseByte 28 5; $g = NoiseByte 6 3; $b = NoiseByte 8 3 }
        if ($col -eq 2) { $r = NoiseByte 75 8; $g = NoiseByte 12 4; $b = NoiseByte 18 4 }
        Set-Px $bmp $x $y $r $g $b 255
    }
}
Save-Png $bmp (Join-Path $blockDir 'bloodline_log.png')

# ---- bloodline_log_top ----
$bmp = New-Bitmap16
$cx = 7.5; $cy = 7.5
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $dx = $x - $cx; $dy = $y - $cy
        $d = [Math]::Sqrt($dx * $dx + $dy * $dy)
        $ring = [Math]::Floor($d / 1.6) % 2
        if ($ring -eq 0) { $r = NoiseByte 90 12; $g = NoiseByte 25 6; $b = NoiseByte 25 6 }
        else { $r = NoiseByte 50 10; $g = NoiseByte 14 4; $b = NoiseByte 16 4 }
        if ($d -gt 7.2) { $r = NoiseByte 30 6; $g = NoiseByte 8 4; $b = NoiseByte 10 4 }
        Set-Px $bmp $x $y $r $g $b 255
    }
}
Save-Png $bmp (Join-Path $blockDir 'bloodline_log_top.png')

# ---- stripped_bloodline_log ----
$bmp = New-Bitmap16
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        Set-Px $bmp $x $y (NoiseByte 130 14) (NoiseByte 38 8) (NoiseByte 36 8) 255
    }
}
Save-Png $bmp (Join-Path $blockDir 'stripped_bloodline_log.png')

# ---- stripped_bloodline_log_top ----
$bmp = New-Bitmap16
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        Set-Px $bmp $x $y (NoiseByte 145 14) (NoiseByte 50 8) (NoiseByte 46 8) 255
    }
}
Save-Png $bmp (Join-Path $blockDir 'stripped_bloodline_log_top.png')

# ---- bloodline_planks ----
$bmp = New-Bitmap16
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $r = NoiseByte 80 12; $g = NoiseByte 22 6; $b = NoiseByte 24 6
        if ($y % 4 -eq 0) { $r = NoiseByte 35 6; $g = NoiseByte 10 4; $b = NoiseByte 12 4 }
        if (($x + [int]($y / 4)) % 8 -eq 0) { $r = NoiseByte 50 6; $g = NoiseByte 14 4; $b = NoiseByte 16 4 }
        Set-Px $bmp $x $y $r $g $b 255
    }
}
Save-Png $bmp (Join-Path $blockDir 'bloodline_planks.png')

# ---- bloodline_leaves ----
$bmp = New-Bitmap16
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $base = $rand.NextDouble()
        $r = NoiseByte 55 18; $g = NoiseByte 8 4; $b = NoiseByte 12 4
        if ($base -lt 0.12) { $r = NoiseByte 130 20; $g = NoiseByte 22 6; $b = NoiseByte 30 6 }
        if ($base -gt 0.92) { $r = 0; $g = 0; $b = 0 }
        $alpha = 255
        if ($base -gt 0.88) { $alpha = 0 }
        Set-Px $bmp $x $y $r $g $b $alpha
    }
}
Save-Png $bmp (Join-Path $blockDir 'bloodline_leaves.png')

# ---- bloodline_grass_top ----
$bmp = New-Bitmap16
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $r = NoiseByte 70 18; $g = NoiseByte 14 6; $b = NoiseByte 22 6
        if ($rand.NextDouble() -lt 0.10) { $r = NoiseByte 110 14; $g = NoiseByte 22 6; $b = NoiseByte 36 6 }
        Set-Px $bmp $x $y $r $g $b 255
    }
}
Save-Png $bmp (Join-Path $blockDir 'bloodline_grass_top.png')

# ---- bloodline_grass_side ----
$bmp = New-Bitmap16
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        if ($y -lt 4) {
            $r = NoiseByte 70 14; $g = NoiseByte 12 5; $b = NoiseByte 22 5
            if ($rand.NextDouble() -lt 0.10) { $r = NoiseByte 120 14; $g = NoiseByte 22 6; $b = NoiseByte 36 6 }
        } else {
            $r = NoiseByte 28 8; $g = NoiseByte 10 5; $b = NoiseByte 10 5
            if ($rand.NextDouble() -lt 0.07) { $r = NoiseByte 80 16; $g = NoiseByte 12 6; $b = NoiseByte 14 6 }
        }
        Set-Px $bmp $x $y $r $g $b 255
    }
}
Save-Png $bmp (Join-Path $blockDir 'bloodline_grass_side.png')

# ---- bloodline_sapling ----
$bmp = New-Bitmap16
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        Set-Px $bmp $x $y 0 0 0 0
    }
}
for ($y = 6; $y -lt 16; $y++) {
    Set-Px $bmp 7 $y (NoiseByte 60 8) (NoiseByte 12 4) (NoiseByte 14 4) 255
    Set-Px $bmp 8 $y (NoiseByte 60 8) (NoiseByte 12 4) (NoiseByte 14 4) 255
}
$leafSpots = @(@(5,3),@(6,2),@(7,1),@(8,1),@(9,2),@(10,3),@(6,5),@(7,4),@(8,4),@(9,5))
foreach ($p in $leafSpots) {
    Set-Px $bmp $p[0] $p[1] (NoiseByte 110 18) (NoiseByte 18 6) (NoiseByte 26 6) 255
}
Save-Png $bmp (Join-Path $blockDir 'bloodline_sapling.png')

# ---- crimstone ----
$bmp = New-Bitmap16
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $r = NoiseByte 90 14; $g = NoiseByte 28 8; $b = NoiseByte 32 8
        if ($rand.NextDouble() -lt 0.08) { $r = NoiseByte 160 18; $g = NoiseByte 40 8; $b = NoiseByte 52 8 }
        if ($rand.NextDouble() -lt 0.02) { $r = NoiseByte 60 10; $g = NoiseByte 18 6; $b = NoiseByte 24 6 }
        Set-Px $bmp $x $y $r $g $b 255
    }
}
Save-Png $bmp (Join-Path $blockDir 'crimstone.png')

# ---- bone_pile ----
$bmp = New-Bitmap16
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $r = NoiseByte 200 18; $g = NoiseByte 190 18; $b = NoiseByte 160 18
        if ($rand.NextDouble() -lt 0.10) { $r = NoiseByte 90 12; $g = NoiseByte 82 12; $b = NoiseByte 64 12 }
        Set-Px $bmp $x $y $r $g $b 255
    }
}
Save-Png $bmp (Join-Path $blockDir 'bone_pile.png')

# ---- particle drift (8x8 with alpha falloff, cyan-blue) ----
function New-Particle($file, $hueShift) {
    $bmp = New-Object System.Drawing.Bitmap 8, 8, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $cx = 3.5; $cy = 3.5
    for ($y = 0; $y -lt 8; $y++) {
        for ($x = 0; $x -lt 8; $x++) {
            $dx = $x - $cx; $dy = $y - $cy
            $d = [Math]::Sqrt($dx * $dx + $dy * $dy)
            if ($d -lt 3.5) {
                $i = 1.0 - ($d / 3.5)
                $a = [int]([Math]::Min(255, $i * 255))
                $b = [int]([Math]::Min(255, ($i * 240) + $hueShift))
                $g = [int]([Math]::Min(255, $i * 180 + $hueShift / 2))
                $r = [int]([Math]::Min(255, $i * 60))
                $color = [System.Drawing.Color]::FromArgb($a, $r, $g, $b)
                $bmp.SetPixel($x, $y, $color)
            } else {
                $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
            }
        }
    }
    $bmp.Save($file, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}
New-Particle (Join-Path $particleDir 'bloodline_drift_0.png') 0
New-Particle (Join-Path $particleDir 'bloodline_drift_1.png') 15
New-Particle (Join-Path $particleDir 'bloodline_drift_2.png') 30

Write-Output "Block textures saved to: $blockDir"
Write-Output "Particle textures saved to: $particleDir"
