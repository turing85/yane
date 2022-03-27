package de.turing85.yane.impl.cpu6502;

import static com.google.common.truth.Truth.*;
import static de.turing85.yane.impl.cpu6502.AddressingMode.*;
import static org.mockito.Mockito.*;

import de.turing85.yane.api.*;
import org.junit.jupiter.api.*;

@DisplayName("Addressing mode function tests")
class AddressingModeTests {

  @Nested
  @DisplayName("ACCUMULATOR addressing mode tests")
  class AccumulatorTests {
    private final Register register = mock(Register.class);
    private final CpuBus bus = mock(CpuBus.class);

    @Test
    @DisplayName("should return register A when called")
    void shouldReturnRegisterA() {
      // WHEN
      AddressingResult actual = ACCUMULATOR.fetch(register, bus);

      // THEN
      verifyNoInteractions(register);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(IMPLIED_LOADED_ADDRESS);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ABSOLUTE addressing mode tests")
  class AbsoluteTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("should read address form bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final int programCounterFirst = 1337;
      final int programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      final int addressLow = 0x17;
      final int addressHigh = 0x88;
      final int address = 0x8817;
      when(bus.read(programCounterFirst)).thenReturn(addressLow);
      when(bus.read(programCounterSecond)).thenReturn(addressHigh);

      // WHEN
      final AddressingResult actual = ABSOLUTE.fetch(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ABSOLUTE_X addressing mode tests")
  class AbsoluteXTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("should read address form bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final int programCounterFirst = 1337;
      final int programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      final int x = 0x07;
      when(register.x()).thenReturn(x);
      final int addressLow = 0x12;
      final int addressHigh = 0x88;
      final int address = 0x8819;
      when(bus.read(programCounterFirst)).thenReturn(addressLow);
      when(bus.read(programCounterSecond)).thenReturn(addressHigh);

      // WHEN
      final AddressingResult actual = ABSOLUTE_X.fetch(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verify(register, times(1)).x();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("should read address form bus, return its value and require an additional cycle")
    void shouldReturnExpectedByteFromBusAndAddACycleWhenPageBoundaryIsCrossed() {
      // GIVEN
      final int programCounterFirst = 1337;
      final int programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      final int x = 0x07;
      when(register.x()).thenReturn(x);
      final int addressLow = 0xFF;
      final int addressHigh = 0x88;
      final int address = 0x8906;
      when(bus.read(programCounterFirst)).thenReturn(addressLow);
      when(bus.read(programCounterSecond)).thenReturn(addressHigh);

      // WHEN
      final AddressingResult actual = ABSOLUTE_X.fetch(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verify(register, times(1)).x();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("ABSOLUTE_Y addressing mode tests")
  class AbsoluteYTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("should read address form bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final int programCounterFirst = 1337;
      final int programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      final int y = 0x07;
      when(register.y()).thenReturn(y);
      final int addressLow = 0x12;
      final int addressHigh = 0x88;
      final int address = 0x8819;
      when(bus.read(programCounterFirst)).thenReturn(addressLow);
      when(bus.read(programCounterSecond)).thenReturn(addressHigh);

      // WHEN
      final AddressingResult actual = ABSOLUTE_Y.fetch(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verify(register, times(1)).y();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("should read address form bus, return its value and require an additional cycle")
    void shouldReturnExpectedByteFromBusAndAddACycleWhenPageBoundaryIsCrossed() {
      // GIVEN
      final int programCounterFirst = 1337;
      final int programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      final int y = 0x07;
      when(register.y()).thenReturn(y);
      final int addressLow = 0xFF;
      final int addressHigh = 0x88;
      final int address = 0x8906;
      when(bus.read(programCounterFirst)).thenReturn(addressLow);
      when(bus.read(programCounterSecond)).thenReturn(addressHigh);

      // WHEN
      final AddressingResult actual = ABSOLUTE_Y.fetch(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verify(register, times(1)).y();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("IMMEDIATE addressing mode tests")
  class ImmediateTests {
    private final Register register = mock(Register.class);
    private final CpuBus bus = mock(CpuBus.class);

    @Test
    @DisplayName("should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);

      // WHEN
      AddressingResult actual = IMMEDIATE.fetch(register, bus);

      // THEN
      verifyNoInteractions(register);
      verifyNoInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(IMMEDIATE_LOADED_ADDRESS);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("IMPLIED addressing mode tests")
  class ImpliedTests {
    private final Register register = mock(Register.class);
    private final CpuBus bus = mock(CpuBus.class);

    @Test
    @DisplayName("should read nothing")
    void shouldReturnZero() {
      // WHEN
      AddressingResult actual = IMPLIED.fetch(register, bus);

      // THEN
      verifyNoInteractions(register);
      verifyNoInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(IMPLIED_LOADED_ADDRESS);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("INDIRECT addressing mode tests")
  class IndirectTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final int programCounterFirst = 1337;
      final int programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      int indirectLow = 0x51;
      int indirectHigh = 0x69;
      when(bus.read(programCounterFirst)).thenReturn(indirectLow);
      when(bus.read(programCounterSecond)).thenReturn(indirectHigh);
      int indirect = 0x6951;
      final int addressLow = 0x12;
      final int addressHigh = 0x88;
      final int address = 0x8812;
      when(bus.read(indirect)).thenReturn(addressLow);
      when(bus.read(indirect + 1)).thenReturn(addressHigh);

      // WHEN
      AddressingResult actual = INDIRECT.fetch(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verify(bus, times(1)).read(indirect);
      verify(bus, times(1)).read(indirect + 1);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("test bug behaviour in 6502")
    void shouldReturnExpectedByteFromBusBuggyBehaviour() {
      // GIVEN
      final int programCounterFirst = 1337;
      final int programCounterSecond = 1338;
      when(register.getAndIncrementProgramCounter())
          .thenReturn(programCounterFirst)
          .thenReturn(programCounterSecond);
      int indirectLow = 0xFF;
      int indirectHigh = 0x69;
      when(bus.read(programCounterFirst)).thenReturn(indirectLow);
      when(bus.read(programCounterSecond)).thenReturn(indirectHigh);
      int indirect = 0x69ff;
      int nextIndirectDueToBug = 0x6900;
      final int addressLow = 0x12;
      final int addressHigh = 0x88;
      final int address = 0x8812;
      when(bus.read(indirect)).thenReturn(addressLow);
      when(bus.read(nextIndirectDueToBug)).thenReturn(addressHigh);

      // WHEN
      AddressingResult actual = INDIRECT.fetch(register, bus);

      // THEN
      verify(register, times(2)).getAndIncrementProgramCounter();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounterFirst);
      verify(bus, times(1)).read(programCounterSecond);
      verify(bus, times(1)).read(indirect);
      verify(bus, times(1)).read(nextIndirectDueToBug);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("INDIRECT_ZERO_PAGE_OFFSET_X addressing mode tests")
  class IndirectZeroPageOffsetXTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final int x = 0x07;
      when(register.x()).thenReturn(x);
      final int indirectLow = 0x51;
      final int indirectPlusX = 0x58;
      final int indirectPlusXPlusOne = 0x59;
      when(bus.read(programCounter)).thenReturn(indirectLow);
      final int addressLow = 0x12;
      final int addressHigh = 0x88;
      final int address = 0x8812;
      when(bus.read(indirectPlusX)).thenReturn(addressLow);
      when(bus.read(indirectPlusXPlusOne)).thenReturn(addressHigh);

      // WHEN
      AddressingResult actual = INDIRECT_ZERO_PAGE_X.fetch(register, bus);

      // THEN
      verify(register, times(1)).getAndIncrementProgramCounter();
      verify(register, times(1)).x();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounter);
      verify(bus, times(1)).read(indirectPlusX);
      verify(bus, times(1)).read(indirectPlusXPlusOne);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("Address reading loops when zero page would be left")
    void shouldReturnLoopedAddressWhenZeroPageIsLeft() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final int x = 0x07;
      when(register.x()).thenReturn(x);
      final int indirectLow = 0xF8;
      final int indirectPlusX = 0xFF;
      final int indirectPlusXPlusOne = 0x00;
      when(bus.read(programCounter)).thenReturn(indirectLow);
      final int addressLow = 0x12;
      final int addressHigh = 0x88;
      final int address = 0x8812;
      when(bus.read(indirectPlusX)).thenReturn(addressLow);
      when(bus.read(indirectPlusXPlusOne)).thenReturn(addressHigh);

      // WHEN
      AddressingResult actual = INDIRECT_ZERO_PAGE_X.fetch(register, bus);

      // THEN
      verify(register, times(1)).getAndIncrementProgramCounter();
      verify(register, times(1)).x();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounter);
      verify(bus, times(1)).read(indirectPlusX);
      verify(bus, times(1)).read(indirectPlusXPlusOne);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }


  @Nested
  @DisplayName("INDIRECT_ZERO_PAGE_OFFSET_Y addressing mode tests")
  class IndirectZeroPageOffsetYTests {
    private final CpuBus bus = mock(CpuBus.class);
    private final Register register = mock(Register.class);

    @Test
    @DisplayName("should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final int y = 0x07;
      when(register.y()).thenReturn(y);
      final int indirectLow = 0x51;
      final int indirect = 0x51;
      final int indirectPlusOne = 0x52;
      when(bus.read(programCounter)).thenReturn(indirectLow);
      final int addressLow = 0x12;
      final int addressHigh = 0x88;
      final int addressPlusY = 0x8819;
      when(bus.read(indirect)).thenReturn(addressLow);
      when(bus.read(indirectPlusOne)).thenReturn(addressHigh);

      // WHEN
      AddressingResult actual = INDIRECT_ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      verify(register, times(1)).getAndIncrementProgramCounter();
      verify(register, times(1)).y();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounter);
      verify(bus, times(1)).read(indirect);
      verify(bus, times(1)).read(indirectPlusOne);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(addressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("should read address form bus, return its value and require an additional cycle")
    void shouldReturnExpectedByteFromBusAndAddACycleWhenPageBoundaryIsCrossed() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final int y = 0x07;
      when(register.y()).thenReturn(y);
      final int indirectLow = 0x51;
      final int indirect = 0x51;
      final int indirectPlusOne = 0x52;
      when(bus.read(programCounter)).thenReturn(indirectLow);
      final int addressLow = 0xFF;
      final int addressHigh = 0x88;
      final int addressPlusY = 0x8906;
      when(bus.read(indirect)).thenReturn(addressLow);
      when(bus.read(indirectPlusOne)).thenReturn(addressHigh);

      // WHEN
      AddressingResult actual = INDIRECT_ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      verify(register, times(1)).getAndIncrementProgramCounter();
      verify(register, times(1)).y();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounter);
      verify(bus, times(1)).read(indirect);
      verify(bus, times(1)).read(indirectPlusOne);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(addressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(1);
    }

    @Test
    @DisplayName("Address reading loops when zero page would be left")
    void shouldReturnLoopedAddressWhenZeroPageIsLeft() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final int y = 0x07;
      when(register.y()).thenReturn(y);
      final int indirect = 0xFF;
      final int indirectPlusOne = 0x00;
      when(bus.read(programCounter)).thenReturn(indirect);
      final int addressLow = 0x12;
      final int addressHigh = 0x88;
      final int addressPlusY = 0x8819;
      when(bus.read(indirect)).thenReturn(addressLow);
      when(bus.read(indirectPlusOne)).thenReturn(addressHigh);

      // WHEN
      AddressingResult actual = INDIRECT_ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      verify(register, times(1)).getAndIncrementProgramCounter();
      verify(register, times(1)).y();
      verifyNoMoreInteractions(register);
      verify(bus, times(1)).read(programCounter);
      verify(bus, times(1)).read(indirect);
      verify(bus, times(1)).read(indirectPlusOne);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(addressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("RELATIVE addressing mode tests")
  class RelTests {
    private final Register register = mock(Register.class);
    private final CpuBus bus = mock(CpuBus.class);

    @Test
    @DisplayName("should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final int relativeAddress = 0x68;
      when(bus.read(programCounter)).thenReturn(relativeAddress);
      final int address = (programCounter + relativeAddress) & 0xFFFF;

      // WHEN
      AddressingResult actual = RELATIVE.fetch(register, bus);

      // THEN
      verify(register).getAndIncrementProgramCounter();
      verifyNoMoreInteractions(register);
      verify(bus).read(programCounter);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("should read negative address from bus and return its value when")
    void shouldReturnExpectedByteFromBusWhenRelativeAddressIsNegative() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final int relativeAddress = -37;
      when(bus.read(programCounter)).thenReturn(relativeAddress);
      final int address = 1300;

      // WHEN
      AddressingResult actual = RELATIVE.fetch(register, bus);

      // THEN
      verify(register).getAndIncrementProgramCounter();
      verifyNoMoreInteractions(register);
      verify(bus).read(programCounter);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ZERO_PAGE addressing mode tests")
  class ZeroPageTests {
    private final Register register = mock(Register.class);
    private final CpuBus bus = mock(CpuBus.class);

    @Test
    @DisplayName("should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final int zeroPageAddress = 0x0012;
      when(bus.read(programCounter)).thenReturn(zeroPageAddress);

      // WHEN
      AddressingResult actual = ZERO_PAGE.fetch(register, bus);

      // THEN
      verify(register).getAndIncrementProgramCounter();
      verifyNoMoreInteractions(register);
      verify(bus).read(programCounter);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(zeroPageAddress);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ZERO_PAGE_X addressing mode tests")
  class ZeroPageXTests {
    private final Register register = mock(Register.class);
    private final CpuBus bus = mock(CpuBus.class);

    @Test
    @DisplayName("should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final int x = 0x07;
      when(register.x()).thenReturn(x);
      final int zeroPageAddress = 0x12;
      final int zeroPageAddressPlusX = 0x0019;
      when(bus.read(programCounter)).thenReturn(zeroPageAddress);

      // WHEN
      AddressingResult actual = ZERO_PAGE_X.fetch(register, bus);

      // THEN
      verify(register).getAndIncrementProgramCounter();
      verify(register).x();
      verifyNoMoreInteractions(register);
      verify(bus).read(programCounter);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(zeroPageAddressPlusX);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("Address reading loops when zero page would be left")
    void shouldReturnLoopedAddressWhenZeroPageIsLeft() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final int x = 0x07;
      when(register.x()).thenReturn(x);
      final int zeroPageAddress = 0xFF;
      final int zeroPageAddressPlusX = 0x06;
      when(bus.read(programCounter)).thenReturn(zeroPageAddress);

      // WHEN
      AddressingResult actual = ZERO_PAGE_X.fetch(register, bus);

      // THEN
      verify(register).getAndIncrementProgramCounter();
      verify(register).x();
      verifyNoMoreInteractions(register);
      verify(bus).read(programCounter);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(zeroPageAddressPlusX);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ZERO_PAGE_Y addressing mode tests")
  class ZeroPageYTests {
    private final Register register = mock(Register.class);
    private final CpuBus bus = mock(CpuBus.class);

    @Test
    @DisplayName("should read address from bus and return its value")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final int y = 0x07;
      when(register.y()).thenReturn(y);
      final int zeroPageAddress = 0x12;
      final int addressPlusY = 0x0019;
      when(bus.read(programCounter)).thenReturn(zeroPageAddress);

      // WHEN
      AddressingResult actual = ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      verify(register).getAndIncrementProgramCounter();
      verify(register).y();
      verifyNoMoreInteractions(register);
      verify(bus).read(programCounter);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(addressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("Address reading loops when zero page would be left")
    void shouldReturnLoopedAddressWhenZeroPageIsLeft() {
      // GIVEN
      final int programCounter = 1337;
      when(register.getAndIncrementProgramCounter()).thenReturn(programCounter);
      final int y = 0x07;
      when(register.y()).thenReturn(y);
      final int address = 0xFF;
      final int addressPlusY = 0x06;
      when(bus.read(programCounter)).thenReturn(address);

      // WHEN
      AddressingResult actual = ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      verify(register).getAndIncrementProgramCounter();
      verify(register).y();
      verifyNoMoreInteractions(register);
      verify(bus).read(programCounter);
      verifyNoMoreInteractions(bus);
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.bus()).isEqualTo(bus);
      assertThat(actual.address()).isEqualTo(addressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }
}