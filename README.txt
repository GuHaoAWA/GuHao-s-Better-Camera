Guhao's Better Camera
=====================

Minecraft 1.20.1 / Forge 47 client camera mod.

The camera shapes mouse rotation with three frame-rate-independent critically
damped velocity channels. The fast channel keeps the controls responsive, the
body channel provides cinematic weight, and the chase channel makes the camera
continue pursuing the invisible target point after input changes. Camera
translation follows the character focus with a separate critically damped spatial
spring. The third-person orbit follows the already-smoothed view rotation directly,
preventing a second large positional trail while turning.

Default behavior
----------------

- Enabled in third person.
- Disabled in first person to avoid visual aiming lag.
- Automatically yields to Minecraft's F8 cinematic camera while that mode is on.
- Position following yields during creative/spectator flight and elytra flight to
  keep the rendered player locked to Minecraft's high-speed movement interpolation.
- Yields while another screen or a non-player camera entity owns mouse input.
- Resets cleanly after pausing, changing perspective, changing worlds, or changing
  the camera entity.

Configuration
-------------

Forge creates `config/better_camera-client.toml` after the first launch.
Run the client command `/bettercamera` to open the in-game configuration screen.
Changes are only written when Save is pressed; Cancel or Escape discards the draft.

- `fastResponseTime`: response of the fast, control-focused channel.
- `bodyResponseTime`: response of the smooth cinematic body channel.
- `chaseResponseTime`: response of the slow target-pursuit channel.
- `fastResponseWeight`: blend between responsive and cinematic motion.
- `chaseResponseWeight`: target-pursuit share inside the cinematic response.
- `positionResponseTime`: character-translation follow response; rotation is not damped twice.
- `verticalResponseMultiplier`: vertical response relative to horizontal motion.
- `applyInFirstPerson`: optional first-person damping.
- `applyInThirdPerson`: third-person damping switch.
- `applyPositionDamping`: third-person camera-position follow switch.

The defaults (`0.032`, `0.135`, `0.20`, `0.28`, `0.30`, `0.24`, `0.90`) use a heavier
Sekiro-inspired third-person profile: restrained at turn-in, visibly chasing the
target point, then settling without overshoot or raw Minecraft snapping.

Position following runs after `Camera.setup`, Perspective API behaviors, Epic
Fight camera construction, and Forge camera-angle listeners have resolved the
camera. Immediately before frustum construction it adds only the smoothed
character-translation offset to the final camera position. Third-party rotation,
lock-on, shoulder offset, zoom, and collision results remain untouched. An
additional eight-ray sweep clips only Better Camera's added translation.

Compatibility
-------------

Mouse shaping remains a low-priority, non-cancellable HEAD injection. It only
reshapes raw accumulated mouse deltas and leaves sensitivity, inverted Y,
scoping, lock-on, player turning, camera transforms, and synthetic rotations to
downstream code. Position following is a separate low-priority render-stage
injection after the final camera transform and before culling.

Mods that temporarily own the camera can register a lightweight bypass without
mixing into Better Camera:

`CameraCompatibility.registerSmoothingBypass(() -> customCameraIsActive);`

Mods that only own one layer can use
`registerRotationSmoothingBypass(...)` or `registerPositionSmoothingBypass(...)`
and keep the other layer active.

Call `CameraCompatibility.unregisterSmoothingBypass(condition)` when the owner
is unloaded. A failing condition is removed automatically instead of breaking
the render thread.

Development
-----------

After the optional project dependencies declared in `build.gradle` are present,
use `gradlew runClient` to launch the client and `gradlew build` to build the mod.
