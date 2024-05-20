# Gomoku

Classic Gomoku (5 in a row) with bots.

How to play: Each players take turn to place their stone on the Go board, Black goes first, who ever make the first 5 in a row wins. Similar to TicTacToe or Caro. Stones once placed cannot be removed. No additional limitation.

## Configuration setup:

### Standard setup:

- [ N ] For new game setup
- [ L ] To show Move Log
- [ ESC ] To quit the program

### Dev tools:
- [ E ] To show Evaluation
- [ I ] To show squares index

## Bots:

#### Bullet: Depth = 0; Sight = 2;

#### Easy: Depth = 1; Sight = 2;

#### Medium: Depth = 2; Sight = 1;

#### Dynamic Medium

## Note:

This is a test for my implementation of bots for Gomoku. Bots: Bullet and Easy can be very fast. While Bots: Medium and Dynamic Medium can think for a very long time, sometimes up to ~50s. I'm still trying to improve upon it, thanks for trying out my program!