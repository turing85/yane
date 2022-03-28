package de.turing85.yane.impl.cpu6502;

import static de.turing85.yane.impl.cpu6502.AddressMode.*;
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
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final int a = register.a();
        final int value = addressResult.value();
        final int rawResult = a + value + (register.isCarryFlagSet() ? 1 : 0);
        final int result = rawResult & 0xFFFF;
        return CommandResult.of(
            register
                .a(result)
                .negativeFlag(isNegative(rawResult))
                .zeroFlag(isZero(result))
                .carryFlag(hasCarried(rawResult))
                .overflowFlag(hasOverflown(a, value, rawResult)),
            addressResult.additionalCyclesNeeded());
      },
      "ADC");

  static Command AND = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final Register updatedRegister = addressResult.register();
        final int a = updatedRegister.a();
        final int value = addressResult.value();
        final int rawResult = a & value;
        final int result = rawResult & 0xFF;
        return CommandResult.of(
            updatedRegister
                .a(result)
                .negativeFlag(isNegative(rawResult))
                .zeroFlag(result == 0),
            addressResult.additionalCyclesNeeded());
      },
      "AND");

  static Command ASL = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final int value = addressResult.value();
        final int rawResult = value << 1;
        final int result = rawResult & 0xFF;
        final int address = addressResult.address();
        Register updatedRegister =
            storeValueDependingOnAddress(address, result, addressResult.register(), bus);
        return CommandResult.of(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(isZero(result))
                .carryFlag(hasCarried(rawResult)),
            addressResult.additionalCyclesNeeded());
      },
      "ASL");

  static Command BCC = new Command(
      (register, bus, addressMode) ->
          branch(addressMode, register, bus, !register.isCarryFlagSet()),
      "BCC");

  static Command BCS = new Command(
      (register, bus, addressMode) ->
          branch(addressMode, register, bus, register.isCarryFlagSet()),
      "BCS");

  static Command BEQ = new Command(
      (register, bus, addressMode) ->
          branch(addressMode, register, bus, register.isZeroFlagSet()),
      "BEQ");

  static Command BIT = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final int value = addressResult.value();
        return CommandResult.of(
            addressResult.register()
                .negativeFlag(isNegative(value))
                .overflowFlag((value & OVERFLOW_MASK) > 0)
                .zeroFlag((value & register.a()) == 0),
            addressResult.additionalCyclesNeeded());
      },
      "BIT");

  static Command BMI = new Command(
      (register, bus, addressMode) -> branch(addressMode, register, bus, register.isNegativeFlagSet()),
      "BMI");

  static Command BNE = new Command(
      (register, bus, addressMode) -> branch(addressMode, register, bus, !register.isZeroFlagSet()),
      "BNE");

  static Command BPL = new Command(
      (register, bus, addressMode) ->
          branch(addressMode, register, bus, !register.isNegativeFlagSet()),
      "BPL");

  static Command BRK = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final Register updatedRegister = register
            .setDisableIrqFlag()
            .setBreakFlag()
            .incrementProgramCounter();
        pushToStack(register, updatedRegister.programCounter() & 0xFFFF, bus);
        return CommandResult.of(
            pushStatusToStack(updatedRegister, bus)
                .unsetBreakFlag()
                .programCounter(FORCE_BREAK_PROGRAM_COUNTER),
            addressResult.additionalCyclesNeeded());
      },
      "BRK");

  static Command BVC = new Command(
      (register, bus, addressMode) ->
          branch(addressMode, register, bus, !register.isOverflowFlagSet()),
      "BVC");

  static Command BVS = new Command(
      (register, bus, addressMode) ->
          branch(addressMode, register, bus, register.isOverflowFlagSet()),
      "BVS");

  static Command CLC = new Command(
      (register, bus, addressMode) -> CommandResult.of(register.unsetCarryFlag(), 0),
      "CLC");

  static Command CLD = new Command(
      (register, bus, addressMode) -> CommandResult.of(register.unsetDecimalModeFlag(), 0),
      "CLD");

  static Command CLI = new Command(
      (register, bus, addressMode) ->
          CommandResult.of(register.unsetDisableIrqFlag(), 0),
      "CLI");

  static Command CLV = new Command(
      (register, bus, addressMode) ->
          CommandResult.of(register.unsetOverflowFlag(), 0),
      "CLV");

  static Command CMP = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        return compare(addressResult, addressResult.register().a());
      },
      "CMP");

  static Command CPX = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        return compare(addressResult, addressResult.register().x());
      },
      "CPX");

  static Command CPY = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        return compare(addressResult, addressResult.register().y());
      },
      "CPY");

  static Command DEC = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final int valueDecremented = (addressResult.value() - 1) & 0xFF;
        bus.write(addressResult.address(), valueDecremented);
        return CommandResult.of(
            addressResult.register()
                .negativeFlag(isNegative(valueDecremented))
                .zeroFlag(valueDecremented == 0),
            addressResult.additionalCyclesNeeded());
      },
      "DEC");

  static Command DEX = new Command(
      (register, bus, addressMode) -> decrement((register.x() - 1) & 0xFF, register::x),
      "DEX");

  static Command DEY = new Command(
      (register, bus, addressMode) -> decrement((register.y() - 1) & 0xFF, register::y),
      "DEY");

  static Command EOR = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final Register updatedRegister = addressResult.register();
        final int newA = updatedRegister.a() ^ addressResult.value();
        return CommandResult.of(
            updatedRegister
                .a(newA)
                .negativeFlag(isNegative(newA))
                .zeroFlag(newA == 0),
            addressResult.additionalCyclesNeeded());
      },
      "EOR");

  static Command INC = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final int newValue = (addressResult.value() + 1) & 0xFF;
        bus.write(addressResult.address(), newValue);
        return CommandResult.of(
            addressResult.register()
                .negativeFlag(isNegative(newValue))
                .zeroFlag(newValue == 0),
            addressResult.additionalCyclesNeeded());
      },
      "INC");

  static Command INX = new Command(
      (register, bus, addressMode) -> increment((register.x() + 1) & 0xFF, register::x),
      "INX");

  static Command INY = new Command(
      (register, bus, addressMode) -> increment((register.y() + 1) & 0xFF, register::y),
      "INY");

  static Command JMP = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        return CommandResult.of(
            addressResult.register().programCounter(addressResult.address()),
            addressResult.additionalCyclesNeeded());
      },
      "JMP");

  static Command JSR = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final Register updatedRegister = addressResult.register().decrementProgramCounter();
        pushProgramCounterToStack(updatedRegister, bus);
        return CommandResult.of(
            updatedRegister.programCounter(addressResult.address()),
            addressResult.additionalCyclesNeeded());
      },
      "JSR");

  static Command LDA = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        return loadIntoRegister(
            addressResult.value(),
            addressResult.register()::a,
            addressResult.additionalCyclesNeeded());
      },
      "LDA");

  static Command LDX = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        return loadIntoRegister(
            addressResult.value(),
            addressResult.register()::x,
            addressResult.additionalCyclesNeeded());
      },
      "LDX");

  static Command LDY = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        return loadIntoRegister(
            addressResult.value(),
            addressResult.register()::y,
            addressResult.additionalCyclesNeeded());
      },
      "LDY");

  static Command LSR = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final int value = addressResult.value();
        final int result = (value >> 1) & 0xFF;
        final int address = addressResult.address();
        final Register updatedRegister =
            storeValueDependingOnAddress(address, result, addressResult.register(), bus);
        return CommandResult.of(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0)
                .carryFlag((value & 0x01) > 0),
            addressResult.additionalCyclesNeeded());
      },
      "LSR");

  static Command NOP = new Command(
      (register, bus, addressMode) -> CommandResult.of(register, 0),
      "NOP");

  static Command ORA = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        Register updatedRegister = addressResult.register();
        final int value = addressResult.value();
        final int result = value | updatedRegister.a();
        return CommandResult.of(
            updatedRegister
                .a(result)
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0),
            addressResult.additionalCyclesNeeded());
      },
      "ORA");

  static Command PHA = new Command(
      (register, bus, addressMode) -> {
        pushToStack(register, register.a(), bus);
        return CommandResult.of(register, 0);
      },
      "PHA");

  static Command PHP = new Command(
      (register, bus, addressMode) ->
          CommandResult.of(pushStatusToStack(register, bus), 0),
      "PHP");

  static Command PLA = new Command(
      (register, bus, addressMode) ->
          CommandResult.of(register.a(pullFromStack(register, bus)), 0),
      "PLA");

  static Command PLP = new Command(
      (register, bus, addressMode) ->
          CommandResult.of(register.status(pullFromStack(register, bus)), 0),
      "PLP");

  static Command ROL = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final int value = addressResult.value();
        final int rawResult = (value << 1) & ((value & 0x100) >> 4);
        final int result = rawResult & 0xFF;
        final int address = addressResult.address();
        final Register updatedRegister =
            storeValueDependingOnAddress(address, result, addressResult.register(), bus);
        return CommandResult.of(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0)
                .carryFlag(hasCarried(rawResult)),
            addressResult.additionalCyclesNeeded());
      },
      "ROL");

  static Command ROR = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final int value = addressResult.value();
        final int rawResult = (value >> 1) & ((value & 0x0001) << 4);
        final int result = rawResult & 0xFF;
        final int address = addressResult.address();
        final Register updatedRegister =
            storeValueDependingOnAddress(address, result, addressResult.register(), bus);
        return CommandResult.of(
            updatedRegister
                .negativeFlag(isNegative(result))
                .zeroFlag(result == 0)
                .carryFlag((value & 0x01) > 0),
            addressResult.additionalCyclesNeeded());
      },
      "ROR");

  static Command RTI = new Command(
      (register, bus, addressMode) ->
          CommandResult.of(
              pullStatusFromStack(pullProgramCounterFromStack(register, bus), bus)
                  .setUnusedFlag()
                  .unsetBreakFlag(),
              0),
      "RTI");

  static Command RTS = new Command(
      (register, bus, addressMode) ->
          CommandResult.of(
              pullProgramCounterFromStack(register, bus).incrementProgramCounter(),
              0),
      "RTS");

  static Command SBC = new Command(
      (register, bus, addressMode) -> {
        final AddressResult addressResult = addressMode.fetch(register, bus);
        final int a = register.a();
        final int value = addressResult.value() ^ 0x00FF;
        final int rawResult = a + value + (register.isCarryFlagSet() ? 1 : 0);
        final int result = rawResult & 0xFF;
        return CommandResult.of(
            addressResult.register()
                .a(result)
                .negativeFlag(isNegative(result))
                .overflowFlag(hasOverflown(a, value, rawResult))
                .zeroFlag(isZero(result))
                .carryFlag(hasCarried(rawResult)),
            addressResult.additionalCyclesNeeded());
      },
      "SBC");

  static Command SEC = new Command(
      (register, bus, addressMode) -> CommandResult.of(register.setCarryFlag(), 0),
      "SEC");

  static Command SED = new Command(
      (register, bus, addressMode) -> CommandResult.of(register.setDecimalModeFlag(), 0),
      "SED");

  static Command SEI = new Command(
      (register, bus, addressMode) -> CommandResult.of(register.setDisableIrqFlag(), 0),
      "SEI");

  static Command STA = new Command(
      (register, bus, addressMode) -> writeToBus(addressMode, register::a, bus, register),
      "STA");

  static Command STX = new Command(
      (register, bus, addressMode) -> writeToBus(addressMode, register::x, bus, register),
      "STX");

  static Command STY = new Command(
      (register, bus, addressMode) -> writeToBus(addressMode, register::y, bus, register),
      "STY");

  static Command TAX = new Command(
      (register, bus, addressMode) -> CommandResult.of(register.x(register.a()), 0),
      "TAX");

  static Command TAY = new Command(
      (register, bus, addressMode) -> CommandResult.of(register.y(register.a()), 0),
      "TAY");

  static Command TSX = new Command(
      (register, bus, addressMode) ->
          CommandResult.of(register.x(pullFromStack(register, bus)), 0),
      "TSX");

  static Command TXA = new Command(
      (register, bus, addressMode) -> CommandResult.of(register.a(register.x()), 0),
      "TXA");

  static Command TXS = new Command(
      (register, bus, addressMode) -> {
        pushToStack(register, register.x(), bus);
        return CommandResult.of(register, 0);
      },
      "TXS");

  static Command TYA = new Command(
      (register, bus, addressMode) -> CommandResult.of(register.a(register.y()), 0),
      "TYA");

  static Command UNKNOWN = new Command(
      (register, bus, addressMode) -> CommandResult.of(register, 0),
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
      AddressMode addressMode,
      Register register,
      CpuBus bus,
      boolean condition) {
    final AddressResult addressResult = addressMode.fetch(register, bus);
    final Register updatedRegister = addressResult.register();
    int additionalCyclesNeeded = addressResult.additionalCyclesNeeded();
    if (condition) {
      ++additionalCyclesNeeded;
      final int programCounter = updatedRegister.programCounter();
      final int newProgramCounter = addressResult.address();
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

  private static CommandResult compare(AddressResult addressResult, int existingValue) {
    final Register updatedRegister = addressResult.register();
    final int value = addressResult.value();
    return CommandResult.of(
        updatedRegister
            .negativeFlag(isNegative(value))
            .zeroFlag(value == existingValue)
            .carryFlag(existingValue >= value),
        addressResult.additionalCyclesNeeded());
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
      AddressMode addressMode,
      Supplier<Integer> registerGetter,
      CpuBus bus,
      Register register) {
    final AddressResult addressResult = addressMode.fetch(register, bus);
    final int address = addressResult.address();
    bus.write(address, registerGetter.get());
    return CommandResult.of(register, addressResult.additionalCyclesNeeded());
  }
}