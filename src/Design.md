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
* `128 - 255`: Robot --> server messages

Start of game (9 bytes per entity):

| Message type | Number of entities | *For each entity:* ID | *For each entity:* X | *For each entity:* Y | *For each entity:* Rotation | *For each entity:* Color |
| :----------: | :----------------: | :-------------------: | :------------------: | :------------------: | :-------------------------: | :----------------------: |
|  0 (1 byte)  |       1 byte       |        2 bytes        |        12 bits       |        12 bits       |           1 byte            |       3 bytes (rgb)      |

Game state (6 bytes per entity):

| Message type | Number of entities | *For each entity:* ID | *For each entity:* X | *For each entity:* Y | *For each entity:* Rotation |
| :----------: | :----------------: | :-------------------: | :------------------: | :------------------: | :-------------------------: |
|  1 (1 byte)  |       1 byte       |        2 bytes        |        12 bits       |        12 bits       |           1 byte            |

Robot request to join game (sent to server):

| Message type |     Color     |
| :----------: | :-----------: |
| 128 (1 byte) | 3 bytes (rgb) |

Server response after robot joins game (sent to robot):

| Message type | Robot ID |
| :----------: | :------: |
|  64 (1 byte) |  2 bytes |

Robot update (sent to server):

| Message type | Robot ID |  X acceleration |  Y acceleration | Rotation |
| :----------: | :------: | :-------------: | :-------------: | :------: |
| 129 (1 byte) |  2 bytes | 4 bytes (float) | 4 bytes (float) |  1 byte  |
