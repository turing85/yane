package de.turing85.yane.cpu;

import de.turing85.yane.Clock;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * The CPU, executing {@link Instruction}s, modifying its {@link Register} and reading and writing
 * from a {@link Bus}.
 */
public final class Cpu {
  /**
   * The two values from this and the successor (i.e. +1) are read when the CPU is set/initialized
   * to determine the initial value of the {@link Register#programCounter(int)}.
   */
  static final int RESET_READ_ADDRESS = 0xFFFC;

  /**
   * Number of cycles needed for a hardware reset.
   */
  private static final int CYCLES_FOR_RESET = 8;

  /**
   * The {@link Bus} that this CPU is connected to.
   */
  @Getter(AccessLevel.PACKAGE)
  private final Bus bus;

  /**
   * The {@link Register} that this CPU uses.
   */
  @Getter(AccessLevel.PACKAGE)
  private final Register register;

  /**
   * The number of cycles elapsed since the last {@link #reset()}.
   */
  @Getter(AccessLevel.PACKAGE)
  private int cycles;

  /**
   * <p>The instruction that is currently executed by the CPU.</p>
   *
   * <p>If the CPU is on its first cycle after a reset, or the last execution has finished on the
   * previous cycle, this field is {@code null}.</p>
   */
  private Instruction currentInstruction;

  /**
   * <p>The numbers of cycles left until the  {@link #currentInstruction} finishes.</p>
   *
   * <p>If, at the beginning of a cycle, this value is {@code 0}, the next instruction is read from
   * the {@link Bus#read(int)} at address {@link Register#programCounter()}.</p>
   */
  private int cyclesLeft;

  /**
   * State-restore Constructor (for future save-state feature).
   *
   * @param bus the {@link Bus} this CPU writes to and reads from
   * @param clock the {@link Clock} that determines the timing
   * @param register the {@link Register} of this CPU
   * @param cycles the {@link #cycles} executed since the last reset
   */
  public Cpu(Bus bus, Clock clock, Register register, int cycles) {
    this.bus = bus;
    clock.addListener(this::tick);
    this.register = register;
    this.cycles = cycles;
    this.cyclesLeft = 0;
    currentInstruction = null;
  }

  /**
   * Constructor.
   *
   * @param bus the {@link Bus} this CPU writes to and reads from
   * @param clock the {@link Clock} that determines the timing
   */
  public Cpu(Bus bus, Clock clock) {
    this(bus, clock, Register.of(), 0);
    reset();
  }

  /**
   * <p>Resets the CPU to a known default state.</p>
   *
   * <p>In particular:</p>
   *
   * <ul>
   *   <li>{@link Register#programCounter(int)} ({@link #register}) is set to {@code
   *       bus.read(RESET_READ_ADDRESS) | (bus.read(RESET_READ_ADDRESS + 1) << 8)}
   *   <li>{@link #cycles} is reset to {@link #CYCLES_FOR_RESET}
   *   <li>{@link #currentInstruction} is set to {@code null}
   *   <li>{@link #cyclesLeft} is set to {@code 0}
   * </ul>
   */
  public void reset() {
    int resetLow = bus.read(RESET_READ_ADDRESS);
    int resetHigh = bus.read(RESET_READ_ADDRESS + 1);
    register.reset(resetLow | (resetHigh << 8));
    cycles = CYCLES_FOR_RESET;
    currentInstruction = null;
    cyclesLeft = 0;
  }

  /**
   * Executes one instruction cycle.
   *
   * @return {@code true}, iff. an instruction was finished at the end of this cycle.
   */
  public boolean tick() {
    if (cyclesLeft == 0) {
      currentInstruction = fetchNextInstruction();
      cyclesLeft = currentInstruction.execute(register, bus);
    }
    --cyclesLeft;
    ++cycles;
    if (cyclesLeft == 0) {
      currentInstruction = null;
      return true;
    }
    return false;
  }

  /**
   * Fetches the next instruction from {@link #bus} ad address {@link Register#programCounter()} (
   * {@link #register}).
   *
   * @return the next instruction.
   */
  private Instruction fetchNextInstruction() {
    int opCode = bus.read(register.getAndIncrementProgramCounter());
    return Instruction.getByOpCode(opCode);
  }
}