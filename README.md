gridderface
===========

A Scala reboot of a keyboard interface for marking grid-based puzzles --- Nikoli-style, generally.

If:

- you want to solve a Sudoku or Slitherlink or Fillomino or Corral or Nurikabe or crossword or most any other puzzle with a square (or rectangular) grid in it
- you want to solve it on your computer (you don't have access to a printer or don't want to use one)
- the author hasn't put it into a puzzle applet, and you're too lazy to enter the numbers into one
- you like using the keyboard, vaguely vi-like keybindings, modal editing, and a steep learning curve
- you have the fortitude to set up Scala and SBT on your machine

then this is the program for you!

## Installation

1. Get [SBT](http://www.scala-sbt.org/).
2. Clone.
3. `cd` into the directory.
4. `sbt run`.
5. Cross your fingers.

## General Usage

The general way keys and commands work should be familiar to any Vim user, except that Gridderface doesn't yet support arbitrarily abbreviating commands.

## Solve a Puzzle

### Load the image

Issue the command `:read <filename>` to read an image from somewhere. Or drag an image or image file in. (In theory, dragging images should work, but the clipboard interface between Java and Mac OS X for images seems to be broken...)

### Setup the Grid

The command `:guess` will attempt to guess the location and size of the grid in the image. It is very naive as of the time of writing. Manually adjusting the grid can be done in grid-setting mode, entered with the key `CTRL-G`, and then using `hjkl+-HJKL` or arrow keys to change position and dimensions. Typing a number will make the adjustments change by larger increments; typing `` ` `` (backquote) followed by a number will make the adjustments change by fractional increments.

Once you've set up the grid, it's often very distracting from the image and your own marks. You can hide it with `:hide grid`.

### Solve

You can start solving now! Enter `CTRL-D` to enter drawing mode (this is the default mode). Move around with the obvious `hjkl` or arrow keys. There are lots of keys for entering particular figures on cells, edges, and intersections, including `f`, `.`, space, and all ten digits; see [StampSet.scala](https://github.com/betaveros/gridderface/blob/master/src/main/scala/gridderface/StampSet.scala) to see the list.

You can also enter arbitrary text into cells with one of several keys: `=`, `;`, `^`, or `_`. All four keys will enter the command line, where you can type some text and hit Enter. `=` enters big text. `;`, `^`, and `_` enter small text aligned in different places.

If you're solving a puzzle where you spend most or all of your time marking only cells (e.g. Sudoku, Nurikabe, crosswords), you can lock the cursor to only ever select cells with `:lock`. For intersections, it's `:ilock`. To unlock, `:unlock`.

To make drawing continuous lines easier, you can use `HJKL`. They can also be set to erase or draw other things, by typing `w` followed by another character (see [WriteSet.scala](https://github.com/betaveros/gridderface/blob/master/src/main/scala/gridderface/WriteSet.scala)).

To change the color of your marks, type `c` followed by a letter (see [PaintSet.scala](https://github.com/betaveros/gridderface/blob/master/src/main/scala/gridderface/PaintSet.scala)) or `%` followed by either a color name or a hex color in `#abcdef` format (with the hash!)

## Create a Puzzle

You can initialize a blank image with standard dimensions (10x10), standard grid dimensions, and standard position with `:initgen`. The command can be followed by one or two numbers to specify the dimensions.

For most puzzles, you now want to draw a grid into which the clues will do. These commands will create some of the presets:

- `:dec pre corral`
- `:dec pre fillomino`
- `:dec pre nurikabe`
- `:dec pre slitherlink`

You can choose exactly what grids you want and where you want them, which you may be able to figure out by looking at [GridderfaceDecorator.scala](https://github.com/betaveros/gridderface/blob/master/src/main/scala/gridderface/GridderfaceDecorator.scala), but these presets should be enough for 99% of uses.
