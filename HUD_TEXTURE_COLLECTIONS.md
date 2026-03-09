# HUD TEXTURE COLLECTIONS: Visual Language Specification

## 1. Goal
To define the visual DNA for procedural texture generation. Each module uses a `styleId` to pull a specific "Collection" of textures, ensuring consistency and high-fidelity aesthetics.

## 2. Style: INDUSTRIAL
- **Theme:** Brutalist, functional, heavy-duty.
- **Palette:** Dark Greys (#1A1A1A), Hazard Orange (#FF8C00), Steel Blue (#4682B4).
- **Edges:** 4px hard bevels, visible rivets (corners).
- **Backgrounds:** 85% opacity, slight metallic noise texture.
- **Highlight:** Glowing orange border on active/pressed state.

## 3. Style: HI_TECH (Vanguard Standard)
- **Theme:** Minimalist, holographic, precise.
- **Palette:** Deep Navy (#000033), Cyan (#00FFFF), Electric Blue (#7DF9FF).
- **Edges:** 1px glowing "wireframe" borders.
- **Backgrounds:** 30% opacity, scan-line overlays.
- **Highlight:** Pulsing cyan aura on active/pressed state.

## 4. Texture Assets per Collection
Every collection must generate the following standard keys:
- `joystick_bg`: 250x250 circular or hexagonal base.
- `joystick_knob`: 60x60 centered handle.
- `button_up`: Square base with style-specific borders.
- `button_down`: Color-inverted or glowing variant of button_up.
- `toggle_off`: Low-saturation variant of button_up.
- `toggle_on`: High-energy variant with state indicator (light).
- `slider_bg`: Thin vertical/horizontal trough.
- `slider_knob`: Wide handle for range tracking.
- `status_light`: Small 20x20 indicator (Red/Yellow/Green states).
