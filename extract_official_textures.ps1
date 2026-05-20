Add-Type -AssemblyName System.Drawing

$src = "C:\Users\leanv\IdeaProjects\BloodlineSAE\dependencies\Textures\Bloodline Biomes"
$blockDir = "C:\Users\leanv\IdeaProjects\BloodlineSAE\src\main\resources\assets\bloodlinesae\textures\block"

# Crop a region from a source PNG, scale to 16x16, save.
function Crop-Resize-16([string]$srcFile, [int]$x, [int]$y, [int]$w, [int]$h, [string]$outFile) {
    $img = [System.Drawing.Image]::FromFile($srcFile)
    $bmp = New-Object System.Drawing.Bitmap 16, 16, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.PixelOffsetMode    = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $g.InterpolationMode  = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $g.DrawImage($img, (New-Object System.Drawing.Rectangle 0, 0, 16, 16), $x, $y, $w, $h, [System.Drawing.GraphicsUnit]::Pixel)
    $g.Dispose()
    $bmp.Save($outFile, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    $img.Dispose()
}

# Each UV unit in the source geo.json corresponds to 4 actual pixels on these 64x64 atlases.
# --- Log (tree block.geo.json): cube size 13, sides UV [0,0] size [3.25,4.25] → px (0,0)..(13,17); top UV [6.5,0] size [3.25,3.25] → px (26,0)..(39,13)
Crop-Resize-16 "$src\bloodlinelog.png"  0  0 13 17 "$blockDir\bloodline_log_official.png"
Crop-Resize-16 "$src\bloodlinelog.png" 26  0 13 13 "$blockDir\bloodline_log_official_top.png"

# --- Grass: side UV [0,0] size [4,4] → (0,0)..(16,16); top UV [8,0] size [4,4] → (32,0)..(48,16)
Crop-Resize-16 "$src\bloodline grass.png"  0 0 16 16 "$blockDir\bloodline_grass_official_side.png"
Crop-Resize-16 "$src\bloodline grass.png" 32 0 16 16 "$blockDir\bloodline_grass_official_top.png"

# --- Dirt: UV [4,4] size [4,4] → pixels (16,16)..(32,32)
Crop-Resize-16 "$src\dirtbloodline.png" 16 16 16 16 "$blockDir\bloodline_dirt_official.png"

# --- Crimstone, Mud, Mud2: all use UV [0,0] size [4,4] → pixels (0,0)..(16,16)
Crop-Resize-16 "$src\crimstone.png" 0 0 16 16 "$blockDir\crimstone_official.png"
Crop-Resize-16 "$src\mud.png"       0 0 16 16 "$blockDir\bloodline_mud.png"
Crop-Resize-16 "$src\mud2.png"      0 0 16 16 "$blockDir\bloodline_mud2.png"

# --- Branches (128x128 atlas → 32x32 silhouette sprite)
function Crop-Resize-32([string]$srcFile, [int]$x, [int]$y, [int]$w, [int]$h, [string]$outFile) {
    $img = [System.Drawing.Image]::FromFile($srcFile)
    $bmp = New-Object System.Drawing.Bitmap 32, 32, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $g.DrawImage($img, (New-Object System.Drawing.Rectangle 0, 0, 32, 32), $x, $y, $w, $h, [System.Drawing.GraphicsUnit]::Pixel)
    $g.Dispose()
    $bmp.Save($outFile, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    $img.Dispose()
}
Crop-Resize-32 "$src\branch1.png" 0 0 64 64 "$blockDir\bloodline_branch.png"
Crop-Resize-32 "$src\branch2.png" 0 0 96 96 "$blockDir\bloodline_branch_2.png"

Write-Output "Re-extracted with correct UVs into $blockDir"
