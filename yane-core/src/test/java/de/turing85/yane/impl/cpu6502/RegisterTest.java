package de.turing85.yane.impl.cpu6502;

import static com.google.common.truth.Truth.*;

import org.junit.jupiter.api.*;

@DisplayName("Register tests")
class RegisterTest {

  @Nested
  @DisplayName("Constructor tests")
  class ConstructorTest {
    @Test
    @DisplayName("no-args constructor initializes everything with 0 or false")
    void everythingShouldBeZeroWhenNoArgsConstructorIsCalled() {
      // WHEN
      final Register register = new Register();

      // THEN
      assertThat(register.a()).isEqualTo( 0);
      assertThat(register.x()).isEqualTo( 0);
      assertThat(register.y()).isEqualTo( 0);
      assertThat(register.stackPointer()).isEqualTo( 0);
      assertThat(register.programCounter()).isEqualTo( 0);
      assertThat(register.isCarryFlagSet()).isEqualTo(false);
      assertThat(register.isCarryFlagSet()).isEqualTo(false);
      assertThat(register.isZeroFlagSet()).isEqualTo(false);
      assertThat(register.isInterruptFlagSet()).isEqualTo(false);
      assertThat(register.isDecimalModeFlagSet()).isEqualTo(false);
      assertThat(register.isBreakFlagSet()).isEqualTo(false);
      assertThat(register.isOverflowFlagSet()).isEqualTo(false);
      assertThat(register.isNegativeFlagSet()).isEqualTo(false);
    }

    @Test
    @DisplayName("Sets everything to the expected values when all-args constructor is called")
    void shouldReturnExpectedValuesWhenAllArgsConstructorIsCalled() {
      // GIVEN
      final int expectedA = 1;
      final int expectedX = 2;
      final int expectedY = 3;
      final int expectedStackPointer = 4;
      final int expectedProgramCounter = 5;
      final boolean expectedCarry = true;
      final boolean expectedZero = true;
      final boolean expectedDisableInterrupt = true;
      final boolean expectedDecimalMode = true;
      final boolean expectedBreakFlag = true;
      final boolean expectedOverflow = true;
      final boolean expectedNegative = true;

      // WHEN
      final Register register = new Register(
          expectedA,
          expectedX,
          expectedY,
          expectedStackPointer,
          expectedProgramCounter,
          expectedNegative,
          expectedOverflow,
          expectedBreakFlag,
          expectedDecimalMode,
          expectedDisableInterrupt,
          expectedZero,
          expectedCarry);

      // THEN
      assertThat(register.a()).isEqualTo(expectedA);
      assertThat(register.x()).isEqualTo(expectedX);
      assertThat(register.y()).isEqualTo(expectedY);
      assertThat(register.stackPointer()).isEqualTo(expectedStackPointer);
      assertThat(register.programCounter()).isEqualTo(expectedProgramCounter);
      assertThat(register.isCarryFlagSet()).isEqualTo(expectedCarry);
      assertThat(register.isZeroFlagSet()).isEqualTo(expectedZero);
      assertThat(register.isInterruptFlagSet()).isEqualTo(expectedDisableInterrupt);
      assertThat(register.isDecimalModeFlagSet()).isEqualTo(expectedDecimalMode);
      assertThat(register.isBreakFlagSet()).isEqualTo(expectedBreakFlag);
      assertThat(register.isOverflowFlagSet()).isEqualTo(expectedOverflow);
      assertThat(register.isNegativeFlagSet()).isEqualTo(expectedNegative);
    }
  }

  @Nested
  @DisplayName("Mutator tests")
  class MutatorTest {
    private Register defaultRegister;
    private Register allSetRegister;

    @BeforeEach
    void setup() {
      defaultRegister = new Register();
      allSetRegister = new Register(
           1,
           2,
           3,
           4,
           5,
          true,
          true,
          true,
          true,
          true,
          true,
          true);
    }

    @Test
    @DisplayName("should get and increment the program counter")
    void shouldGetAndIncrementTheProgramCounter() {
      // GIVEN
      final int expectedProgramCounter = 1337;
      final Register register = allSetRegister.programCounter(expectedProgramCounter);

      // WHEN
      final int actualProgramCounter = register.getAndIncrementProgramCounter();

      // THEN
      assertThat(actualProgramCounter).isEqualTo(expectedProgramCounter);
      assertThat(register.programCounter()).isEqualTo(expectedProgramCounter + 1);
    }

    @Test
    @DisplayName("should return expected value when register A is mutated")
    void shouldReturnExpectedValueWhenAIsMutated() {
      // GIVEN
      final int expectedA = 17;

      // WHEN
      Register actual = defaultRegister.a(expectedA);

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(actual.a()).isEqualTo(expectedA);
    }

    @Test
    @DisplayName("should return expected value when register X is mutated")
    void shouldReturnExpectedValueWhenXIsMutated() {
      // GIVEN
      final int expectedX = 17;

      // WHEN
      Register actual = defaultRegister.x(expectedX);

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(actual.x()).isEqualTo(expectedX);
    }

    @Test
    @DisplayName("should return expected value when register Y is mutated")
    void shouldReturnExpectedValueWhenYIsMutated() {
      // GIVEN
      final int expectedY = 17;

      // WHEN
      Register actual = defaultRegister.y(expectedY);

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(actual.y()).isEqualTo(expectedY);
    }

    @Test
    @DisplayName("should return expected value when the stack pointer is mutated")
    void shouldReturnExpectedValueWhenStackPointerIsMutated() {
      // GIVEN
      final int expectedStackPointer = 17;

      // WHEN
      Register actual = defaultRegister.stackPointer(expectedStackPointer);

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(actual.stackPointer()).isEqualTo(expectedStackPointer);
    }

    @Test
    @DisplayName("should return expected value when the program counter is mutated")
    void shouldReturnExpectedValueWhenProgramCounterIsMutated() {
      // GIVEN
      final int expectedProgramCounter = 1337;

      // WHEN
      Register actual = defaultRegister.programCounter(expectedProgramCounter);

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(actual.programCounter()).isEqualTo(expectedProgramCounter);
    }

    @Test
    @DisplayName("should enable carry flag when carry flag is set")
    void shouldReturnTrueWhenCarryFlagIsSet() {
      // WHEN
      final Register actual = defaultRegister.setCarryFlag();

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(defaultRegister.isCarryFlagSet()).isTrue();
    }

    @Test
    @DisplayName("should disable carry flag when carry flag is unset")
    void shouldReturnFalseWhenCarryFlagIsUnset() {
      // WHEN
      final Register actual = allSetRegister.unsetCarryFlag();

      // THEN
      assertThat(actual).isEqualTo(allSetRegister);
      assertThat(actual.isCarryFlagSet()).isFalse();
    }

    @Test
    @DisplayName("should enable zero flag when zero flag is set")
    void shouldReturnTrueWhenZeroFlagIsSet() {
      // WHEN
      final Register actual = defaultRegister.setZeroFlag();

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(actual.isZeroFlagSet()).isTrue();
    }

    @Test
    @DisplayName("should disable zero bit when zero flag is unset")
    void shouldReturnFalseWhenZeroFlagIsUnset() {
      // WHEN
      final Register actual = allSetRegister.unsetZeroFlag();

      // THEN
      assertThat(actual).isEqualTo(allSetRegister);
      assertThat(actual.isZeroFlagSet()).isFalse();
    }

    @Test
    @DisplayName("should enable interrupt flag when disable interrupt flag is set")
    void shouldReturnTrueWhenInterruptFlagIsSet() {
      // WHEN
      final Register actual = defaultRegister.setDisableInterruptFlag();

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(actual.isInterruptFlagSet()).isTrue();
    }

    @Test
    @DisplayName("should disable interrupt flag when disable interrupt flag is unset")
    void shouldReturnFalseWhenDisableInterruptIsUnset() {
      // WHEN
      final Register actual = allSetRegister.unsetDisableInterruptFlag();

      // THEN
      assertThat(actual).isEqualTo(allSetRegister);
      assertThat(actual.isInterruptFlagSet()).isFalse();
    }

    @Test
    @DisplayName("should enable decimal mode flag when decimal mode flag is set")
    void shouldReturnTrueWhenDecimalModeFlagIsSet() {
      // WHEN
      final Register actual = defaultRegister.setDecimalModeFlag();

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(actual.isDecimalModeFlagSet()).isTrue();
    }

    @Test
    @DisplayName("should disable decimal mode flag when decimal mode flag is unset")
    void shouldReturnFalseWhenDecimalModeFlagIsUnset() {
      // WHEN
      final Register actual = allSetRegister.unsetDecimalModeFlag();

      // THEN
      assertThat(actual).isEqualTo(allSetRegister);
      assertThat(actual.isDecimalModeFlagSet()).isFalse();
    }

    @Test
    @DisplayName("should enable break flag when break flag is set")
    void shouldReturnTrueWhenBreakFlagIsSet() {
      // WHEN
      final Register actual = defaultRegister.setBreakFlag();

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(actual.isBreakFlagSet()).isTrue();
    }

    @Test
    @DisplayName("should disable break flag when break flag is unset")
    void shouldReturnFalseWhenBreakFlagIsUnset() {
      // WHEN
      final Register actual = allSetRegister.unsetBreakFlag();

      // THEN
      assertThat(actual).isEqualTo(allSetRegister);
      assertThat(actual.isBreakFlagSet()).isFalse();
    }

    @Test
    @DisplayName("should enable overflow flag when overflow flag is set")
    void shouldReturnTrueWhenOverflowFlagIsSet() {
      // WHEN
      final Register actual = defaultRegister.setOverflowFlag();

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(actual.isOverflowFlagSet()).isTrue();
    }

    @Test
    @DisplayName("should disable overflow flag when overflow flag is unset")
    void shouldReturnFalseWhenOverflowFlagIsUnset() {
      // WHEN
      final Register actual = allSetRegister.unsetOverflowFlag();

      // THEN
      assertThat(actual).isEqualTo(allSetRegister);
      assertThat(actual.isOverflowFlagSet()).isFalse();
    }

    @Test
    @DisplayName("should enable negative flag when negative flag is set")
    void shouldReturnTrueWhenNegativeFlagIsSet() {
      // WHEN
      final Register actual = defaultRegister.setNegativeFlag();

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(actual.isNegativeFlagSet()).isTrue();
    }

    @Test
    @DisplayName("should disable negative flag when negative flag is unset")
    void shouldReturnFalseWhenNegativeFlagIsUnset() {
      // WHEN
      final Register actual = allSetRegister.unsetNegativeFlag();

      // THEN
      assertThat(actual).isEqualTo(allSetRegister);
      assertThat(actual.isNegativeFlagSet()).isFalse();
    }
  }
}