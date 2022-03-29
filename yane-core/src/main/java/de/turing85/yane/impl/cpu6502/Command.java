package de.turing85.yane.impl.cpu6502;

import static de.turing85.yane.impl.cpu6502.AddressingMode.*;
import static de.turing85.yane.impl.cpu6502.Register.*;

import de.turing85.yane.api.*;
import java.util.function.*;
import lombok.*;
import lombok.experimental.Delegate;

/**
 * <p>The commands supported by the 6502 processor.</p>
 *
 * <p>As described in {@link CommandFunction}, each {@link Command} takes a {@link AddressingResult}
 * as input and returns a {@link CommandResult}.</p>
 *
 * <p>For not, all instructions are executed "at once", so this emulation is not cycle-accurate.</p>
 *
 * <p>In the following, if we talk about {@code value} and {@code address}, we refer to
 * {@link AddressingResult#value} and {@link AddressingResult#address}, unless otherwise noted.</p>
 *
 * <p>The {@link Register}-flags are set as expected, unless otherwise noted. The flags are set
 * after the core semantic of the command have been executed. For example, when {@code #ADC}
 * (Add with Carry) is executed, the whole addition (including the evaluation of the {{@code C}
 * ({@link Register#isCarryFlagSet()}} and increasing the final result by {@code 1} if it is set) is
 * executed, before the new flag-values are determined.</p>
 */
@Value
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class Command implements CommandFunction {
  /**
   * The program counter is set to this value when a forced break is encountered.
   */
  private static final int FORCE_BREAK_PROGRAM_COUNTER = 0xFFFFFFFE;

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
   * <p>The {@code value} is added to {@link Register#a}. If the {@code C}
   * ({@link Register#isCarryFlagSet()}) is set, the final value of {@link Register#a} is increased
   * by {@code 1}.</p>
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
   */
  static Command ADC = new Command(
      addressingResult -> {
        final Register updatedRegister = addressingResult.register();
        final int a = updatedRegister.a();
        final int value = addressingResult.value();
        final int rawResult = a + value + (updatedRegister.isCarryFlagSet() ? 1 : 0);
        final int result = rawResult & 0xFF;
        return CommandResult.of(
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
   * <p>The {@code value} "and"-ed {@link Register#a} and written to {@link Register#a}.</p>
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
  static Command AND = new Command(
      addressingResult -> {
        final Register updatedRegister = addressingResult.register();
        final int a = updatedRegister.a();
        final int value = addressingResult.value();
        final int result = a & value;
        return CommandResult.of(
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
   * <p>The {@code value} logically shifted by 1 position and written back.</p>
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
  static Command ASL = new Command(
      addressingResult -> {
        final int value = addressingResult.value();
        final int rawResult = value << 1;
        final int result = rawResult & 0xFF;
        final int address = addressingResult.address();
        final CpuBus bus = addressingResult.bus();
        Register updatedRegister = storeValueDependingOnAddress(
            address,
            result,
            addressingResult.register(),
            bus);
        return CommandResult.of(
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
   * <p>Sets {@link Register#programCounter} to {@code address} if the {@code C}-flag
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
   */
  static Command BCC = new Command(
      addressingResult ->
          branchIf(!addressingResult.register().isCarryFlagSet(), addressingResult),
      "BCC");

  /**
   * <p>Branch on Carry Set command.</p>
   *
   * <p>Sets {@link Register#programCounter} to {@code address} if the {@code C}-flag
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
   */
  static Command BCS = new Command(
      addressingResult ->
          branchIf(addressingResult.register().isCarryFlagSet(), addressingResult),
      "BCS");

  /**
   * <p>Branch on Result Zero command.</p>
   *
   * <p>Sets {@link Register#programCounter} to {@code address} if the {@code Z}-flag
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
   */
  static Command BEQ = new Command(
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
   *        {@link Register#a} is {@code 0}.
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
  static Command BIT = new Command(
      addressingResult -> {
        final int value = addressingResult.value();
        return CommandResult.of(
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
   * <p>Sets {@link Register#programCounter} to {@code address} if the {@code N}-flag
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
   */
  static Command BMI = new Command(
      addressingResult ->
          branchIf(addressingResult.register().isNegativeFlagSet(), addressingResult),
      "BMI");

  /**
   * <p>Branch on Result not Equal command.</p>
   *
   * <p>Sets {@link Register#programCounter} to {@code address} if the {@code Z}-flag
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
   */
  static Command BNE = new Command(
      addressingResult ->
          branchIf(!addressingResult.register().isZeroFlagSet(), addressingResult),
      "BNE");

  /**
   * <p>Branch on Result Plus command.</p>
   *
   * <p>Sets {@link Register#programCounter} to {@code address} if the {@code N}-flag
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
   */
  static Command BPL = new Command(
      addressingResult ->
          branchIf(!addressingResult.register().isNegativeFlagSet(), addressingResult),
      "BPL");

  static Command BRK = new Command(
      addressingResult -> {
        final Register register = addressingResult.register()
            .setDisableIrqFlag()
            .setBreakFlag()
            .incrementProgramCounter();
        final CpuBus bus = addressingResult.bus();
        pushToStack(register, register.programCounter(), bus);
        return CommandResult.of(
            pushStatusToStack(register, bus)
                .unsetBreakFlag()
                .programCounter(FORCE_BREAK_PROGRAM_COUNTER),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "BRK");

  static Command BVC = new Command(
      addressingResult ->
          branchIf(!addressingResult.register().isOverflowFlagSet(), addressingResult),
      "BVC");

  static Command BVS = new Command(
      addressingResult ->
          branchIf(addressingResult.register().isOverflowFlagSet(), addressingResult),
      "BVS");

  static Command CLC = new Command(
      addressingResult -> CommandResult.of(
          addressingResult.register().unsetCarryFlag(),
          addressingResult.bus(),
          0),
      "CLC");

  static Command CLD = new Command(
      addressingResult -> CommandResult.of(
          addressingResult.register().unsetDecimalModeFlag(),
          addressingResult.bus(),
          0),
      "CLD");

  static Command CLI = new Command(
      addressingResult ->
          CommandResult.of(
              addressingResult.register().unsetDisableIrqFlag(),
              addressingResult.bus(),
              0),
      "CLI");

  static Command CLV = new Command(
      addressingResult ->
          CommandResult.of(
              addressingResult.register().unsetOverflowFlag(),
              addressingResult.bus(),
              0),
      "CLV");

  static Command CMP = new Command(
      addressingResult -> {
        final int a = addressingResult.register().a();
        final int value = addressingResult.value();
        return CommandResult.of(
            addressingResult.register()
                .negativeFlag(value > a)
                .zeroFlag(value == a)
                .carryFlag(a >= value),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "CMP");

  static Command CPX = new Command(
      addressingResult -> {
        final int x = addressingResult.register().x();
        final int value = addressingResult.value();
        return CommandResult.of(
            addressingResult.register()
                .negativeFlag(value > x)
                .zeroFlag(value == x)
                .carryFlag(x >= value),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "CPX");

  static Command CPY = new Command(
      addressingResult -> {
        final int y = addressingResult.register().y();
        final int value = addressingResult.value();
        return CommandResult.of(
            addressingResult.register()
                .negativeFlag(value > y)
                .zeroFlag(value == y)
                .carryFlag(y >= value),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "CPY");

  static Command DEC = new Command(
      addressingResult -> {
        final int valueDecremented = (addressingResult.value() - 1) & 0xFF;
        final CpuBus bus = addressingResult.bus();
        bus.write(addressingResult.address(), valueDecremented);
        return CommandResult.of(
            addressingResult.register()
                .negativeFlag(isNegative(valueDecremented))
                .zeroFlag(valueDecremented == 0),
            bus,
            addressingResult.additionalCyclesNeeded());
      },
      "DEC");

  static Command DEX = new Command(
      addressingResult -> CommandResult.of(
          set(addressingResult.register()::x, (addressingResult.register().x() - 1) & 0xFF),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "DEX");

  static Command DEY = new Command(
      addressingResult -> CommandResult.of(
          set(addressingResult.register()::y, (addressingResult.register().y() - 1) & 0xFF),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "DEY");

  static Command EOR = new Command(
      addressingResult -> {
        final Register updatedRegister = addressingResult.register();
        final int newA = updatedRegister.a() ^ addressingResult.value();
        return CommandResult.of(
            updatedRegister
                .a(newA)
                .negativeFlag(isNegative(newA))
                .zeroFlag(newA == 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "EOR");

  static Command INC = new Command(
      addressingResult -> {
        final int newValue = (addressingResult.value() + 1) & 0xFF;
        final CpuBus bus = addressingResult.bus();
        bus.write(addressingResult.address(), newValue);
        return CommandResult.of(
            addressingResult.register()
                .negativeFlag(isNegative(newValue))
                .zeroFlag(newValue == 0),
            bus,
            addressingResult.additionalCyclesNeeded());
      },
      "INC");

  static Command INX = new Command(
      addressingResult -> CommandResult.of(
          set(addressingResult.register()::x, (addressingResult.register().x() + 1) & 0xFF),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "INX");

  static Command INY = new Command(
      addressingResult -> CommandResult.of(
          set(addressingResult.register()::y, (addressingResult.register().y() + 1) & 0xFF),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "INY");

  static Command JMP = new Command(
      addressingResult -> CommandResult.of(
          addressingResult.register().programCounter(addressingResult.address()),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "JMP");

  static Command JSR = new Command(
      addressingResult -> {
        final Register updatedRegister = addressingResult.register().decrementProgramCounter();
        pushProgramCounterToStack(updatedRegister, addressingResult.bus());
        return CommandResult.of(
            updatedRegister.programCounter(addressingResult.address()),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "JSR");

  static Command LDA = new Command(
      addressingResult -> CommandResult.of(
          addressingResult.register().a(addressingResult.value()),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "LDA");

  static Command LDX = new Command(
      addressingResult -> CommandResult.of(
          addressingResult.register().x(addressingResult.value()),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "LDX");

  static Command LDY = new Command(
      addressingResult -> CommandResult.of(
          addressingResult.register().y(addressingResult.value()),
          addressingResult.bus(),
          addressingResult.additionalCyclesNeeded()),
      "LDY");

  static Command LSR = new Command(
      addressingResult -> {
        final int value = addressingResult.value();
        final int result = (value >> 1) & 0xFF;
        final int address = addressingResult.address();
        final Register updatedRegister = storeValueDependingOnAddress(
            address,
            result,
            addressingResult.register(),
            addressingResult.bus());
        return CommandResult.of(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0)
                .carryFlag((value & 0x01) > 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "LSR");

  static Command NOP = new Command(
      addressingResult -> CommandResult.of(addressingResult.register(), addressingResult.bus(), 0),
      "NOP");

  static Command ORA = new Command(
      addressingResult -> {
        Register updatedRegister = addressingResult.register();
        final int value = addressingResult.value();
        final int result = value | updatedRegister.a();
        return CommandResult.of(
            updatedRegister
                .a(result)
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "ORA");

  static Command PHA = new Command(
      addressingResult -> {
        pushToStack(
            addressingResult.register(),
            addressingResult.register().a(),
            addressingResult.bus());
        return CommandResult.of(addressingResult.register(), addressingResult.bus(), 0);
      },
      "PHA");

  static Command PHP = new Command(
      addressingResult ->
          CommandResult.of(
              pushStatusToStack(addressingResult.register(), addressingResult.bus()),
              addressingResult.bus(),
              0),
      "PHP");

  static Command PLA = new Command(
      addressingResult ->
          CommandResult.of(
              addressingResult.register().a(pullFromStack(
                  addressingResult.register(),
                  addressingResult.bus())),
              addressingResult.bus(),
              0),
      "PLA");

  static Command PLP = new Command(
      addressingResult ->
          CommandResult.of(
              addressingResult.register().status(pullFromStack(
                  addressingResult.register(),
                  addressingResult.bus())),
              addressingResult.bus(),
              0),
      "PLP");

  static Command ROL = new Command(
      addressingResult -> {
        final int value = addressingResult.value();
        final int rawResult = (value << 1) & ((value & 0x100) >> 4);
        final int result = rawResult & 0xFF;
        final int address = addressingResult.address();
        final Register updatedRegister = storeValueDependingOnAddress(
            address,
            result,
            addressingResult.register(),
            addressingResult.bus());
        return CommandResult.of(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0)
                .carryFlag(hasCarried(rawResult)),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "ROL");

  static Command ROR = new Command(
      addressingResult -> {
        final int value = addressingResult.value();
        final int rawResult = (value >> 1) & ((value & 0x0001) << 4);
        final int result = rawResult & 0xFF;
        final int address = addressingResult.address();
        final Register updatedRegister = storeValueDependingOnAddress(
            address,
            result,
            addressingResult.register(),
            addressingResult.bus());
        return CommandResult.of(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0)
                .carryFlag((value & 0x01) > 0),
            addressingResult.bus(),
            addressingResult.additionalCyclesNeeded());
      },
      "ROR");

  static Command RTI = new Command(
      addressingResult -> {
        final CpuBus bus = addressingResult.bus();
        return CommandResult.of(
            pullStatusFromStack(pullProgramCounterFromStack(addressingResult.register(), bus), bus)
                .setUnusedFlag()
                .unsetBreakFlag(),
            bus,
            0);
      },
      "RTI");

  static Command RTS = new Command(
      addressingResult ->
          CommandResult.of(
              pullProgramCounterFromStack(addressingResult.register(), addressingResult.bus())
                  .incrementProgramCounter(),
              addressingResult.bus(),
              0),
      "RTS");

  static Command SBC = new Command(
      addressingResult -> {
        final Register register = addressingResult.register();
        final int a = register.a();
        final int value = addressingResult.value();
        final int rawResult = a + value + (register.isCarryFlagSet() ? 1 : 0);
        final int result = rawResult & 0xFF;
        return CommandResult.of(
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

  static Command SEC = new Command(
      addressingResult ->
          CommandResult.of(addressingResult.register().setCarryFlag(), addressingResult.bus(), 0),
      "SEC");

  static Command SED = new Command(
      addressingResult -> CommandResult.of(
          addressingResult.register().setDecimalModeFlag(),
          addressingResult.bus(),
          0),
      "SED");

  static Command SEI = new Command(
      addressingResult -> CommandResult.of(
          addressingResult.register().setDisableIrqFlag(),
          addressingResult.bus(),
          0),
      "SEI");

  static Command STA = new Command(
      addressingResult -> {
        final CpuBus bus = addressingResult.bus();
        final Register register = addressingResult.register();
        bus.write(addressingResult.address(), register.a());
        return CommandResult.of(register, bus, addressingResult.additionalCyclesNeeded());
      },
      "STA");

  static Command STX = new Command(
      addressingResult -> {
        final CpuBus bus = addressingResult.bus();
        final Register register = addressingResult.register();
        bus.write(addressingResult.address(), register.x());
        return CommandResult.of(register, bus, addressingResult.additionalCyclesNeeded());
      },
      "STX");

  static Command STY = new Command(
      addressingResult -> {
        final CpuBus bus = addressingResult.bus();
        final Register register = addressingResult.register();
        bus.write(addressingResult.address(), register.y());
        return CommandResult.of(register, bus, addressingResult.additionalCyclesNeeded());
      },
      "STY");

  static Command TAX = new Command(
      addressingResult -> transfer(
          addressingResult.register()::a,
          addressingResult.register()::x,
          addressingResult),
      "TAX");

  private static CommandResult transfer(
      Supplier<Integer> transferSource,
      Function<Integer, Register> transferTarget,
      AddressingResult addressingResult) {
    return CommandResult.of(
        transferTarget.apply(transferSource.get()),
        addressingResult.bus(),
        0);
  }

  static Command TAY = new Command(
      addressingResult -> transfer(
          addressingResult.register()::a,
          addressingResult.register()::y,
          addressingResult),
      "TAY");

  static Command TSX = new Command(
      addressingResult ->
          CommandResult.of(
              addressingResult.register().x(pullFromStack(
                  addressingResult.register(),
                  addressingResult.bus())),
              addressingResult.bus(),
              0),
      "TSX");

  static Command TXA = new Command(
      addressingResult -> transfer(
          addressingResult.register()::x,
          addressingResult.register()::a,
          addressingResult),
      "TXA");

  static Command TXS = new Command(
      addressingResult -> {
        pushToStack(
            addressingResult.register(),
            addressingResult.register().x(),
            addressingResult.bus());
        return CommandResult.of(addressingResult.register(), addressingResult.bus(), 0);
      },
      "TXS");

  static Command TYA = new Command(
      addressingResult -> transfer(
          addressingResult.register()::y,
          addressingResult.register()::a,
          addressingResult),
      "TYA");

  static Command UNKNOWN = new Command(
      addressingResult -> CommandResult.of(addressingResult.register(), addressingResult.bus(), 0),
      "???");

  @Override
  public String toString() {
    return mnemonic();
  }

  private static boolean isNegative(int result) {
    return (result & NEGATIVE_MASK) > 0;
  }

  private static boolean isZero(int result) {
    return (result & 0xFF) == 0;
  }

  private static boolean hasCarried(int result) {
    return (result & 0xFF00) > 0;
  }

  private static boolean hasOverflown(int lhs, int rhs, int result) {
    return bytesHaveSameSign(lhs, rhs) && bytesHaveDifferentSign(lhs, result);
  }

  private static boolean bytesHaveSameSign(int lhs, int rhs) {
    return !bytesHaveDifferentSign(lhs, rhs);
  }

  private static boolean bytesHaveDifferentSign(int lhs, int rhs) {
    return ((lhs ^ rhs) & 0x80) > 0;
  }

  private static CommandResult branchIf(boolean condition, AddressingResult addressingResult) {
    final Register updatedRegister = addressingResult.register();
    int additionalCyclesNeeded = addressingResult.additionalCyclesNeeded();
    if (condition) {
      ++additionalCyclesNeeded;
      final int programCounter = updatedRegister.programCounter();
      final int newProgramCounter = addressingResult.address();
      if (addressesAreOnDifferentPages(programCounter, newProgramCounter)) {
        ++additionalCyclesNeeded;
      }
    }
    return CommandResult.of(updatedRegister, addressingResult.bus(), additionalCyclesNeeded);
  }

  private static boolean addressesAreOnDifferentPages(int lhs, int rhs) {
    return (lhs & 0xFF00) != (rhs & 0xFF00);
  }

  private static void pushProgramCounterToStack(Register register, CpuBus bus) {
    pushToStack(register, (register.programCounter() >> 8), bus);
    pushToStack(register, register.programCounter() & 0xFF, bus);
  }

  private static Register pullProgramCounterFromStack(Register register, CpuBus bus) {
    final int programCounterLow = pullFromStack(register, bus);
    final int programCounterHigh = pullFromStack(register, bus) << 8;
    return register.programCounter(programCounterLow | programCounterHigh);
  }

  private static Register pushStatusToStack(Register register, CpuBus bus) {
    final Register updatedRegister = register.setUnusedFlag();
    pushToStack(register, updatedRegister.status(), bus);
    return updatedRegister;
  }

  private static Register pullStatusFromStack(Register register, CpuBus bus) {
    return register
        .status(pullFromStack(register, bus))
        .unsetUnusedFlag()
        .unsetBreakFlag();
  }

  private static void pushToStack(Register register, int value, CpuBus bus) {
    bus.write(register.getAndDecrementStackPointer(), value);
  }

  static int pullFromStack(Register register, CpuBus bus) {
    return bus.read(register.incrementAndGetStackPointer());
  }

  private static Register set(Function<Integer, Register> registerSetter, int newValue) {
    return registerSetter.apply(newValue)
        .negativeFlag(isNegative(newValue))
        .zeroFlag(newValue == 0);
  }

  private static Register storeValueDependingOnAddress(
      int address,
      int result,
      Register register,
      CpuBus bus) {
    if (address == IMPLIED_LOADED_ADDRESS) {
      return register.a(result);
    } else {
      bus.write(address, result);
      return register;
    }
  }
}