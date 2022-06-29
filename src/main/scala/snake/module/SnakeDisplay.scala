package snake.module

import chisel3.*
import chisel3.util.{ShiftRegisters, is, switch}
import snake.bundle.SevenSeg

class SnakeDisplayIO extends Bundle {
  // 移动
  val move   = Input(Bool())
  // 移动模式
  //     moveMode(ledSeg): 高4位为led号 低4位为数码管段号seg
  //
  //     led号: [7][6][5][4][3][2][1][0]
  //     数码管段号:
  //         dp g f e d c b a
  //         7  6 5 4 3 2 1 0
  //
  //         === 0 ===       === 0 ===       === 0 ===       === 0 ===       === 0 ===       === 0 ===       === 0 ===       === 0 ===
  //         ||     ||       ||     ||       ||     ||       ||     ||       ||     ||       ||     ||       ||     ||       ||     ||
  //         5      1        5      1        5      1        5      1        5      1        5      1        5      1        5      1
  //         ||     ||       ||     ||       ||     ||       ||     ||       ||     ||       ||     ||       ||     ||       ||     ||
  //         === 6 ===       === 6 ===       === 6 ===       === 6 ===       === 6 ===       === 6 ===       === 6 ===       === 6 ===
  //         ||     ||       ||     ||       ||     ||       ||     ||       ||     ||       ||     ||       ||     ||       ||     ||
  //         4      2        4      2        4      2        4      2        4      2        4      2        4      2        4      2
  //         ||     ||       ||     ||       ||     ||       ||     ||       ||     ||       ||     ||       ||     ||       ||     ||
  //         === 3 ===  [7]  === 3 ===  [7]  === 3 ===  [7]  === 3 ===  [7]  === 3 ===  [7]  === 3 ===  [7]  === 3 ===  [7]  === 3 ===  [7]
  //
  //             7               6               5               4               3               2               1               0
  val ledSeg = Input(UInt(8.W))
  // 蛇身长度 3-7
  val length = Input(UInt(3.W))

  // 数码管
  val sevenSeg = Output(new SevenSeg)
}

class SnakeDisplay extends Module {
  val io = IO(new SnakeDisplayIO)

  // 使用移位寄存器缓存来增加蛇的长度
  val delay  = ShiftRegisters(io.ledSeg, 7, io.move)
  val snake3 = delay.take(3)
  val snake4 = delay.take(4)
  val snake5 = delay.take(5)
  val snake6 = delay.take(6)
  val snake7 = delay.take(7)

  // 寄存蛇身
  val led7   = RegInit(UInt(8.W), 0.U)
  val led6   = RegInit(UInt(8.W), 0.U)
  val led5   = RegInit(UInt(8.W), 0.U)
  val led4   = RegInit(UInt(8.W), 0.U)
  val led3   = RegInit(UInt(8.W), 0.U)
  val led2   = RegInit(UInt(8.W), 0.U)
  val led1   = RegInit(UInt(8.W), 0.U)
  val led0   = RegInit(UInt(8.W), 0.U)
  val ledSeq = Seq(led0, led1, led2, led3, led4, led5, led6, led7)

  private def cleanLedReg() = ledSeq.foreach { led => led := 0.U(8.W) }

  private def fillLedReg(ledSeg: UInt) = {
    val led = ledSeg(7, 4)
    val seg = (1.U << ledSeg(3, 0)).asUInt

    switch(led) {
      is(7.U) { led7 := led7 | seg }
      is(6.U) { led6 := led6 | seg }
      is(5.U) { led5 := led5 | seg }
      is(4.U) { led4 := led4 | seg }
      is(3.U) { led3 := led3 | seg }
      is(2.U) { led2 := led2 | seg }
      is(1.U) { led1 := led1 | seg }
      is(0.U) { led0 := led0 | seg }
    }
  }

  // 当移动时, 清空寄存器, 再按不同长度载入寄存器
  when(io.move) {
    cleanLedReg()

    switch(io.length) {
      is(3.U) { snake3.foreach(fillLedReg) }
      is(4.U) { snake4.foreach(fillLedReg) }
      is(5.U) { snake5.foreach(fillLedReg) }
      is(6.U) { snake6.foreach(fillLedReg) }
      is(7.U) { snake7.foreach(fillLedReg) }
    }
  }

  // 刷新数码管 800Hz
  val selector = RegInit(UInt(3.W), 0.U)
  val refresh  = Ticker.keepBeating(800)
  when(refresh) { selector := Mux(selector === 7.U, 0.U, selector + 1.U) }

  // 输出
  io.sevenSeg.an := ~(1.U << selector).asUInt
  when(selector === 0.U) { io.sevenSeg.common := ~led0 }
    .elsewhen(selector === 1.U) { io.sevenSeg.common := ~led1 }
    .elsewhen(selector === 2.U) { io.sevenSeg.common := ~led2 }
    .elsewhen(selector === 3.U) { io.sevenSeg.common := ~led3 }
    .elsewhen(selector === 4.U) { io.sevenSeg.common := ~led4 }
    .elsewhen(selector === 5.U) { io.sevenSeg.common := ~led5 }
    .elsewhen(selector === 6.U) { io.sevenSeg.common := ~led6 }
    .otherwise { io.sevenSeg.common := ~led7 }
}

object SnakeDisplay {
  def main(args: Array[String]): Unit = {
    println(getVerilogString(new SnakeDisplay))
  }
}
