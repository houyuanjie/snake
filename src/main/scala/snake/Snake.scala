package snake

import chisel3.*
import snake.bundle.SevenSeg
import snake.module.{SnakeDisplay, Ticker}

class SnakeIO extends Bundle {
  // 方向键输入
  //     暂停显示时间
  val up     = Input(Bool())
  //     改变蛇身长度
  val down   = Input(Bool())
  //     改变移动速度
  val left   = Input(Bool())
  //     切换手动/自动控制移动模式
  val right  = Input(Bool())
  //     手动改变移动模式
  val center = Input(Bool())

  // 七段数码管输出
  val sevenSeg = Output(new SevenSeg)
}

class Snake extends Module {
  val io = IO(new SnakeIO)

  // 暂停移动蛇 移动移动蛇(寄存器写入信号)
  val pause      = RegInit(Bool(), false.B)
  val move       = RegInit(Bool(), false.B)
  // 自动切换模式
  val auto       = RegInit(Bool(), false.B)
  // 移动模式
  val mode0      = VecInit(
    "h70".U,
    "h60".U,
    "h50".U,
    "h40".U,
    "h30".U,
    "h20".U,
    "h10".U,
    "h00".U,
    "h01".U,
    "h06".U,
    "h16".U,
    "h26".U,
    "h36".U,
    "h46".U,
    "h56".U,
    "h66".U,
    "h76".U,
    "h74".U,
    "h73".U,
    "h63".U,
    "h53".U,
    "h43".U,
    "h33".U,
    "h23".U,
    "h13".U,
    "h03".U
  )
  val mode1      = VecInit(
    "h70".U,
    "h60".U,
    "h50".U,
    "h40".U,
    "h30".U,
    "h20".U,
    "h10".U,
    "h00".U
  )
  val mode2      = VecInit(
    "h70".U,
    "h60".U,
    "h50".U,
    "h40".U,
    "h30".U,
    "h20".U,
    "h10".U,
    "h00".U
  )
  val mode3      = VecInit(
    "h70".U,
    "h60".U,
    "h50".U,
    "h40".U,
    "h30".U,
    "h20".U,
    "h10".U,
    "h00".U
  )
  val mode4      = VecInit(
    "h70".U,
    "h60".U,
    "h50".U,
    "h40".U,
    "h30".U,
    "h20".U,
    "h10".U,
    "h00".U
  )
  val modeLength = VecInit(mode0.length.U, mode1.length.U, mode2.length.U, mode3.length.U, mode4.length.U)
  // 当前选中的移动模式
  val mode       = RegInit(UInt(3.W), 0.U)      // 5种移动模式 mode <- [0,1,2,3,4]
  val step       = RegInit(UInt(64.W), 0.U)     // 当前处于选中移动模式的第几步 index < mode.length
  val ledSeg     = RegInit(UInt(8.W), mode0(0)) // mode01234[mode][step]
  // 蛇身长度
  val length     = RegInit(UInt(3.W), 3.U)

  // 自动模式
  when(io.right) { auto := ~auto }

  // 控制蛇移动速度
  val speed0 = Module(new Ticker(10))
  val speed1 = Module(new Ticker(2))
  val speed2 = Module(new Ticker(1))
  speed0.io.en := ~pause
  speed1.io.en := ~pause
  speed2.io.en := ~pause
  val speedSelector = RegInit(UInt(2.W), 0.U)
  // 切换时钟
  when(io.left) { speedSelector := Mux(speedSelector === 2.U, 0.U, speedSelector + 1.U) }
  // 选择时钟
  when(speedSelector === 0.U) { move := speed0.io.tck }
    .elsewhen(speedSelector === 1.U) { move := speed2.io.tck }
    .otherwise { move := speed2.io.tck }

  // 蛇移动起来
  //     切换到下一个移动模式
  private def gotoNextMode() = { mode := Mux(mode === 4.U, 0.U, mode + 1.U) }

  //     移动
  when(move) {
    when(step === (modeLength(mode) - 1.U)) {
      // 移动到了当前模式的最后一步
      step := 0.U
      when(auto) { gotoNextMode() }
    }.otherwise {
      // 移动到当前模式的下一步
      step := step + 1.U
    }
  }

  //     改变移动模式
  when(io.center) {
    when(!auto) {
      step := 0.U
      gotoNextMode()
    }
  }

  // 根据 mode 和 step 更新 ledSeg
  when(mode === 0.U) { ledSeg := mode0(step) }
    .elsewhen(mode === 1.U) { ledSeg := mode1(step) }
    .elsewhen(mode === 2.U) { ledSeg := mode2(step) }
    .elsewhen(mode === 3.U) { ledSeg := mode3(step) }
    .otherwise { ledSeg := mode4(step) }

  // 改变蛇身长度
  when(io.down) { length := Mux(length === 7.U, 3.U, length + 1.U) }

  val display = Module(new SnakeDisplay)
  display.io.move   := move
  display.io.legSeg := ledSeg
  display.io.length := length

  // todo: 实现暂停显示时间 目前只能输出移动蛇
  io.sevenSeg := display.io.sevenSeg
}

object Snake {
  def main(args: Array[String]): Unit = {
    println(getVerilogString(new Snake))
  }
}
