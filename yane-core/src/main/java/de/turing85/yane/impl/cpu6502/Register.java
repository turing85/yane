package de.turing85.yane.impl.cpu6502;

import lombok.*;

@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
class Register {
  static final int STACK_POINTER_MASK = 0xFF;
  static final int PROGRAM_COUNTER_MASK = 0xFFFF;

  static final int NEGATIVE_MASK = 0x80;
  static final int OVERFLOW_MASK = 0x40;
  static final int UNUSED_MASK = 0x20;
  static final int BREAK_MASK = 0x10;
  static final int DECIMAL_MASK = 0x08;
  static final int DISABLE_IRQ_MASK = 0x04;
  static final int ZERO_MASK = 0x02;
  static final int CARRY_MASK = 0x01;

  private int a;
  private int x;
  private int y;

  @Getter(AccessLevel.NONE)
  private int stackPointer;

  @Getter(AccessLevel.NONE)
  private int programCounter;

  private int status;

  public Register(
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

  int programCounter() {
    return programCounter & PROGRAM_COUNTER_MASK;
  }

  int getAndIncrementProgramCounter() {
    return programCounter++ & PROGRAM_COUNTER_MASK;
  }

  Register incrementProgramCounter() {
    ++programCounter;
    return this;
  }

  Register decrementProgramCounter() {
    --programCounter;
    return this;
  }

  int stackPointer() {
    return stackPointer & STACK_POINTER_MASK;
  }

  int getAndDecrementStackPointer() {
    return stackPointer-- & STACK_POINTER_MASK;
  }

  Register decrementStackPointer() {
    --stackPointer;
    return this;
  }

  int incrementAndGetStackPointer() {
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

  final Register unusedFlag(boolean unusedFlag) {
    if (unusedFlag) {
      setUnusedFlag();
    } else {
      unsetUnusedFlag();
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