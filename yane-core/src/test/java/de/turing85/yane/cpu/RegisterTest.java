package de.turing85.yane.cpu;

import static com.google.common.truth.Truth.*;

import org.junit.jupiter.api.*;

@DisplayName("Register tests")
class RegisterTest {

  @Nested
  @DisplayName("Constructor tests")
  class ConstructorTest {
    @Test
    @DisplayName("no-args constructor initializes everything with the default values")
    void everythingShouldBeZeroWhenNoArgsConstructorIsCalled() {
      // WHEN
      final Register register = Register.of();

      // THEN
      assertThat(register.a()).isEqualTo(0);
      assertThat(register.x()).isEqualTo(0);
      assertThat(register.y()).isEqualTo(0);
      assertThat(register.stackPointer()).isEqualTo(Register.INITIAL_STACK_POINTER_VALUE);
      assertThat(register.programCounter()).isEqualTo(0);
      assertThat(register.isNegativeFlagSet()).isEqualTo(false);
      assertThat(register.isOverflowFlagSet()).isEqualTo(false);
      assertThat(register.isUnusedFlagSet()).isEqualTo(true);
      assertThat(register.isBreakFlagSet()).isEqualTo(true);
      assertThat(register.isDecimalModeFlagSet()).isEqualTo(false);
      assertThat(register.isDisableIrqFlagSet()).isEqualTo(true);
      assertThat(register.isZeroFlagSet()).isEqualTo(false);
      assertThat(register.isCarryFlagSet()).isEqualTo(false);
    }

    @Test
    @DisplayName("Sets everything to the expected values when all-args constructor is called")
    void shouldReturnExpectedValuesWhenAllArgsConstructorIsCalled() {
      // GIVEN
      final int expectedA = 1;
      final int expectedX = 2;
      final int expectedY = 3;
      final int stackPointer = 4;
      final int expectedProgramCounter = 5;
      final boolean expectedCarry = true;
      final boolean expectedZero = true;
      final boolean expectedDisableInterrupt = true;
      final boolean expectedDecimalMode = true;
      final boolean expectedBreakFlag = true;
      final boolean expectedOverflow = true;
      final boolean expectedNegative = true;

      // WHEN
      final Register register = Register.of(
          expectedA,
          expectedX,
          expectedY,
          stackPointer,
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
      assertThat(register.stackPointer()).isEqualTo(stackPointer);
      assertThat(register.programCounter()).isEqualTo(expectedProgramCounter);
      assertThat(register.isCarryFlagSet()).isEqualTo(expectedCarry);
      assertThat(register.isZeroFlagSet()).isEqualTo(expectedZero);
      assertThat(register.isDisableIrqFlagSet()).isEqualTo(expectedDisableInterrupt);
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
      defaultRegister = Register.of();
      allSetRegister = Register.of(
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

    @Nested
    @DisplayName("Program counter tests")
    class ProgramCounterTests {
      @Test
      @DisplayName("should get and increment the program counter")
      void shouldGetAndIncrementTheProgramCounter() {
        // GIVEN
        final int initialProgramCounter = 1337;
        final int expectedProgramCounter = 1338;
        final Register register = defaultRegister.programCounter(initialProgramCounter);

        // WHEN
        final int actualProgramCounter = register.getAndIncrementProgramCounter();

        // THEN
        assertThat(actualProgramCounter).isEqualTo(initialProgramCounter);
        assertThat(register.programCounter()).isEqualTo(expectedProgramCounter);
      }

      @Test
      @DisplayName("should return expected value when the program counter is mutated")
      void shouldReturnExpectedValueWhenProgramCounterIsMutated() {
        // GIVEN
        final int expectedProgramCounter = 1337;
        final Register register = defaultRegister;

        // WHEN
        Register actual = register.programCounter(expectedProgramCounter);

        // THEN
        assertThat(actual).isEqualTo(register);
        assertThat(actual.programCounter()).isEqualTo(expectedProgramCounter);
      }

      @Test
      @DisplayName("should mask the program counter if it is too large")
      void shouldGetMaskedProgramCounter() {
        // GIVEN
        final int programCounter = 0xF_ABCD;
        final int expectedProgramCounter = 0xABCD;
        final Register register = defaultRegister;

        // WHEN
        final Register actual = register.programCounter(programCounter);

        // THEN
        assertThat(actual).isEqualTo(register);
        assertThat(actual.programCounter()).isEqualTo(expectedProgramCounter);
      }

      @Test
      @DisplayName("should wrap program counter during increment")
      void shouldWrapProgramCounterDuringIncrement() {
        // GIVEN
        final int initialProgramCounter = 0xFF;
        final int expectedIncrementedProgramCounter =
            (initialProgramCounter + 1) & Register.PROGRAM_COUNTER_MASK;
        final Register register = defaultRegister.programCounter(initialProgramCounter);

        // WHEN
        final Register actual = register.incrementProgramCounter();

        // THEN
        assertThat(actual).isEqualTo(register);
        assertThat(actual.programCounter()).isEqualTo(expectedIncrementedProgramCounter);
      }
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

    @Nested
    @DisplayName("Stack Pointer tests")
    class StackPointerTests {
      @Test
      @DisplayName("should return expected value when the stack pointer is mutated")
      void shouldReturnExpectedValueWhenStackPointerIsMutated() {
        // GIVEN
        final int stackPointer = 0x17;

        // WHEN
        Register actual = defaultRegister.stackPointer(stackPointer);

        // THEN
        assertThat(actual).isEqualTo(defaultRegister);
        assertThat(actual.stackPointer()).isEqualTo(stackPointer);
      }

      @Test
      @DisplayName("should increment and get the stack pointer")
      void shouldIncrementAndGetTheStackPointer() {
        // GIVEN
        final int initialStackPointer = 0x17;

        final Register register = defaultRegister.stackPointer(initialStackPointer);

        // WHEN
        final int actualStackPointer = register.incrementAndGetStackPointer();

        // THEN
        assertThat(actualStackPointer).isEqualTo(initialStackPointer + 1);
      }

      @Test
      @DisplayName("should get and decrement the stack pointer")
      void shouldGetAndDecrementTheStackPointer() {
        // GIVEN
        final int initialStackPointer = 17;
        final Register register = defaultRegister.stackPointer(initialStackPointer);

        // WHEN
        final int actualStackPointer = register.getAndDecrementStackPointer();

        // THEN
        assertThat(actualStackPointer).isEqualTo(initialStackPointer);
        assertThat(register.stackPointer()).isEqualTo(initialStackPointer - 1);
      }

      @Test
      @DisplayName("should mask the stack pointer if it is too large")
      void shouldGetMaskedStackPointer() {
        // GIVEN
        final int stackPointer = 0xF_AB;
        final int expectedStackPointer = 0x00AB;
        final Register register = defaultRegister;

        // WHEN
        final Register actualRegister = register.stackPointer(stackPointer);

        // THEN
        assertThat(actualRegister.stackPointer())
            .isEqualTo(expectedStackPointer);
      }

      @Test
      @DisplayName("should wrap stack pointer during increment")
      void shouldWrapStackPointerDuringIncrement() {
        // GIVEN
        final int initialStackPointer = 0xFF;
        final int expectedIncrementedStackPointer = 0x00;

        // WHEN
        final int actual = defaultRegister.stackPointer(initialStackPointer)
            .incrementAndGetStackPointer();

        // THEN
        assertThat(actual).isEqualTo(expectedIncrementedStackPointer);
      }

      @Test
      @DisplayName("should wrap stack pointer during decrement")
      void shouldWrapStackPointerDuringDecrement() {
        // GIVEN
        final int initialStackPointer = 0x00;
        final int expectedDecrementedStackPointer = 0xFF;
        final Register register = defaultRegister.stackPointer(initialStackPointer);

        // WHEN
        final int actual = register.getAndDecrementStackPointer();

        // THEN
        assertThat(actual).isEqualTo(initialStackPointer);
        assertThat(register.stackPointer()).isEqualTo(expectedDecrementedStackPointer);
      }
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
      final Register actual = defaultRegister.setDisableIrqFlag();

      // THEN
      assertThat(actual).isEqualTo(defaultRegister);
      assertThat(actual.isDisableIrqFlagSet()).isTrue();
    }

    @Test
    @DisplayName("should disable interrupt flag when disable interrupt flag is unset")
    void shouldReturnFalseWhenDisableInterruptIsUnset() {
      // WHEN
      final Register actual = allSetRegister.unsetDisableIrqFlag();

      // THEN
      assertThat(actual).isEqualTo(allSetRegister);
      assertThat(actual.isDisableIrqFlagSet()).isFalse();
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