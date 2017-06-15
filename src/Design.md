# Design notes

As a couple of notes:

* IDs are stored as shorts. Since they are always served by the server the server can incrememnt this short for every new robot, and sincethey don't last very long a short allows enough bits of data.
Since no math is ever done on them, they are kept as shorts.
* X and Y positions are stored with 12 bits (so together they take up 3 bytes). They don't need any more precision than that.
However, in Java they are kept as integers so that they are compaitible with generic math routines that use integers.
* Rotation is stored as a byte. That should be neough resolution for display (on the server it has much more precision).
* Color is stored as 3 bytes as it's only passed once.
