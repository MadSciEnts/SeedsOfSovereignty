# HUD INSTRUMENT CLASSIFICATION SYSTEM

## 1. Goal
To define a rigid, data-driven taxonomy for HUD controls. Instead of the UI "guessing" layout, the Hardware Module DNA specifies exactly which classification of control it requires.

## 2. Control Classifications

### 1. ACTION_BUTTON
- **Description:** A large, primary interactive surface for high-frequency usage.
- **Properties:** relX, relY, relativeTo, color, label, icon.
- **Standard Footprint:** 250x250.
- **Visuals:** Primary background texture (Up/Down), high-contrast icon.

### 2. MOVEMENT_JOYSTICK
- **Description:** A standard analog stick that snaps back to center (0,0).
- **Properties:** relX, relY, relativeTo, bgTexture, handleTexture.
- **Standard Footprint:** 250x250.
- **Logic:** Magnitude-based input, non-persistent.

### 3. TACTICAL_JOYSTICK
- **Description:** A precision stick for aiming or drone focus.
- **Properties:** relX, relY, relativeTo, bgTexture, handleTexture, isPersistent (Boolean), snapToOrigin (Boolean).
- **Standard Footprint:** 250x250.
- **Logic:** Optional persistent position tracking.

### 4. TOGGLE_BUTTON
- **Description:** A minor switch for changing states (e.g., Mode, Localization).
- **Properties:** relX, relY, relativeTo, color, label, icon, toggleStyle (ON_OFF vs MOMENTARY).
- **Standard Footprint:** 80x80.
- **Visuals:** State-aware textures.

### 5. DIAMOND_BUTTON
- **Description:** A small directional tap surface used in 4-way arrays (e.g., Shields).
- **Properties:** relX, relY, relativeTo, icon.
- **Standard Footprint:** 80x80.
- **Visuals:** Directional arrow icons, highlight on press.

### 6. PRECISION_SLIDER
- **Description:** A vertical or horizontal range modifier.
- **Properties:** relX, relY, relativeTo, thickness, length, color, orientation.
- **Standard Footprint:** Dynamic (e.g., 40x250).

## 3. Chained Positioning Logic
- `relativeTo`: String ID (actionKey) of the anchor control.
- If `relativeTo` is null, the control is positioned relative to the **Command Matrix Origin** (Bottom-Right).
- Every subsequent control sums the `relX` and `relY` values of its ancestors.
