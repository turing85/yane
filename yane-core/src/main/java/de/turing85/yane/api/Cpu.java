package de.turing85.yane.api;

import java.util.*;

public interface Cpu<I extends Instruction> {

  Set<I> instructions();

  /**
   * Resets the CPU in a know state.
   *
   * <p>In most circumstances, the reset state is hardwired in the CPU itself.
   */
  void reset();

  /**
   * Executes one CPU-cycle on the CPU.
   *
   * @return {@code true}, iff. the executed cycle competed a command.
   */
  boolean tick();
}