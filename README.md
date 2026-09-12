# Potion-Optimizer-v.1.21
---updated and ported to v.1.21---
Overview

Potion Optimizer removes the small delay between clicking to throw a splash or lingering potion and actually seeing it leave your hand. Normally your client waits for the server to confirm the throw before showing anything - on higher ping that wait is noticeable. This mod spawns the real potion entity on your client the instant you click, using the same physics and rendering as vanilla, then quietly hands off to the server's own potion once it arrives.
What it does not do

This is a visual-only client-side prediction. The predicted potion cannot apply any effect, damage, or healing - all of that is still decided entirely by the server, exactly like vanilla. Nothing about the actual outcome of a throw is different with or without this mod installed.
Features

    Instant visual feedback when throwing splash or lingering potions - no waiting on the server round-trip
    Uses vanilla's real throw physics (speed, arc, gravity) so the prediction matches the real throw
    Automatically hands off to the server's real potion entity once it arrives, so there's never a duplicate
    Can be fully enabled or disabled from the config screen (Mod Menu / Cloth Config)
    Supports a server-side opt-out signal for anti-cheat plugins, so server admins can disable the mod for their server without needing to detect or ban it

Requirements

    Fabric Loader 0.19.3+
    Fabric API
    Cloth Config
    Mod Menu (for the config screen)


