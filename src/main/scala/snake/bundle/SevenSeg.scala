package snake.bundle

import chisel3.*

class SevenSeg extends Bundle {
  // [7][6][5][4][3][2][1][0]
  val an     = UInt(8.W)
  // [dp][g][f][e][d][c][b][a]
  val common = UInt(8.W)
}
