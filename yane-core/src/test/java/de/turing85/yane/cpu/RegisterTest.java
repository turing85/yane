package de.turing85.yane.cpu;

import static com.google.common.truth.Truth.*;
import static de.turing85.yane.cpu.Register.*;

import org.junit.jupiter.api.*;

@DisplayName("Register tests")
class RegisterTest {

  @Nested
  @DisplayName("Constructor tests")
  class ConstructorTest {
    @Test
    @DisplayName("no-args constructor initializes everything with the default values")
    void everythingSetToDefaultValues() {
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
    void setEverythingToTheExpectedValues() {
      // GIVEN
      final int expectedA = 1;
      final int expectedX = 2;
      final int expectedY = 3;
      final int stackPointer = 4;
      final int expectedProgramCounter = 5;
      final boolean expectedCarry = true;
      final boolean expectedZero = true;
      final boolean expectedUnused = true;
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
          expectedUnused,
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
      assertThat(register.isNegativeFlagSet()).isEqualTo(expectedNegative);
      assertThat(register.isOverflowFlagSet()).isEqualTo(expectedOverflow);
      assertThat(register.isUnusedFlagSet()).isEqualTo(expectedUnused);
      assertThat(register.isBreakFlagSet()).isEqualTo(expectedBreakFlag);
      assertThat(register.isDecimalModeFlagSet()).isEqualTo(expectedDecimalMode);
      assertThat(register.isDisableIrqFlagSet()).isEqualTo(expectedDisableInterrupt);
      assertThat(register.isZeroFlagSet()).isEqualTo(expectedZero);
      assertThat(register.isCarryFlagSet()).isEqualTo(expectedCarry);
    }
  }

  @Nested
  @DisplayName("Mutator tests")
  class MutatorTest {
    private final Register defaultRegister = Register.of();
    private final Register allSetRegister = Register.of(
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
        true,
        true);

    @Nested
    @DisplayName("Program counter tests")
    class ProgramCounterTests {
      @Test
      @DisplayName("Get and increment the program counter")
      void getAndIncrementTheProgramCounter() {
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
      @DisplayName("Set program counter")
      void setProgramCounter() {
        // GIVEN
        final int expectedProgramCounter = 1337;

        // WHEN
        Register actual = defaultRegister.programCounter(expectedProgramCounter);

        // THEN
        assertThat(actual.programCounter()).isEqualTo(expectedProgramCounter);
      }

      @Test
      @DisplayName("Mask program counter if it is too large")
      void maskedProgramCounter() {
        // GIVEN
        final int programCounter = 0xF_ABCD;
        final int expectedProgramCounter = 0xABCD;

        // WHEN
        final Register actual = defaultRegister.programCounter(programCounter);

        // THEN
        assertThat(actual.programCounter()).isEqualTo(expectedProgramCounter);
      }

      @Test
      @DisplayName("Wrap program counter during increment")
      void wrapProgramCounterDuringIncrement() {
        // GIVEN
        final int initialProgramCounter = 0xFF;
        final int expectedIncrementedProgramCounter =
            (initialProgramCounter + 1) & Register.PROGRAM_COUNTER_MASK;
        final Register register = defaultRegister.programCounter(initialProgramCounter);

        // WHEN
        final Register actual = register.incrementProgramCounter();

        // THEN
        assertThat(actual.programCounter()).isEqualTo(expectedIncrementedProgramCounter);
      }

      @Test
      @DisplayName("Wrap program counter during decrement")
      void wrapProgramCounterDuringDecrement() {
        // GIVEN
        final int initialProgramCounter = 0x00;
        final int expectedDecrementedProgramCounter =
            (initialProgramCounter - 1) & Register.PROGRAM_COUNTER_MASK;
        final Register register = defaultRegister.programCounter(initialProgramCounter);

        // WHEN
        final Register actual = register.decrementProgramCounter();

        // THEN
        assertThat(actual.programCounter()).isEqualTo(expectedDecrementedProgramCounter);
      }
    }

    @Test
    @DisplayName("mutate accumulator")
    void setAccumulator() {
      // GIVEN
      final int expectedA = 17;

      // WHEN
      Register actual = defaultRegister.a(expectedA);

      // THEN
      assertThat(actual.a()).isEqualTo(expectedA);
    }

    @Test
    @DisplayName("mutate register X")
    void mutateX() {
      // GIVEN
      final int expectedX = 17;

      // WHEN
      Register actual = defaultRegister.x(expectedX);

      // THEN
      assertThat(actual.x()).isEqualTo(expectedX);
    }

    @Test
    @DisplayName("mutate register Y")
    void mutateY() {
      // GIVEN
      final int expectedY = 17;

      // WHEN
      Register actual = defaultRegister.y(expectedY);

      // THEN
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

        // WHEN
        final Register actualRegister = defaultRegister.stackPointer(stackPointer);

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

    @Nested
    @DisplayName("Status flag tests")
    class StatusFlagTests {
      @Nested
      @DisplayName("Set Status byte tests")
      class SetStatusByteTests {
        @Test
        @DisplayName("Enable all flags")
        void enableAll() {
          // WHEN
          final Register register = defaultRegister.status(0);
          
          // THEN
          assertThat(register.status()).isEqualTo(0);
          assertThat(register.isNegativeFlagSet()).isFalse();
          assertThat(register.isOverflowFlagSet()).isFalse();
          assertThat(register.isUnusedFlagSet()).isFalse();
          assertThat(register.isBreakFlagSet()).isFalse();
          assertThat(register.isDecimalModeFlagSet()).isFalse();
          assertThat(register.isDisableIrqFlagSet()).isFalse();
          assertThat(register.isZeroFlagSet()).isFalse();
          assertThat(register.isCarryFlagSet()).isFalse();
        }

        @Test
        @DisplayName("DisableAll")
        void disableAll() {
          // WHEN
          final Register register = defaultRegister.status(0xFF);

          // THEN
          assertThat(register.status()).isEqualTo(0xFF);
          assertThat(register.isNegativeFlagSet()).isTrue();
          assertThat(register.isOverflowFlagSet()).isTrue();
          assertThat(register.isUnusedFlagSet()).isTrue();
          assertThat(register.isBreakFlagSet()).isTrue();
          assertThat(register.isDecimalModeFlagSet()).isTrue();
          assertThat(register.isDisableIrqFlagSet()).isTrue();
          assertThat(register.isZeroFlagSet()).isTrue();
          assertThat(register.isCarryFlagSet()).isTrue();
        }
      }
      
      @Nested
      @DisplayName("Negative Flag tests")
      class NegativeFlagTests {
        @Test
        @DisplayName("enable")
        void enable() {
          // WHEN
          final Register actual = defaultRegister.setNegativeFlag();

          // THEN
          assertThat(actual.isNegativeFlagSet()).isTrue();
          assertThat(actual.status() & NEGATIVE_MASK).isNotEqualTo(0);
        }

        @Test
        @DisplayName("disable")
        void disable() {
          // WHEN
          final Register actual = allSetRegister.unsetNegativeFlag();

          // THEN
          assertThat(actual.isNegativeFlagSet()).isFalse();
          assertThat(actual.status() & NEGATIVE_MASK).isEqualTo(0);
        }
      }

      @Nested
      @DisplayName("Overflow Flag tests")
      class OverflowFlagTests {
        @Test
        @DisplayName("enable")
        void enable() {
          // WHEN
          final Register actual = defaultRegister.setOverflowFlag();

          // THEN
          assertThat(actual.isOverflowFlagSet()).isTrue();
          assertThat(actual.status() & OVERFLOW_MASK).isNotEqualTo(0);
        }

        @Test
        @DisplayName("disable")
        void disable() {
          // WHEN
          final Register actual = allSetRegister.unsetOverflowFlag();

          // THEN
          assertThat(actual.isOverflowFlagSet()).isFalse();
          assertThat(actual.status() & OVERFLOW_MASK).isEqualTo(0);
        }
      }

      @Nested
      @DisplayName("Unused Flag tests")
      class UnusedFlagTests {
        @Test
        @DisplayName("enable")
        void enable() {
          // WHEN
          final Register actual = defaultRegister.setUnusedFlag();

          // THEN
          assertThat(actual.isUnusedFlagSet()).isTrue();
          assertThat(actual.status() & UNUSED_MASK).isNotEqualTo(0);
        }

        @Test
        @DisplayName("disable")
        void disable() {
          // WHEN
          final Register actual = allSetRegister.unsetUnusedFlag();

          // THEN
          assertThat(actual.isUnusedFlagSet()).isFalse();
          assertThat(actual.status() & UNUSED_MASK).isEqualTo(0);
        }
      }

      @Nested
      @DisplayName("Break Flag tests")
      class BreakFlagTests {
        @Test
        @DisplayName("enable")
        void enable() {
          // WHEN
          final Register actual = defaultRegister.setBreakFlag();

          // THEN
          assertThat(actual.isBreakFlagSet()).isTrue();
          assertThat(actual.status() & BREAK_MASK).isNotEqualTo(0);
        }

        @Test
        @DisplayName("disable")
        void disable() {
          // WHEN
          final Register actual = allSetRegister.unsetBreakFlag();

          // THEN
          assertThat(actual.isBreakFlagSet()).isFalse();
          assertThat(actual.status() & BREAK_MASK).isEqualTo(0);
        }
      }

      @Nested
      @DisplayName("Decimal Flag tests")
      class DecimalFlagTests {
        @Test
        @DisplayName("enable")
        void enable() {
          // WHEN
          final Register actual = defaultRegister.setDecimalModeFlag();

          // THEN
          assertThat(actual.isDecimalModeFlagSet()).isTrue();
          assertThat(actual.status() & DECIMAL_MASK).isNotEqualTo(0);
        }

        @Test
        @DisplayName("disable")
        void disable() {
          // WHEN
          final Register actual = allSetRegister.unsetDecimalModeFlag();

          // THEN
          assertThat(actual.isDecimalModeFlagSet()).isFalse();
          assertThat(actual.status() & DECIMAL_MASK).isEqualTo(0);
        }
      }

      @Nested
      @DisplayName("Disable IRQ Flag tests")
      class DisableIrqFlagTests {
        @Test
        @DisplayName("enable")
        void enable() {
          // WHEN
          final Register actual = defaultRegister.setDisableIrqFlag();

          // THEN
          assertThat(actual.isDisableIrqFlagSet()).isTrue();
          assertThat(actual.status() & DISABLE_IRQ_MASK).isNotEqualTo(0);
        }

        @Test
        @DisplayName("disable")
        void disable() {
          // WHEN
          final Register actual = allSetRegister.unsetDisableIrqFlag();

          // THEN
          assertThat(actual.isDisableIrqFlagSet()).isFalse();
          assertThat(actual.status() & DISABLE_IRQ_MASK).isEqualTo(0);
        }
      }

      @Nested
      @DisplayName("Zero Flag tests")
      class ZeroFlagTests {
        @Test
        @DisplayName("enable")
        void enable() {
          // WHEN
          final Register actual = defaultRegister.setZeroFlag();

          // THEN
          assertThat(actual.isZeroFlagSet()).isTrue();
          assertThat(actual.status() & ZERO_MASK).isNotEqualTo(0);
        }

        @Test
        @DisplayName("disable")
        void disable() {
          // WHEN
          final Register actual = allSetRegister.unsetZeroFlag();

          // THEN
          assertThat(actual.isZeroFlagSet()).isFalse();
          assertThat(actual.status() & ZERO_MASK).isEqualTo(0);
        }
      }

      @Nested
      @DisplayName("Carry Flag tests")
      class CarryFlagTests {
        @Test
        @DisplayName("enable")
        void enable() {
          // WHEN
          final Register actual = defaultRegister.setCarryFlag();

          // THEN
          assertThat(actual.isCarryFlagSet()).isTrue();

          assertThat(actual.status() & CARRY_MASK).isNotEqualTo(0);
        }

        @Test
        @DisplayName("disable")
        void disable() {
          // WHEN
          final Register actual = allSetRegister.unsetCarryFlag();

          // THEN
          assertThat(actual.isCarryFlagSet()).isFalse();
          assertThat(actual.status() & CARRY_MASK).isEqualTo(0);
        }
      }
    }
  }
}