# Processing Project
> A random small game (engine?) made with the Java Processing visual library

 I made this when I was bored of doing Minecraft coding.
 
 # To-do
 
  - [ ] Add enemies
  - [ ] Fixed sprite tree clipping
  - [ ] Keep one time boxes through sessions
 
 # How it Works
 
 Most of this engine (I'm calling it an engine idrc) runs off of a yml system (except collisions) taken from the Spigot Java API
 
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
  0: #NPC ID
    sprite: 1 #The NPC sprite to be displayed
    x: 10 #The X tile where the NPC is
    y: 12 #The Y tile where the NPC is
    areaX: 0 #The X of the area where the NPC is
    areaY: 2 #The Y of the area where the NPC is
    textboxes: #These textboxes are based on IDs in textboxes.yml
      initial: BLACKSMITHQUEST #This textbox only shows on first interaction
      normal: BLACKSMITHTALK #This textbox shows every other time
    objActivate: 2 #After first interaction, the objective (based on ID in objectives.yml) activates
    overCounter: true #For stuff like shops, increases downward interact range
    extraTags: #Extra booleans for the Player class to use
      hasSword: true
```
