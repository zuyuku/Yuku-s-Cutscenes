# Overview
This mod adds tools for server admins to create smooth cutscenes. Intended for adventure maps, managed servers and other story-based content!

## Commands
This mod adds 2 new commands for creating cutscenes:
- /screeneffect
- /cutscene

### Basic Syntax
- /screeneffect [player] [screenEffect name] [introLength] [holdLength] [outroLength] ["command to run while screeneffect is dark"] _(display a screenEffect to a player)_

- /cutscene list _(lists the world's current cutscenes)_
- /cutscene add [name] _(adds a cutscene to the world at your camera position and rotation)_
- /cutscene remove [name] _(removes a given cutscene)_
- /cutscene resetAll confirm _(removes all of the world's cutscenes. USE WITH CAUTION)_
- /cutscene play [player] [cutscene name] [length] [easingType] [holdStart and/or holdEnd length] [endPlayer and/or startPlayer] _(display a cutscene to a player)_

## Editor
It also adds a new item to view and edit cutscene paths in the world. Get with "/give @s yukuscutscenes:editor".

After making cutscene, nodes of the path can be moved by right clicking-and-dragging with the editor tool. While dragging a node, scrolling can push/pull it away from you.

Right clicking a node on the end of the path adds a new node. Shift clicking a node on the end of the path deletes it.
