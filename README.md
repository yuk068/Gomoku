# Gomoku

Classic Gomoku (5 in a row) with bots.

How to play: Each players take turn to place their stone on a 19 x 19 (and 15 x 15) Go board, Black goes first, who ever make the first 5 in a row wins. Similar to TicTacToe or Caro. Stones once placed cannot be removed. No additional limitation.

## Configuration setup:

### Standard setup:

- [ N ] For new game setup
- [ L ] To show Move Log

### Dev tools:
- [ E ] To show Evaluation
- [ I ] To show squares index

## Bots:

#### Bullet: Depth = 0; Sight = 2;

#### Blitz: Depth = 1; Sight = 2;

#### Standard: Depth = 2; Sight = 1;

#### Dynamic: Min depth = 1; Max depth = 2; Min sight = 1; Max sight = 2;

## Note:

This is a test for my implementation of bots for Gomoku. Bots: Bullet and Easy can be very fast. While Bots: Medium and Dynamic Medium can think for a very long time, sometimes up to ~50s. I'm still trying to improve upon it, if you wish to review the bot's log, execute ***"dev_run.bat"***, or open cmd and type ***"java -jar Gomoku.jar"*** (only for Windows and you should place ***"/Gomoku"*** in a different directory other than ***"/Downloads"***), if not then you can just execute ***"Gomoku.jar"***, or run ***"run.vbs"***. Thanks for trying out my program!
