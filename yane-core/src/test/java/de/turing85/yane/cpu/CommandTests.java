package de.turing85.yane.cpu;

import static com.google.common.truth.Truth.*;

import org.junit.jupiter.api.*;

@DisplayName("Command tests")
class CommandTests {

  @Nested
  @DisplayName("ADC command tests")
  class AdcTests {
    @Test
    @DisplayName("adds value to accumulator")
    void addsValueToAccumulator() {
      // GIVEN
      final int a = 0x01;
      final int value = 0x02;
      final int sum = 0x03;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult =
          new AddressingResult(register, null, AddressingMode.IMPLIED_LOADED_ADDRESS, value);

      // WHEN
      CommandResult result = Command.ADC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(sum);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("takes carry into account")
    void takesCarryIntoAccount() {
      // GIVEN
      final int a = 0x01;
      final int value = 0x02;
      final int sum = 0x04;
      final Register register = Register.of()
          .a(a)
          .setCarryFlag();
      final AddressingResult addressingResult =
          new AddressingResult(register, null, AddressingMode.IMPLIED_LOADED_ADDRESS, value);

      // WHEN
      CommandResult result = Command.ADC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(sum);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero and carry")
    void detectsZeroAndCarry() {
      // GIVEN
      final int a = 0xFF;
      final int value = 0x01;
      final int sum = 0x00;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult =
          new AddressingResult(register, null, Integer.MIN_VALUE, value);

      // WHEN
      CommandResult result = Command.ADC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(sum);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }

    @Test
    @DisplayName("detects overflow and carry")
    void detectsOverflowAndCarry() {
      // GIVEN
      final int a = 0x80;
      final int value = 0xFF;
      final int sum = 0x7F;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult =
          new AddressingResult(register, null, Integer.MIN_VALUE, value);

      // WHEN
      CommandResult result = Command.ADC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(sum);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isOverflowFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int a = 0xFE;
      final int value = 0x01;
      final int sum = 0xFF;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult =
          new AddressingResult(register, null, Integer.MIN_VALUE, value);

      // WHEN
      CommandResult result = Command.ADC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(sum);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }
  }

  @Nested
  @DisplayName("AND command tests")
  class AndTests {
    @Test
    @DisplayName("ands value to accumulator")
    void andsValueToAccumulator() {
      // GIVEN
      final int a = 0x7F;
      final int value = 0xF7;
      final int expected = 0x77;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult result = Command.AND.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int a = 0xEF;
      final int value = 0xFE;
      final int expected = 0xEE;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult result = Command.AND.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int a = 0x07;
      final int value = 0x70;
      final int expected = 0x00;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult result = Command.AND.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("ASL command tests")
  class AslTests {
    @Test
    @DisplayName("shifts value in accumulator")
    void shiftsValueInAccumulator(){
      // GIVEN
      final int a = 0x3F;
      final int expected = 0x7E;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult =
          new AddressingResult(register, null, AddressingMode.IMPLIED_LOADED_ADDRESS, a);

      // WHEN
      CommandResult actual = Command.ASL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("shifts value on bus")
    void shiftsValueOnBus(){
      // GIVEN
      final int value = 0x3F;
      final int expected = 0x7E;
      final int address = 0x1337;
      final Register register = Register.of();
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, value);

      // WHEN
      CommandResult actual = Command.ASL.execute(addressingResult);

      // THEN
      final Bus actualBus = actual.bus();
      assertThat(actualBus.read(address)).isEqualTo(expected);
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(0);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative(){
      // GIVEN
      final int a = 0x7F;
      final int expected = 0xFE;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult =
          new AddressingResult(register, null, AddressingMode.IMPLIED_LOADED_ADDRESS, a);

      // WHEN
      CommandResult actual = Command.ASL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero(){
      // GIVEN
      final int a = 0x00;
      final int expected = 0x00;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult =
          new AddressingResult(register, null, AddressingMode.IMPLIED_LOADED_ADDRESS, a);

      // WHEN
      CommandResult actual = Command.ASL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects carry")
    void detectsCarry(){
      // GIVEN
      final int a = 0x81;
      final int expected = 0x02;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult =
          new AddressingResult(register, null, AddressingMode.IMPLIED_LOADED_ADDRESS, a);

      // WHEN
      CommandResult actual = Command.ASL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("BCC command tests")
  class BccTests {
    @Test
    @DisplayName("does branch")
    void doesBranch() {
      final int address = 0x0042;
      final Register register = Register.of();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BCC.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0142;
      final Register register = Register.of();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BCC.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0142;
      final Register register = Register.of().setCarryFlag();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BCC.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(additionalCyclesForAddressing);
    }
  }

  @Nested
  @DisplayName("BCS command tests")
  class BcsTests {
    @Test
    @DisplayName("does branch")
    void doesBranch() {
      final int address = 0x0042;
      final Register register = Register.of().setCarryFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BCS.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0142;
      final Register register = Register.of().setCarryFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BCS.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0142;
      final Register register = Register.of();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BCS.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(additionalCyclesForAddressing);
    }
  }

  @Nested
  @DisplayName("BEQ command tests")
  class BeqTests {
    @Test
    @DisplayName("does branch")
    void doesBranch() {
      final int address = 0x0042;
      final Register register = Register.of().setZeroFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BEQ.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0142;
      final Register register = Register.of().setZeroFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BEQ.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0142;
      final Register register = Register.of();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BEQ.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(additionalCyclesForAddressing);
    }
  }

  @Nested
  @DisplayName("BIT command tests")
  class BitTests {
    @Test
    @DisplayName("Sets negative flag")
    void setsNegativeFlag() {
      // GIVEN
      final int a = 0x0F;
      final int value = 0x8F;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.BIT.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("Sets overflow flag")
    void setsOverflowFlag() {
      // GIVEN
      final int a = 0x0F;
      final int value = 0x4F;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.BIT.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isOverflowFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("Sets zero flag")
    void setsZeroFlag() {
      // GIVEN
      final int a = 0xF0;
      final int value = 0x0F;
      final Register register = Register.of()
          .a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.BIT.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("BMI command tests")
  class BmiTests {
    @Test
    @DisplayName("does branch")
    void doesBranch() {
      final int address = 0x0042;
      final Register register = Register.of().setNegativeFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BMI.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0142;
      final Register register = Register.of().setNegativeFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BMI.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0142;
      final Register register = Register.of();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BMI.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(additionalCyclesForAddressing);
    }
  }

  @Nested
  @DisplayName("BNE command tests")
  class BneTests {
    @Test
    @DisplayName("does branch")
    void doesBranch() {
      final int address = 0x0042;
      final Register register = Register.of();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BNE.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0142;
      final Register register = Register.of();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BNE.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0142;
      final Register register = Register.of().setZeroFlag();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BNE.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(additionalCyclesForAddressing);
    }
  }

  @Nested
  @DisplayName("BPL command tests")
  class BplTests {
    @Test
    @DisplayName("does branch")
    void doesBranch() {
      final int address = 0x0042;
      final Register register = Register.of();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BPL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0142;
      final Register register = Register.of();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BPL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0142;
      final Register register = Register.of().setNegativeFlag();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      CommandResult actual = Command.BPL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(additionalCyclesForAddressing);
    }
  }
}