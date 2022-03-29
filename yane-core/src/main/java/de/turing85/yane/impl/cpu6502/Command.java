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
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        final int a = register.a();
        final int value = addressingResult.value();
        final int rawResult = a + value + (register.isCarryFlagSet() ? 1 : 0);
        final int result = rawResult & 0xFF;
        return CommandResult.of(
            register
                .a(result)
                .negativeFlag(isNegative(rawResult))
                .zeroFlag(isZero(result))
                .carryFlag(hasCarried(rawResult))
                .overflowFlag(hasOverflown(a, value, rawResult)),
            addressingResult.additionalCyclesNeeded());
      },
      "ADC");

  static Command AND = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        final Register updatedRegister = addressingResult.register();
        final int a = updatedRegister.a();
        final int value = addressingResult.value();
        final int rawResult = a & value;
        final int result = rawResult & 0xFF;
        return CommandResult.of(
            updatedRegister
                .a(result)
                .negativeFlag(isNegative(rawResult))
                .zeroFlag(result == 0),
            addressingResult.additionalCyclesNeeded());
      },
      "AND");

  static Command ASL = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        final int value = addressingResult.value();
        final int rawResult = value << 1;
        final int result = rawResult & 0xFF;
        final int address = addressingResult.address();
        Register updatedRegister =
            storeValueDependingOnAddress(address, result, addressingResult.register(), bus);
        return CommandResult.of(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(isZero(result))
                .carryFlag(hasCarried(rawResult)),
            addressingResult.additionalCyclesNeeded());
      },
      "ASL");

  static Command BCC = new Command(
      (register, bus, addressingMode) ->
          branch(addressingMode, register, bus, !register.isCarryFlagSet()),
      "BCC");

  static Command BCS = new Command(
      (register, bus, addressingMode) ->
          branch(addressingMode, register, bus, register.isCarryFlagSet()),
      "BCS");

  static Command BEQ = new Command(
      (register, bus, addressingMode) ->
          branch(addressingMode, register, bus, register.isZeroFlagSet()),
      "BEQ");

  static Command BIT = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        final int value = addressingResult.value();
        return CommandResult.of(
            addressingResult.register()
                .negativeFlag(isNegative(value))
                .overflowFlag((value & OVERFLOW_MASK) > 0)
                .zeroFlag((value & register.a()) == 0),
            addressingResult.additionalCyclesNeeded());
      },
      "BIT");

  static Command BMI = new Command(
      (register, bus, addressingMode) ->
          branch(addressingMode, register, bus, register.isNegativeFlagSet()),
      "BMI");

  static Command BNE = new Command(
      (register, bus, addressingMode) ->
          branch(addressingMode, register, bus, !register.isZeroFlagSet()),
      "BNE");

  static Command BPL = new Command(
      (register, bus, addressingMode) ->
          branch(addressingMode, register, bus, !register.isNegativeFlagSet()),
      "BPL");

  static Command BRK = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        final Register updatedRegister = register
            .setDisableIrqFlag()
            .setBreakFlag()
            .incrementProgramCounter();
        pushToStack(register, updatedRegister.programCounter() & 0xFFFF, bus);
        return CommandResult.of(
            pushStatusToStack(updatedRegister, bus)
                .unsetBreakFlag()
                .programCounter(FORCE_BREAK_PROGRAM_COUNTER),
            addressingResult.additionalCyclesNeeded());
      },
      "BRK");

  static Command BVC = new Command(
      (register, bus, addressingMode) ->
          branch(addressingMode, register, bus, !register.isOverflowFlagSet()),
      "BVC");

  static Command BVS = new Command(
      (register, bus, addressingMode) ->
          branch(addressingMode, register, bus, register.isOverflowFlagSet()),
      "BVS");

  static Command CLC = new Command(
      (register, bus, addressingMode) -> CommandResult.of(register.unsetCarryFlag(), 0),
      "CLC");

  static Command CLD = new Command(
      (register, bus, addressingMode) -> CommandResult.of(register.unsetDecimalModeFlag(), 0),
      "CLD");

  static Command CLI = new Command(
      (register, bus, addressingMode) ->
          CommandResult.of(register.unsetDisableIrqFlag(), 0),
      "CLI");

  static Command CLV = new Command(
      (register, bus, addressingMode) ->
          CommandResult.of(register.unsetOverflowFlag(), 0),
      "CLV");

  static Command CMP = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        return compare(addressingResult, addressingResult.register().a());
      },
      "CMP");

  static Command CPX = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        return compare(addressingResult, addressingResult.register().x());
      },
      "CPX");

  static Command CPY = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        return compare(addressingResult, addressingResult.register().y());
      },
      "CPY");

  static Command DEC = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        final int valueDecremented = (addressingResult.value() - 1) & 0xFF;
        bus.write(addressingResult.address(), valueDecremented);
        return CommandResult.of(
            addressingResult.register()
                .negativeFlag(isNegative(valueDecremented))
                .zeroFlag(valueDecremented == 0),
            addressingResult.additionalCyclesNeeded());
      },
      "DEC");

  static Command DEX = new Command(
      (register, bus, addressingMode) -> decrement((register.x() - 1) & 0xFF, register::x),
      "DEX");

  static Command DEY = new Command(
      (register, bus, addressingMode) -> decrement((register.y() - 1) & 0xFF, register::y),
      "DEY");

  static Command EOR = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
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
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        final int newValue = (addressingResult.value() + 1) & 0xFF;
        bus.write(addressingResult.address(), newValue);
        return CommandResult.of(
            addressingResult.register()
                .negativeFlag(isNegative(newValue))
                .zeroFlag(newValue == 0),
            addressingResult.additionalCyclesNeeded());
      },
      "INC");

  static Command INX = new Command(
      (register, bus, addressingMode) -> increment((register.x() + 1) & 0xFF, register::x),
      "INX");

  static Command INY = new Command(
      (register, bus, addressingMode) -> increment((register.y() + 1) & 0xFF, register::y),
      "INY");

  static Command JMP = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        return CommandResult.of(
            addressingResult.register().programCounter(addressingResult.address()),
            addressingResult.additionalCyclesNeeded());
      },
      "JMP");

  static Command JSR = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        final Register updatedRegister = addressingResult.register().decrementProgramCounter();
        pushProgramCounterToStack(updatedRegister, bus);
        return CommandResult.of(
            updatedRegister.programCounter(addressingResult.address()),
            addressingResult.additionalCyclesNeeded());
      },
      "JSR");

  static Command LDA = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        return loadIntoRegister(
            addressingResult.value(),
            addressingResult.register()::a,
            addressingResult.additionalCyclesNeeded());
      },
      "LDA");

  static Command LDX = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        return loadIntoRegister(
            addressingResult.value(),
            addressingResult.register()::x,
            addressingResult.additionalCyclesNeeded());
      },
      "LDX");

  static Command LDY = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        return loadIntoRegister(
            addressingResult.value(),
            addressingResult.register()::y,
            addressingResult.additionalCyclesNeeded());
      },
      "LDY");

  static Command LSR = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        final int value = addressingResult.value();
        final int result = (value >> 1) & 0xFF;
        final int address = addressingResult.address();
        final Register updatedRegister =
            storeValueDependingOnAddress(address, result, addressingResult.register(), bus);
        return CommandResult.of(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0)
                .carryFlag((value & 0x01) > 0),
            addressingResult.additionalCyclesNeeded());
      },
      "LSR");

  static Command NOP = new Command(
      (register, bus, addressingMode) -> CommandResult.of(register, 0),
      "NOP");

  static Command ORA = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
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
      (register, bus, addressingMode) -> {
        pushToStack(register, register.a(), bus);
        return CommandResult.of(register, 0);
      },
      "PHA");

  static Command PHP = new Command(
      (register, bus, addressingMode) ->
          CommandResult.of(pushStatusToStack(register, bus), 0),
      "PHP");

  static Command PLA = new Command(
      (register, bus, addressingMode) ->
          CommandResult.of(register.a(pullFromStack(register, bus)), 0),
      "PLA");

  static Command PLP = new Command(
      (register, bus, addressingMode) ->
          CommandResult.of(register.status(pullFromStack(register, bus)), 0),
      "PLP");

  static Command ROL = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        final int value = addressingResult.value();
        final int rawResult = (value << 1) & ((value & 0x100) >> 4);
        final int result = rawResult & 0xFF;
        final int address = addressingResult.address();
        final Register updatedRegister =
            storeValueDependingOnAddress(address, result, addressingResult.register(), bus);
        return CommandResult.of(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0)
                .carryFlag(hasCarried(rawResult)),
            addressingResult.additionalCyclesNeeded());
      },
      "ROL");

  static Command ROR = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        final int value = addressingResult.value();
        final int rawResult = (value >> 1) & ((value & 0x0001) << 4);
        final int result = rawResult & 0xFF;
        final int address = addressingResult.address();
        final Register updatedRegister =
            storeValueDependingOnAddress(address, result, addressingResult.register(), bus);
        return CommandResult.of(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0)
                .carryFlag((value & 0x01) > 0),
            addressingResult.additionalCyclesNeeded());
      },
      "ROR");

  static Command RTI = new Command(
      (register, bus, addressingMode) ->
          CommandResult.of(
              pullStatusFromStack(pullProgramCounterFromStack(register, bus), bus)
                  .setUnusedFlag()
                  .unsetBreakFlag(),
              0),
      "RTI");

  static Command RTS = new Command(
      (register, bus, addressingMode) ->
          CommandResult.of(
              pullProgramCounterFromStack(register, bus).incrementProgramCounter(),
              0),
      "RTS");

  static Command SBC = new Command(
      (register, bus, addressingMode) -> {
        final AddressingResult addressingResult = addressingMode.fetch(register, bus);
        final int a = register.a();
        final int value = addressingResult.value() ^ 0x00FF;
        final int rawResult = a + value + (register.isCarryFlagSet() ? 1 : 0);
        final int result = rawResult & 0xFF;
        return CommandResult.of(
            addressingResult.register()
                .a(result)
                .negativeFlag(isNegative(result))
                .overflowFlag(hasOverflown(a, value, rawResult))
                .zeroFlag(isZero(result))
                .carryFlag(hasCarried(rawResult)),
            addressingResult.additionalCyclesNeeded());
      },
      "SBC");

  static Command SEC = new Command(
      (register, bus, addressingMode) -> CommandResult.of(register.setCarryFlag(), 0),
      "SEC");

  static Command SED = new Command(
      (register, bus, addressingMode) -> CommandResult.of(register.setDecimalModeFlag(), 0),
      "SED");

  static Command SEI = new Command(
      (register, bus, addressingMode) -> CommandResult.of(register.setDisableIrqFlag(), 0),
      "SEI");

  static Command STA = new Command(
      (register, bus, addressingMode) -> writeToBus(addressingMode, register::a, bus, register),
      "STA");

  static Command STX = new Command(
      (register, bus, addressingMode) -> writeToBus(addressingMode, register::x, bus, register),
      "STX");

  static Command STY = new Command(
      (register, bus, addressingMode) -> writeToBus(addressingMode, register::y, bus, register),
      "STY");

  static Command TAX = new Command(
      (register, bus, addressingMode) -> CommandResult.of(register.x(register.a()), 0),
      "TAX");

  static Command TAY = new Command(
      (register, bus, addressingMode) -> CommandResult.of(register.y(register.a()), 0),
      "TAY");

  static Command TSX = new Command(
      (register, bus, addressingMode) ->
          CommandResult.of(register.x(pullFromStack(register, bus)), 0),
      "TSX");

  static Command TXA = new Command(
      (register, bus, addressingMode) -> CommandResult.of(register.a(register.x()), 0),
      "TXA");

  static Command TXS = new Command(
      (register, bus, addressingMode) -> {
        pushToStack(register, register.x(), bus);
        return CommandResult.of(register, 0);
      },
      "TXS");

  static Command TYA = new Command(
      (register, bus, addressingMode) -> CommandResult.of(register.a(register.y()), 0),
      "TYA");

  static Command UNKNOWN = new Command(
      (register, bus, addressingMode) -> CommandResult.of(register, 0),
      "???");

  @Override
  public String toString() {
    return mnemonic();
  }

  private static boolean hasCarried(int result) {
    return (result & 0xFF00) > 0;
  }

  private static boolean isNegative(int result) {
    return (result & NEGATIVE_MASK) > 0;
  }


  private static boolean isZero(int result) {
    return (result & 0xFF) == 0;
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

  private static CommandResult branch(
      AddressingMode addressingMode,
      Register register,
      CpuBus bus,
      boolean condition) {
    final AddressingResult addressingResult = addressingMode.fetch(register, bus);
    final Register updatedRegister = addressingResult.register();
    int additionalCyclesNeeded = addressingResult.additionalCyclesNeeded();
    if (condition) {
      ++additionalCyclesNeeded;
      final int programCounter = updatedRegister.programCounter();
      final int newProgramCounter = addressingResult.address();
      if (countersAreOnDifferentPages(programCounter, newProgramCounter)) {
        ++additionalCyclesNeeded;
      }
    }
    return CommandResult.of(updatedRegister, additionalCyclesNeeded);
  }

  private static boolean countersAreOnDifferentPages(int lhs, int rhs) {
    return (lhs & 0xFF00) != (rhs & 0xFF00);
  }

  private static void pushProgramCounterToStack(Register register, CpuBus bus) {
    pushToStack(register, (register.programCounter() >> 8 & 0xFF), bus);
    pushToStack(register, register.programCounter() & 0xFF, bus);
  }

  private static Register pullProgramCounterFromStack(Register register, CpuBus bus) {
    final int programCounterLow = pullFromStack(register, bus) & 0xFF;
    final int programCounterHigh = (pullFromStack(register, bus) & 0xFF) << 8;
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
    bus.write(register.getAndDecrementStackPointer(), value & 0xFF);
  }

  static int pullFromStack(Register register, CpuBus bus) {
    return bus.read(register.incrementAndGetStackPointer());
  }

  private static CommandResult compare(AddressingResult addressingResult, int existingValue) {
    final Register updatedRegister = addressingResult.register();
    final int value = addressingResult.value();
    return CommandResult.of(
        updatedRegister
            .negativeFlag(isNegative(value))
            .zeroFlag(value == existingValue)
            .carryFlag(existingValue >= value),
        addressingResult.additionalCyclesNeeded());
  }

  private static CommandResult decrement(int newValue, Function<Integer, Register> registerSetter) {
    return CommandResult.of(
        registerSetter.apply(newValue)
            .negativeFlag(isNegative(newValue))
            .zeroFlag(newValue == 0),
        0);
  }

  private static CommandResult increment(int newValue, Function<Integer, Register> registerSetter) {
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
      register = register.a(result);
    } else {
      bus.write(address, result);
    }
    return register;
  }

  private static CommandResult writeToBus(
      AddressingMode addressingMode,
      Supplier<Integer> registerGetter,
      CpuBus bus,
      Register register) {
    final AddressingResult addressingResult = addressingMode.fetch(register, bus);
    final int address = addressingResult.address();
    bus.write(address, registerGetter.get());
    return CommandResult.of(register, addressingResult.additionalCyclesNeeded());
  }
}