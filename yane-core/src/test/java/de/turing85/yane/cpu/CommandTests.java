package de.turing85.yane.cpu;

import static com.google.common.truth.Truth.*;
import static de.turing85.yane.cpu.Bus.*;
import static de.turing85.yane.cpu.Register.*;

import org.junit.jupiter.api.*;

@DisplayName("Command tests")
class CommandTests {

  @Nested
  @DisplayName("ADC command tests")
  class AdcTests {
    @Test
    @DisplayName("adds accumulator to itself")
    void addsValueToAccumulator() {
      // GIVEN
      final int a = 0x01;
      final int expected = 0x02;
      final Register register = Register.of().a(a).unsetCarryFlag();
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult result = Command.ADC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(expected);
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
      final int expected = 0x04;
      final Register register = Register.of().a(a).setCarryFlag();
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult result = Command.ADC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(expected);
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
      final int expected = 0x00;
      final Register register = Register.of().a(a).unsetCarryFlag();
      final AddressingResult addressingResult =
          new AddressingResult(register, null, Integer.MIN_VALUE, value);

      // WHEN
      final CommandResult result = Command.ADC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(expected);
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
      final int expected = 0x7F;
      final Register register = Register.of().a(a).unsetCarryFlag();
      final AddressingResult addressingResult =
          new AddressingResult(register, null, Integer.MIN_VALUE, value);

      // WHEN
      final CommandResult result = Command.ADC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(expected);
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
      final int expected = 0xFF;
      final Register register = Register.of().a(a).unsetCarryFlag();
      final AddressingResult addressingResult =
          new AddressingResult(register, null, Integer.MIN_VALUE, value);

      // WHEN
      final CommandResult result = Command.ADC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(expected);
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
      final Register register = Register.of().a(a).unsetCarryFlag();
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
      final Register register = Register.of().a(a).unsetCarryFlag();
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
      final Register register = Register.of().a(a).unsetCarryFlag();
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
    void shiftsValueInAccumulator() {
      // GIVEN
      final int a = 0x3F;
      final int expected = 0x7E;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.ASL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("shifts value on bus")
    void shiftsValueOnBus() {
      // GIVEN
      final int value = 0x3F;
      final int expected = 0x7E;
      final int address = 0x0012;
      final Register register = Register.of();
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, value);

      // WHEN
      final CommandResult actual = Command.ASL.execute(addressingResult);

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
    void detectsNegative() {
      // GIVEN
      final int a = 0x7F;
      final int expected = 0xFE;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.ASL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int a = 0x00;
      final int expected = 0x00;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.ASL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects carry")
    void detectsCarry() {
      // GIVEN
      final int a = 0x81;
      final int expected = 0x02;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.ASL.execute(addressingResult);

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
      final int address = 0x0012;
      final Register register = Register.of().unsetCarryFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BCC.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0112;
      final Register register = Register.of().unsetCarryFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BCC.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0112;
      final Register register = Register.of().setCarryFlag();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BCC.execute(addressingResult);

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
      final int address = 0x0012;
      final Register register = Register.of().setCarryFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BCS.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0112;
      final Register register = Register.of().setCarryFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BCS.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0112;
      final Register register = Register.of().unsetCarryFlag();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BCS.execute(addressingResult);

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
      final int address = 0x0012;
      final Register register = Register.of().setZeroFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BEQ.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0112;
      final Register register = Register.of().setZeroFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BEQ.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register().unsetZeroFlag();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0112;
      final Register register = Register.of();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BEQ.execute(addressingResult);

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
      final Register register = Register.of().a(a);
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
      final Register register = Register.of().a(a);
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
      final Register register = Register.of().a(a);
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
      final int address = 0x0012;
      final Register register = Register.of().setNegativeFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BMI.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0112;
      final Register register = Register.of().setNegativeFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BMI.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0112;
      final Register register = Register.of().unsetNegativeFlag();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BMI.execute(addressingResult);

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
      final int address = 0x0012;
      final Register register = Register.of().unsetZeroFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BNE.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0112;
      final Register register = Register.of().unsetZeroFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BNE.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0112;
      final Register register = Register.of().setZeroFlag();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BNE.execute(addressingResult);

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
      final int address = 0x0012;
      final Register register = Register.of().unsetNegativeFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BPL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0112;
      final Register register = Register.of().unsetNegativeFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BPL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0112;
      final Register register = Register.of().setNegativeFlag();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BPL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(additionalCyclesForAddressing);
    }
  }

  @Nested
  @DisplayName("BRK command tests")
  class BrkTests {
    @Test
    @DisplayName("breaks")
    void breaks() {
      // GIVEN
      final int address = 0x0012;
      final int addressLow = address & VALUE_MASK;
      final int addressHigh = address >> 8;
      final int stackPointer = 0x17;
      final int programCounter = 0xABCD;
      final int nextProgramCounter = programCounter + 1;
      final int nextProgramCounterLow = nextProgramCounter & VALUE_MASK;
      final int nextProgramCounterHigh = nextProgramCounter >> 8;
      final Register register = Register.of()
          .programCounter(programCounter)
          .stackPointer(stackPointer);
      final int initialStackPointer = register.stackPointer();
      final int expectedStatusOnStack = register.status() | BREAK_MASK | DISABLE_IRQ_MASK;
      final Bus bus = new Bus()
          .write(RESET_VECTOR, addressLow)
          .write(RESET_VECTOR + 1, addressHigh);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, bus);

      // WHEN
      final CommandResult actual = Command.BRK.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actualRegister.stackPointer()).isEqualTo(stackPointer - 3);
      assertThat(actualRegister.isBreakFlagSet()).isFalse();
      assertThat(actualRegister.isDisableIrqFlagSet()).isTrue();
      final Bus actualBus = actual.bus();
      assertThat(actualBus.readFromStack(initialStackPointer)).isEqualTo(nextProgramCounterHigh);
      assertThat(actualBus.readFromStack(initialStackPointer - 1)).isEqualTo(nextProgramCounterLow);
      assertThat(actualBus.readFromStack(initialStackPointer - 2))
          .isEqualTo(expectedStatusOnStack);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("BVC command tests")
  class BvcTests {
    @Test
    @DisplayName("does branch")
    void doesBranch() {
      final int address = 0x0012;
      final Register register = Register.of().unsetOverflowFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BVC.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0112;
      final Register register = Register.of().unsetOverflowFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BVC.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0112;
      final Register register = Register.of().setOverflowFlag();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BVC.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(additionalCyclesForAddressing);
    }
  }

  @Nested
  @DisplayName("BVS command tests")
  class BvsTests {
    @Test
    @DisplayName("does branch")
    void doesBranch() {
      final int address = 0x0012;
      final Register register = Register.of().setOverflowFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 1;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BVS.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does branch to different memory page")
    void doesBranchToDifferentMemoryPage() {
      final int address = 0x0112;
      final Register register = Register.of().setOverflowFlag();
      final int additionalCyclesForAddressing = 2;
      final int expectedAdditionalCyclesNeeded = additionalCyclesForAddressing + 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BVS.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(expectedAdditionalCyclesNeeded);
    }

    @Test
    @DisplayName("does not branch")
    void doesNotBranch() {
      final int address = 0x0112;
      final Register register = Register.of().unsetOverflowFlag();
      final int additionalCyclesForAddressing = 2;
      final AddressingResult addressingResult =
          new AddressingResult(register, null, address, 0, additionalCyclesForAddressing);

      // WHEN
      final CommandResult actual = Command.BVS.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(additionalCyclesForAddressing);
    }
  }


  @Nested
  @DisplayName("CLC command tests")
  class ClcTests {
    @Test
    @DisplayName("clears bit")
    void clearsBit() {
      // GIVEN
      final Register register = Register.of()
          .setCarryFlag();
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.CLC.execute(addressingResult);

      // THEN
      assertThat(actual.register().isCarryFlagSet()).isFalse();
    }
  }

  @Nested
  @DisplayName("CLD command tests")
  class CldTests {
    @Test
    @DisplayName("clears bit")
    void clearsBit() {
      // GIVEN
      final Register register = Register.of()
          .setDecimalModeFlag();
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.CLD.execute(addressingResult);

      // THEN
      assertThat(actual.register().isDecimalModeFlagSet()).isFalse();
    }
  }

  @Nested
  @DisplayName("CLI command tests")
  class CliTests {
    @Test
    @DisplayName("clears bit")
    void clearsBit() {
      // GIVEN
      final Register register = Register.of()
          .setDisableIrqFlag();
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.CLI.execute(addressingResult);

      // THEN
      assertThat(actual.register().isDisableIrqFlagSet()).isFalse();
    }
  }

  @Nested
  @DisplayName("CLV command tests")
  class ClvTests {
    @Test
    @DisplayName("clears bit")
    void clearsBit() {
      // GIVEN
      final Register register = Register.of()
          .setOverflowFlag();
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.CLV.execute(addressingResult);

      // THEN
      assertThat(actual.register().isOverflowFlagSet()).isFalse();
    }
  }

  @Nested
  @DisplayName("CMP command tests")
  class CmpTests {
    @Test
    @DisplayName("value > accumulator")
    void valueLargerThanAccumulator() {
      // GIVEN
      final int a = 0x01;
      final int value = 0x02;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.CMP.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("value == accumulator")
    void valueEqualToAccumulator() {
      // GIVEN
      final int a = 0x01;
      final int value = 0x01;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.CMP.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }

    @Test
    @DisplayName("value < accumulator")
    void valueSmallerAccumulator() {
      // GIVEN
      final int a = 0x01;
      final int value = 0x00;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.CMP.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("CPX command tests")
  class CpxTests {
    @Test
    @DisplayName("value > x register")
    void valueLargerThanX() {
      // GIVEN
      final int x = 0x01;
      final int value = 0x02;
      final Register register = Register.of().x(x);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.CPX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("value == x")
    void valueEqualToX() {
      // GIVEN
      final int x = 0x01;
      final int value = 0x01;
      final Register register = Register.of().x(x);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.CPX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }

    @Test
    @DisplayName("value < x")
    void valueSmallerX() {
      // GIVEN
      final int x = 0x01;
      final int value = 0x00;
      final Register register = Register.of().x(x);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.CPX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("CPY command tests")
  class CpyTests {
    @Test
    @DisplayName("value > y register")
    void valueLargerThanX() {
      // GIVEN
      final int y = 0x01;
      final int value = 0x02;
      final Register register = Register.of().y(y);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.CPY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("value == y register")
    void valueEqualToY() {
      // GIVEN
      final int y = 0x01;
      final int value = 0x01;
      final Register register = Register.of().y(y);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.CPY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }

    @Test
    @DisplayName("value < y register")
    void valueSmallerY() {
      // GIVEN
      final int y = 0x01;
      final int value = 0x00;
      final Register register = Register.of().y(y);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.CPY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("DEC command tests")
  class DecTests {
    @Test
    @DisplayName("decrements value")
    void decrementsValue() {
      // GIVEN
      final int address = 0x0012;
      final int value = 0x13;
      final Register register = Register.of();
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, value);

      // WHEN
      final CommandResult actual = Command.DEC.execute(addressingResult);

      // THEN
      assertThat(actual.bus().read(address)).isEqualTo(value - 1);
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int address = 0x0012;
      final int value = 0x8F;
      final Register register = Register.of();
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, value);

      // WHEN
      final CommandResult actual = Command.DEC.execute(addressingResult);

      // THEN
      assertThat(actual.bus().read(address)).isEqualTo(value - 1);
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int address = 0x0012;
      final int value = 0x01;
      final Register register = Register.of();
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, value);

      // WHEN
      final CommandResult actual = Command.DEC.execute(addressingResult);

      // THEN
      assertThat(actual.bus().read(address)).isEqualTo(value - 1);
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("DEX command tests")
  class DexTests {
    @Test
    @DisplayName("decrements X register")
    void decrementsValue() {
      // GIVEN
      final int x = 0x13;
      final Register register = Register.of().x(x);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.DEX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(x - 1);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int x = 0x8F;
      final Register register = Register.of().x(x);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.DEX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(x - 1);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int x = 0x01;
      final Register register = Register.of().x(x);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.DEX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(x - 1);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("DEY command tests")
  class DeyTests {
    @Test
    @DisplayName("decrements Y register")
    void decrementsValue() {
      // GIVEN
      final int y = 0x13;
      final Register register = Register.of().y(y);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.DEY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.y()).isEqualTo(y - 1);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int y = 0x8F;
      final Register register = Register.of().y(y);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.DEY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.y()).isEqualTo(y - 1);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int y = 0x01;
      final Register register = Register.of().y(y);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.DEY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.y()).isEqualTo(y - 1);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("EOR command tess")
  class EorTests {
    @Test
    @DisplayName("xor-s value with accumulator")
    void xorWithAccumulator() {
      // GIVEN
      final int a = 0xFC;
      final int value = 0xF8;
      final int expected = 0x04;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.EOR.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int a = 0xFF;
      final int value = 0x70;
      final int expected = 0x8F;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.EOR.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int a = 0x01;
      final int value = 0x01;
      final int expected = 0x00;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.EOR.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }


  @Nested
  @DisplayName("INC command tests")
  class IncTests {
    @Test
    @DisplayName("increments value")
    void incrementsValue() {
      // GIVEN
      final int address = 0x0012;
      final int value = 0x13;
      final Register register = Register.of();
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, value);

      // WHEN
      final CommandResult actual = Command.INC.execute(addressingResult);

      // THEN
      assertThat(actual.bus().read(address)).isEqualTo(value + 1);
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int address = 0x0012;
      final int value = 0x7F;
      final Register register = Register.of();
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, value);

      // WHEN
      final CommandResult actual = Command.INC.execute(addressingResult);

      // THEN
      assertThat(actual.bus().read(address)).isEqualTo(value + 1);
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int address = 0x0012;
      final int value = 0xFF;
      final int expected = 0x00;
      final Register register = Register.of();
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, value);

      // WHEN
      final CommandResult actual = Command.INC.execute(addressingResult);

      // THEN
      assertThat(actual.bus().read(address)).isEqualTo(expected);
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("INX command tests")
  class InxTests {
    @Test
    @DisplayName("increments X register")
    void incrementsValue() {
      // GIVEN
      final int x = 0x13;
      final Register register = Register.of().x(x);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.INX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(x + 1);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int x = 0x7F;
      final Register register = Register.of().x(x);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.INX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(x + 1);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int x = 0xFF;
      final int expected = 0x00;
      final Register register = Register.of().x(x);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.INX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("INY command tests")
  class InyTests {
    @Test
    @DisplayName("increments Y register")
    void incrementsValue() {
      // GIVEN
      final int y = 0x13;
      final Register register = Register.of().y(y);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.INY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.y()).isEqualTo(y + 1);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int y = 0x7F;
      final Register register = Register.of().y(y);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.INY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.y()).isEqualTo(y + 1);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int y = 0xFF;
      final int expected = 0x00;
      final Register register = Register.of().y(y);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.INY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.y()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("JMP command tests")
  class JmpTests {
    @Test
    @DisplayName("Jumps")
    void jumps() {
      // GIVEN
      final int address = 0x0012;
      final Register register = Register.of();
      final AddressingResult addressingResult = new AddressingResult(register, null, address, 0);

      // WHEN
      final CommandResult actual = Command.JMP.execute(addressingResult);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(address);
    }
  }

  @Nested
  @DisplayName("JSR command tests")
  class JsrTests {
    @Test
    @DisplayName("Jumps")
    void jumps() {
      final int address = 0x0012;
      final int programCounter = 0xABCD;
      final int expectedProgramCounterOnStack = programCounter - 1;
      final int expectedProgramCounterLow = expectedProgramCounterOnStack & VALUE_MASK;
      final int expectedProgramCounterHigh = expectedProgramCounterOnStack >> 8;
      final Register register = Register.of().programCounter(programCounter);
      final int stackPointer = register.stackPointer();
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, 0);

      // WHEN
      final CommandResult actual = Command.JSR.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(address);
      final int actualStackPointer = actualRegister.stackPointer();
      assertThat(actualStackPointer).isEqualTo(stackPointer - 2);
      final Bus actualBus = actual.bus();
      assertThat(actualBus.readFromStack(actualStackPointer + 1))
          .isEqualTo(expectedProgramCounterLow);
      assertThat(actualBus.readFromStack(actualStackPointer + 2))
          .isEqualTo(expectedProgramCounterHigh);
    }
  }

  @Nested
  @DisplayName("LDA command tests")
  class LdaTests {
    @Test
    @DisplayName("loads value")
    void loadsValue() {
      // GIVEN
      final int expected = 0x13;
      final Register register = Register.of();
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, expected);

      // WHEN
      final CommandResult actual = Command.LDA.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int expected = 0x80;
      final Register register = Register.of();
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, expected);

      // WHEN
      final CommandResult actual = Command.LDA.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int expected = 0x00;
      final Register register = Register.of();
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, expected);

      // WHEN
      final CommandResult actual = Command.LDA.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("LDX command tests")
  class LdxTests {
    @Test
    @DisplayName("loads value")
    void loadsValue() {
      // GIVEN
      final int expected = 0x13;
      final Register register = Register.of();
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, expected);

      // WHEN
      final CommandResult actual = Command.LDX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int expected = 0x80;
      final Register register = Register.of();
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, expected);

      // WHEN
      final CommandResult actual = Command.LDX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int expected = 0x00;
      final Register register = Register.of();
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, expected);

      // WHEN
      final CommandResult actual = Command.LDX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("LDY command tests")
  class LdyTests {
    @Test
    @DisplayName("loads value")
    void loadsValue() {
      // GIVEN
      final int expected = 0x13;
      final Register register = Register.of();
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, expected);

      // WHEN
      final CommandResult actual = Command.LDY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.y()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int expected = 0x80;
      final Register register = Register.of();
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, expected);

      // WHEN
      final CommandResult actual = Command.LDY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.y()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int expected = 0x00;
      final Register register = Register.of();
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, expected);

      // WHEN
      final CommandResult actual = Command.LDY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.y()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("LSR command tests")
  class LsrTests {
    @Test
    @DisplayName("shifts value in accumulator")
    void shiftsValueInAccumulator() {
      // GIVEN
      final int a = 0xFE;
      final int expected = 0x7F;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.LSR.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("shifts value on bus")
    void shiftsValueOnBus() {
      // GIVEN
      int address = 0x0012;
      int value = 0xFE;
      int expected = 0x7F;
      final Register register = Register.of();
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, value);

      // WHEN
      final CommandResult actual = Command.LSR.execute(addressingResult);

      // THEN
      assertThat(actual.bus().read(address)).isEqualTo(expected);
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int a = 0x00;
      final int expected = 0x00;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.LSR.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects carry")
    void detectsCarry() {
      // GIVEN
      final int a = 0x03;
      final int expected = 0x01;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.LSR.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("NOP command tests")
  class NopTests {
    @Test
    @DisplayName("does nothing")
    void doesNothing() {
      // GIVEN
      final Register register = Register.of();
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.NOP.execute(addressingResult);

      // THEN
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ORA command tests")
  class OraTests {
    @Test
    @DisplayName("ors value into accumulator")
    void orsValueIntoAccumulator() {
      // GIVEN
      final int a = 0x22;
      final int value = 0x13;
      final int expected = 0x33;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.ORA.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int a = 0xA2;
      final int value = 0x13;
      final int expected = 0xB3;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.ORA.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int a = 0x00;
      final int value = 0x00;
      final int expected = 0x00;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult actual = Command.ORA.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("PHA command tests")
  class PhaTests {
    @Test
    @DisplayName("pushes accumulator onto stack")
    void pushesAccumulatorOntoStack() {
      // GIVEN
      final int expected = 0x12;
      final int stackPointer = 0x17;
      final Register register = Register.of().a(expected).stackPointer(stackPointer);
      final Bus bus = new Bus();
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, bus);

      // WHEN
      final CommandResult actual = Command.PHA.execute(addressingResult);

      // THEN
      assertThat(actual.bus().readFromStack(stackPointer)).isEqualTo(expected);
      assertThat(actual.register().stackPointer()).isEqualTo(stackPointer - 1);
    }
  }

  @Nested
  @DisplayName("PHP command tests")
  class PhpTests {
    @Test
    @DisplayName("pushes status onto stack")
    void pushesStatusOntoStack() {
      // GIVEN
      final int expected = 0x12 | UNUSED_MASK;
      final int stackPointer = 0x17;
      final Register register = Register.of().status(expected).stackPointer(stackPointer);
      final Bus bus = new Bus();
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, bus);

      // WHEN
      final CommandResult actual = Command.PHP.execute(addressingResult);

      // THEN
      assertThat(actual.bus().readFromStack(stackPointer)).isEqualTo(expected);
      assertThat(actual.register().stackPointer()).isEqualTo(stackPointer - 1);
    }
  }

  @Nested
  @DisplayName("PLA command tests")
  class PlaTests {
    @Test
    @DisplayName("pulls accumulator form stack")
    void pullsAccumulatorFromStack() {
      final int expected = 0x13;
      final int stackPointer = 0x17;
      final Register register = Register.of().stackPointer(stackPointer);
      final Bus bus = new Bus().writeToStack(stackPointer + 1, expected);
      final AddressingResult addressingResult = AddressingMode.IMMEDIATE.fetch(register, bus);

      // WHEN
      final CommandResult actual = Command.PLA.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.stackPointer()).isEqualTo(stackPointer + 1);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      final int expected = 0x80;
      final int stackPointer = 0x17;
      final Register register = Register.of().stackPointer(stackPointer);
      final Bus bus = new Bus().writeToStack(stackPointer + 1, expected);
      final AddressingResult addressingResult = AddressingMode.IMMEDIATE.fetch(register, bus);

      // WHEN
      final CommandResult actual = Command.PLA.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.stackPointer()).isEqualTo(stackPointer + 1);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      final int expected = 0x00;
      final int stackPointer = 0x17;
      final Register register = Register.of().stackPointer(stackPointer);
      final Bus bus = new Bus().writeToStack(stackPointer + 1, expected);
      final AddressingResult addressingResult = AddressingMode.IMMEDIATE.fetch(register, bus);

      // WHEN
      final CommandResult actual = Command.PLA.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.stackPointer()).isEqualTo(stackPointer + 1);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("PLP command tests")
  class PlpTests {
    @Test
    @DisplayName("pulls status form stack")
    void pullsAccumulatorFromStack() {
      final int expected = 0x33;
      final int stackPointer = 0x17;
      final Register register = Register.of().stackPointer(stackPointer);
      final Bus bus = new Bus().writeToStack(stackPointer + 1, expected);
      final AddressingResult addressingResult = AddressingMode.IMMEDIATE.fetch(register, bus);

      // WHEN
      final CommandResult actual = Command.PLP.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.status()).isEqualTo(expected);
      assertThat(actualRegister.stackPointer()).isEqualTo(stackPointer + 1);
    }

    @Test
    @DisplayName("unused flag is set")
    void breakFlagIsIgnored() {
      final int value = 0x13;
      final int expected = value | UNUSED_MASK;
      final int stackPointer = 0x17;
      final Register register = Register.of().stackPointer(stackPointer);
      final Bus bus = new Bus().writeToStack(stackPointer + 1, value);
      final AddressingResult addressingResult = AddressingMode.IMMEDIATE.fetch(register, bus);

      // WHEN
      final CommandResult actual = Command.PLP.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.status()).isEqualTo(expected);
      assertThat(actualRegister.stackPointer()).isEqualTo(stackPointer + 1);
    }
  }

  @Nested
  @DisplayName("ROL command tests")
  class RolTests {
    @Test
    @DisplayName("rotates value in accumulator")
    void rotatesValueInAccumulator() {
      // GIVEN
      final int a = 0x13;
      final int expected = 0x26;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.ROL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("rotates value on bus")
    void rotatesValueOnBus() {
      // GIVEN
      final int address = 0x12;
      final int value = 0x13;
      final int expected = 0x26;
      final Register register = Register.of();
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, value);

      // WHEN
      final CommandResult actual = Command.ROL.execute(addressingResult);

      // THEN
      assertThat(actual.bus().read(address)).isEqualTo(expected);
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      // GIVEN
      final int a = 0x43;
      final int expected = 0x86;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.ROL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int a = 0x00;
      final int expected = 0x00;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.ROL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects carry")
    void detectsCarry() {
      // GIVEN
      final int a = 0x83;
      final int expected = 0x07;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.ROL.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("ROR command tests")
  class RorTests {
    @Test
    @DisplayName("rotates value in accumulator")
    void rotatesValueInAccumulator() {
      // GIVEN
      final int a = 0x12;
      final int expected = 0x09;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.ROR.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("rotates value on bus")
    void rotatesValueOnBus() {
      // GIVEN
      final int address = 0x12;
      final int value = 0x12;
      final int expected = 0x09;
      final Register register = Register.of();
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, value);

      // WHEN
      final CommandResult actual = Command.ROR.execute(addressingResult);

      // THEN
      assertThat(actual.bus().read(address)).isEqualTo(expected);
      final Register actualRegister = actual.register();
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative and carry")
    void detectsNegative() {
      // GIVEN
      final int a = 0x13;
      final int expected = 0x89;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.ROR.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int a = 0x00;
      final int expected = 0x00;
      final Register register = Register.of().a(a);
      final AddressingResult addressingResult = AddressingMode.ACCUMULATOR.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.ROR.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }
  }

  @Nested
  @DisplayName("RTI command tests")
  class RtiTests {
    @Test
    @DisplayName("returns")
    void returns() {
      // GIVEN
      final int status = 0x13;
      final int expectedStatus = (status | UNUSED_MASK) & ~BREAK_MASK;
      final int expectedProgramCounter = 0xABCD;
      final int expectedProgramCounterHigh = expectedProgramCounter >> 8;
      final int expectedProgramCounterLow = expectedProgramCounter & VALUE_MASK;
      final int stackPointer = 0x17;
      final Register register = Register.of().stackPointer(stackPointer);
      final Bus bus = new Bus()
          .writeToStack(stackPointer + 1, expectedStatus)
          .writeToStack(stackPointer + 2, expectedProgramCounterLow)
          .writeToStack(stackPointer + 3, expectedProgramCounterHigh);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, bus);

      // WHEN
      final CommandResult actual = Command.RTI.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(expectedProgramCounter);
      assertThat(actualRegister.status()).isEqualTo(expectedStatus);
      assertThat(actualRegister.stackPointer()).isEqualTo(stackPointer + 3);
    }
  }

  @Nested
  @DisplayName("RTS command tests")
  class RtsTests {
    @Test
    @DisplayName("returns")
    void returns() {
      // GIVEN
      final int programCounter = 0xABCD;
      final int stackPointer = 0x17;
      final Register register = Register.of().stackPointer(stackPointer);
      final Bus bus = new Bus().writeAddressToStack(stackPointer + 2, programCounter);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, bus);

      // WHEN
      final CommandResult actual = Command.RTS.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.programCounter()).isEqualTo(programCounter + 1);
      assertThat(actualRegister.stackPointer()).isEqualTo(stackPointer + 2);
    }
  }

  @Nested
  @DisplayName("SBC command tests")
  class SbcTests {
    @Test
    @DisplayName("subtracts one from accumulator")
    void subtractsValueFromAccumulator() {
      // GIVEN
      final int a = 0x02;
      final int value = 0x01;
      final int expected = 0x01;
      final Register register = Register.of().a(a).setCarryFlag();
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult result = Command.SBC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }

    @Test
    @DisplayName("takes borrow into account")
    void takesBorrowIntoAccount() {
      // GIVEN
      final int a = 0x03;
      final int value = 0x01;
      final int expected = 0x01;
      final Register register = Register.of().a(a).unsetCarryFlag();
      final AddressingResult addressingResult = new AddressingResult(register, null, 0, value);

      // WHEN
      final CommandResult result = Command.SBC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      // GIVEN
      final int a = 0x01;
      final int value = 0x01;
      final int expected = 0x00;
      final Register register = Register.of().a(a).setCarryFlag();
      final AddressingResult addressingResult =
          new AddressingResult(register, null, Integer.MIN_VALUE, value);

      // WHEN
      final CommandResult result = Command.SBC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }

    @Test
    @DisplayName("detects overflow")
    void detectsOverflow() {
      // GIVEN
      final int a = 0x80;
      final int value = 0x1;
      final int expected = 0x7F;
      final Register register = Register.of().a(a).setCarryFlag();
      final AddressingResult addressingResult =
          new AddressingResult(register, null, Integer.MIN_VALUE, value);

      // WHEN
      final CommandResult result = Command.SBC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isOverflowFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegativeAndCarry() {
      // GIVEN
      final int a = 0xFF;
      final int value = 0x00;
      final int expected = 0xFF;
      final Register register = Register.of().a(a).setCarryFlag();
      final AddressingResult addressingResult =
          new AddressingResult(register, null, Integer.MIN_VALUE, value);

      // WHEN
      final CommandResult result = Command.SBC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
    }

    @Test
    @DisplayName("detects borrow")
    void detectsBorrow() {
      // GIVEN
      final int a = 0x3E;
      final int value = 0xFF;
      final int expected = 0x3E;
      final Register register = Register.of().a(a).unsetCarryFlag();
      final AddressingResult addressingResult =
          new AddressingResult(register, null, Integer.MIN_VALUE, value);

      // WHEN
      final CommandResult result = Command.SBC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(0);
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
    }
  }

  @Nested
  @DisplayName("SEC command tests")
  class SecTests {
    @Test
    @DisplayName("sets bit")
    void setsBit() {
      // GIVEN
      final Register register = Register.of().unsetCarryFlag();
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.SEC.execute(addressingResult);

      // THE
      assertThat(actual.register().isCarryFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("SED command tests")
  class SedTests {
    @Test
    @DisplayName("sets bit")
    void setsBit() {
      // GIVEN
      final Register register = Register.of().unsetDecimalModeFlag();
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.SED.execute(addressingResult);

      // THE
      assertThat(actual.register().isDecimalModeFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("SEI command tests")
  class SeiTests {
    @Test
    @DisplayName("sets bit")
    void setsBit() {
      // GIVEN
      final Register register = Register.of().setDisableIrqFlag();
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.SEI.execute(addressingResult);

      // THE
      assertThat(actual.register().isDisableIrqFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("STA command tests")
  class StaTests {
    @Test
    @DisplayName("stores value")
    void stores() {
      // GIVEN
      final int address = 0x0012;
      final int expected = 0x13;
      final Register register = Register.of().a(expected);
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, 0);

      // WHEN
      final CommandResult actual = Command.STA.execute(addressingResult);

      // THEN
      assertThat(actual.bus().read(address)).isEqualTo(expected);
    }
  }

  @Nested
  @DisplayName("STX command tests")
  class StxTests {
    @Test
    @DisplayName("stores value")
    void stores() {
      // GIVEN
      final int address = 0x0012;
      final int expected = 0x13;
      final Register register = Register.of().x(expected);
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, 0);

      // WHEN
      final CommandResult actual = Command.STX.execute(addressingResult);

      // THEN
      assertThat(actual.bus().read(address)).isEqualTo(expected);
    }
  }

  @Nested
  @DisplayName("STY command tests")
  class StyTests {
    @Test
    @DisplayName("stores value")
    void stores() {
      // GIVEN
      final int address = 0x0012;
      final int expected = 0x13;
      final Register register = Register.of().y(expected);
      final Bus bus = new Bus();
      final AddressingResult addressingResult = new AddressingResult(register, bus, address, 0);

      // WHEN
      final CommandResult actual = Command.STY.execute(addressingResult);

      // THEN
      assertThat(actual.bus().read(address)).isEqualTo(expected);
    }
  }

  @Nested
  @DisplayName("TAX command tests")
  class TaxTests {
    @Test
    @DisplayName("transfers")
    void transfers() {
      final int expected = 0x13;
      final Register register = Register.of().a(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TAX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      final int expected = 0x83;
      final Register register = Register.of().a(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TAX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      final int expected = 0x00;
      final Register register = Register.of().a(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TAX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("TAY command tests")
  class TayTests {
    @Test
    @DisplayName("transfers")
    void transfers() {
      final int expected = 0x13;
      final Register register = Register.of().a(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TAY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.y()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      final int expected = 0x83;
      final Register register = Register.of().a(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TAY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.y()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      final int expected = 0x00;
      final Register register = Register.of().a(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TAY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.y()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("TSX command tests")
  class TsxTests {
    @Test
    @DisplayName("transfers")
    void transfers() {
      final int expected = 0x13;
      final Register register = Register.of().stackPointer(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TSX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      final int expected = 0x83;
      final Register register = Register.of().stackPointer(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TSX.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      final int expected = 0x00;
      final Register register = Register.of().stackPointer(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TAY.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.x()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("TXS command tests")
  class TxsTests {
    @Test
    @DisplayName("transfers")
    void transfers() {
      final int expected = 0x13;
      final Register register = Register.of().x(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TXS.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.stackPointer()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      final int expected = 0x83;
      final Register register = Register.of().x(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TXS.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.stackPointer()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      final int expected = 0x00;
      final Register register = Register.of().stackPointer(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TXS.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.stackPointer()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }

  @Nested
  @DisplayName("TYA command tests")
  class TyaTests {
    @Test
    @DisplayName("transfers")
    void transfers() {
      final int expected = 0x13;
      final Register register = Register.of().y(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TYA.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects negative")
    void detectsNegative() {
      final int expected = 0x83;
      final Register register = Register.of().y(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TYA.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isTrue();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("detects zero")
    void detectsZero() {
      final int expected = 0x00;
      final Register register = Register.of().y(expected);
      final AddressingResult addressingResult = AddressingMode.IMPLIED.fetch(register, null);

      // WHEN
      final CommandResult actual = Command.TYA.execute(addressingResult);

      // THEN
      final Register actualRegister = actual.register();
      assertThat(actualRegister.a()).isEqualTo(expected);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
    }
  }
}