package de.turing85.yane.impl.cpu6502;

import static com.google.common.truth.Truth.*;
import static de.turing85.yane.impl.cpu6502.AddressingModeFunction.*;
import static org.mockito.Mockito.*;

import de.turing85.yane.api.*;
import org.junit.jupiter.api.*;

@DisplayName("Addressing mode function tests")
class AddressingModeFunctionTests {

  @Nested
  @DisplayName("ACCUMULATOR addressing mode tests")
  class AccumulatorTests {
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("Should return register A when called")
    void shouldReturnRegisterA() {
      // GIVEN
      final byte expectedValue = 0x13;
      when(register.a()).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = ACCUMULATOR.apply(register, null);

      // THEN
      verify(register, times(1)).a();
      verifyNoMoreInteractions(register);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
    }
  }

  @Nested
  @DisplayName("ABSOLUTE addressing mode tests")
  class AbsoluteTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("Should read address form bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final short programCounterFirst = 1337;
      final short programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      final byte addressLow = 0x17;
      final byte addressHigh = (byte) 0x88;
      final short address = (short) 0x8817;
      final byte expectedValue = 0x13;
      when(bus.read(programCounterFirst)).thenReturn(addressLow);
      when(bus.read(programCounterSecond)).thenReturn(addressHigh);
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = ABSOLUTE.apply(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verify(bus, times(1)).read(address);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ABSOLUTE_X addressing mode tests")
  class AbsoluteXTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("Should read address form bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final short programCounterFirst = 1337;
      final short programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      final byte x = 0x07;
      when(register.x()).thenReturn(x);
      final byte addressLow = 0x12;
      final byte addressHigh = (byte) 0x88;
      final short address = (short) 0x8819;
      final byte expectedValue = 0x13;
      when(bus.read(programCounterFirst)).thenReturn(addressLow);
      when(bus.read(programCounterSecond)).thenReturn(addressHigh);
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = ABSOLUTE_X.apply(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verify(register, times(1)).x();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verify(bus, times(1)).read(address);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should read address form bus, return its value and require an additional cycle")
    void shouldReturnExpectedByteFromBusAndAddACycleWhenPageBoundaryIsCrossed() {
      // GIVEN
      final short programCounterFirst = 1337;
      final short programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      final byte x = 0x07;
      when(register.x()).thenReturn(x);
      final byte addressLow = (byte) 0xFF;
      final byte addressHigh = (byte) 0x88;
      final short address = (short) 0x8906;
      final byte expectedValue = 0x13;
      when(bus.read(programCounterFirst)).thenReturn(addressLow);
      when(bus.read(programCounterSecond)).thenReturn(addressHigh);
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = ABSOLUTE_X.apply(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verify(register, times(1)).x();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verify(bus, times(1)).read(address);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("ABSOLUTE_Y addressing mode tests")
  class AbsoluteYTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("Should read address form bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final short programCounterFirst = 1337;
      final short programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      final byte y = 0x07;
      when(register.y()).thenReturn(y);
      final byte addressLow = 0x12;
      final byte addressHigh = (byte) 0x88;
      final short address = (short) 0x8819;
      final byte expectedValue = 0x13;
      when(bus.read(programCounterFirst)).thenReturn(addressLow);
      when(bus.read(programCounterSecond)).thenReturn(addressHigh);
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = ABSOLUTE_Y.apply(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verify(register, times(1)).y();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verify(bus, times(1)).read(address);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should read address form bus, return its value and require an additional cycle")
    void shouldReturnExpectedByteFromBusAndAddACycleWhenPageBoundaryIsCrossed() {
      // GIVEN
      final short programCounterFirst = 1337;
      final short programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      final byte y = 0x07;
      when(register.y()).thenReturn(y);
      final byte addressLow = (byte) 0xFF;
      final byte addressHigh = (byte) 0x88;
      final short address = (short) 0x8906;
      final byte expectedValue = 0x13;
      when(bus.read(programCounterFirst)).thenReturn(addressLow);
      when(bus.read(programCounterSecond)).thenReturn(addressHigh);
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = ABSOLUTE_Y.apply(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verify(register, times(1)).y();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verify(bus, times(1)).read(address);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("IMMEDIATE addressing mode tests")
  class ImmediateTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("Should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final short programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final byte expectedValue = 0x13;
      when(bus.read(programCounter)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = IMMEDIATE.apply(register, bus);

      // THEN
      verify(register, times(1)).getAndIncrementProgramCounter();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounter);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("IMPLIED addressing mode tests")
  class ImpliedTests {
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("Should read nothing")
    void shouldReturnZero() {
      // WHEN
      AddressingResult actual = IMPLIED.apply(register, null);

      // THEN
      verifyNoInteractions(register);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(0);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("INDIRECT addressing mode tests")
  class IndirectTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("Should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final short programCounterFirst = 1337;
      final short programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      byte indirectLow = 0x51;
      byte indirectHigh = (byte) 0x86;
      when(bus.read(programCounterFirst)).thenReturn(indirectLow);
      when(bus.read(programCounterSecond)).thenReturn(indirectHigh);
      short indirect = (short) 0x8651;
      short indirectPlusOne = (short) ((indirect & 0xFFFF) + 1);
      final byte addressLow = (byte) 0x12;
      final byte addressHigh = (byte) 0x88;
      final short address = (short) 0x8812;
      when(bus.read(indirect)).thenReturn(addressLow);
      when(bus.read(indirectPlusOne)).thenReturn(addressHigh);
      final byte expectedValue = 0x13;
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = INDIRECT.apply(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verify(bus, times(1)).read(indirect);
      verify(bus, times(1)).read(indirectPlusOne);
      verify(bus, times(1)).read(address);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("Test bug behaviour in 6502")
    void shouldReturnExpectedByteFromBusBuggyBehaviour() {
      // GIVEN
      final short programCounterFirst = 1337;
      final short programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      byte indirectLow = (byte) 0xFF;
      byte indirectHigh = (byte) 0x86;
      when(bus.read(programCounterFirst)).thenReturn(indirectLow);
      when(bus.read(programCounterSecond)).thenReturn(indirectHigh);
      short indirect = (short) 0x86ff;
      final byte addressLow = (byte) 0x12;
      final short address = (short) 0x8612;
      when(bus.read(indirect)).thenReturn(addressLow);
      when(bus.read(indirectHigh)).thenReturn(indirectHigh);
      final byte expectedValue = 0x13;
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = INDIRECT.apply(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verify(bus, times(1)).read(indirect);
      verify(bus, times(1)).read(address);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("INDIRECT_ZERO_PAGE_OFFSET_X addressing mode tests")
  class IndirectZeroPageOffsetXTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("Should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final short programCounter = 1337;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounter);
      final byte x = 0x07;
      when(register.x()).thenReturn(x);
      final byte indirectLow = 0x51;
      final short indirectPlusX = 0x58;
      final short indirectPlusXPlusOne = 0x59;
      when(bus.read(programCounter)).thenReturn(indirectLow);
      final byte addressLow = 0x12;
      final byte addressHigh = (byte) 0x88;
      final short address = (short) 0x8812;
      when(bus.read(indirectPlusX)).thenReturn(addressLow);
      when(bus.read(indirectPlusXPlusOne)).thenReturn(addressHigh);
      final byte expectedValue = 0x13;
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = INDIRECT_ZERO_PAGE_X.apply(register, bus);

      // THEN
      verify(register, times(1)).getAndIncrementProgramCounter();
      verify(register, times(1)).x();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounter);
      verify(bus, times(1)).read(indirectPlusX);
      verify(bus, times(1)).read(indirectPlusXPlusOne);
      verify(bus, times(1)).read(address);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }


  @Nested
  @DisplayName("INDIRECT_ZERO_PAGE_OFFSET_Y addressing mode tests")
  class IndirectZeroPageOffsetYTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("Should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final short programCounter = 1337;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounter);
      final byte y = 0x07;
      when(register.y()).thenReturn(y);
      final byte indirectLow = 0x51;
      final short indirect = 0x51;
      final short indirectPlusOne = 0x52;
      when(bus.read(programCounter)).thenReturn(indirectLow);
      final byte addressLow = 0x12;
      final byte addressHigh = (byte) 0x88;
      final short addressPlusY = (short) 0x8819;
      when(bus.read(indirect)).thenReturn(addressLow);
      when(bus.read(indirectPlusOne)).thenReturn(addressHigh);
      final byte expectedValue = 0x13;
      when(bus.read(addressPlusY)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = INDIRECT_ZERO_PAGE_Y.apply(register, bus);

      // THEN
      verify(register, times(1)).getAndIncrementProgramCounter();
      verify(register, times(1)).y();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounter);
      verify(bus, times(1)).read(indirect);
      verify(bus, times(1)).read(indirectPlusOne);
      verify(bus, times(1)).read(addressPlusY);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should read address form bus, return its value and require an additional cycle")
    void shouldReturnExpectedByteFromBusAndAddACycleWhenPageBoundaryIsCrossed() {
      // GIVEN
      final short programCounter = 1337;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounter);
      final byte y = 0x07;
      when(register.y()).thenReturn(y);
      final byte indirectLow = 0x51;
      final short indirect = 0x51;
      final short indirectPlusOne = 0x52;
      when(bus.read(programCounter)).thenReturn(indirectLow);
      final byte addressLow = (byte) 0xFF;
      final byte addressHigh = (byte) 0x88;
      final short addressPlusY = (short) 0x8906;
      when(bus.read(indirect)).thenReturn(addressLow);
      when(bus.read(indirectPlusOne)).thenReturn(addressHigh);
      final byte expectedValue = 0x13;
      when(bus.read(addressPlusY)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = INDIRECT_ZERO_PAGE_Y.apply(register, bus);

      // THEN
      verify(register, times(1)).getAndIncrementProgramCounter();
      verify(register, times(1)).y();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounter);
      verify(bus, times(1)).read(indirect);
      verify(bus, times(1)).read(indirectPlusOne);
      verify(bus, times(1)).read(addressPlusY);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("REL addressing mode tests")
  class RelTests {
    private final Register register = mock(Register.class);
    private final CpuBus bus = mock(CpuBus.class);

    @Test
    @DisplayName("Should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final short programCounter = 1337;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounter);

      final byte expectedValue = 0x13;
      when(bus.read(programCounter)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = REL.apply(register, bus);

      // THEN
      verify(register).getAndIncrementProgramCounter();
      verifyNoMoreInteractions(register);
      verify(bus).read(programCounter);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ZERO_PAGE addressing mode tests")
  class ZeroPageTests {
    private final Register register = mock(Register.class);
    private final CpuBus bus = mock(CpuBus.class);

    @Test
    @DisplayName("Should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final short programCounter = 1337;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounter);
      final byte addressLow = 0x12;
      final short address = 0x0012;
      when(bus.read(programCounter)).thenReturn(addressLow);
      final byte expectedValue = 0x13;
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = ZERO_PAGE.apply(register, bus);

      // THEN
      verify(register).getAndIncrementProgramCounter();
      verifyNoMoreInteractions(register);
      verify(bus).read(programCounter);
      verify(bus).read(address);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ZERO_PAGE_X addressing mode tests")
  class ZeroPageXTests {
    private final Register register = mock(Register.class);
    private final CpuBus bus = mock(CpuBus.class);

    @Test
    @DisplayName("Should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final short programCounter = 1337;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounter);
      final byte x = 0x07;
      when(register.x()).thenReturn(x);
      final byte addressLow = 0x12;
      final short addressPlusX = 0x0019;
      when(bus.read(programCounter)).thenReturn(addressLow);
      final byte expectedValue = 0x13;
      when(bus.read(addressPlusX)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = ZERO_PAGE_X.apply(register, bus);

      // THEN
      verify(register).getAndIncrementProgramCounter();
      verify(register).x();
      verifyNoMoreInteractions(register);
      verify(bus).read(programCounter);
      verify(bus).read(addressPlusX);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ZERO_PAGE_Y addressing mode tests")
  class ZeroPageYTests {
    private final Register register = mock(Register.class);
    private final CpuBus bus = mock(CpuBus.class);

    @Test
    @DisplayName("Should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final short programCounter = 1337;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounter);
      final byte y = 0x07;
      when(register.y()).thenReturn(y);
      final byte addressLow = 0x12;
      final short addressPlusY = 0x0019;
      when(bus.read(programCounter)).thenReturn(addressLow);
      final byte expectedValue = 0x13;
      when(bus.read(addressPlusY)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = ZERO_PAGE_Y.apply(register, bus);

      // THEN
      verify(register).getAndIncrementProgramCounter();
      verify(register).y();
      verifyNoMoreInteractions(register);
      verify(bus).read(programCounter);
      verify(bus).read(addressPlusY);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.valueRead()).isEqualTo(expectedValue);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }
}