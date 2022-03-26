package de.turing85.yane.impl.cpu6502;

import lombok.*;

@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Register {
  private byte a;
  private byte x;
  private byte y;
  private byte stackPointer;
  private short programCounter;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private boolean carryFlag;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private boolean zeroFlag;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private boolean disableInterruptFlag;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private boolean decimalModeFlag;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private boolean breakFlag;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private boolean overflowFlag;

  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private boolean negativeFlag;

  short getAndIncrementProgramCounter() {
    return programCounter++;
  }

  Register setCarryFlag(){
    carryFlag = true;
    return this;
  }

  Register unsetCarryFlag() {
    carryFlag = false;
    return this;
  }

  boolean isCarryFlagSet() {
    return carryFlag;
  }

  Register setZeroFlag() {
    zeroFlag = true;
    return this;
  }

  Register unsetZeroFlag() {
    zeroFlag = false;
    return this;
  }

  boolean isZeroFlagSet() {
    return zeroFlag;
  }

  Register setDisableInterruptFlag() {
    disableInterruptFlag = true;
    return this;
  }

  Register unsetDisableInterruptFlag() {
    disableInterruptFlag = false;
    return this;
  }

  boolean isInterruptFlagSet() {
    return disableInterruptFlag;
  }

  Register setDecimalModeFlag() {
    decimalModeFlag = true;
    return this;
  }

  Register unsetDecimalModeFlag() {
    decimalModeFlag = false;
    return this;
  }
  boolean isDecimalModeFlagSet() {
    return decimalModeFlag;
  }

  Register setBreakFlag() {
    breakFlag = true;
    return this;
  }

  Register unsetBreakFlag() {
    breakFlag = false;
    return this;
  }

  boolean isBreakFlagSet() {
    return breakFlag;
  }

  Register setOverflowFlag() {
    overflowFlag = true;
    return this;
  }

  Register unsetOverflowFlag() {
    overflowFlag = false;
    return this;
  }

  boolean isOverflowFlagSet() {
    return overflowFlag;
  }

  Register setNegativeFlag() {
    negativeFlag = true;
    return this;
  }

  Register unsetNegativeFlag() {
    negativeFlag = false;
    return this;
  }

  boolean isNegativeFlagSet() {
    return negativeFlag;
  }

}
