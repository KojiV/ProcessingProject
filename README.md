# Processing Project
> A random small game (engine?) made with the Java Processing visual library

 I made this when I was bored of doing Minecraft coding.
 
 # To-do
 
  - [ ] Add enemies
    - [ ] Fix an enemy spawning for everytime you visit an area
  - [ ] Fix going to non-existent area causing the player the get stuck
  - [ ] Fix sprite tree clipping **(Might not fix)**
  - [x] Keep one time boxes through sessions **(Fixed through player data)**
  - [x] Add game scaling feature (already added in non-pushed commit, but must be optimized) **(Added cuz I'm cool)**
  - [x] Fix issue with moving sprite at the end of sword animation (tf is going on?) **(Fixed, I feel like an idiot lmao)**
  - [x] See combining PImage and custom Image class **(NOPE, not doing!)**
 
 # How it Works
 
 Most of this engine (I'm calling it an engine idrc) runs off of a yml system (except collisions) taken from the Spigot Java API.
 
 ## Areas

There are 3 parts of each "area" that makes it work, the collisions, top half, and bottom half.

### Collisions

Collisons is the main area with an NPC overlayed that will automatically generate the neccessary txt file that makes collisions work.
![Spawn Area Example](https://user-images.githubusercontent.com/69867605/218506474-53f55a95-5f04-4221-bb99-024bde17fd69.png)

### Bottom Half

Everything on this layer goes behind the Player
![Spawn Area Example](https://user-images.githubusercontent.com/69867605/218506943-b320ac6b-beb2-4e56-967f-e64bd50dad9b.png)

### Top Half

Everything on this layer goes in front of the Player
![Spawn Area Example](https://user-images.githubusercontent.com/69867605/218507293-8ab356ff-d56e-4826-84a3-c3a3d16f6d14.png)

## NPCs

NPCs exist and sometimes have textboxes, with the file format shown below:

```
npcs:
  0: 
    sprite: 1
    x: 10
    y: 12
    areaX: 0 
    areaY: 2 
    textboxes:
      initial: BLACKSMITHQUEST
      normal: BLACKSMITHTALK 
    objActivate: 2 
    overCounter: true 
    extraTags:
      hasSword: true
```

### Arguments
- Sprite
     - 0 through 1, chooses what sprite is displayed
     - Defaults to 0
- X
     - The X tile where NPC is
- Y
     - The Y tile where NPC is
- areaX
     - The X area where the NPC is (Start area is X: 0 Y: 0)
- areaY
     - The Y area where the NPC is (Start area is X: 0 Y: 0)
- textboxes
     - initial
          - Corresponds to box in textboxes.yml
          - This box will only show on first interaction (per save)
     - normal
          - Corresponds to box in textboxes.yml
          - This box will show every time after the first
- objActivate
     - Argument takes int corresponding to obj in objectives.yml
     - Upon first interaction, the obj corresponding activates
- overCounter
     - Default: true
     - For stuff like shops, increases downward interact range
- extraTags
     - Anything here corresponds to a boolean in Player class
     - Will set to true on activate
