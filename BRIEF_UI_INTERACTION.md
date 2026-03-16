# AGENT BRIEF: COMMAND MATRIX & UI INTERACTION

## ROLE
You are the **UI/UX Agent** responsible for the high-fidelity HUD and interaction layer of "Seeds of Sovereignty."

## CONTEXT
The architecture uses a `CommandMatrixView` class to manage the bottom-right HUD. Data is piped to the ECS via `ModuleChannelsComponent`.

## OBJECTIVES
1. **Full Control Layouts**: 
   - Define and implement the visual layout for all Tier 1 modules (Kinetic, Laser, Shields, Power).
   - Ensure widgets are positioned and sized according to `ModuleTypeDefinition`.
2. **Joystick Synchronization**:
   - Ensure the attack joystick in the Command Matrix matches the exact scale and feel of the movement joystick.
3. **Visual Feedback (The "Feel")**:
   - Implement "Aperture" style UI transitions.
   - Add visual tweens or "glitch" frames when buttons are tapped or categories switched.
4. **Data Visualization**:
   - Read from `ModuleChannelsComponent.outputs` to render status bars (Heat, Ammo, Shield Strength).

## CONSTRAINTS
- **LibKTX Scene2D**: Use the DSL for all layout logic.
- **Responsive Design**: Ensure layouts scale correctly based on the `joystickSize` passed from `PlayScreen`.
- **Reference**: @CommandMatrixView.kt, @UI_STYLE_GUIDE.md, @DNA_UI_PIXEL_GEN.md
