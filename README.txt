Guhao's Better Camera
=====================

Minecraft 1.20.1 / Forge 47 client camera mod.

The camera shapes mouse rotation with three frame-rate-independent critically
damped velocity channels. The fast channel keeps the controls responsive, the
body channel provides cinematic weight, and the chase channel makes the camera
continue pursuing the invisible target point after input changes. Camera position
also follows the collision-safe third-person target with a separate critically
damped spatial spring.

Default behavior
----------------

- Enabled in third person.
- Disabled in first person to avoid visual aiming lag.
- Automatically yields to Minecraft's F8 cinematic camera while that mode is on.
- Yields while another screen or a non-player camera entity owns mouse input.
- Resets cleanly after pausing, changing perspective, changing worlds, or changing
  the camera entity.

Configuration
-------------

Forge creates `config/better_camera-client.toml` after the first launch.

- `fastResponseTime`: response of the fast, control-focused channel.
- `bodyResponseTime`: response of the smooth cinematic body channel.
- `chaseResponseTime`: response of the slow target-pursuit channel.
- `fastResponseWeight`: blend between responsive and cinematic motion.
- `chaseResponseWeight`: target-pursuit share inside the cinematic response.
- `positionResponseTime`: third-person camera-position follow response.
- `verticalResponseMultiplier`: vertical response relative to horizontal motion.
- `applyInFirstPerson`: optional first-person damping.
- `applyInThirdPerson`: third-person damping switch.
- `applyPositionDamping`: third-person camera-position follow switch.

The defaults (`0.032`, `0.135`, `0.32`, `0.28`, `0.45`, `0.90`) use a heavier
Sekiro-inspired third-person profile: restrained at turn-in, visibly chasing the
target point, then settling without overshoot or raw Minecraft snapping.

Position following runs after vanilla camera distance and wall collision are
calculated. It performs an additional eight-ray collision check, snaps on camera
mode changes, teleports, or long frame stalls, and is disabled in first person.

Compatibility
-------------

The mixin is a low-priority, non-cancellable HEAD injection. It only reshapes
the raw accumulated mouse deltas and leaves sensitivity, inverted Y, scoping,
lock-on, player turning, camera transforms, and synthetic rotations to downstream
code. It uses its own clock, passes input through after long frame stalls, and
does not redirect or overwrite `MouseHandler.turnPlayer`.

Mods that temporarily own the camera can register a lightweight bypass without
mixing into Better Camera:

`CameraCompatibility.registerSmoothingBypass(() -> customCameraIsActive);`

Call `CameraCompatibility.unregisterSmoothingBypass(condition)` when the owner
is unloaded. A failing condition is removed automatically instead of breaking
the render thread.

Development
-----------

After the optional project dependencies declared in `build.gradle` are present,
use `gradlew runClient` to launch the client and `gradlew build` to build the mod.
