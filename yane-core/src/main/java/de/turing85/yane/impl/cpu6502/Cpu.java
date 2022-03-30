package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.Clock;
import de.turing85.yane.api.CpuBus;
import java.util.Set;

public class Cpu implements de.turing85.yane.api.Cpu<Instruction> {
  private static final int RESET_READ_ADDRESS = 0xFFFC;
  private static final int CYCLES_FOR_RESET = 8;

  private final CpuBus bus;

  private final Register register;
  private int cycles;

  public Cpu(CpuBus bus, Clock clock, Register register, int cycles) {
    this.bus = bus;
    clock.addListener(this::tick);
    this.register = register;
    this.cycles = cycles;
  }

  public Cpu(CpuBus bus, Clock clock) {
    this(bus, clock, new Register(), 0);
    reset();
  }

  public final Set<Instruction> instructions() {
    return Instruction.INSTRUCTIONS;
  }

  @Override
  public void reset() {
    int resetLow = bus.read(RESET_READ_ADDRESS);
    int resetHigh = bus.read(RESET_READ_ADDRESS + 1);
    register.reset(resetLow | (resetHigh << 8));
    cycles = CYCLES_FOR_RESET;
  }

  @Override
  public boolean tick() {
    return false;
  }
}