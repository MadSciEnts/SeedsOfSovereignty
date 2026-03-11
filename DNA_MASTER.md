# SEED OF SOVEREIGNTY: THE MASTER DNA (v1.2.0)

## 1. CORE MATHEMATICS & SPATIAL LOGIC
### 1.1 Floating Origin Protocol
- **The Snap:** If `abs(player.position) > 10,000`, the `OriginSystem` snaps all entities back: `entity.pos -= player.pos`.
### 1.2 Camera Leash (Lerp)
- **Lead Offset:** Camera follows `player.pos + (player.velocity * 0.5s)`.
- **Dynamic Zoom:** Camera zoom lerps (1.0 to 1.4) during high-speed combat.

## 2. PROCEDURAL WEAPONRY & PROGRESSION
### 2.1 Power Progression
- 3-subtier model (a, b, c). Growth Factor: **3.2x DPS per Tier**.
### 2.2 Visual Language
- **Kinetic (Yellow)**, **Ion (Violet)**, **Acid (Green)**, **Piercer (Orange)**, **Disruptor (Cyan)**.

## 3. UI STYLE & AESTHETIC DNA
### 3.1 Aesthetic: "Tactical Minimalism"
- **Vibe:** Military terminal meets alien technology.
- **Lines:** Precise 1-2px lines. No rounded borders.
- **Typography:** Monospaced for data, wide-kerning sans-serif for headings.
### 3.2 The "Aperture" Standard
- **Frame:** 45-degree chamfered corners. 2-pixel borders with 1-pixel data-gaps.
- **Animation:** Windows slide open from a central horizontal slit. Background world blurs/darkens on focus.
### 3.3 Shader Standards
- **Bloom:** Energy pixels (Value > 0.8) bleed light.
- **Chromatic Aberration:** 1-pixel RGB shift during "Breach" or "Warp".
- **Scanlines:** Subtle 10% opacity HUD overlay.

## 4. DRONE ESCALATION
- **Scaling:** 1 Slot (T5-8) -> 4 Slots (T13+).
- **Count:** T5: 1 Drone -> T13+: 16 Drones.
