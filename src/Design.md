# Design notes

As a couple of notes:

* IDs are stored as shorts. Since they are always served by the server the server can incrememnt this short for every new robot, and sincethey don't last very long a short allows enough bits of data.
Since no math is ever done on them, they are kept as shorts.
* X and Y positions are stored with 12 bits (so together they take up 3 bytes). They don't need any more precision than that.
However, in Java they are kept as integers so that they are compaitible with generic math routines that use integers.
* Rotation is stored as a byte. That should be neough resolution for display (on the server it has much more precision).
* Color is stored as 3 bytes as it's only passed once.

## Protocol

### Message ranges

* `0 - 63`: Server --> displays messages
* `64 - 127`: Server --> robots messages
* `128 - 191`: Robot --> server messages
* `192 - 255`: Display --> server messages

Start of game (11 bytes per entity, 5 bytes per obstacle):

| Message type | Number of entities | Number of obstacles |
| :----------: | :----------------: | :-----------------: |
|  0 (1 byte)  |       1 byte       |       1 byte        |

Continued...

| *For each entity:* ID | *For each entity:* X | *For each entity:* Y | *For each entity:* Rotation | *For each entity:* V angle | *For each entity:* A angle | *For each entity:* Color |
| :-------------------: | :------------------: | :------------------: | :-------------------------: | :------------------------: | :------------------------: | :----------------------: |
|        2 bytes        |        12 bits       |        12 bits       |           1 byte            |           1 byte           |           1 byte           |       3 bytes (rgb)      |

Continued...

| *For each obstacle:* ID | *For each obstacle*: type | *For each obstacle*: X | *For each obstacle*: Y |
| :---------------------: | :-----------------------: | :--------------------: | :--------------------: |
|         1 byte          |          1 byte           |        12 bits         |         12 bits        |

Game state (8 bytes per entity and 4 bytes per bullet):

| Message type | Number of entities | Number of bullets | Vx for connection's robot | Vy for connection's robot |
| :----------: | :----------------: | :---------------: | :-----------------------: | :-----------------------: |
|  1 (1 byte)  |       1 byte       |      2 bytes      |      4 bytes (float)      |      4 bytes (float)      |

Continued...

| *For each entity:* ID | *For each entity:* X | *For each entity:* Y | *For each entity:* Rotation | *For each entity:* V angle | *For each entity:* A angle |
| :-------------------: | :------------------: | :------------------: | :-------------------------: | :------------------------: | :------------------------: |
|        2 bytes        |        12 bits       |        12 bits       |           1 byte            |           1 byte           |           1 byte           |

Continued...

| *For each bullet:* X | *For each bullet:* Y | *For each bullet*: Rotation |
| :------------------: | :------------------: | :-------------------------: |
|        12 bits       |        12 bits       |            1 byte           |

Health update (5 bytes per entity):

| Message type | Number of entities | *For each entity:* ID | *For each entity:* Health | *For each entity:* Bullet Angle |
| :----------: | :----------------: | :-------------------: | :-----------------------: | :-----------------------------: |
|  2 (1 byte)  |       1 byte       |        2 bytes        |           1 byte          | exists = 1 bit, angle = 11 bits |

*Note: If the bullet comes from a vaporizer, exists will = 0 and the value of angle will be the id of the vaporizer.*

Obstacle update (2 bytes per obstacle):

| Message type | Number of obstacles | *For each obstacle:* ID | *For each obstacle:* Rotation |
| :----------: | :-----------------: | :---------------------: | :---------------------------: |
|  3 (1 byte)  |       1 byte        |         1 byte          |            1 byte             |

Server response after a display spectates a game for success (sent to display):

| Message type |
| :----------: |
|  4 (1 byte)  |

Robot request to join game (sent to server):

| Message type | Room ID |     Color     |
| :----------: | :-----: | :-----------: |
| 128 (1 byte) | 2 bytes | 3 bytes (rgb) |

Server response after robot joins game for success (sent to robot):

| Message type | Robot ID |
| :----------: | :------: |
|  64 (1 byte) |  2 bytes |

Server response if robot tries to join a nonexistent room (sent to robot):

| Message type |
| :----------: |
|  65 (1 byte) |

Server response if robot tries to join a room that already began its game (sent to robot):

| Message type |
| :----------: |
|  66 (1 byte) |

Robot update (sent to server):

| Message type | Robot ID |  X acceleration |  Y acceleration |    Rotation     |
| :----------: | :------: | :-------------: | :-------------: | :-------------: |
| 129 (1 byte) |  2 bytes | 4 bytes (float) | 4 bytes (float) | 2 bytes (short) |

Robot request to shoot (sent to server):

| Message type | Robot ID |
| :----------: | :------: |
| 130 (1 byte) |  2 bytes |

Spectate game (sent to server):

| Message type | Room ID |
| :----------: | :-----: |
| 192 (1 byte) | 2 bytes |
