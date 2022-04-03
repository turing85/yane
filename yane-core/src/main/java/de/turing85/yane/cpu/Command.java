package de.turing85.yane.cpu;

import static de.turing85.yane.cpu.AddressingMode.*;
import static de.turing85.yane.cpu.Bus.*;
import static de.turing85.yane.cpu.Register.*;

import java.util.function.*;
import lombok.*;
import lombok.experimental.Delegate;

/**
 * <p>The commands supported by the 6502 processor.</p>
 *
 * <p>In its core, a command operates on a single {@code value} and/or the {@code address}
 * from which the {@code value} was read. For example, the command {@link #ASL} (Arithmetic shift
 * left) shifts the value to the left by 1 position and writes it back to its original address. If
 * the value was read from the accumulator (represented by the addressing mode {@link
 * AddressingMode#ACCUMULATOR}), it will be written to {@link Register#a(int)} (which has no
 * explicit address on the bus). Some commands only need the {@link AddressingResult#value}, some
 * need the {@link AddressingResult#address}, some need both, and some need none.</p>
 *
 * <p>As described in {@link CommandFunction}, each {@link Command} takes a {@link
 * AddressingResult} as input and returns a {@link CommandResult}.</p>
 *
 * <p>For not, all instructions are executed "at once", so this emulation is not
 * cycle-accurate.</p>
 *
 * <p>In the following, if we talk about {@code value} and {@code address}, we refer to
 * {@link AddressingResult#value} and {@link AddressingResult#address}, unless otherwise noted.</p>
 *
 * <p>The {@link Register} flags are set as expected, unless otherwise noted. The flags are set
 * after the core semantic of the command have been executed. For example, when {@code #ADC} (Add
 * with Carry) is executed, the whole addition (including the evaluation of the {{@code C} ({@link
 * Register#isCarryFlagSet()}} and increasing the final result by {@code 1} if it is set) is
 * executed, before the new flag-values are determined.</p>
 */
@Value
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class Command implements CommandFunction {
  /**
   * The program counter is set to this value when a forced break is encountered.
   */
  private static final int FORCE_BREAK_PROGRAM_COUNTER = 0x0000FFFE;

  /**
   * The actual implementation of the accessing mode.
   */
  @Delegate
  CommandFunction function;

  /**
   * The mnemonic of the command.
   */
  String mnemonic;

  /**
   * <p>Add with Carry command.</p>
   *
   * <p>The {@code value} is added to {@link Register#a()}. If the {@code C}
   * ({@link Register#isCarryFlagSet()}) is set, the final value of {@link Register#a()} is
   * increased by {@code 1}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>yes</td> </tr>
   * </table>
   *
   * @see #SBC
   */
  static final Command ADC = new Command(
      addressingResult -> {
        final Register updatedRegister = addressingResult.register();
        final int a = updatedRegister.a();
        final int value = addressingResult.value();
        final int rawResult = a + value + (updatedRegister.isCarryFlagSet() ? 1 : 0);
        final int result = rawResult & VALUE_MASK;
        return new CommandResult(
            updatedRegister
                .a(result)
                .negativeFlag(isNegative(rawResult))
                .zeroFlag(isZero(result))
                .carryFlag(hasCarried(rawResult))
                .overflowFlag(hasOverflown(a, value, rawResult)),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "ADC");

  /**
   * <p>Logic And command.</p>
   *
   * <p>The {@code value} "and"-ed {@link Register#a()} and written to {@link Register#a(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #EOR
   * @see #ORA
   */
  static final Command AND = new Command(
      addressingResult -> {
        final Register updatedRegister = addressingResult.register();
        final int a = updatedRegister.a();
        final int value = addressingResult.value();
        final int result = a & value;
        return new CommandResult(
            updatedRegister
                .a(result)
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "AND");

  /**
   * <p>Arithmetic Shift left command.</p>
   *
   * <p>The {@code value} is logically shifted by 1 position to the left and written back.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>yes</td> </tr>
   * </table>
   *
   * @see #LSR
   */
  static final Command ASL = new Command(
      addressingResult -> {
        final int value = addressingResult.value();
        final int rawResult = value << 1;
        final int result = rawResult & VALUE_MASK;
        final int address = addressingResult.address();
        final Bus bus = addressingResult.bus();
        Register updatedRegister = storeValueDependingOnAddress(
            address,
            result,
            addressingResult.register(),
            bus);
        return new CommandResult(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(isZero(result))
                .carryFlag(hasCarried(rawResult)),
            bus,
            addressingResult.additionalCyclesNeeded());
      },
      "ASL");

  /**
   * <p>Branch on Carry Clear command.</p>
   *
   * <p>Sets {@link Register#programCounter()} to {@code address} if the {@code C} flag
   * ({@link Register#isCarryFlagSet()}) is not set.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #BCS
   * @see #BEQ
   * @see #BMI
   * @see #BNE
   * @see #BPL
   * @see #BVC
   * @see #BVS
   * @see AddressingMode#RELATIVE
   */
  static final Command BCC = new Command(
      addressingResult ->
          branchIf(!addressingResult.register().isCarryFlagSet(), addressingResult),
      "BCC");

  /**
   * <p>Branch on Carry Set command.</p>
   *
   * <p>Sets {@link Register#programCounter()} to {@code address} if the {@code C} flag
   * ({@link Register#isCarryFlagSet()}) is set.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #BCC
   * @see #BEQ
   * @see #BMI
   * @see #BNE
   * @see #BPL
   * @see #BVC
   * @see #BVS
   * @see AddressingMode#RELATIVE
   */
  static final Command BCS = new Command(
      addressingResult ->
          branchIf(addressingResult.register().isCarryFlagSet(), addressingResult),
      "BCS");

  /**
   * <p>Branch on Result Zero command.</p>
   *
   * <p>Sets {@link Register#programCounter()} to {@code address} if the {@code Z} flag
   * ({@link Register#isZeroFlagSet()}) is set.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #BCC
   * @see #BCS
   * @see #BMI
   * @see #BNE
   * @see #BPL
   * @see #BVC
   * @see #BVS
   * @see AddressingMode#RELATIVE
   */
  static final Command BEQ = new Command(
      addressingResult ->
          branchIf(addressingResult.register().isZeroFlagSet(), addressingResult),
      "BEQ");

  /**
   * <p>Test Bit in Memory with Accumulator command.</p>
   *
   * <p>This command sets the flags-values to the following:</p>
   *
   * <ul>
   *   <li> Flag {@code N} ({@link Register#isNegativeFlagSet()}) is set to bit 7 of {@code value}
   *   <li> Flag {@code V} ({@link Register#isOverflowFlagSet()}) is set to bit 6 of {@code value}
   *   <li> Flag {@code Z} ({@link Register#isZeroFlagSet()}) is set if {@code value &}
   *        {@link Register#a()} is {@code 0}.
   * </ul>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command BIT = new Command(
      addressingResult -> {
        final int value = addressingResult.value();
        return new CommandResult(
            addressingResult.register()
                .negativeFlag(isNegative(value))
                .overflowFlag((value & OVERFLOW_MASK) > 0)
                .zeroFlag((value & addressingResult.register().a()) == 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "BIT");

  /**
   * <p>Branch on Result Minus command.</p>
   *
   * <p>Sets {@link Register#programCounter()} to {@code address} if the {@code N} flag
   * ({@link Register#isNegativeFlagSet()}) is set.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #BCC
   * @see #BCS
   * @see #BEQ
   * @see #BNE
   * @see #BPL
   * @see #BVC
   * @see #BVS
   * @see AddressingMode#RELATIVE
   */
  static final Command BMI = new Command(
      addressingResult ->
          branchIf(addressingResult.register().isNegativeFlagSet(), addressingResult),
      "BMI");

  /**
   * <p>Branch on Result not Equal command.</p>
   *
   * <p>Sets {@link Register#programCounter()} to {@code address} if the {@code Z} flag
   * ({@link Register#isZeroFlagSet()}) is not set.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #BCC
   * @see #BCS
   * @see #BEQ
   * @see #BMI
   * @see #BPL
   * @see #BVC
   * @see #BVS
   * @see AddressingMode#RELATIVE
   */
  static final Command BNE = new Command(
      addressingResult ->
          branchIf(!addressingResult.register().isZeroFlagSet(), addressingResult),
      "BNE");

  /**
   * <p>Branch on Result Plus command.</p>
   *
   * <p>Sets {@link Register#programCounter()} to {@code address} if the {@code N} flag
   * ({@link Register#isNegativeFlagSet()}) is not set.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #BCC
   * @see #BCS
   * @see #BEQ
   * @see #BMI
   * @see #BNE
   * @see #BVC
   * @see #BVS
   * @see AddressingMode#RELATIVE
   */
  static final Command BPL = new Command(
      addressingResult ->
          branchIf(!addressingResult.register().isNegativeFlagSet(), addressingResult),
      "BPL");

  /**
   * <p>Break command.</p>
   *
   * <p>This command is used to push the current {@link Register} state onto the stack and move the
   * {@link Register#programCounter()} to another bus address.</p>
   *
   * <p>In detail, the command executes the following steps in order:</p>
   * <ul>
   *   <li> writes the current program counter to the stack (first the higher 8 bits, then lower
   *        8 bits)
   *   <li> Sets the {@link Register#isBreakFlagSet()} flag
   *   <li> pushes the {@link Register#status} to the stack
   *   <li> Unsets the {@link Register#isBreakFlagSet()} flag
   *   <li> Sets {@link Register#programCounter} to {@code (bus.read(}{@link
   *        #FORCE_BREAK_PROGRAM_COUNTER}{@code ) << 8) | bus.read(}{@link
   *        #FORCE_BREAK_PROGRAM_COUNTER}{@code )}
   * </ul>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>yes</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #RTI
   */
  static final Command BRK = new Command(
      addressingResult -> {
        final Bus bus = addressingResult.bus();
        final Register register = addressingResult.register()
            .setDisableIrqFlag()
            .setBreakFlag()
            .incrementProgramCounter();
        pushProgramCounterToStack(register, bus);
        bus.writeToStack(register.getAndDecrementStackPointer(), register.status());
        return new CommandResult(
            register
                .unsetBreakFlag()
                .programCounter(bus.readAddressFrom(RESET_VECTOR)),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "BRK");

  /**
   * <p>Branch on Overflow Clear command.</p>
   *
   * <p>Sets {@link Register#programCounter()} to {@code address} if the {@code V} flag
   * ({@link Register#isOverflowFlagSet()}) is not set.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #BCC
   * @see #BCS
   * @see #BEQ
   * @see #BMI
   * @see #BNE
   * @see #BPL
   * @see #BVS
   * @see AddressingMode#RELATIVE
   */
  static final Command BVC = new Command(
      addressingResult ->
          branchIf(!addressingResult.register().isOverflowFlagSet(), addressingResult),
      "BVC");

  /**
   * <p>Branch on Overflow Set command.</p>
   *
   * <p>Sets {@link Register#programCounter()} to {@code address} if the {@code V} flag
   * ({@link Register#isOverflowFlagSet()}) is set.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #BCC
   * @see #BCS
   * @see #BEQ
   * @see #BMI
   * @see #BNE
   * @see #BPL
   * @see #BVC
   * @see AddressingMode#RELATIVE
   */
  static final Command BVS = new Command(
      addressingResult ->
          branchIf(addressingResult.register().isOverflowFlagSet(), addressingResult),
      "BVS");

  /**
   * <p>Clear Carry flag command.</p>
   *
   * <p>Clears the {@code C} flag ({@link Register#isCarryFlagSet()}).</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>yes</td> </tr>
   * </table>
   */
  static final Command CLC = new Command(
      addressingResult -> new CommandResult(
          addressingResult.register().unsetCarryFlag(),
          addressingResult.bus(),
          0),
      "CLC");

  /**
   * <p>Clear Decimal mode flag command.</p>
   *
   * <p>Clears the {@code D} flag ({@link Register#isDecimalModeFlagSet()}).</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>yes</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command CLD = new Command(
      addressingResult -> new CommandResult(
          addressingResult.register().unsetDecimalModeFlag(),
          addressingResult.bus(),
          0),
      "CLD");

  /**
   * <p>Clear Disable IRQ flag command.</p>
   *
   * <p>Clears the {@code I} flag ({@link Register#isDisableIrqFlagSet()}).</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>yes</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command CLI = new Command(
      addressingResult ->
          new CommandResult(
              addressingResult.register().unsetDisableIrqFlag(),
              addressingResult.bus(),
              0),
      "CLI");

  /**
   * <p>Clear Overflow flag command.</p>
   *
   * <p>Clears the {@code V} flag ({@link Register#isOverflowFlagSet()}).</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command CLV = new Command(
      addressingResult ->
          new CommandResult(
              addressingResult.register().unsetOverflowFlag(),
              addressingResult.bus(),
              0),
      "CLV");

  /**
   * <p>Compare with Accumulator ({@link Register#a()}) command.</p>
   *
   * <p>Compares {@link AddressingResult#value} with {@link Register#a()}. Sets</p>
   *
   * <ul>
   *   <li> the {@code N} flag ({@link Register#isNegativeFlagSet()}) if
   *        {@link AddressingResult#value}{@code  > }{@link Register#a()}
   *   <li> the {@code Z} flag ({@link Register#isZeroFlagSet()}) if
   *        {@link AddressingResult#value}{@code  == }{@link Register#a()}
   *   <li> the {@code C} flag ({@link Register#isZeroFlagSet()}) if
   *        {@link AddressingResult#value}{@code  <= }{@link Register#a()}
   * </ul>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>yes</td> </tr>
   * </table>
   */
  static final Command CMP = new Command(
      addressingResult -> {
        final int a = addressingResult.register().a();
        final int value = addressingResult.value();
        return new CommandResult(
            addressingResult.register()
                .negativeFlag(value > a)
                .zeroFlag(value == a)
                .carryFlag(a >= value),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "CMP");

  /**
   * <p>Compare with X register ({@link Register#x()}) command.</p>
   *
   * <p>Compares {@link AddressingResult#value} with {@link Register#x()}. Sets</p>
   *
   * <ul>
   *   <li> the {@code N} flag ({@link Register#isNegativeFlagSet()}) if
   *        {@link AddressingResult#value}{@code  > }{@link Register#x()}
   *   <li> the {@code Z} flag ({@link Register#isZeroFlagSet()}) if
   *        {@link AddressingResult#value}{@code  == }{@link Register#x()}
   *   <li> the {@code C} flag ({@link Register#isZeroFlagSet()}) if
   *        {@link AddressingResult#value}{@code  <= }{@link Register#x()}
   * </ul>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>yes</td> </tr>
   * </table>
   */
  static final Command CPX = new Command(
      addressingResult -> {
        final int x = addressingResult.register().x();
        final int value = addressingResult.value();
        return new CommandResult(
            addressingResult.register()
                .negativeFlag(value > x)
                .zeroFlag(value == x)
                .carryFlag(x >= value),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "CPX");

  /**
   * <p>Compare with Y register ({@link Register#y()}) command.</p>
   *
   * <p>Compares {@link AddressingResult#value} with {@link Register#y()}. Sets</p>
   *
   * <ul>
   *   <li> the {@code N} flag ({@link Register#isNegativeFlagSet()}) if
   *        {@link AddressingResult#value}{@code  > }{@link Register#y()}
   *   <li> the {@code Z} flag ({@link Register#isZeroFlagSet()}) if
   *        {@link AddressingResult#value}{@code  == }{@link Register#y()}
   *   <li> the {@code C} flag ({@link Register#isZeroFlagSet()}) if
   *        {@link AddressingResult#value}{@code  <= }{@link Register#y()}
   * </ul>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>yes</td> </tr>
   * </table>
   */
  static final Command CPY = new Command(
      addressingResult -> {
        final int y = addressingResult.register().y();
        final int value = addressingResult.value();
        return new CommandResult(
            addressingResult.register()
                .negativeFlag(value > y)
                .zeroFlag(value == y)
                .carryFlag(y >= value),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "CPY");

  /**
   * <p>Decrement {@link AddressingResult#value} Command.</p>
   *
   * <p>Decrements {@link AddressingResult#value} by 1, and writes it back to
   * {@link AddressingResult#address}.</p>
   *
   * <p>If {@link AddressingMode#ACCUMULATOR} was used, the result is written back to
   * {@link Register#a(int)} instead.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command DEC = new Command(
      addressingResult -> {
        final int valueDecremented = (addressingResult.value() - 1) & VALUE_MASK;
        final Bus bus = addressingResult.bus();
        bus.write(addressingResult.address(), valueDecremented);
        return new CommandResult(
            addressingResult.register()
                .negativeFlag(isNegative(valueDecremented))
                .zeroFlag(valueDecremented == 0),
            bus,
            addressingResult.additionalCyclesNeeded());
      },
      "DEC");

  /**
   * <p>Decrement {@link Register#x()} Command.</p>
   *
   * <p>Decrements {@link Register#x()} by 1, and writes it back to {@link Register#x(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command DEX = new Command(
      addressingResult -> {
        int newValue = (addressingResult.register().x() - 1) & VALUE_MASK;
        return new CommandResult(
            addressingResult.register().x(newValue)
                .negativeFlag(isNegative(newValue))
                .zeroFlag(newValue == 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "DEX");

  /**
   * <p>Decrement {@link Register#y()} Command.</p>
   *
   * <p>Decrements {@link Register#y()} by 1, and writes it back to {@link Register#y(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command DEY = new Command(
      addressingResult -> {
        int newValue = (addressingResult.register().y() - 1) & VALUE_MASK;
        return new CommandResult(
            addressingResult.register().y(newValue)
                .negativeFlag(isNegative(newValue))
                .zeroFlag(newValue == 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "DEY");

  /**
   * <p>Exclusive-Or Command.</p>
   *
   * <p>"Exclusive-Or"s {@link AddressingResult#value} with {@link Register#a()} and writes the
   * result back to {@link Register#a(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #AND
   * @see #ORA
   */
  static final Command EOR = new Command(
      addressingResult -> {
        final Register register = addressingResult.register();
        final int newA = register.a() ^ addressingResult.value();
        return new CommandResult(
            register
                .a(newA)
                .negativeFlag(isNegative(newA))
                .zeroFlag(newA == 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "EOR");

  /**
   * <p>Increment {@link AddressingResult#value} Command.</p>
   *
   * <p>Increments {@link AddressingResult#value} by 1, and writes it back to
   * {@link AddressingResult#address}.</p>
   *
   * <p>If {@link AddressingMode#ACCUMULATOR} was used, the result is written back to
   * {@link Register#a(int)} instead.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command INC = new Command(
      addressingResult -> {
        final int newValue = (addressingResult.value() + 1) & VALUE_MASK;
        final Bus bus = addressingResult.bus();
        bus.write(addressingResult.address(), newValue);
        return new CommandResult(
            addressingResult.register()
                .negativeFlag(isNegative(newValue))
                .zeroFlag(newValue == 0),
            bus,
            addressingResult.additionalCyclesNeeded());
      },
      "INC");

  /**
   * <p>Increment {@link Register#x()} Command.</p>
   *
   * <p>Increments {@link Register#x()} by 1, and writes it back to {@link Register#x(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command INX = new Command(
      addressingResult -> {
        int newValue = (addressingResult.register().x() + 1) & VALUE_MASK;
        return new CommandResult(
            addressingResult.register().x(newValue)
                .negativeFlag(isNegative(newValue))
                .zeroFlag(newValue == 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "INX");

  /**
   * <p>Increment {@link Register#y()} Command.</p>
   *
   * <p>Increments {@link Register#y()} by 1, and writes it back to {@link Register#y(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command INY = new Command(
      addressingResult -> {
        int newValue = (addressingResult.register().y() + 1) & VALUE_MASK;
        return new CommandResult(
            addressingResult.register().y(newValue)
                .negativeFlag(isNegative(newValue))
                .zeroFlag(newValue == 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "INY");

  /**
   * <p>Jump Command.</p>
   *
   * <p>Sets {@link Register#programCounter()} to {@link AddressingResult#address}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command JMP = new Command(
      addressingResult -> new CommandResult(
          addressingResult.register().programCounter(addressingResult.address()),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "JMP");

  /**
   * <p>Jump and Save Return Address Command.</p>
   *
   * <p>Pushes {@link Register#programCounter()}{@code + 2} to the stack (higher 8 bits first,
   * lower 8 bits second). Then writes {@link Register#programCounter()} to {@code bus(} {@link
   * Register#programCounter()}{@code  + 1)} (lower 8 bits) and {@code bus(} {@link
   * Register#programCounter()}{@code  + 2)}(higher 8 bits). Finally, sets {@link
   * Register#programCounter()} to {@link AddressingResult#address}</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #RTS
   */
  static final Command JSR = new Command(
      addressingResult -> {
        final Register updatedRegister = addressingResult.register().decrementProgramCounter();
        pushProgramCounterToStack(updatedRegister, addressingResult.bus());
        return new CommandResult(
            updatedRegister.programCounter(addressingResult.address()),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "JSR");

  /**
   * <p>Load value into Accumulator Command.</p>
   *
   * <p>Sets {@link Register#a(int)} to {@link AddressingResult#value}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #STA
   */
  static final Command LDA = new Command(
      addressingResult -> new CommandResult(
          addressingResult.register().a(addressingResult.value())
              .negativeFlag(isNegative(addressingResult.value()))
              .zeroFlag(isZero(addressingResult.value())),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "LDA");

  /**
   * <p>Load value into X register Command.</p>
   *
   * <p>Sets {@link Register#x(int)} to {@link AddressingResult#value}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #STX
   */
  static final Command LDX = new Command(
      addressingResult -> new CommandResult(
          addressingResult.register().x(addressingResult.value())
              .negativeFlag(isNegative(addressingResult.value()))
              .zeroFlag(isZero(addressingResult.value())),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "LDX");

  /**
   * <p>Load value into Y Command.</p>
   *
   * <p>Sets {@link Register#y(int)} to {@link AddressingResult#value}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #STY
   */
  static final Command LDY = new Command(
      addressingResult -> new CommandResult(
          addressingResult.register().y(addressingResult.value())
              .negativeFlag(isNegative(addressingResult.value()))
              .zeroFlag(isZero(addressingResult.value())),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "LDY");

  /**
   * <p>Logic Shift Right command.</p>
   *
   * <p>Shifts {@link AddressingResult#value} one position to the right and writes the result back
   * to {@link AddressingResult#address}.</p>
   *
   * <p>If {@link AddressingMode#ACCUMULATOR} was used, the result is written back to
   * {@link Register#a(int)} instead.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr>
   *     <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>
   *     <td>yes (always {@code 0})</td>
   *   </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>yes</td> </tr>
   * </table>
   *
   * @see #ASL
   */
  static final Command LSR = new Command(
      addressingResult -> {
        final int value = addressingResult.value();
        final int result = (value >> 1) & VALUE_MASK;
        final int address = addressingResult.address();
        final Register updatedRegister = storeValueDependingOnAddress(
            address,
            result,
            addressingResult.register(),
            addressingResult.bus());
        return new CommandResult(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0)
                .carryFlag((value & 0x01) > 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "LSR");

  /**
   * <p>No-Operation command.</p>
   *
   * <p>It literally does nothing.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command NOP = new Command(
      addressingResult -> new CommandResult(addressingResult.register(), addressingResult.bus(), 0),
      "NOP");

  /**
   * <p>Or with Accumulator command.</p>
   *
   * <p>"Or"s {@link AddressingResult#value} with {@link Register#a()} and writes the result back
   * to {@link Register#a(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #AND
   * @see #EOR
   */
  static final Command ORA = new Command(
      addressingResult -> {
        Register updatedRegister = addressingResult.register();
        final int value = addressingResult.value();
        final int result = value | updatedRegister.a();
        return new CommandResult(
            updatedRegister
                .a(result)
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "ORA");

  /**
   * <p>Push Accumulator to Stack command.</p>
   *
   * <p>Pushes {@link Register#a()} to the stack.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #PLA
   */
  static final Command PHA = new Command(
      addressingResult -> new CommandResult(
          pushToStack(
              addressingResult.register(),
              addressingResult.register().a(),
              addressingResult.bus()),
          addressingResult.bus(),
          0),
      "PHA");

  /**
   * <p>Push Status to Stack command.</p>
   *
   * <p>Pushes {@link Register#status()} to the stack.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #PLP
   */
  static final Command PHP = new Command(
      addressingResult ->
          new CommandResult(
              pushStatusToStack(addressingResult.register(), addressingResult.bus()),
              addressingResult.bus(),
              0),
      "PHP");

  /**
   * <p>Pull Accumulator from Stack command.</p>
   *
   * <p>Pulls {@link Register#a(int)} from the stack.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #PHA
   */
  static final Command PLA = new Command(
      addressingResult -> {
        final int newA = pullFromStack(addressingResult.register(), addressingResult.bus());
        return new CommandResult(
            addressingResult.register()
                .a(newA)
                .negativeFlag(isNegative(newA))
                .zeroFlag(isZero(newA)),
            addressingResult.bus(),
            0);
      },
      "PLA");

  /**
   * <p>Pull Status from Stack command.</p>
   *
   * <p>Pulls {@link Register#status()} to the stack. The flags {@code B} ({@link
   * Register#isBreakFlagSet()}) and {@code U} ({@link Register#isUnusedFlagSet()}) are ignored when
   * {@link Register#status()} is pulled.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>from stack</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>from stack</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>from stack</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>from stack</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>from stack</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>from stack</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>from stack</td> </tr>
   * </table>
   *
   * @see #PHP
   */
  static final Command PLP = new Command(
      addressingResult ->
          new CommandResult(
              addressingResult.register()
                  .status(pullFromStack(addressingResult.register(), addressingResult.bus()))
                  .setUnusedFlag(),
              addressingResult.bus(),
              0),
      "PLP");

  /**
   * <p>Rotate Left command.</p>
   *
   * <p>Rotates {@link AddressingResult#value} one position to the left and writes it back to
   * {@link AddressingResult#address}.</p>
   *
   * <p>If {@link AddressingMode#ACCUMULATOR} was used, the result is written back to
   * {@link Register#a(int)} instead.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>yes</td> </tr>
   * </table>
   *
   * @see #ROR
   */
  static final Command ROL = new Command(
      addressingResult -> {
        final int value = addressingResult.value();
        final int rawResult = (value << 1) | ((value & 0x80) >> 7);
        final int result = rawResult & VALUE_MASK;
        final int address = addressingResult.address();
        final Register updatedRegister = storeValueDependingOnAddress(
            address,
            result,
            addressingResult.register(),
            addressingResult.bus());
        return new CommandResult(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0)
                .carryFlag(hasCarried(rawResult)),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "ROL");

  /**
   * <p>Rotate Right command.</p>
   *
   * <p>Rotates {@link AddressingResult#value} one position to the right and writes it back to
   * {@link AddressingResult#address}.</p>
   *
   * <p>If {@link AddressingMode#ACCUMULATOR} was used, the result is written back to
   * {@link Register#a(int)} instead.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>yes</td> </tr>
   * </table>
   *
   * @see #ROL
   */
  static final Command ROR = new Command(
      addressingResult -> {
        final int value = addressingResult.value();
        final int rawResult = (value >> 1) | ((value & 0x01) << 7);
        final int result = rawResult & VALUE_MASK;
        final int address = addressingResult.address();
        final Register updatedRegister = storeValueDependingOnAddress(
            address,
            result,
            addressingResult.register(),
            addressingResult.bus());
        return new CommandResult(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0)
                .carryFlag((value & 0x01) > 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "ROR");

  /**
   * <p>Return from Interrupt command.</p>
   *
   * <p>This command is used to pull the current {@link Register} state from the stack and move the
   * {@link Register#programCounter()} to another bus address.</p>
   *
   * <p>In detail, the command executes the following steps in order:</p>
   *
   * <ul>
   *   <li> Pulls {@link Register#programCounter()} form the stack (lowest 8 bit first, highest 8
   *        bit second),
   *   <li> Pulls {@link Register#status()} from stack}
   * </ul>
   *
   * <p>The {@code B}- ({@link Register#isBreakFlagSet()}) and {@code U}- ({@link
   * Register#isUnusedFlagSet()}) flags are ignored when {@link Register#status()} is pulled.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>from stack</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>from stack</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>from stack</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>from stack</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>from stack</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>from stack</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>from stack</td> </tr>
   * </table>
   *
   * @see #BRK
   */
  static final Command RTI = new Command(
      addressingResult -> {
        final Bus bus = addressingResult.bus();
        return new CommandResult(
            pullProgramCounterFromStack(pullStatusFromStack(addressingResult.register(), bus), bus)
                .setUnusedFlag()
                .unsetBreakFlag(),
            bus,
            0);
      },
      "RTI");

  /**
   * <p>Return from Subroutine command.</p>
   *
   * <p>Pulls {@link Register#programCounter()} from the stack and increments
   * {@link Register#programCounter()} by 1.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #JSR
   */
  static final Command RTS = new Command(
      addressingResult ->
          new CommandResult(
              pullProgramCounterFromStack(addressingResult.register(), addressingResult.bus())
                  .incrementProgramCounter(),
              addressingResult.bus(),
              0),
      "RTS");

  /**
   * <p>Subtract with Borrow command.</p>
   *
   * <p>Subtracts {@link AddressingResult#value} from {@link Register#a()} and writes the result
   * back to {@link Register#a(int)}.</p>
   *
   * <p>There is no explicit borrow bit on the 6502. Instead, the negated {@code C} flag ({@link
   * Register#isCarryFlagSet()}) is used, i.e. if the carry bit is set, the borrow is unset and
   * vice-versa. Thus, if the {@code C} flag ({@link Register#isCarryFlagSet()}) is not set, the
   * final result is decremented by {@code 1}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>yes</td> </tr>
   * </table>
   *
   * @see #ADC
   */
  static final Command SBC = new Command(
      addressingResult -> {
        final Register register = addressingResult.register();
        final int a = register.a();
        final int value = addressingResult.value() ^ 0x00FF;
        final int rawResult = a + value + (register.isCarryFlagSet() ? 1 : 0);
        final int result = rawResult & VALUE_MASK;
        return new CommandResult(
            register
                .a(result)
                .negativeFlag(isNegative(result))
                .overflowFlag(hasOverflown(a, value, rawResult))
                .zeroFlag(isZero(result))
                .carryFlag(hasCarried(rawResult)),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "SBC");

  /**
   * <p>Set Carry flag command.</p>
   *
   * <p>Sets the {@code C} flag ({@link Register#isCarryFlagSet()}).</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>yes</td> </tr>
   * </table>
   */
  static final Command SEC = new Command(
      addressingResult ->
          new CommandResult(addressingResult.register().setCarryFlag(), addressingResult.bus(), 0),
      "SEC");

  /**
   * <p>Set Decimal mode flag command.</p>
   *
   * <p>Sets the {@code D} flag ({@link Register#isDecimalModeFlagSet()}).</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>yes</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command SED = new Command(
      addressingResult -> new CommandResult(
          addressingResult.register().setDecimalModeFlag(),
          addressingResult.bus(),
          0),
      "SED");

  /**
   * <p>Set Disable IRQ flag command.</p>
   *
   * <p>Sets the {@code I} flag ({@link Register#isDisableIrqFlagSet()} ()}).</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>yes</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   */
  static final Command SEI = new Command(
      addressingResult -> new CommandResult(
          addressingResult.register().setDisableIrqFlag(),
          addressingResult.bus(),
          0),
      "SEI");

  /**
   * <p>Store Accumulator command.</p>
   *
   * <p>Writes the value of {@link Register#a()} to {@code address}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #LDA
   */
  static final Command STA = new Command(
      addressingResult -> {
        final Bus bus = addressingResult.bus();
        final Register register = addressingResult.register();
        bus.write(addressingResult.address(), register.a());
        return new CommandResult(register, bus, addressingResult.additionalCyclesNeeded());
      },
      "STA");

  /**
   * <p>Store X register command.</p>
   *
   * <p>Writes the value of {@link Register#x()} to {@code address}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #LDX
   */
  static final Command STX = new Command(
      addressingResult -> {
        final Bus bus = addressingResult.bus();
        final Register register = addressingResult.register();
        bus.write(addressingResult.address(), register.x());
        return new CommandResult(register, bus, addressingResult.additionalCyclesNeeded());
      },
      "STX");

  /**
   * <p>Store Y register command.</p>
   *
   * <p>Writes the value of {@link Register#y()} to {@code address}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #LDY
   */
  static final Command STY = new Command(
      addressingResult -> {
        final Bus bus = addressingResult.bus();
        final Register register = addressingResult.register();
        bus.write(addressingResult.address(), register.y());
        return new CommandResult(register, bus, addressingResult.additionalCyclesNeeded());
      },
      "STY");

  /**
   * <p>Transfer Accumulator to X register.</p>
   *
   * <p>Writes the value of {@link Register#a()} to {@link Register#x(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #TXA
   */
  static final Command TAX = new Command(
      addressingResult -> new CommandResult(
          transfer(addressingResult.register()::a, addressingResult.register()::x),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "TAX");

  /**
   * <p>Transfer Accumulator to Y register.</p>
   *
   * <p>Writes the value of {@link Register#a()} to {@link Register#y(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #TYA
   */
  static final Command TAY = new Command(
      addressingResult -> new CommandResult(
          transfer(addressingResult.register()::a, addressingResult.register()::y),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "TAY");

  /**
   * <p>Transfer stack pointer to X register.</p>
   *
   * <p>Transfers {@link Register#stackPointer()} to {@link Register#x(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #TXS
   */
  static final Command TSX = new Command(
      addressingResult ->
          new CommandResult(
              transfer(addressingResult.register()::stackPointer, addressingResult.register()::x),
              addressingResult.bus(),
              0),
      "TSX");

  /**
   * <p>Transfer X register to Accumulator.</p>
   *
   * <p>Transfers {@link Register#x()} to {@link Register#a(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #TAX
   */
  static final Command TXA = new Command(
      addressingResult -> new CommandResult(
          transfer(addressingResult.register()::x, addressingResult.register()::a),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "TXA");

  /**
   * <p>Transfer X register to stack pointer.</p>
   *
   * <p>Transfers {@link Register#x()} to {@link Register#stackPointer(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #TSX
   */
  static final Command TXS = new Command(
      addressingResult ->
          new CommandResult(
              transfer(addressingResult.register()::x, addressingResult.register()::stackPointer),
              addressingResult.bus(),
              0),
      "TXS");

  /**
   * <p>Transfer Y register to Accumulator.</p>
   *
   * <p>Transfers {@link Register#y()} to {@link Register#a(int)}.</p>
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>yes</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>yes</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see #TAY
   */
  static final Command TYA = new Command(
      addressingResult -> new CommandResult(
          transfer(addressingResult.register()::y, addressingResult.register()::a),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "TYA");

  /**
   * This command is used to represent unknown instructions.
   *
   * <table border="1">
   *   <caption>Flag change summary</caption>
   *   <tr> <th>Flag</th> <th>set by this command</th> </tr>
   *   <tr> <td>{@code N} ({@link Register#isNegativeFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code V} ({@link Register#isOverflowFlagSet()}</td>    <td>no</td> </tr>
   *   <tr> <td>{@code B} ({@link Register#isBreakFlagSet()}</td>       <td>no</td> </tr>
   *   <tr> <td>{@code D} ({@link Register#isDecimalModeFlagSet()}</td> <td>no</td> </tr>
   *   <tr> <td>{@code I} ({@link Register#isDisableIrqFlagSet()}</td>  <td>no</td> </tr>
   *   <tr> <td>{@code Z} ({@link Register#isZeroFlagSet()}</td>        <td>no</td> </tr>
   *   <tr> <td>{@code C} ({@link Register#isCarryFlagSet()}</td>       <td>no</td> </tr>
   * </table>
   *
   * @see AddressingMode#UNKNOWN
   */
  static final Command UNKNOWN = new Command(
      addressingResult -> new CommandResult(addressingResult.register(), addressingResult.bus(), 0),
      "???");

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return mnemonic();
  }

  /**
   * Checks whether {@code value} is negative in the yte domain.
   *
   * @param value
   *     the value to check
   *
   * @return {@code true} iff. {@code value & 0x80 > 0}
   */
  private static boolean isNegative(int value) {
    return (value & NEGATIVE_MASK) > 0;
  }

  /**
   * Checks whether {@code value} is zero in the byte domain.
   *
   * @param value
   *     the value to check
   *
   * @return {@code true} iff. {@code (value & VALUE_MASK) == 0}
   */
  private static boolean isZero(int value) {
    return (value & VALUE_MASK) == 0;
  }

  /**
   * Checks whether {@code value} has bits set in the higher 8 bits.
   *
   * @param value
   *     the value to check
   *
   * @return {@code true} iff. {@code value & 0xFF00 > 0}
   */
  private static boolean hasCarried(int value) {
    return (value & 0xFF00) > 0;
  }

  /**
   * Checks whether {@code lhs + rhs (= result)} has overflown.
   *
   * @param lhs
   *     left-handed side
   * @param rhs
   *     right-handed side
   * @param result
   *     the result in the 8-bit domain
   *
   * @return {@code true} iff. {@code lhs} and {@code rhs} have the same sign, but {@code result}
   *     has a different sign (in the byte domain)
   */
  private static boolean hasOverflown(int lhs, int rhs, int result) {
    return bytesHaveSameSign(lhs, rhs) && bytesHaveDifferentSign(lhs, result);
  }

  /**
   * Checks whether {@code lhs} and {@code rhs} have the same sign in the byte domain.
   *
   * @param lhs
   *     left-handed side
   * @param rhs
   *     right-handed side
   *
   * @return {@code true} iff. {@code (lhs & 0x80) == (rhs & 0x80)}
   */
  private static boolean bytesHaveSameSign(int lhs, int rhs) {
    return !bytesHaveDifferentSign(lhs, rhs);
  }

  /**
   * Checks whether {@code lhs} and {@code rhs} have different sign in the byte domain.
   *
   * @param lhs
   *     left-handed side
   * @param rhs
   *     right-handed side
   *
   * @return {@code true} iff. {@code (lhs & 0x80) != (rhs & 0x80)}
   */
  private static boolean bytesHaveDifferentSign(int lhs, int rhs) {
    return ((lhs ^ rhs) & 0x80) > 0;
  }

  /**
   * Helper-function for branch commands.
   *
   * @param condition
   *     branch-condition. The branching is only executed when {@code condition} is {@code true}
   * @param addressingResult
   *     the {@link AddressingResult}, holding the {@link Register} and {@link Bus}
   *
   * @return the {@link CommandResult}, holding the {@link Register}, {@link Bus} and the number of
   *     additional cycles needed (including the additional cycles needed by the {@link
   *     AddressingMode}).
   */
  private static CommandResult branchIf(boolean condition, AddressingResult addressingResult) {
    Register updatedRegister = addressingResult.register();
    int additionalCyclesNeeded = addressingResult.additionalCyclesNeeded();
    if (condition) {
      ++additionalCyclesNeeded;
      final int newProgramCounter = addressingResult.address();
      final int programCounter = updatedRegister.programCounter();
      updatedRegister = updatedRegister.programCounter(newProgramCounter);
      if (addressesAreOnDifferentPages(programCounter, newProgramCounter)) {
        ++additionalCyclesNeeded;
      }
    }
    return new CommandResult(updatedRegister, addressingResult.bus(), additionalCyclesNeeded);
  }

  /**
   * Checks whether two addresses are on different memory pages, i.e. whether the 2nd byte differs.
   *
   * @param lhs
   *     left-handed side
   * @param rhs
   *     right-handed side
   *
   * @return {@code true} iff. {@code (lhs & 0xFF00) != (rhs & 0xFF00)}
   */
  private static boolean addressesAreOnDifferentPages(int lhs, int rhs) {
    return (lhs & 0xFF00) != (rhs & 0xFF00);
  }

  /**
   * <p>Helper-method to push the program counter (16-bit value) to the stack.</p>
   *
   * <p>The program counter is written in little-endianness, i.e. the higher 8 bits are written
   * first, the lower 8 bits are written second.</p>
   *
   * @param register
   *     the {@link Register}, holding the {@link Register#programCounter()} to push and the {@link
   *     Register#stackPointer()}
   * @param bus
   *     the {@link Bus} to write to
   */
  private static void pushProgramCounterToStack(Register register, Bus bus) {
    final int programCounterHigh = register.programCounter() >> 8;
    final int programCounterLow = register.programCounter() & VALUE_MASK;
    bus.writeToStack(register.getAndDecrementStackPointer(), programCounterHigh);
    bus.writeToStack(register.getAndDecrementStackPointer(), programCounterLow);
  }

  /**
   * <p>Helper-method to pull the program counter (16-bit value) from the stack.</p>
   *
   * <p>The program counter is is read in little-endianness, i.e. the lower 8 bits are read
   * first, the higher 8 bits are read second.</p>
   *
   * @param register
   *     the {@link Register} to store the read {@link Register#programCounter()} in and holding the
   *     {@link Register#stackPointer()}
   * @param bus
   *     the {@link Bus} to read from
   *
   * @return the {code register}, for method chaining
   */
  private static Register pullProgramCounterFromStack(Register register, Bus bus) {
    return register
        .programCounter(bus.readAddressFromStack(register.incrementAndGetStackPointer()))
        .incrementStackPointer();
  }

  /**
   * <p>Helper-method to push the status to the stack.</p>
   *
   * @param register
   *     the {@link Register}, holding the {@link Register#stackPointer()} to push and the {@link
   *     Register#stackPointer()}
   * @param bus
   *     the {@link Bus} to write to
   *
   * @return the {code register} parameter, for method chaining
   */
  private static Register pushStatusToStack(Register register, Bus bus) {
    final Register updatedRegister = register.setUnusedFlag();
    bus.writeToStack(register.getAndDecrementStackPointer(), register.status());
    return updatedRegister;
  }

  /**
   * <p>Helper-method to pull the stack from the stack.</p>
   *
   * @param register
   *     the {@link Register} to store the read {@link Register#stackPointer()} in and holding the
   *     {@link Register#stackPointer()}
   * @param bus
   *     the {@link Bus} to read from
   *
   * @return the {code register} parameter, for method chaining
   */
  private static Register pullStatusFromStack(Register register, Bus bus) {
    return register
        .status(pullFromStack(register, bus))
        .unsetUnusedFlag()
        .unsetBreakFlag();
  }

  /**
   * <p>Helper-method to push a value to the stack.</p>
   *
   * @param register
   *     the {@link Register}, holding the {@link Register#stackPointer()}
   * @param value
   *     the value to push
   * @param bus
   *     the {@link Bus} to write to
   */
  private static Register pushToStack(Register register, int value, Bus bus) {
    bus.writeToStack(register.getAndDecrementStackPointer(), value);
    return register;
  }

  /**
   * <p>Helper-method to pull a value from the stack.</p>
   *
   * @param register
   *     the {@link Register} holding the {@link Register#stackPointer()}
   * @param bus
   *     the {@link Bus} to read from
   *
   * @return the value pulled from the stack
   */
  static int pullFromStack(Register register, Bus bus) {
    return bus.readFromStack(register.incrementAndGetStackPointer());
  }

  /**
   * <p>Stores a {@code value}, depending on the {@code address}.</p>
   *
   * <p>If {@code address} is {@link AddressingMode#IMPLIED_LOADED_ADDRESS}, then {@code value}
   * is written to {@link Register#a(int)}. Otherwise, the {@code value} is written to the {@link
   * Bus} at address {@code address}.</p>
   *
   * @param address
   *     address to write to
   * @param value
   *     value to write
   * @param register
   *     {@link Register} holding the {@link Register#a()}
   * @param bus
   *     the {@link Bus} to write to
   *
   * @return the {code register} parameter, for method chaining
   */
  private static Register storeValueDependingOnAddress(
      int address,
      int value,
      Register register,
      Bus bus) {
    if (address == IMPLIED_LOADED_ADDRESS) {
      return register.a(value);
    } else {
      bus.write(address, value);
      return register;
    }
  }

  /**
   * Helper-function to transfer one value to a register/accumulator.
   *
   * @param transferSource
   *     Getter of a {@link Register}
   * @param transferTarget
   *     Setter of a {@link Register}
   *
   * @return the {code register} parameter, for method chaining
   */
  private static Register transfer(
      IntSupplier transferSource,
      IntFunction<Register> transferTarget) {
    final int value = transferSource.getAsInt();
    return transferTarget.apply(value)
        .negativeFlag(isNegative(value))
        .zeroFlag(isZero(value));
  }
}