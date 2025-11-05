
<div align="center">

![IsoRogue Banner](https://placehold.co/800x200/2ECC71/000000?text=IsoRogue&font=montserrat)

![Java](https://img.shields.io/badge/Java-8%20&%2021-blue?logo=openjdk&logoColor=white)
![Spigot API](https://img.shields.io/badge/Spigot-1.8%20&%201.21-orange?logo=spigotmc)
![License](https://img.shields.io/badge/License-MIT-yellow?logo=opensourceinitiative)

</div>

I've always been fascinated by the challenge of pushing Minecraft's engine to its limits. **IsoRogue** is my personal exploration into creating a completely new gameplay genre: a top-down, isometric rogue-like. It was a fun excuse to dive deep into Netty for packet injection, build a custom camera system that feels smooth and responsive, all to see if I could build a polished and genuinely fun arcade experience.

---

## Showcase: The Player Experience

This project is built around a single core principle: creating a seamless, third-person, isometric gameplay loop that feels native to Minecraft, despite the engine's limitations. The player controls a miniature avatar on a procedurally generated map, battling themed enemies and collecting loot in a high-stakes, action-RPG format.


| Gameplay Moment                                           | Description |
|:----------------------------------------------------------| :--- |
| ![Dash Showcase](.github/assets/dash_through_hellion.gif) | The core combat loop features a high-impact dash ability that allows players to reposition, escape, or execute enemies with bonus damage. The camera smoothly follows the action, providing a clean and cinematic view. |
| ![XRay System](.github/assets/xray_outline.gif)           | To ensure a perfect line of sight at all times, the custom camera system dynamically renders occluding blocks transparent, outlining their wireframe so the player never loses spatial awareness. |
| ![Equiping Armor](.github/assets/equip_armor.gif)         | Looting is designed to be fast and fluid. Chests explode with items, and the auto-equip system instantly upgrades the player's gear if the new item is a higher tier, keeping them in the action without inventory management. |

---

## Dev Log: A Look Behind the Scenes

This project was an exercise in solving complex architectural and engine-level challenges. Here’s a look at the design philosophy behind some of the core systems.

### 1. The Unified Camera & Input System

The single greatest challenge was creating a system where the camera could move smoothly to follow the action *without* severing the player's connection to their input vehicle. Early attempts to teleport the player directly resulted in a broken input stream, as the client would believe it had dismounted.

**The Solution: The "Mounted Camera" Architecture**

The final architecture is a unified system where the camera and the input mechanism are the same entity. This creates a perfectly stable and smooth experience by working *with* the game engine's passenger physics, not against it.

<div align="center">
    <img src=".github/assets/mounted_camera_2.gif" alt="Mounted Camera BTS" width="800"/>
</div>

*   **A. The Input Vehicle & Camera Mount (`ArmorStand`):** A single, server-side, invisible `ArmorStand` is spawned at the camera's position. It has no gravity. This entity serves two critical purposes:
    1.  It is the vehicle the player is a **passenger** of, enabling the `PacketPlayInSteerVehicle` input system.
    2.  It is the physical "body" of our camera.

*   **B. The Player (The "Eyeball"):** The player's own entity is the camera. Their viewport moves because the vehicle they are riding moves. They are essentially a passenger on a smooth, invisible camera drone.

*   **C. The `GameLoop` (The "Cameraman"):** The game loop acts as the cameraman. Every tick, it:
    1.  Calculates the *ideal* position for the camera based on the `PlayerNPC`'s location.
    2.  Applies a smooth, interpolated movement (via packet-based teleportation) to the **`ArmorStand` vehicle**.
    3.  The server's native passenger logic automatically and perfectly keeps the player synced to the moving vehicle, ensuring the input stream is **never broken**.

This design provides the best of all worlds: the unbreakable input from a vehicle system and the perfectly smooth, cinematic camera movement from a decoupled follow system.

### 2. The Dynamic X-Ray: Maintaining a Clear View

A core challenge for any third-person or isometric camera is occlusion, what happens when the player's character moves behind a wall or a tree? A camera that gets stuck or a view that is blocked creates a frustrating and unplayable experience.

**The Solution: A Client-Side Block Rendering Engine**

The plugin features a real-time X-ray system, that ensures the player always has a clear line of sight to the action. It operates entirely on the client-side using packets, meaning the server's world is never actually modified.

<div align="center">
    <img src=".github/assets/xray_raycast.gif" alt="XRay Raycast BTS" width="800"/>
</div>

The `XRayManager` performs this task every single tick:
1.  **Volumetric Raycast:** A 3x3 "beam" is cast from the camera's position to the `PlayerNPC`'s position. This gathers a list of all potentially view-blocking blocks.
2.  **State Management:** The system maintains a set of `hiddenBlocks`. It intelligently compares the blocks found in the current tick with the set from the previous tick to determine what has changed.
3.  **Packet-Based Rendering:**
    *   For any **new** block that enters the beam, a `PacketPlayOutBlockChange` is sent to the player, telling their client to render that block as `AIR`.
    *   For any block that is **no longer** in the beam, a second packet is sent to change it back to its original, real material.
4.  **Merged Wireframe Outline:** To maintain spatial awareness, the system then analyzes the shape of the entire hidden volume. It detects only the "exterior" faces and draws their edges with a clean particle wireframe. This prevents a messy "cage" effect and provides a clear, readable outline of the obstacle.

This system is highly efficient and creates a seamless visual experience.

### 3. The Hybrid NPC: A Solution for AI Targeting & Interaction

A core architectural challenge was making server-side enemies "see" and interact with the player's character, which is a purely client-side, "ghost" entity created with packets. The server has no knowledge of this visual entity, so how can an enemy chase it?

**The Solution: The "Living" Hitbox**

The character on screen is actually a composite of two perfectly synchronized entities, creating a flawless illusion that both the player and the server's AI can interact with.

<div align="center">
    <img src=".github/assets/server_side_hitbox.gif" alt="Server Side Hitbox" width="600"/>
    <img src=".github/assets/client_side_hitbox_2.gif" alt="Client Side Hitbox" width="600"/>
</div>
<p align="center"><em>Comparison of server-side (left) and client-side (right) hitboxes.</em></p>

*   **The Visual NPC:** This is the character you see. It's a client-side `EntityPlayer` created via packets, complete with your skin, equipment, and animations. It provides all the visual feedback but is a ghost to the server's physics and AI.

*   **The Hitbox:** This is the character's body. It's a server-side, custom `Entity` (an invisible, Armorstand). The `GameLoop` applies all player movement and physics to this entity. Because it's a real, physical object on the server, a few key things become possible:
    1.  It provides accurate collision with the game world.
    2.  It can be reliably targeted by other server-side entities.
    3.  It can trigger Bukkit events, like `EntityDamageByEntityEvent`.

The `GameLoop`'s final job is to perfectly synchronize the position of the visual NPC to its physical hitbox every single tick. To make the enemies chase this hitbox, a **custom AI state machine** is used. It bypasses vanilla targeting and uses a geometric vision cone check to detect the player. Once detected, it issues direct commands to the enemy's `Navigation` component, telling it to pathfind towards the hitbox's location, creating a completely predictable AI behavior.

---

## Features

*   **Multi-Version Support (1.8.8 & 1.21+):** Built on a multi-module Gradle architecture.
*   **Custom NMS Entities:** Enemies with custom AI, and behaviors.
*   **Packet-Based Systems:**
    *   Netty injection for a vehicle-based input system.
    *   Client-side PlayerNPC with full skin layer support.
    *   Custom camera system.
    *   Client-side block changes for X-Ray vision and chest animations.
*   **Procedural, Themed World Generation:** Three distinct level themes (Overworld, Nether, End) with unique blocks, decorations, and enemies.
*   **Dynamic Gameplay Systems:**
    *   Enemy vision cone/aggro system.
    *   Dash ability with offensive and defensive uses.
    *   Tiered weapon and armor system.
    *   "Exploding" chests with an auto-pickup/auto-equip loot system.
*   **Polished Visuals:**
    *   Dynamic X-Ray vision with merged block outlines.
    *   Static and dynamic holograms for player guidance.
    *   Custom death animations and particle effects.