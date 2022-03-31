package de.turing85.yane.cpu;

import de.turing85.yane.*;
import lombok.*;

/**
 * <p>The register holds values used by the CPU. Those values do not have a bus address and are
 * thus
 * only accessible by the CPU.</p>
 *
 * <p>The register holds</p>
 * <ul>
 *   <li>an accumulator ({@link #a}): a byte value. Its initial value is {@code 0}.
 *   <li>an X offset ({@link #x}): a byte value. Its initial value is {@code 0}.
 *   <li>an Y offset ({@link #y}): a byte value. Its initial value is {@code 0}.
 *   <li>a status ({@link #status}: a byte value, representing 8 separate flags. These bits are -
 *   from the most significant to the least significant bit:
 *   <table border="1">
 *     <caption>CPU status byte description</caption>
 *     <tr> <th>Short form</th> <th>Long form</th> <th>Description</th> <th>Initial value</th></tr>
 *     <tr>
 *       <td>{@code N}</td>
 *       <td>Negative flag</td>
 *       <td>Set when the result of an operation is negative</td>
 *       <td>{@code 0}</td>
 *     </tr>
 *     <tr>
 *       <td>{@code V}</td>
 *       <td>Overflow flag</td>
 *       <td>Set when the operation overflew</td>
 *       <td>{@code 0}</td>
 *     </tr>
 *     <tr>
 *       <td>{@code U}</td>
 *       <td>Unused</td>
 *       <td>This flag is unused</td>
 *       <td>{@code 1}</td>
 *     </tr>
 *     <tr>
 *       <td>{@code B}</td>
 *       <td>Break</td>
 *       <td>
 *         Set when the {@link Command#BRK} and {@link Command#PHP} push the state to the stack.
 *         Within the CPU, the flag is never set.
 *       </td>
 *       <td>{@code 0}</td>
 *     </tr>
 *     <tr>
 *       <td>{@code D}</td>
 *       <td>Decimal mode enabled</td>
 *       <td>
 *         When activated, the expression {@code 0x09 + 0x01} would evaluate to {@code 0x10}.
 *         Currently, this mode is not implemented
 *       </td>
 *       <td>{@code 0}</td>
 *     </tr>
 *     <tr>
 *       <td>{@code I}</td>
 *       <td>disable IRQ</td>
 *       <td>When set, IRQs are not executed</td>
 *       <td>{@code 0}</td>
 *     </tr>
 *     <tr>
 *       <td>{@code Z}</td>
 *       <td>Zero flag</td>
 *       <td>Set when the result of an operation is zero</td>
 *       <td>{@code 1}</td>
 *     </tr>
 *     <tr>
 *       <td>{@code C}</td>
 *       <td>Carry bit</td>
 *       <td>Set when an operation carried a bit</td>
 *       <td>{@code 0}</td>
 *     </tr>
 *   </table>
 *   <li>
 *     a program counter ({@link #programCounter}): a 16-bit value, interpreted as bus address.
 *     The CPU reads the next instruction from the bus address represented by this value.  Its
 *     initial value is {@code 0x0000}. Its initial value is determined by reading the reset vector
 *     from the {@link CpuBus}: {@code bus.read(}{@link #RESET_VECTOR}{@code ) | ((bus.read(}
 *     {@link #RESET_VECTOR}{@code + 1) << 8)}
 *   <li>
 *     a stack pointer ({@link #stackPointer}): a byte value, interpreted as bus address. Since
 *     the 6502 assumes a 16-bit bus, a byte value can only represent a partial address. the stack
 *     pointer represents the lower 8 bits of an address,  the higher 8 bits are set to {@code
 *     0x01}. When a value is "stored on the stack", it is written to address {@code 0x01 << 8 |}
 *     {@link #stackPointer}, and the {@link #stackPointer} is decremented by {@code 1}. When a
 *     value is read from the stack, {@link #stackPointer} is increased by {@code 1}, and the value
 *     is read from address {@code 0x01 << 8 |}{@link #stackPointer} (after decrement). Its
 *     initial value is {@link #INITIAL_STACK_POINTER_VALUE}.
 * </ul>
 */
@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
@AllArgsConstructor
@EqualsAndHashCode
class Register {
  /**
   * This bit mask is used to bring values in a legal range for the {@link #stackPointer}.
   */
  static final int STACK_POINTER_MASK = 0xFF;

  /**
   * This bit mask is used to bring values in a legal range for the {@link #programCounter}.
   */
  static final int PROGRAM_COUNTER_MASK = 0xFFFF;

  /**
   * This bit maks is used to extract the negative flag from {@link #status}.
   */
  static final int NEGATIVE_MASK = 0x80;

  /**
   * This bit maks is used to extract the overflow flag from {@link #status}.
   */
  static final int OVERFLOW_MASK = 0x40;

  /**
   * This bit maks is used to extract the unused flag from {@link #status}.
   */
  static final int UNUSED_MASK = 0x20;

  /**
   * This bit maks is used to extract the break flag from {@link #status}.
   */
  static final int BREAK_MASK = 0x10;

  /**
   * This bit maks is used to extract the decimal mode flag from {@link #status}.
   */
  static final int DECIMAL_MASK = 0x08;

  /**
   * This bit maks is used to extract the disable IRQ flag from {@link #status}.
   */
  static final int DISABLE_IRQ_MASK = 0x04;

  /**
   * This bit maks is used to extract the zero flag from {@link #status}.
   */
  static final int ZERO_MASK = 0x02;

  /**
   * This bit maks is used to extract the carry flag from {@link #status}.
   */
  static final int CARRY_MASK = 0x01;

  /**
   * The initial value for {@link #stackPointer}.
   */
  static final int INITIAL_STACK_POINTER_VALUE = 0xFD;

  /**
   * The initial value for {@link #status}.
   */
  static final int INITIAL_STATUS_VALUE = 0x34;

  /**
   * <p>Reset vector.</p>
   *
   * <p>When a {@link CpuBus} is passed along to the constructor or the {@link #reset(int)} method,
   * {@link #programCounter} is initialized with the following value: {@code bus.read(RESET_VECTOR)
   * | ((bus.read(RESET_VECTOR + 1) << 8)}</p>
   */
  static final int RESET_VECTOR = 0xFFFC;

  /**
   * Register value A.
   */
  private int a;

  /**
   * Register value X.
   */
  private int x;

  /**
   * Register value Y.
   */
  private int y;

  /**
   * Stack pointer.
   */
  @Getter(AccessLevel.NONE)
  private int stackPointer;

  /**
   * Program counter.
   */
  @Getter(AccessLevel.NONE)
  private int programCounter;

  /**
   * Status.
   */
  private int status;

  /**
   * No-args constructor.
   */
  Register() {
    this(0, 0, 0, INITIAL_STACK_POINTER_VALUE, 0, INITIAL_STATUS_VALUE);
  }

  /**
   * <p>Constructor.</p>
   *
   * <p>{@link #programCounter} is initialized with the following value: {@code
   * bus.read(RESET_VECTOR) | ((bus.read(RESET_VECTOR + 1) << 8)}</p>
   *
   * @param bus
   *     the {@link CpuBus} to read the value for {@link #programCounter} from.
   */
  Register(CpuBus bus) {
    this(bus.read(RESET_VECTOR) | (bus.read(RESET_VECTOR + 1) << 8));
  }

  /**
   * All-args constructor.
   *
   * @param a
   *     initial value for {@link #a}
   * @param x
   *     initial value for {@link #x}
   * @param y
   *     initial value for {@link #y}
   * @param stackPointer
   *     initial value for {@link #stackPointer}
   * @param programCounter
   *     initial value for {@link #programCounter}
   * @param negativeFlag
   *     initial value for the negative flag
   * @param overflowFlag
   *     initial value for the overflow flag
   * @param breakFlag
   *     initial value for the break flag
   * @param decimalFlag
   *     initial value for the decimal mode flag
   * @param disableIrqFlag
   *     initial value for the disable IRQ flag
   * @param zeroFlag
   *     initial value for the zero flag
   * @param carryFlag
   *     initial value for the carry flag
   */
  Register(
      int a,
      int x,
      int y,
      int stackPointer,
      int programCounter,
      boolean negativeFlag,
      boolean overflowFlag,
      boolean breakFlag,
      boolean decimalFlag,
      boolean disableIrqFlag,
      boolean zeroFlag,
      boolean carryFlag) {
    this(a, x, y, stackPointer, programCounter, 0);
    initializeStatus(
        negativeFlag,
        overflowFlag,
        breakFlag,
        decimalFlag,
        disableIrqFlag,
        zeroFlag,
        carryFlag);
  }

  /**
   * Constructor.
   *
   * @param programCounter
   *     the initial value for {@link #programCounter}
   */
  Register(int programCounter) {
    this(
        0,
        0,
        0,
        INITIAL_STACK_POINTER_VALUE,
        programCounter,
        INITIAL_STATUS_VALUE);
  }

  /**
   * Initializes the status according to the given parameters.
   *
   * @param negativeFlag
   *     whether the negative flag should be set
   * @param overflowFlag
   *     whether the overflow flag should be set
   * @param breakFlag
   *     whether the break flag should be set
   * @param decimalFlag
   *     whether the decimal flag should be set
   * @param disableIrqFlag
   *     whether the disable IRQ flag should be set
   * @param zeroFlag
   *     whether the zero flag should be set
   * @param carryFlag
   *     whether the carry flag should be set
   */
  private void initializeStatus(
      boolean negativeFlag,
      boolean overflowFlag,
      boolean breakFlag,
      boolean decimalFlag,
      boolean disableIrqFlag,
      boolean zeroFlag,
      boolean carryFlag) {
    if (negativeFlag) {
      setNegativeFlag();
    }
    if (overflowFlag) {
      setOverflowFlag();
    }
    if (breakFlag) {
      setBreakFlag();
    }
    if (decimalFlag) {
      setDecimalModeFlag();
    }
    if (disableIrqFlag) {
      setDisableIrqFlag();
    }
    if (zeroFlag) {
      setZeroFlag();
    }
    if (carryFlag) {
      setCarryFlag();
    }
  }

  /**
   * <p>Resets the register.</p>
   *
   * {@link #programCounter} is set to the following value: {@code bus.read(RESET_VECTOR) |
   * ((bus.read(RESET_VECTOR + 1) << 8)}
   *
   * @param bus
   *     the {@link CpuBus} to read from
   *
   * @return self
   */
  Register reset(CpuBus bus) {
    return reset(bus.read(RESET_VECTOR) | (bus.read(RESET_VECTOR + 1) << 8));
  }

  /**
   * Resets the register.
   *
   * @param programCounterForReset
   *     the value for {@link #programCounter}
   *
   * @return self
   */
  Register reset(int programCounterForReset) {
    status(INITIAL_STATUS_VALUE);
    a(0);
    x(0);
    y(0);
    stackPointer(INITIAL_STACK_POINTER_VALUE);
    programCounter(programCounterForReset);
    return this;
  }

  final int programCounter() {
    return programCounter & PROGRAM_COUNTER_MASK;
  }

  final int getAndIncrementProgramCounter() {
    return programCounter++ & PROGRAM_COUNTER_MASK;
  }

  final Register incrementProgramCounter() {
    ++programCounter;
    return this;
  }

  final Register decrementProgramCounter() {
    --programCounter;
    return this;
  }

  final int stackPointer() {
    return stackPointer & STACK_POINTER_MASK;
  }

  final int getAndDecrementStackPointer() {
    return stackPointer-- & STACK_POINTER_MASK;
  }

  final Register decrementStackPointer() {
    --stackPointer;
    return this;
  }

  final int incrementAndGetStackPointer() {
    return ++stackPointer & STACK_POINTER_MASK;
  }

  final Register negativeFlag(boolean negativeFlag) {
    if (negativeFlag) {
      setNegativeFlag();
    } else {
      unsetNegativeFlag();
    }
    return this;
  }

  final Register setNegativeFlag() {
    status |= NEGATIVE_MASK;
    return this;
  }

  final Register unsetNegativeFlag() {
    status &= ~NEGATIVE_MASK;
    return this;
  }

  final boolean isNegativeFlagSet() {
    return (status & NEGATIVE_MASK) > 0;
  }

  final Register overflowFlag(boolean overflowFlag) {
    if (overflowFlag) {
      setOverflowFlag();
    } else {
      unsetOverflowFlag();
    }
    return this;
  }

  final Register setUnusedFlag() {
    status |= UNUSED_MASK;
    return this;
  }

  final Register unsetUnusedFlag() {
    status &= ~UNUSED_MASK;
    return this;
  }

  final boolean isUnusedFlagSet() {
    return (status & UNUSED_MASK) > 0;
  }

  final Register setOverflowFlag() {
    status |= OVERFLOW_MASK;
    return this;
  }

  final Register unsetOverflowFlag() {
    status &= ~OVERFLOW_MASK;
    return this;
  }

  final boolean isOverflowFlagSet() {
    return (status & OVERFLOW_MASK) > 0;
  }

  final Register breakFlag(boolean breakFlag) {
    if (breakFlag) {
      setBreakFlag();
    } else {
      unsetBreakFlag();
    }
    return this;
  }

  final Register setBreakFlag() {
    status |= BREAK_MASK;
    return this;
  }

  final Register unsetBreakFlag() {
    status &= ~BREAK_MASK;
    return this;
  }

  final boolean isBreakFlagSet() {
    return (status & BREAK_MASK) > 0;
  }

  final Register decimalModeFlag(boolean decimalModeFlag) {
    if (decimalModeFlag) {
      setDecimalModeFlag();
    } else {
      unsetDecimalModeFlag();
    }
    return this;
  }

  final Register setDecimalModeFlag() {
    status |= DECIMAL_MASK;
    return this;
  }

  final Register unsetDecimalModeFlag() {
    status &= ~DECIMAL_MASK;
    return this;
  }

  final boolean isDecimalModeFlagSet() {
    return (status & DECIMAL_MASK) > 0;
  }

  final Register setDisableIrqFlag(boolean disableIrqFlag) {
    if (disableIrqFlag) {
      setDisableIrqFlag();
    } else {
      unsetDisableIrqFlag();
    }
    return this;
  }

  final Register setDisableIrqFlag() {
    status |= DISABLE_IRQ_MASK;
    return this;
  }

  final Register unsetDisableIrqFlag() {
    status &= ~DISABLE_IRQ_MASK;
    return this;
  }

  final boolean isDisableIrqFlagSet() {
    return (status & DISABLE_IRQ_MASK) > 0;
  }

  final Register zeroFlag(boolean zeroFlag) {
    if (zeroFlag) {
      setZeroFlag();
    } else {
      unsetZeroFlag();
    }
    return this;
  }

  final Register setZeroFlag() {
    status |= ZERO_MASK;
    return this;
  }

  final Register unsetZeroFlag() {
    status &= ~ZERO_MASK;
    return this;
  }

  final boolean isZeroFlagSet() {
    return (status & ZERO_MASK) > 0;
  }

  final Register carryFlag(boolean carryFlag) {
    if (carryFlag) {
      setCarryFlag();
    } else {
      unsetCarryFlag();
    }
    return this;
  }

  final Register setCarryFlag() {
    status |= CARRY_MASK;
    return this;
  }

  final Register unsetCarryFlag() {
    status &= ~CARRY_MASK;
    return this;
  }

  final boolean isCarryFlagSet() {
    return (status & CARRY_MASK) > 0;
  }
}