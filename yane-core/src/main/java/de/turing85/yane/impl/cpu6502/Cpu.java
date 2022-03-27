package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import java.util.*;
import lombok.*;

@AllArgsConstructor
public class Cpu implements de.turing85.yane.api.Cpu<Instruction> {
  private static final int RESET_READ_ADDRESS = 0xFFFC;
  private static final int INITIAL_STACK_POINTER_ADDRESS = 0xFD;
  private static final int CYCLES_FOR_RESET = 8;

  private final CpuBus bus;
  private final Clock clock;

  private final Register register;
  private int instructionPointer;
  private int cycles;

  public Cpu(CpuBus bus, Clock clock) {
    this(bus, clock, new Register(), (short) 0, 0);
    reset();
  }

  public final Set<Instruction> instructions() {
    return Instruction.INSTRUCTIONS;
  }

  @Override
  public void reset() {
    int low = bus.read(RESET_READ_ADDRESS);
    int high = bus.read(RESET_READ_ADDRESS + 1);
    register.programCounter((high << 8) | low);
    register.a(0);
    register.x(0);
    register.y(0);
    register.stackPointer(INITIAL_STACK_POINTER_ADDRESS);
    cycles = CYCLES_FOR_RESET;
  }

  @Override
  public boolean tick() {
    return false;
  }
}