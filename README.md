# FedoraBots

## Instructions

Run `server.Test` to spawn a server that hosts a game, with one room that accepts six robots.

Start up six `example.CircleBot`s, and the game will start after the sixth `CircleBot` is started.

To show another display, run `client.Display` and give the room id (in this case `0`) as an argument.

## Gradle tasks

To build a jar with the required libraries, use `gradlew jar`. For a jar with javadocs for the library and BoofCV, use `gradlew javadocJar`.

Both of these jars will be put in the `build/libs` folder.
