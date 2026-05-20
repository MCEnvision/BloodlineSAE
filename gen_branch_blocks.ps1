# Generates blockstate, core model, arm model, and item model JSONs for branch
# thickness blocks driven by a single halfWidth value (1..8).
#
# Run once per session to regenerate or extend the branch block set.

$root = "C:\Users\leanv\IdeaProjects\BloodlineSAE\src\main\resources\assets\bloodlinesae"
$blockstatesDir = Join-Path $root "blockstates"
$blockModelsDir = Join-Path $root "models\block"
$itemModelsDir  = Join-Path $root "models\item"

# (block_name, halfWidth)
$blocks = @(
    @{name="bloodline_branch_xl";        h=7},
    @{name="bloodline_branch_large";     h=6},
    @{name="bloodline_branch_thick";     h=5},
    @{name="bloodline_branch_medium";    h=3}
)

function Write-Blockstate($name) {
    $json = @"
{
  "multipart": [
    { "apply": { "model": "bloodlinesae:block/${name}_core" } },
    { "when": { "north": "true" }, "apply": { "model": "bloodlinesae:block/${name}_arm" } },
    { "when": { "east":  "true" }, "apply": { "model": "bloodlinesae:block/${name}_arm", "y":  90 } },
    { "when": { "south": "true" }, "apply": { "model": "bloodlinesae:block/${name}_arm", "y": 180 } },
    { "when": { "west":  "true" }, "apply": { "model": "bloodlinesae:block/${name}_arm", "y": 270 } },
    { "when": { "up":    "true" }, "apply": { "model": "bloodlinesae:block/${name}_arm", "x": 270 } },
    { "when": { "down":  "true" }, "apply": { "model": "bloodlinesae:block/${name}_arm", "x":  90 } }
  ]
}
"@
    Set-Content -Path (Join-Path $blockstatesDir "$name.json") -Value $json -Encoding utf8
}

function Write-CoreModel($name, $h) {
    $lo = 8 - $h
    $hi = 8 + $h
    # Core shows BARK (#side) on every face. The arms cover up the faces in their direction,
    # so what's left visible is always the bark wrap of the branch — never end-grain rings.
    $json = @"
{
  "parent": "minecraft:block/block",
  "textures": {
    "end": "bloodlinesae:block/bloodline_log_official_top",
    "side": "bloodlinesae:block/bloodline_log_official",
    "particle": "bloodlinesae:block/bloodline_log_official"
  },
  "elements": [
    {
      "from": [$lo, $lo, $lo],
      "to":   [$hi, $hi, $hi],
      "faces": {
        "north": {"uv": [$lo, $lo, $hi, $hi], "texture": "#side"},
        "south": {"uv": [$lo, $lo, $hi, $hi], "texture": "#side"},
        "east":  {"uv": [$lo, $lo, $hi, $hi], "texture": "#side"},
        "west":  {"uv": [$lo, $lo, $hi, $hi], "texture": "#side"},
        "up":    {"uv": [$lo, $lo, $hi, $hi], "texture": "#side", "rotation": 270},
        "down":  {"uv": [$lo, $lo, $hi, $hi], "texture": "#side", "rotation": 90}
      }
    }
  ]
}
"@
    Set-Content -Path (Join-Path $blockModelsDir "${name}_core.json") -Value $json -Encoding utf8
}

function Write-ArmModel($name, $h) {
    $lo = 8 - $h
    $hi = 8 + $h
    # arm depth = lo + 1 so it slightly overlaps the core face (no Z-fight)
    $depth = $lo + 1
    $json = @"
{
  "parent": "minecraft:block/block",
  "textures": {
    "side": "bloodlinesae:block/bloodline_log_official",
    "end": "bloodlinesae:block/bloodline_log_official_top",
    "particle": "bloodlinesae:block/bloodline_log_official"
  },
  "elements": [
    {
      "from": [$lo, $lo, 0],
      "to":   [$hi, $hi, $depth],
      "faces": {
        "north": {"uv": [$lo, $lo, $hi, $hi], "texture": "#end"},
        "east":  {"uv": [0, $lo, $depth, $hi], "texture": "#side"},
        "west":  {"uv": [0, $lo, $depth, $hi], "texture": "#side"},
        "up":    {"uv": [$lo, 0, $hi, $depth], "texture": "#side", "rotation": 270},
        "down":  {"uv": [$lo, 0, $hi, $depth], "texture": "#side", "rotation": 90}
      }
    }
  ]
}
"@
    Set-Content -Path (Join-Path $blockModelsDir "${name}_arm.json") -Value $json -Encoding utf8
}

function Write-ItemModel($name) {
    $json = @"
{ "parent": "bloodlinesae:block/${name}_core" }
"@
    Set-Content -Path (Join-Path $itemModelsDir "$name.json") -Value $json -Encoding utf8
}

foreach ($b in $blocks) {
    Write-Blockstate $b.name
    Write-CoreModel  $b.name $b.h
    Write-ArmModel   $b.name $b.h
    Write-ItemModel  $b.name
    Write-Output "Generated: $($b.name) (halfWidth=$($b.h))"
}
