package snake.module

import chisel3.*

class TimeDisplayIO extends Bundle {
  // 正在显示时间 也就是正在暂停 show := snake.pause
  val showing = Input(Bool())

  // 显示了足够长的时间 snake 将根据此信号结束 pause 状态
  val expired = Output(Bool())
}

class TimeDisplay extends Module {
  val io = IO(new TimeDisplayIO)

  val ticker   = Ticker.keepBeating(1)
  val hexHour1 = RegInit(UInt(4.W), 0.U)
  val hexHour0 = RegInit(UInt(4.W), 8.U)
  val hexMin1  = RegInit(UInt(4.W), 2.U)
  val hexMin0  = RegInit(UInt(4.W), 0.U)
  val hexSec1  = RegInit(UInt(4.W), 0.U)
  val hexSec0  = RegInit(UInt(4.W), 0.U)

  // todo: 实现时间显示

}
