package de.turing85.yane.cpu;

import static com.google.common.truth.Truth.*;

import org.junit.jupiter.api.*;

class CommandTest {

  @Nested
  class AddCommandTest {
    @Test
    void simpleAddition() {
      // GIVEN
      final int programCounter = 1337;
      final int a = 0x01;
      final int value = 0x02;
      final int sum = 0x03;
      final Register register = new Register()
          .programCounter(programCounter)
          .a(a)
          .unsetCarryFlag();
      final AddressingResult addressingResult =
          new AddressingResult(register, null, Integer.MIN_VALUE, value);

      // WHEN
      CommandResult result = Command.ADC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(programCounter);
      assertThat(actualRegister.a()).isEqualTo(sum);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isFalse();
      assertThat(actualRegister.isCarryFlagSet()).isFalse();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
    }

    @Test
    void detectsZeroAndCarry() {
      // GIVEN
      final int programCounter = 1337;
      final int a = 0xFF;
      final int value = 0x00;
      final int sum = 0x00;
      final Register register = new Register()
          .programCounter(programCounter)
          .a(a)
          .setCarryFlag();
      final AddressingResult addressingResult =
          new AddressingResult(register, null, Integer.MIN_VALUE, value);

      // WHEN
      CommandResult result = Command.ADC.execute(addressingResult);

      // THEN
      final Register actualRegister = result.register();
      assertThat(actualRegister.programCounter()).isEqualTo(programCounter);
      assertThat(actualRegister.a()).isEqualTo(sum);
      assertThat(actualRegister.isNegativeFlagSet()).isFalse();
      assertThat(actualRegister.isZeroFlagSet()).isTrue();
      assertThat(actualRegister.isCarryFlagSet()).isTrue();
      assertThat(actualRegister.isOverflowFlagSet()).isFalse();
    }
  }
}