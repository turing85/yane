package de.turing85.yane.impl.cpu6502;

import static de.turing85.yane.impl.cpu6502.AddressingModeFunction.*;
import static de.turing85.yane.impl.cpu6502.Register.*;

import de.turing85.yane.api.*;
import java.util.function.*;

public interface Command {
  int FORCE_BREAK_PROGRAM_COUNTER = 0xFFFFFFFE;

  Command ADC = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    final int a = register.a();
    final int value = addressingResult.valueRead();
    final int rawResult = a + value + (register.isCarryFlagSet() ? 1 : 0);
    final int result = rawResult & 0xFFFF;

    return CommandResult.of(
        register
            .a(result)
            .negativeFlag(isNegative(rawResult))
            .zeroFlag(isZero(result))
            .carryFlag(hasCarried(rawResult))
            .overflowFlag(hasOverflown(a, value, rawResult)),
        addressingResult.additionalCyclesNeeded());
  };

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
    return (~(lhs ^ rhs) & (lhs ^ result)) > 0;
  }

  Command AND = (register, bus, addressMode) -> {
    final AddressingResult addressingResult = addressMode.apply(register, bus);
    final Register updatedRegister = addressingResult.register();
    final int a = updatedRegister.a();
    final int value = addressingResult.valueRead();
    final int rawResult = a & value;
    final int result = rawResult & 0xFF;

    return CommandResult.of(
        updatedRegister
            .a(result)
            .negativeFlag(isNegative(rawResult))
            .zeroFlag(result == 0),
        addressingResult.additionalCyclesNeeded());
  };

  Command ASL = (register, bus, addressMode) -> {
    final AddressingResult addressingResult = addressMode.apply(register, bus);
    final int value = addressingResult.valueRead();
    final int rawResult = value << 1;
    final int result = rawResult & 0xFF;
    final int address = addressingResult.addressLoaded();
    Register updatedRegister =
        storeResultDependingOnAddress(address, result, addressingResult.register(), bus);
    return CommandResult.of(
        updatedRegister
            .negativeFlag(isNegative(result))
            .zeroFlag(isZero(result))
            .carryFlag(hasCarried(rawResult)),
        addressingResult.additionalCyclesNeeded());
  };

  Command BCC = (register, bus, addressMode) ->
      branch(addressMode, register, bus, !register.isCarryFlagSet());

  private static CommandResult branch(
      AddressingModeFunction addressingMode,
      Register register,
      CpuBus bus,
      boolean condition) {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    final Register updatedRegister = addressingResult.register();
    int additionalCyclesNeeded = addressingResult.additionalCyclesNeeded();
    if (condition) {
      ++additionalCyclesNeeded;
      final int programCounter = updatedRegister.programCounter();
      final int newProgramCounter = addressingResult.addressLoaded();
      if (countersAreOnDifferentPages(programCounter, newProgramCounter)) {
        ++additionalCyclesNeeded;
      }
    }
    return CommandResult.of(updatedRegister, additionalCyclesNeeded);
  }

  private static boolean countersAreOnDifferentPages(int lhs, int rhs) {
    return (lhs & 0xFF00) != (rhs & 0xFF00);
  }

  Command BCS = (register, bus, addressMode) ->
      branch(addressMode, register, bus, register.isCarryFlagSet());

  Command BEQ = (register, bus, addressingMode) ->
      branch(addressingMode, register, bus, register.isZeroFlagSet());

  Command BIT = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    final int value = addressingResult.valueRead();
    return CommandResult.of(
        addressingResult.register()
            .negativeFlag(isNegative(value))
            .overflowFlag((value & OVERFLOW_MASK) > 0)
            .zeroFlag((value & register.a()) == 0),
        addressingResult.additionalCyclesNeeded());
  };

  Command BMI = (register, bus, addressingMode) ->
      branch(addressingMode, register, bus, register.isNegativeFlagSet());

  Command BNE = (register, bus, addressingMode) ->
      branch(addressingMode, register, bus, !register.isZeroFlagSet());

  Command BPL = (register, bus, addressingMode) ->
      branch(addressingMode, register, bus, !register.isNegativeFlagSet());

  Command BRK = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    final Register updatedRegister = register
        .setDisableInterruptFlag()
        .setBreakFlag();
    final int returnAddress = (updatedRegister.programCounter() + 1) & 0xFFFF;
    pushToStack(register, returnAddress, bus);

    return CommandResult.of(
        pushStatusToStack(updatedRegister, bus)
            .unsetBreakFlag()
            .programCounter(FORCE_BREAK_PROGRAM_COUNTER),
        addressingResult.additionalCyclesNeeded());
  };

  private static void pushProgramCounterToStack(Register register, CpuBus bus) {
    pushToStack(register, (register.programCounter() >> 8 & 0xFF), bus);
    pushToStack(register, register.programCounter() & 0xFF, bus);
  }

  private static Register pushStatusToStack(Register register, CpuBus bus) {
    final Register updatedRegister = register.setUnusedFlag();
    pushToStack(register, updatedRegister.status(), bus);
    return updatedRegister;
  }

  private static void pushToStack(Register register, int value, CpuBus bus) {
    bus.write(register.getAndDecrementStackPointer(), value & 0xFF);
  }

  Command BVC = (register, bus, addressingMode) ->
      branch(addressingMode, register, bus, !register.isOverflowFlagSet());

  Command BVS = (register, bus, addressingMode) ->
      branch(addressingMode, register, bus, register.isOverflowFlagSet());

  Command CLC = (register, bus, addressingMode) ->
      CommandResult.of(register.unsetCarryFlag(), 0);

  Command CLD = (register, bus, addressingMode) ->
      CommandResult.of(register.unsetDecimalModeFlag(), 0);

  Command CLI = (register, bus, addressingMode) ->
      CommandResult.of(register.unsetDisableInterruptFlag(), 0);

  Command CLV = (register, bus, addressingMode) ->
      CommandResult.of(register.unsetOverflowFlag(), 0);

  Command CMP = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    return compare(addressingResult, addressingResult.register().a());
  };

  private static CommandResult compare(AddressingResult addressingResult, int existingValue) {
    final Register updatedRegister = addressingResult.register();
    final int value = addressingResult.valueRead();
    return CommandResult.of(
        updatedRegister
            .negativeFlag(isNegative(value))
            .zeroFlag(value == existingValue)
            .carryFlag(existingValue >= value),
        addressingResult.additionalCyclesNeeded());
  }

  Command CPX = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    return compare(addressingResult, addressingResult.register().x());
  };

  Command CPY = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    return compare(addressingResult, addressingResult.register().y());
  };

  Command DEC = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    final int valueDecremented = (addressingResult.valueRead() - 1) & 0xFF;
    bus.write(addressingResult.addressLoaded(), valueDecremented);
    return CommandResult.of(
        addressingResult.register()
            .negativeFlag(isNegative(valueDecremented))
            .zeroFlag(valueDecremented == 0),
        addressingResult.additionalCyclesNeeded());
  };

  Command DEX = (register, bus, addressingMode) ->
      decrement((register.x() - 1) & 0xFF, register::x);

  private static CommandResult decrement(int newValue, Function<Integer, Register> registerSetter) {
    return CommandResult.of(
        registerSetter.apply(newValue)
            .negativeFlag(isNegative(newValue))
            .zeroFlag(newValue == 0),
        0);
  }

  Command DEY = (register, bus, addressingMode) ->
      decrement((register.y() - 1) & 0xFF, register::y);

  Command EOR = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    final Register updatedRegister = addressingResult.register();
    final int newA = updatedRegister.a() ^ addressingResult.valueRead();
    return CommandResult.of(
        updatedRegister
            .a(newA)
            .negativeFlag(isNegative(newA))
            .zeroFlag(newA == 0),
        addressingResult.additionalCyclesNeeded());
  };

  Command INC = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    final int newValue = (addressingResult.valueRead() + 1) & 0xFF;
    bus.write(addressingResult.addressLoaded(), newValue);
    return CommandResult.of(
        addressingResult.register()
            .negativeFlag(isNegative(newValue))
            .zeroFlag(newValue == 0),
        addressingResult.additionalCyclesNeeded());
  };

  Command INX = (register, bus, addressingMode) ->
      increment((register.x() + 1) & 0xFF, register::x);

  private static CommandResult increment(int newValue, Function<Integer, Register> registerSetter) {
    return CommandResult.of(
        registerSetter.apply(newValue)
            .negativeFlag(isNegative(newValue))
            .zeroFlag(newValue == 0),
        0);
  }

  Command INY = (register, bus, addressingMode) ->
      increment((register.y() + 1) & 0xFF, register::y);

  Command JMP = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    return CommandResult.of(
        addressingResult.register()
            .programCounter(addressingResult.addressLoaded()),
        addressingResult.additionalCyclesNeeded());
  };

  Command JSR = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    Register updatedRegister = addressingResult.register()
        .decrementProgramCounter();
    pushProgramCounterToStack(updatedRegister, bus);
    return CommandResult.of(
        updatedRegister.programCounter(addressingResult.addressLoaded()),
        addressingResult.additionalCyclesNeeded());
  };

  Command LDA = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    return loadIntoRegister(
        addressingResult.valueRead(),
        addressingResult.register()::a,
        addressingResult.additionalCyclesNeeded());
  };

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

  Command LDX = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    return loadIntoRegister(
        addressingResult.valueRead(),
        addressingResult.register()::x,
        addressingResult.additionalCyclesNeeded());
  };

  Command LDY = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    return loadIntoRegister(
        addressingResult.valueRead(),
        addressingResult.register()::y,
        addressingResult.additionalCyclesNeeded());
  };

  Command LSR = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    final int value = addressingResult.valueRead();
    final int result = (value >> 1) & 0xFF;
    final int address = addressingResult.addressLoaded();
    final Register updatedRegister =
        storeResultDependingOnAddress(address, result, addressingResult.register(), bus);
    return CommandResult.of(
        updatedRegister
            .negativeFlag(isNegative(result))
            .zeroFlag(result == 0)
            .carryFlag((value & 0x01) > 0),
        addressingResult.additionalCyclesNeeded());
  };

  Command NOP = (register, bus, addressingMode) -> CommandResult.of(register, 0);

  Command ORA = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    Register updatedRegister = addressingResult.register();
    final int value = addressingResult.valueRead();
    final int result = value | updatedRegister.a();
    return CommandResult.of(
        updatedRegister
            .a(result)
            .negativeFlag(isNegative(result))
            .zeroFlag(result == 0),
        addressingResult.additionalCyclesNeeded());
  };

  Command PHA = (register, bus, addressingMode) -> {
    pushToStack(register, register.a(), bus);
    return CommandResult.of(register, 0);
  };

  Command PHP = (register, bus, addressingMode) ->
      CommandResult.of(pushStatusToStack(register, bus), 0);

  Command PLA = (register, bus, addressingMode) ->
      CommandResult.of(register.a(pullFromStack(register, bus)), 0);

  static int pullFromStack(Register register, CpuBus bus) {
    return bus.read(register.incrementAndGetStackPointer());
  }

  Command PLP = (register, bus, addressingMode) ->
      CommandResult.of(register.status(pullFromStack(register, bus)), 0);

  Command ROL = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    final int value = addressingResult.valueRead();
    final int rawResult = (value << 1) & ((value & 0x100) >> 4);
    final int result = rawResult & 0xFF;
    final int address = addressingResult.addressLoaded();
    final Register updatedRegister =
        storeResultDependingOnAddress(address, result, addressingResult.register(), bus);
    return CommandResult.of(
        updatedRegister
            .negativeFlag(isNegative(result))
            .zeroFlag(result == 0)
            .carryFlag(hasCarried(rawResult)),
        addressingResult.additionalCyclesNeeded());
  };

  private static Register storeResultDependingOnAddress(
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

  Command ROR = (register, bus, addressingMode) -> {
    final AddressingResult addressingResult = addressingMode.apply(register, bus);
    final int value = addressingResult.valueRead();
    final int rawResult = (value >> 1) & ((value & 0x0001) << 4);
    final int result = rawResult & 0xFF;
    final int address = addressingResult.addressLoaded();
    final Register updatedRegister =
        storeResultDependingOnAddress(address, result, addressingResult.register(), bus);
    return CommandResult.of(
        updatedRegister
            .negativeFlag(isNegative(result))
            .zeroFlag(result == 0)
            .carryFlag((value & 0x01) > 0),
        addressingResult.additionalCyclesNeeded());
  };

  Command RTI = (register, bus, addressMode) ->
      CommandResult.of(
          pullStatusFromStack(pullProgramCounterFromStack(register, bus), bus)
              .setUnusedFlag()
              .unsetBreakFlag(),
          0);

  private static Register pullStatusFromStack(Register register, CpuBus bus) {
    return register
        .status(pullFromStack(register, bus))
        .unsetUnusedFlag()
        .unsetBreakFlag();
  }

  private static Register pullProgramCounterFromStack(Register register, CpuBus bus) {
    final int programCounterLow = pullFromStack(register, bus) & 0xFF;
    final int programCounterHigh = (pullFromStack(register, bus) & 0xFF) << 8;
    return register.programCounter(programCounterLow | programCounterHigh);
  }

  Command RTS = (register, bus, addressMode) ->
      CommandResult.of(
          pullProgramCounterFromStack(register, bus)
              .incrementProgramCounter(),
          0);

  Command SBC = (register, bus, addressMode) -> {
    final AddressingResult addressingResult = addressMode.apply(register, bus);
    final int a = register.a();
    final int value = addressingResult.valueRead() ^ 0x00FF;
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
  };

  Command SEC = (register, bus, addressMode) -> CommandResult.of(register.setCarryFlag(), 0);

  Command SED = (register, bus, addressMode) -> CommandResult.of(register.setDecimalModeFlag(), 0);

  Command SEI = (register, bus, addressMode) ->
      CommandResult.of(register.setDisableInterruptFlag(), 0);

  Command STA = (register, bus, addressMode) -> store(addressMode, register::a, bus, register);

  private static CommandResult store(
      AddressingModeFunction addressMode,
      Supplier<Integer> registerGetter,
      CpuBus bus,
      Register register) {
    final AddressingResult addressingResult = addressMode.apply(register, bus);
    final int address = addressingResult.addressLoaded();
    bus.write(address, registerGetter.get());
    return CommandResult.of(register, addressingResult.additionalCyclesNeeded());
  }

  Command STX = (register, bus, addressMode) -> store(addressMode, register::x, bus, register);

  Command STY = (register, bus, addressMode) -> store(addressMode, register::y, bus, register);

  Command TAX = (register, bus, addressMode) -> CommandResult.of(register.x(register.a()), 0);

  Command TAY = (register, bus, addressMode) -> CommandResult.of(register.y(register.a()), 0);

  Command TSX = (register, bus, addressMode) ->
      CommandResult.of(register.x(pullFromStack(register, bus)), 0);

  Command TXA = (register, bus, addressMode) -> CommandResult.of(register.a(register.x()), 0);

  Command TXS = (register, bus, addressMode) -> {
    pushToStack(register, register.x(), bus);
    return CommandResult.of(register, 0);
  };

  Command TYA = (register, bus, addressMode) -> CommandResult.of(register.a(register.y()), 0);

  CommandResult execute(Register register, CpuBus bus, AddressingModeFunction addressingMode);
}
