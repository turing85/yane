package de.turing85.yane.cpu;

import lombok.*;

/**
 * <p>The register holds values used by the CPU. Those values do not have a bus address and are
 * thus only accessible by the CPU.</p>
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
 *     from the {@link Bus}: {@code bus.read(}{@link #RESET_VECTOR}{@code ) | ((bus.read(}
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
public interface Register {
  /**
   * This bit mask is used to bring values in a legal range for the {@link #stackPointer}.
   */
  int STACK_POINTER_MASK = 0xFF;

  /**
   * This bit mask is used to bring values in a legal range for the {@link #programCounter}.
   */
  int PROGRAM_COUNTER_MASK = 0xFFFF;

  /**
   * This bit maks is used to extract the negative flag from {@link #status}.
   */
  int NEGATIVE_MASK = 0x80;

  /**
   * This bit maks is used to extract the overflow flag from {@link #status}.
   */
  int OVERFLOW_MASK = 0x40;

  /**
   * This bit maks is used to extract the unused flag from {@link #status}.
   */
  int UNUSED_MASK = 0x20;

  /**
   * This bit maks is used to extract the break flag from {@link #status}.
   */
  int BREAK_MASK = 0x10;

  /**
   * This bit maks is used to extract the decimal mode flag from {@link #status}.
   */
  int DECIMAL_MASK = 0x08;

  /**
   * This bit maks is used to extract the disable IRQ flag from {@link #status}.
   */
  int DISABLE_IRQ_MASK = 0x04;

  /**
   * This bit maks is used to extract the zero flag from {@link #status}.
   */
  int ZERO_MASK = 0x02;

  /**
   * This bit maks is used to extract the carry flag from {@link #status}.
   */
  int CARRY_MASK = 0x01;

  /**
   * The initial value for {@link #stackPointer}.
   */
  int INITIAL_STACK_POINTER_VALUE = 0xFD;

  /**
   * The initial value for {@link #status}.
   */
  int INITIAL_STATUS_VALUE = 0x34;

  /**
   * <p>Reset vector.</p>
   *
   * <p>When a {@link Bus} is passed along to the constructor or the {@link #reset(int)} method,
   * {@link #programCounter} is initialized with the following value: {@code bus.read(RESET_VECTOR)
   * | ((bus.read(RESET_VECTOR + 1) << 8)}</p>
   */
  int RESET_VECTOR = 0xFFFC;

  /**
   * Factory method to construct a register with default initialized values.
   *
   * @return a register with default initialized values
   *
   * @see Impl#Impl()
   */
  static Register of() {
    return new Impl();
  }

  /**
   * Returns a register with the specified values.
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
   *
   * @return a register with the specified values
   *
   * @see Impl#Impl(int, int, int, int, int, boolean, boolean, boolean, boolean, boolean, boolean,
   *     boolean)
   */
  static Register of(
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
    return new Impl(
        a,
        x,
        y,
        stackPointer,
        programCounter,
        negativeFlag,
        overflowFlag,
        breakFlag,
        decimalFlag,
        disableIrqFlag,
        zeroFlag,
        carryFlag);
  }

  /**
   * Gets the value of the accumulator.
   *
   * @return the value of the accumulator
   */
  int a();

  /**
   * Sets the value of the accumulator.
   *
   * @param a
   *     the new value for the accumulator
   *
   * @return self, for method chaining
   */
  Register a(int a);

  /**
   * Gets the value of the X register.
   *
   * @return the value of the X register
   */
  int x();

  /**
   * Sets the value of the X register.
   *
   * @param x
   *     the new value for the X register
   *
   * @return self, for method chaining
   */
  Register x(int x);

  /**
   * Gets the value of the Y register.
   *
   * @return the value of the Y register
   */
  int y();

  /**
   * Sets the value of the Y register.
   *
   * @param y
   *     the new value for the Y register
   *
   * @return self, for method chaining
   */
  Register y(int y);

  /**
   * Gets the value of the stack pointer.
   *
   * @return the value of the stack pointer
   */
  int stackPointer();

  /**
   * Sets the value of the stack pointer.
   *
   * @param stackPointer
   *     the new value for the stack pointer
   *
   * @return self, for method chaining
   */
  Register stackPointer(int stackPointer);

  /**
   * Increments the value of the stack pointer by one, and returns it.
   *
   * @return the stack pointer value, after incrementing
   */
  int incrementAndGetStackPointer();

  /**
   * Decrements the value of the stack pointer by one, and returns it.
   *
   * @return the stack pointer value, before decrementing
   */
  int getAndDecrementStackPointer();

  /**
   * Gets the value of the program counter.
   *
   * @return the value of the program counter
   */
  int programCounter();

  /**
   * Sets the value of the program counter.
   *
   * @param programCounter
   *     the new value for the program counter
   *
   * @return self, for method chaining
   */
  Register programCounter(int programCounter);

  /**
   * Increments the value of the stack pointer by one, and returns it.
   *
   * @return the stack pointer value, after incrementing
   */
  int getAndIncrementProgramCounter();

  /**
   * Increments the value of the program counter.
   *
   * @return self, for method chaining
   */
  Register incrementProgramCounter();

  /**
   * Decrements the value of the program counter.
   *
   * @return self, for method chaining
   */
  Register decrementProgramCounter();

  /**
   * <p>Returns the status flag, as int.</p>
   *
   * <p>the upper 24 bits are unused. The lower 8 bits are set as follows:</p>
   *
   * <ul>
   *   <li>bit 7 is set to the {@code N} (negative, {@link #isNegativeFlagSet()}) flag </li>
   *   <li>bit 6 is set to the {@code V} (overflow, {@link #isOverflowFlagSet()}) flag </li>
   *   <li>bit 5 is set to the {@code U} (unused, {@link #isUnusedFlagSet()}) flag </li>
   *   <li>bit 4 is set to the {@code B} (break, {@link #isBreakFlagSet()}) flag </li>
   *   <li>bit 3 is set to the {@code D} (decimal mode, {@link #isDecimalModeFlagSet()}) flag </li>
   *   <li>bit 2 is set to the {@code I} (disable IRQ, {@link #isDisableIrqFlagSet()}) flag </li>
   *   <li>bit 1 is set to the {@code Z} (zero, {@link #isZeroFlagSet()}) flag </li>
   *   <li>bit 0 is set to the {@code C} (carry, {@link #isCarryFlagSet()}) flag </li>
   * </ul>
   *
   * @return the status, as described above
   */
  int status();

  /**
   * <p>Sets the status bits.</p>
   *
   * <p>the upper 24 bits are unused. The lower 8 bits are interpreted follows:</p>
   *
   * <ul>
   *   <li>bit 7 is set to the {@code N} (negative, {@link #negativeFlag(boolean)} ()}) flag </li>
   *   <li>bit 6 is set to the {@code V} (overflow, {@link #overflowFlag(boolean)}) flag </li>
   *   <li>bit 5 is set to the {@code U} (unused, {@link #unusedFlag(boolean)}) flag </li>
   *   <li>bit 4 is set to the {@code B} (break, {@link #breakFlag(boolean)}) flag </li>
   *   <li>
   *     bit 3 is set to the {@code D} (decimal mode, {@link #decimalModeFlag(boolean)}) flag
   *   </li>
   *   <li>
   *     bit 2 is set to the {@code I} (disable IRQ, {@link #setDisableIrqFlag(boolean)} flag
   *   </li>
   *   <li>bit 1 is set to the {@code Z} (zero, {@link #zeroFlag(boolean)}) flag </li>
   *   <li>bit 0 is set to the {@code C} (carry, {@link #carryFlag(boolean)}) flag </li>
   * </ul>
   *
   * @param status
   *     the status, as describe above
   *
   * @return self, for method chaining
   */
  Register status(int status);

  /**
   * <p>Resets the register.</p>
   *
   * {@link #programCounter()} is set to the following value: {@code bus.read(RESET_VECTOR) |
   * ((bus.read(RESET_VECTOR + 1) << 8)}
   *
   * @param bus
   *     the {@link Bus} to read from
   *
   * @return self, for method chaining
   */
  default Register reset(Bus bus) {
    return reset(bus.read(RESET_VECTOR) | (bus.read(RESET_VECTOR + 1) << 8));
  }

  /**
   * Resets the register.
   *
   * @param programCounterForReset
   *     the value for {@link #programCounter}
   *
   * @return self, for method chaining
   */
  Register reset(int programCounterForReset);

  /**
   * Sets the value of the {@code N} (negative) flag (7th bit of {@link #status()}).
   *
   * @param negativeFlag
   *     the new value for the {@code N} flag
   *
   * @return self, for method chaining
   */
  default Register negativeFlag(boolean negativeFlag) {
    if (negativeFlag) {
      setNegativeFlag();
    } else {
      unsetNegativeFlag();
    }
    return this;
  }

  /**
   * Sets the value of the {@code N} (negative) flag (7th bit of {@link #status()}) to {@code
   * true}.
   *
   * @return self, for method chaining
   */
  default Register setNegativeFlag() {
    return status(status() | NEGATIVE_MASK);
  }

  /**
   * Sets the value of the {@code N} (negative) flag (7th bit of {@link #status()}) to {@code
   * false}.
   *
   * @return self, for method chaining
   */
  default Register unsetNegativeFlag() {
    return status(status() & ~NEGATIVE_MASK);
  }

  /**
   * Returns the value of the {@code N} (negative) flag (7th bit of {@link #status()}).
   *
   * @return the value of the {@code N} flag
   */
  default boolean isNegativeFlagSet() {
    return (status() & NEGATIVE_MASK) > 0;
  }

  /**
   * Sets the value of the {@code V} (overflow) flag (7th bit of {@link #status()}).
   *
   * @param overflowFlag
   *     the new value for the {@code V} flag
   *
   * @return self, for method chaining
   */
  default Register overflowFlag(boolean overflowFlag) {
    if (overflowFlag) {
      setOverflowFlag();
    } else {
      unsetOverflowFlag();
    }
    return this;
  }

  /**
   * Sets the value of the {@code V} (negative) flag (6th bit of {@link #status()}) to {@code
   * true}.
   *
   * @return self, for method chaining
   */
  default Register setOverflowFlag() {
    return status(status() | OVERFLOW_MASK);
  }

  /**
   * Sets the value of the {@code V} (overflow) flag (6th bit of {@link #status()}) to {@code
   * false}.
   *
   * @return self, for method chaining
   */
  default Register unsetOverflowFlag() {
    return status(status() & ~OVERFLOW_MASK);
  }

  /**
   * Returns the value of the {@code V} (overflow) flag (6th bit of {@link #status()}).
   *
   * @return the value of the {@code V} flag
   */
  default boolean isOverflowFlagSet() {
    return (status() & OVERFLOW_MASK) > 0;
  }

  /**
   * Sets the value of the {@code U} (unused) flag (5th bit of {@link #status()}).
   *
   * @param unusedFlag
   *     the new value for the {@code U} flag
   *
   * @return self, for method chaining
   */
  default Register unusedFlag(boolean unusedFlag) {
    if (unusedFlag) {
      setUnusedFlag();
    } else {
      unsetUnusedFlag();
    }
    return this;
  }

  /**
   * Sets the value of the {@code U} (unused) flag (5th bit of {@link #status()}) to {@code true}.
   *
   * @return self, for method chaining
   */
  default Register setUnusedFlag() {
    return status(status() | UNUSED_MASK);
  }

  /**
   * Sets the value of the {@code U} (unused) flag (5th bit of {@link #status()}) to {@code false}.
   *
   * @return self, for method chaining
   */
  default Register unsetUnusedFlag() {
    return status(status() & ~UNUSED_MASK);
  }

  /**
   * Returns the value of the {@code U} (unused) flag (5th bit of {@link #status()}).
   *
   * @return the value of the {@code U} flag
   */
  default boolean isUnusedFlagSet() {
    return (status() & UNUSED_MASK) > 0;
  }

  /**
   * Sets the value of the {@code B} (break) flag (4th bit of {@link #status()}).
   *
   * @param breakFlag
   *     the new value for the {@code B} flag
   *
   * @return self, for method chaining
   */
  default Register breakFlag(boolean breakFlag) {
    if (breakFlag) {
      setBreakFlag();
    } else {
      unsetBreakFlag();
    }
    return this;
  }

  /**
   * Sets the value of the {@code B} (break) flag (4th bit of {@link #status()}) to {@code true}.
   *
   * @return self, for method chaining
   */
  default Register setBreakFlag() {
    return status(status() | BREAK_MASK);
  }

  /**
   * Sets the value of the {@code B} (break) flag (4th bit of {@link #status()}) to {@code false}.
   *
   * @return self, for method chaining
   */
  default Register unsetBreakFlag() {
    return status(status() & ~BREAK_MASK);
  }

  /**
   * Returns the value of the {@code B} (break) flag (4th bit of {@link #status()}).
   *
   * @return the value of the {@code B} flag
   */
  default boolean isBreakFlagSet() {
    return (status() & BREAK_MASK) > 0;
  }

  /**
   * Sets the value of the {@code D} (decimal mode) flag (3rd bit of {@link #status()}).
   *
   * @param decimalModeFlag
   *     the new value for the {@code D} flag
   *
   * @return self, for method chaining
   */
  default Register decimalModeFlag(boolean decimalModeFlag) {
    if (decimalModeFlag) {
      setDecimalModeFlag();
    } else {
      unsetDecimalModeFlag();
    }
    return this;
  }

  /**
   * Sets the value of the {@code D} (decimal mode) flag (3rd bit of {@link #status()}) to {@code
   * true}.
   *
   * @return self, for method chaining
   */
  default Register setDecimalModeFlag() {
    return status(status() | DECIMAL_MASK);
  }

  /**
   * Sets the value of the {@code D} (decimal mode) flag (3rd bit of {@link #status()}) to {@code
   * false}.
   *
   * @return self, for method chaining
   */
  default Register unsetDecimalModeFlag() {
    return status(status() & ~DECIMAL_MASK);
  }

  /**
   * Returns the value of the {@code D} (decimal mode) flag (3rd bit of {@link #status()}).
   *
   * @return the value of the {@code D} flag
   */
  default boolean isDecimalModeFlagSet() {
    return (status() & DECIMAL_MASK) > 0;
  }

  /**
   * Sets the value of the {@code I} (disable IRQ) flag (2nd bit of {@link #status()}).
   *
   * @param disableIrqFlag
   *     the new value for the {@code B} flag
   *
   * @return self, for method chaining
   */
  default Register setDisableIrqFlag(boolean disableIrqFlag) {
    if (disableIrqFlag) {
      setDisableIrqFlag();
    } else {
      unsetDisableIrqFlag();
    }
    return this;
  }

  /**
   * Sets the value of the {@code I} (disable IRQ) flag (2nd bit of {@link #status()}) to {@code
   * true}.
   *
   * @return self, for method chaining
   */
  default Register setDisableIrqFlag() {
    return status(status() | DISABLE_IRQ_MASK);
  }

  /**
   * Sets the value of the {@code I} (disable IRQ) flag (2nd bit of {@link #status()}) to {@code
   * false}.
   *
   * @return self, for method chaining
   */
  default Register unsetDisableIrqFlag() {
    return status(status() & ~DISABLE_IRQ_MASK);
  }

  /**
   * Returns the value of the {@code I} (disable IRQ) flag (2nd bit of {@link #status()}).
   *
   * @return the value of the {@code I} flag
   */
  default boolean isDisableIrqFlagSet() {
    return (status() & DISABLE_IRQ_MASK) > 0;
  }

  /**
   * Sets the value of the {@code Z} (zero) flag (1st bit of {@link #status()}).
   *
   * @param zeroFlag
   *     the new value for the {@code Z} flag
   *
   * @return self, for method chaining
   */
  default Register zeroFlag(boolean zeroFlag) {
    if (zeroFlag) {
      setZeroFlag();
    } else {
      unsetZeroFlag();
    }
    return this;
  }

  /**
   * Sets the value of the {@code Z} (zero) flag (1st bit of {@link #status()}) to {@code true}.
   *
   * @return self, for method chaining
   */
  default Register setZeroFlag() {
    return status(status() | ZERO_MASK);
  }

  /**
   * Sets the value of the {@code Z} (zero) flag (1st bit of {@link #status()}) to {@code false}.
   *
   * @return self, for method chaining
   */
  default Register unsetZeroFlag() {
    return status(status() & ~ZERO_MASK);
  }

  /**
   * Returns the value of the {@code Z} (zero) flag (1st bit of {@link #status()}).
   *
   * @return the value of the {@code Z} flag
   */
  default boolean isZeroFlagSet() {
    return (status() & ZERO_MASK) > 0;
  }

  /**
   * Sets the value of the {@code C} (carry) flag (0th bit of {@link #status()}).
   *
   * @param carryFlag
   *     the new value for the {@code C} flag
   *
   * @return self, for method chaining
   */
  default Register carryFlag(boolean carryFlag) {
    if (carryFlag) {
      setCarryFlag();
    } else {
      unsetCarryFlag();
    }
    return this;
  }

  /**
   * Sets the value of the {@code C} (carry) flag (0th bit of {@link #status()}) to {@code true}.
   *
   * @return self, for method chaining
   */
  default Register setCarryFlag() {
    return status(status() | CARRY_MASK);
  }

  /**
   * Sets the value of the {@code C} (carry) flag (0th bit of {@link #status()}) to {@code false}.
   *
   * @return self, for method chaining
   */
  default Register unsetCarryFlag() {
    return status(status() & ~CARRY_MASK);
  }

  /**
   * Returns the value of the {@code C} (carry) flag (4th bit of {@link #status()}).
   *
   * @return the value of the {@code C} flag
   */
  default boolean isCarryFlagSet() {
    return (status() & CARRY_MASK) > 0;
  }

  /**
   * Internal implementation of {@link Register}.
   */
  @Getter
  @Setter
  @AllArgsConstructor
  @EqualsAndHashCode
  final class Impl implements Register {

    /**
     * Accumulator.
     */
    private int a;

    /**
     * X register.
     */
    private int x;

    /**
     * Y register.
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
     *
     * @see Register#of()
     */
    private Impl() {
      this(0, 0, 0, INITIAL_STACK_POINTER_VALUE, 0, INITIAL_STATUS_VALUE);
    }

    /**
     * <p>Constructor.</p>
     *
     * <p>{@link #programCounter} is initialized with the following value: {@code
     * bus.read(RESET_VECTOR) | ((bus.read(RESET_VECTOR + 1) << 8)}</p>
     *
     * @param bus
     *     the {@link Bus} to read the value for {@link #programCounter} from.
     */
    private Impl(Bus bus) {
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
     *
     * @see Register#of(int, int, int, int, int, boolean, boolean, boolean, boolean, boolean,
     *     boolean, boolean)
     */
    private Impl(
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
    private Impl(int programCounter) {
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
     * {@inheritDoc}
     */
    @Override
    public Impl reset(Bus bus) {
      return reset(bus.read(RESET_VECTOR) | (bus.read(RESET_VECTOR + 1) << 8));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Impl reset(int programCounterForReset) {
      status(INITIAL_STATUS_VALUE);
      a(0);
      x(0);
      y(0);
      stackPointer(INITIAL_STACK_POINTER_VALUE);
      programCounter(programCounterForReset);
      return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int programCounter() {
      return programCounter & PROGRAM_COUNTER_MASK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAndIncrementProgramCounter() {
      return programCounter++ & PROGRAM_COUNTER_MASK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Impl incrementProgramCounter() {
      ++programCounter;
      return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Impl decrementProgramCounter() {
      --programCounter;
      return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int stackPointer() {
      return stackPointer & STACK_POINTER_MASK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAndDecrementStackPointer() {
      return stackPointer-- & STACK_POINTER_MASK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int incrementAndGetStackPointer() {
      return ++stackPointer & STACK_POINTER_MASK;
    }
  }
}
