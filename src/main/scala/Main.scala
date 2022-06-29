import chisel3.getVerilogString
import snake.Snake

object Main extends App {
  println(getVerilogString(new Snake))
}
