package de.turing85.yane.cpu;

import de.turing85.yane.*;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;

public final class Cpu {
  static final int RESET_READ_ADDRESS = 0xFFFC;
  private static final int CYCLES_FOR_RESET = 8;

  private final Bus bus;

  @Getter(AccessLevel.PACKAGE)
  private final Register register;

  @Getter(AccessLevel.PACKAGE)
  private int cycles;

  private int ticksLeft;
  private Instruction currentInstruction;

  public Cpu(Bus bus, Clock clock, Register register, int cycles) {
    this.bus = bus;
    clock.addListener(this::tick);
    this.register = register;
    this.cycles = cycles;
    this.ticksLeft = 0;
    currentInstruction = null;
  }

  public Cpu(Bus bus, Clock clock) {
    this(bus, clock, Register.of(), 0);
    reset();
  }

  public Set<Instruction> instructions() {
    return Instruction.INSTRUCTIONS;
  }

  public void reset() {
    int resetLow = bus.read(RESET_READ_ADDRESS);
    int resetHigh = bus.read(RESET_READ_ADDRESS + 1);
    register.reset(resetLow | (resetHigh << 8));
    cycles = CYCLES_FOR_RESET;
  }

  public boolean tick() {
    if (ticksLeft == 0) {
      currentInstruction = fetchNextInstruction();
      ticksLeft = currentInstruction.execute(register, bus);
    }
    --ticksLeft;
    ++cycles;
    return ticksLeft == 0;
  }

  private Instruction fetchNextInstruction() {
    int opCode = bus.read(register.getAndIncrementProgramCounter());
    return Instruction.getByOpCode(opCode);
  }
}