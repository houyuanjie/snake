package snake.module

import chisel3.*
import chisel3.util.log2Ceil
import snake.module.Ticker.FREQ

class TickerIO extends Bundle {
  val en  = Input(Bool())
  val tck = Output(Bool())
}

class Ticker(freq: Int) extends Module {
  val io = IO(new TickerIO)

  val counter     = RegInit(UInt(log2Ceil(FREQ / freq).W), 0.U)
  val counterNext = Mux(counter === (FREQ / freq - 1).U, 0.U, counter + 1.U)
  counter := Mux(io.en, counterNext, counter)

  io.tck := Mux(counter === (FREQ / freq - 1).U, true.B, false.B)
}

object Ticker {

  // 全局 100MHz 时钟
  val FREQ = 100000000

  // 持续节拍信号的构造器
  def keepBeating(freq: Int) = {
    val ticker = Module(new Ticker(freq))
    ticker.io.en := true.B

    ticker.io.tck
  }

}
