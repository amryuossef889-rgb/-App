#!/usr/bin/env python3
"""Generate Android launcher icon densities from the repository's app_icon.png."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageOps

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "app/src/main/res/drawable/app_icon.png"
RES = ROOT / "app/src/main/res"
DENSITIES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

if not SOURCE.is_file():
    raise SystemExit(f"Required repository app icon not found: {SOURCE}")

with Image.open(SOURCE) as opened:
    image = opened.convert("RGBA")

if image.width < 128 or image.height < 128:
    raise SystemExit(f"app_icon.png must be at least 128x128; got {image.size}")

# App icons should fill their square canvas; crop evenly if the source isn't square.
square = ImageOps.fit(image, (512, 512), method=Image.Resampling.LANCZOS, centering=(0.5, 0.5))

for folder, size in DENSITIES.items():
    out_dir = RES / folder
    out_dir.mkdir(parents=True, exist_ok=True)
    resized = square.resize((size, size), Image.Resampling.LANCZOS)
    resized.save(out_dir / "ic_launcher.webp", "WEBP", quality=100, method=6)

    round_icon = resized.copy()
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, size - 1, size - 1), fill=255)
    round_icon.putalpha(mask)
    round_icon.save(out_dir / "ic_launcher_round.webp", "WEBP", quality=100, method=6)

print(f"Generated all legacy launcher icon densities from {SOURCE}")
