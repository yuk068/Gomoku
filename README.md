# Gomoku

Classic Gomoku (5 in a row) with bots.

Is still a work in progress.

How to play: Each players take turn to place their stone on a 19 x 19 board, Black goes first, who ever make the first 5 in a row wins. Similar to TicTacToe or Caro. Stones once placed cannot be removed. No additional limitation.

This is a test for my implementation of bots for Gomoku. I'm still trying to improve upon it, if you wish to review the bot's log, execute ***"dev_run.bat"***, or open cmd and type ***"java -jar Gomoku.jar"*** (only for Windows and you should place ***"/Gomoku"*** in a different directory other than ***"/Downloads"***), if not then you can just execute ***"Gomoku.jar"***, or run ***"run.vbs"***. Thanks for trying out my program!

Compiled in Java 15

## Note:

### Updates:
- Implemented transposition table to significantly sped up some of the calculations.
- Replay mode: after a game ended you can review a game by pressing [ R ].
- Reworked some of the bots logic.

### Known issues:
- Bots can't behave correctly near the rear of the board yet.
- Removed support for 15 x 15 board for reason mentioned above.
- Some of them can take a very long time to think.
- Evaluation function needs more work.

## Configuration setup:

### Standard setup:
- [ N ] For new game setup
- [ L ] To show Move Log

### Dev tools:
- [ E ] To show Evaluation
- [ I ] To show squares index

### Replay mode:
- [ R ] To enter replay mode after a game ended
  - [ L ] To wind backwards
  - [ K ] To wind forwards

## Bots:
- Bullet
- Blitz
- Standard
- Overthinking
- Dynamic


