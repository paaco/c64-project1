# c64-project1

## Code

Compile with [C64Studio](https://github.com/GeorgRottensteiner/C64Studio) (uses ACME assembler syntax).
The solution and project file (.c64 and .s64) assume c:\dev\c64\c64-project1 folder.

## Data

Level data is stored in a .ctm file, made with [Charpad](http://www.subchristsoftware.com/charpad.htm).
The data is exported to chars (8 bytes per character), tiles (4 * 4 = 16 bytes per tile top-down-left-right) and map (width * height tile numbers bytes).

## Thoughts

6502: Arrays of structures are not your friend. Use structures of arrays where possible. For pointer lists, separate low and high bytes over two arrays.

Tiles of 4 x 4 bytes are stored as "structures". Unpacking tiles to 16 consecutive arrays will make it a lot faster to access. 