package de.turing85.yane.impl.cpu6502;

import static com.google.common.truth.Truth.*;
import static de.turing85.yane.impl.cpu6502.AddressingMode.*;
import static org.mockito.Mockito.*;

import de.turing85.yane.api.*;
import org.junit.jupiter.api.*;

@DisplayName("Addressing mode function tests")
class AddressingModeTests {
  private final CpuBus bus = mock(CpuBus.class);
  private final Register register = new Register();

  @Nested
  @DisplayName("ACCUMULATOR addressing mode tests")
  class AccumulatorTests {
    @Test
    @DisplayName("loads value from accumulator")
    void loadsValueFromAccumulator() {
      // GIVEN
      final int expectedA = 0x13;
      register.a(expectedA);

      // WHEN
      final AddressingResult actual = ACCUMULATOR.fetch(register, null);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(0);
      assertThat(actual.value()).isEqualTo(expectedA);
      assertThat(actual.address()).isEqualTo(IMPLIED_LOADED_ADDRESS);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ABSOLUTE addressing mode tests")
  class AbsoluteTests {
    @Test
    @DisplayName("loads value from absolute address")
    void loadsValueFromAbsoluteAddress() {
      // GIVEN
      final int addressLow = 0x17;
      final int addressHigh = 0x88;
      final int expectedAddress = 0x8817;
      when(bus.read(0)).thenReturn(addressLow);
      when(bus.read(1)).thenReturn(addressHigh);
      final int expectedValue = 0x13;
      when(bus.read(expectedAddress)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = ABSOLUTE.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(ABSOLUTE.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(expectedAddress);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ABSOLUTE_X addressing mode tests")
  class AbsoluteXTests {
    @Test
    @DisplayName("loads value from absolute address + x)")
    void loadsValueFromAbsolutePlusX() {
      // GIVEN
      register.x(0x07);
      final int addressLow = 0x12;
      final int addressHigh = 0x88;
      final int addressPlusX = 0x8819;
      when(bus.read(0)).thenReturn(addressLow);
      when(bus.read(1)).thenReturn(addressHigh);
      final int expectedValue = 0x13;
      when(bus.read(addressPlusX)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = ABSOLUTE_X.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(ABSOLUTE_X.bytesToRead());
      assertThat(actual.register()).isEqualTo(register);
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(addressPlusX);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("adds one cycle when absolute address + x resides in new memory page")
    void addsOneCycleWhenAddressPlusXIsOnNewPage() {
      // GIVEN
      register.x(0x07);
      final int addressLow = 0xFF;
      final int addressHigh = 0x88;
      final int addressPlusX = 0x8906;
      when(bus.read(0)).thenReturn(addressLow);
      when(bus.read(1)).thenReturn(addressHigh);
      final int expectedValue = 0x13;
      when(bus.read(addressPlusX)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = ABSOLUTE_X.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(ABSOLUTE_X.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(addressPlusX);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("ABSOLUTE_Y addressing mode tests")
  class AbsoluteYTests {
    @Test
    @DisplayName("loads value from absolute address + y")
    void loadsValueFromAbsolutePlusY() {
      // GIVEN
      register.y(0x07);
      final int addressLow = 0x12;
      final int addressHigh = 0x88;
      final int addressPlusY = 0x8819;
      when(bus.read(0)).thenReturn(addressLow);
      when(bus.read(1)).thenReturn(addressHigh);
      final int expectedValue = 0x13;
      when(bus.read(addressPlusY)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = ABSOLUTE_Y.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(ABSOLUTE_Y.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(addressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("adds one cycle when absolute address + y resides in new memory page")
    void addsOneCycleWhenAddressPlusYIsOnNewPage() {
      // GIVEN
      register.y(0x07);
      final int addressLow = 0xFF;
      final int addressHigh = 0x88;
      final int addressPlusY = 0x8906;
      when(bus.read(0)).thenReturn(addressLow);
      when(bus.read(1)).thenReturn(addressHigh);
      final int expectedValue = 0x13;
      when(bus.read(addressPlusY)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = ABSOLUTE_Y.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(ABSOLUTE_Y.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(addressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("IMMEDIATE addressing mode tests")
  class ImmediateTests {
    @Test
    @DisplayName("loads the byte at program counter")
    void loadsTheByteAtProgramCounter() {
      // GIVEN
      final int expectedValue = 0x13;
      when(bus.read(0)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = IMMEDIATE.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(IMMEDIATE.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(0);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("IMPLIED addressing mode tests")
  class ImpliedTests {
    @Test
    @DisplayName("loads nothing")
    void readsNothing() {
      // WHEN
      final AddressingResult actual = IMPLIED.fetch(null, null);

      // THEN
      assertThat(actual.value()).isEqualTo(NOTHING_READ_VALUE);
      assertThat(actual.address()).isEqualTo(IMPLIED_LOADED_ADDRESS);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("INDIRECT addressing mode tests")
  class IndirectTests {
    @Test
    @DisplayName("loads the value at indirect address")
    void loadsValueAtIndirectAddress() {
      // GIVEN
      int indirectLow = 0x51;
      int indirectHigh = 0x69;
      when(bus.read(0)).thenReturn(indirectLow);
      when(bus.read(1)).thenReturn(indirectHigh);
      int indirect = 0x6951;
      final int addressLow = 0x12;
      final int addressHigh = 0x88;
      final int address = 0x8812;
      when(bus.read(indirect)).thenReturn(addressLow);
      when(bus.read(indirect + 1)).thenReturn(addressHigh);
      final int expectedValue = 0x13;
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = INDIRECT.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(INDIRECT.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("reproduces bug in 6502")
    void reproducesBug() {
      // GIVEN
      int indirectLow = 0xFF;
      int indirectHigh = 0x69;
      when(bus.read(0)).thenReturn(indirectLow);
      when(bus.read(1)).thenReturn(indirectHigh);
      int indirect = 0x69ff;
      int nextIndirectDueToBug = 0x6900;
      final int addressLow = 0x12;
      final int addressHigh = 0x88;
      final int address = 0x8812;
      when(bus.read(indirect)).thenReturn(addressLow);
      when(bus.read(nextIndirectDueToBug)).thenReturn(addressHigh);
      final int expectedValue = 0x13;
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = INDIRECT.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(INDIRECT.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("INDIRECT_ZERO_PAGE_OFFSET_X addressing mode tests")
  class IndirectZeroPageOffsetXTests {
    @Test
    @DisplayName("loads the value at indirect address + X")
    void loadsValueAtIndirectAddressWithXOffset() {
      // GIVEN
      register.x(0x07);
      final int indirectZeroPageAddress = 0x0051;
      final int indirectZeroPageAddressPlusX = 0x0058;
      when(bus.read(0)).thenReturn(indirectZeroPageAddress);
      final int zeroPageAddress = 0x0012;
      when(bus.read(indirectZeroPageAddressPlusX)).thenReturn(zeroPageAddress);
      final int expectedValue = 0x13;
      when(bus.read(zeroPageAddress)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = INDIRECT_ZERO_PAGE_X.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(
          INDIRECT_ZERO_PAGE_X.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(zeroPageAddress);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("does not leave the memory page")
    void doesNotLeaveMemoryPage() {
      // GIVEN
      register.x(0x07);
      final int indirectZeroPageAddress = 0x00FF;
      final int indirectZeroPAgeAddressPlusX = 0x0006;
      when(bus.read(0)).thenReturn(indirectZeroPageAddress);
      final int zeroPageAddress = 0x0012;
      when(bus.read(indirectZeroPAgeAddressPlusX)).thenReturn(zeroPageAddress);
      final int expectedValue = 0x13;
      when(bus.read(zeroPageAddress)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = INDIRECT_ZERO_PAGE_X.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(
          INDIRECT_ZERO_PAGE_X.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(zeroPageAddress);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("INDIRECT_ZERO_PAGE_OFFSET_Y addressing mode tests")
  class IndirectZeroPageOffsetYTests {
    @Test
    @DisplayName("loads the value at  indirect address + Y")
    void loadsValueAtIndirectAddressWithYOffset() {
      // GIVEN
      register.y(0x07);
      final int indirectZeroPageAddress = 0x0051;
      when(bus.read(0)).thenReturn(indirectZeroPageAddress);
      final int zeroPageAddress = 0x0012;
      final int zeroPageAddressPlusY = 0x0019;
      when(bus.read(indirectZeroPageAddress)).thenReturn(zeroPageAddress);
      final int expectedValue = 0x13;
      when(bus.read(zeroPageAddressPlusY)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = INDIRECT_ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      assertThat(register.programCounter()).isEqualTo(INDIRECT_ZERO_PAGE_Y.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(zeroPageAddressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("adds one cycle when indirect address + y resides in new memory page")
    void addsOneCycleWhenAddressPlusYIsOnNewPage() {
      // GIVEN
      register.y(0x07);
      final int indirect = 0xFF;
      final int indirectPlusOne = 0x00;
      final int addressLow = 0xFF;
      final int addressHigh = 0xFF;
      final int addressPlusY = 0x06;
      when(bus.read(0)).thenReturn(indirect);
      when(bus.read(indirect)).thenReturn(addressLow);
      when(bus.read(indirectPlusOne)).thenReturn(addressHigh);
      final int expectedValue = 0x13;
      when(bus.read(addressPlusY)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = INDIRECT_ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(
          INDIRECT_ZERO_PAGE_Y.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(addressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("RELATIVE addressing mode tests")
  class RelTests {
    @Test
    @DisplayName("reads relative address")
    void shouldReturnExpectedByteFromBus() {
      // GIVEN
      final int relativeAddress = 0x68;
      when(bus.read(0)).thenReturn(relativeAddress);
      final int address = (relativeAddress) & 0xFFFF;
      final int expectedValue = 0x13;
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      final AddressingResult actual = RELATIVE.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(RELATIVE.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("handlesNegativeAddress")
    void handlesNegativeRelativeAddress() {
      // GIVEN
      final int relativeAddress = -37;
      when(bus.read(0)).thenReturn(relativeAddress);
      final int address = (relativeAddress) & 0xFFFF;
      final int expectedValue = 0x13;
      when(bus.read(address)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = RELATIVE.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(RELATIVE.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ZERO_PAGE addressing mode tests")
  class ZeroPageTests {
    @Test
    @DisplayName("reads zero page address")
    void readsZeroPageAddress() {
      // GIVEN
      final int zeroPageAddress = 0x0012;
      when(bus.read(0)).thenReturn(zeroPageAddress);
      final int expectedValue = 0x13;
      when(bus.read(zeroPageAddress)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = ZERO_PAGE.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(ZERO_PAGE.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(zeroPageAddress);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ZERO_PAGE_X addressing mode tests")
  class ZeroPageXTests {
    @Test
    @DisplayName("reads zero page address + x")
    void readsZeroPageAddressPlusX() {
      // GIVEN
      register.x(0x07);
      final int zeroPageAddress = 0x12;
      final int zeroPageAddressPlusX = 0x0019;
      when(bus.read(0)).thenReturn(zeroPageAddress);
      final int expectedValue = 0x13;
      when(bus.read(zeroPageAddressPlusX)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = ZERO_PAGE_X.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(INDIRECT_ZERO_PAGE_X.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(zeroPageAddressPlusX);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("stays on zero page")
    void staysOnZeroPage() {
      // GIVEN
      register.x(0x07);
      final int zeroPageAddress = 0xFF;
      final int zeroPageAddressPlusX = 0x06;
      when(bus.read(0)).thenReturn(zeroPageAddress);
      final int expectedValue = 0x13;
      when(bus.read(zeroPageAddressPlusX)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = ZERO_PAGE_X.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(INDIRECT_ZERO_PAGE_X.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(zeroPageAddressPlusX);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("ZERO_PAGE_Y addressing mode tests")
  class ZeroPageYTests {
    @Test
    @DisplayName("reads zero page address + Y")
    void readsZeroPageAddressPlusY() {
      // GIVEN
      register.y(0x07);
      final int zeroPAgeAddress = 0x0012;
      final int zeroPageAddressPlusY = 0x0019;
      when(bus.read(0)).thenReturn(zeroPAgeAddress);
      final int expectedValue = 0x13;
      when(bus.read(zeroPageAddressPlusY)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(INDIRECT_ZERO_PAGE_Y.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(zeroPageAddressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("stays on zero page")
    void staysOnZeroPage() {
      // GIVEN
      register.y(0x07);
      final int zeroPageAddress = 0xFF;
      final int zeroPageAddressPlusY = 0x06;
      when(bus.read(0)).thenReturn(zeroPageAddress);
      final int expectedValue = 0x13;
      when(bus.read(zeroPageAddressPlusY)).thenReturn(expectedValue);

      // WHEN
      AddressingResult actual = ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(INDIRECT_ZERO_PAGE_Y.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(zeroPageAddressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }
}