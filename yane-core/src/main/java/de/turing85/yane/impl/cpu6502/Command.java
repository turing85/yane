package de.turing85.yane.impl.cpu6502;

import static de.turing85.yane.impl.cpu6502.AddressingMode.*;
import static de.turing85.yane.impl.cpu6502.Register.*;

import de.turing85.yane.api.*;
import java.util.function.*;
import lombok.*;
import lombok.experimental.Delegate;

@Value
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class Command implements CommandFunction {
  private static final int FORCE_BREAK_PROGRAM_COUNTER = 0xFFFFFFFE;

  @Delegate
  CommandFunction function;

  String mnemonic;

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
            addressingResult.additionalCyclesNeeded());
      },
      "ADC");

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
            addressingResult.additionalCyclesNeeded());
      },
      "AND");

  static Command ASL = new Command(
      addressingResult -> {
        final int value = addressingResult.value();
        final int rawResult = value << 1;
        final int result = rawResult & 0xFF;
        final int address = addressingResult.address();
        Register updatedRegister = storeValueDependingOnAddress(
            address,
            result,
            addressingResult.register(),
            addressingResult.bus());
        return CommandResult.of(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(isZero(result))
                .carryFlag(hasCarried(rawResult)),
            addressingResult.additionalCyclesNeeded());
      },
      "ASL");

  static Command BCC = new Command(
      addressingResult ->
          branchIf(!addressingResult.register().isCarryFlagSet(), addressingResult),
      "BCC");

  static Command BCS = new Command(
      addressingResult ->
          branchIf(addressingResult.register().isCarryFlagSet(), addressingResult),
      "BCS");

  static Command BEQ = new Command(
      addressingResult ->
          branchIf(addressingResult.register().isZeroFlagSet(), addressingResult),
      "BEQ");

  static Command BIT = new Command(
      addressingResult -> {
        final int value = addressingResult.value();
        return CommandResult.of(
            addressingResult.register()
                .negativeFlag(isNegative(value))
                .overflowFlag((value & OVERFLOW_MASK) > 0)
                .zeroFlag((value & addressingResult.register().a()) == 0),
            addressingResult.additionalCyclesNeeded());
      },
      "BIT");

  static Command BMI = new Command(
      addressingResult ->
          branchIf(addressingResult.register().isNegativeFlagSet(), addressingResult),
      "BMI");

  static Command BNE = new Command(
      addressingResult ->
          branchIf(!addressingResult.register().isZeroFlagSet(), addressingResult),
      "BNE");

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
      addressingResult -> CommandResult.of(addressingResult.register().unsetCarryFlag(), 0),
      "CLC");

  static Command CLD = new Command(
      addressingResult -> CommandResult.of(addressingResult.register().unsetDecimalModeFlag(), 0),
      "CLD");

  static Command CLI = new Command(
      addressingResult ->
          CommandResult.of(addressingResult.register().unsetDisableIrqFlag(), 0),
      "CLI");

  static Command CLV = new Command(
      addressingResult ->
          CommandResult.of(addressingResult.register().unsetOverflowFlag(), 0),
      "CLV");

  static Command CMP = new Command(
      addressingResult -> compare(addressingResult, addressingResult.register().a()),
      "CMP");

  static Command CPX = new Command(
      addressingResult -> compare(addressingResult, addressingResult.register().x()),
      "CPX");

  static Command CPY = new Command(
      addressingResult -> compare(addressingResult, addressingResult.register().y()),
      "CPY");

  static Command DEC = new Command(
      addressingResult -> {
        final int valueDecremented = (addressingResult.value() - 1) & 0xFF;
        addressingResult.bus().write(addressingResult.address(), valueDecremented);
        return CommandResult.of(
            addressingResult.register()
                .negativeFlag(isNegative(valueDecremented))
                .zeroFlag(valueDecremented == 0),
            addressingResult.additionalCyclesNeeded());
      },
      "DEC");

  static Command DEX = new Command(
      addressingResult -> set(
          addressingResult.register()::x,
          (addressingResult.register().x() - 1) & 0xFF),
      "DEX");

  static Command DEY = new Command(
      addressingResult -> set(
          addressingResult.register()::y,
          (addressingResult.register().y() - 1) & 0xFF),
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
            addressingResult.additionalCyclesNeeded());
      },
      "EOR");

  static Command INC = new Command(
      addressingResult -> {
        final int newValue = (addressingResult.value() + 1) & 0xFF;
        addressingResult.bus().write(addressingResult.address(), newValue);
        return CommandResult.of(
            addressingResult.register()
                .negativeFlag(isNegative(newValue))
                .zeroFlag(newValue == 0),
            addressingResult.additionalCyclesNeeded());
      },
      "INC");

  static Command INX = new Command(
      addressingResult -> set(
          addressingResult.register()::x,
          (addressingResult.register().x() + 1) & 0xFF),
      "INX");

  static Command INY = new Command(
      addressingResult -> set(
          addressingResult.register()::y,
          (addressingResult.register().y() + 1) & 0xFF),
      "INY");

  static Command JMP = new Command(
      addressingResult -> CommandResult.of(
          addressingResult.register().programCounter(addressingResult.address()),
          addressingResult.additionalCyclesNeeded()),
      "JMP");

  static Command JSR = new Command(
      addressingResult -> {
        final Register updatedRegister = addressingResult.register().decrementProgramCounter();
        pushProgramCounterToStack(updatedRegister, addressingResult.bus());
        return CommandResult.of(
            updatedRegister.programCounter(addressingResult.address()),
            addressingResult.additionalCyclesNeeded());
      },
      "JSR");

  static Command LDA = new Command(
      addressingResult -> loadIntoRegister(
          addressingResult.value(),
          addressingResult.register()::a,
          addressingResult.additionalCyclesNeeded()),
      "LDA");

  static Command LDX = new Command(
      addressingResult -> loadIntoRegister(
          addressingResult.value(),
          addressingResult.register()::x,
          addressingResult.additionalCyclesNeeded()),
      "LDX");

  static Command LDY = new Command(
      addressingResult -> loadIntoRegister(
          addressingResult.value(),
          addressingResult.register()::y,
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
            addressingResult.additionalCyclesNeeded());
      },
      "LSR");

  static Command NOP = new Command(
      addressingResult -> CommandResult.of(addressingResult.register(), 0),
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
            addressingResult.additionalCyclesNeeded());
      },
      "ORA");

  static Command PHA = new Command(
      addressingResult -> {
        pushToStack(
            addressingResult.register(),
            addressingResult.register().a(),
            addressingResult.bus());
        return CommandResult.of(addressingResult.register(), 0);
      },
      "PHA");

  static Command PHP = new Command(
      addressingResult ->
          CommandResult.of(pushStatusToStack(
              addressingResult.register(),
              addressingResult.bus()), 0),
      "PHP");

  static Command PLA = new Command(
      addressingResult ->
          CommandResult.of(
              addressingResult.register().a(pullFromStack(
                  addressingResult.register(),
                  addressingResult.bus())),
              0),
      "PLA");

  static Command PLP = new Command(
      addressingResult ->
          CommandResult.of(
              addressingResult.register().status(pullFromStack(
                  addressingResult.register(), addressingResult.bus())),
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
            0);
      },
      "RTI");

  static Command RTS = new Command(
      addressingResult ->
          CommandResult.of(
              pullProgramCounterFromStack(addressingResult.register(), addressingResult.bus())
                  .incrementProgramCounter(),
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
            addressingResult.additionalCyclesNeeded());
      },
      "SBC");

  static Command SEC = new Command(
      addressingResult -> CommandResult.of(addressingResult.register().setCarryFlag(), 0),
      "SEC");

  static Command SED = new Command(
      addressingResult -> CommandResult.of(addressingResult.register().setDecimalModeFlag(), 0),
      "SED");

  static Command SEI = new Command(
      addressingResult -> CommandResult.of(addressingResult.register().setDisableIrqFlag(), 0),
      "SEI");

  static Command STA = new Command(
      addressingResult -> writeToBus(
          addressingResult,
          addressingResult.register()::a,
          addressingResult.bus(),
          addressingResult.register()),
      "STA");

  static Command STX = new Command(
      addressingResult -> writeToBus(
          addressingResult,
          addressingResult.register()::x,
          addressingResult.bus(),
          addressingResult.register()),
      "STX");

  static Command STY = new Command(
      addressingResult -> writeToBus(
          addressingResult,
          addressingResult.register()::y,
          addressingResult.bus(),
          addressingResult.register()),
      "STY");

  static Command TAX = new Command(
      addressingResult -> transfer(addressingResult.register()::a, addressingResult.register()::x),
      "TAX");

  private static CommandResult transfer(
      Supplier<Integer> transferSource,
      Function<Integer, Register> transferTarget) {
    return CommandResult.of(transferTarget.apply(transferSource.get()), 0);
  }

  static Command TAY = new Command(
      addressingResult -> transfer(addressingResult.register()::a, addressingResult.register()::y),
      "TAY");

  static Command TSX = new Command(
      addressingResult ->
          CommandResult.of(
              addressingResult.register().x(pullFromStack(
                  addressingResult.register(),
                  addressingResult.bus())),
              0),
      "TSX");

  static Command TXA = new Command(
      addressingResult -> transfer(addressingResult.register()::x, addressingResult.register()::a),
      "TXA");

  static Command TXS = new Command(
      addressingResult -> {
        pushToStack(
            addressingResult.register(),
            addressingResult.register().x(),
            addressingResult.bus());
        return CommandResult.of(addressingResult.register(), 0);
      },
      "TXS");

  static Command TYA = new Command(
      addressingResult -> transfer(addressingResult.register()::y, addressingResult.register()::a),
      "TYA");

  static Command UNKNOWN = new Command(
      addressingResult -> CommandResult.of(addressingResult.register(), 0),
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
    return CommandResult.of(updatedRegister, additionalCyclesNeeded);
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

  private static CommandResult compare(AddressingResult addressingResult, int existingValue) {
    final int value = addressingResult.value();
    return CommandResult.of(
        addressingResult.register()
            .negativeFlag(isNegative(value))
            .zeroFlag(value == existingValue)
            .carryFlag(existingValue >= value),
        addressingResult.additionalCyclesNeeded());
  }

  private static CommandResult set(Function<Integer, Register> registerSetter, int newValue) {
    return CommandResult.of(
        registerSetter.apply(newValue)
            .negativeFlag(isNegative(newValue))
            .zeroFlag(newValue == 0),
        0);
  }

  private static CommandResult loadIntoRegister(
      int newValue,
      Function<Integer, Register> registerSetter,
      int additionalCyclesNeeded) {
    return CommandResult.of(
        registerSetter.apply(newValue)
            .negativeFlag(isNegative(newValue))
            .zeroFlag(newValue == 0),
        additionalCyclesNeeded);
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

  private static CommandResult writeToBus(
      AddressingResult addressingResult,
      Supplier<Integer> registerGetter,
      CpuBus bus,
      Register register) {
    bus.write(addressingResult.address(), registerGetter.get());
    return CommandResult.of(register, addressingResult.additionalCyclesNeeded());
  }
}