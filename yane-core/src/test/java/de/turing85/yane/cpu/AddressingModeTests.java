package de.turing85.yane.cpu;

import static com.google.common.truth.Truth.*;

import org.junit.jupiter.api.*;

@DisplayName("Addressing mode function tests")
class AddressingModeTests {
  private final Bus bus = new Bus();
  private final Register register = Register.of();

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
      final AddressingResult actual = AddressingMode.ACCUMULATOR.fetch(register, null);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(0);
      assertThat(actual.value()).isEqualTo(expectedA);
      assertThat(actual.address()).isEqualTo(AddressingMode.IMPLIED_LOADED_ADDRESS);
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
      final int expectedAddress = 0x8817;
      bus.writeAddressTo(0, expectedAddress);
      final int expectedValue = 0x13;
      bus.write(expectedAddress, expectedValue);

      // WHEN
      final AddressingResult actual = AddressingMode.ABSOLUTE.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.ABSOLUTE.bytesToRead());
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
      final int address = 0x0012;
      bus.writeAddressTo(0, address);
      final int addressPlusX = 0x0019;
      final int expectedValue = 0x13;
      bus.write(addressPlusX, expectedValue);

      // WHEN
      final AddressingResult actual = AddressingMode.ABSOLUTE_X.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.ABSOLUTE_X.bytesToRead());
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
      final int address = 0x88FF;
      bus.writeAddressTo(0, address);
      final int addressPlusX = 0x8906;
      final int expectedValue = 0x13;
      bus.write(addressPlusX, expectedValue);

      // WHEN
      final AddressingResult actual = AddressingMode.ABSOLUTE_X.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.ABSOLUTE_X.bytesToRead());
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
      final int address = 0x0012;
      bus.writeAddressTo(0, address);
      final int addressPlusY = 0x0019;
      final int expectedValue = 0x13;
      bus.write(addressPlusY, expectedValue);

      // WHEN
      final AddressingResult actual = AddressingMode.ABSOLUTE_Y.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.ABSOLUTE_Y.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(addressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("adds one cycle when absolute address + y resides in new memory page")
    void addsOneCycleWhenAddressPlusYIsOnNewPage() {
      // GIVEN
      register.y(0x07);
      final int address = 0x88FF;
      final int addressPlusY = 0x8906;
      bus.writeAddressTo(0, address);
      final int expectedValue = 0x13;
      bus.write(addressPlusY, expectedValue);

      // WHEN
      final AddressingResult actual = AddressingMode.ABSOLUTE_Y.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.ABSOLUTE_Y.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(addressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("IMMEDIATE addressing mode tests")
  class ImmediateTests {
    @Test
    @DisplayName("loads the byte value at program counter")
    void loadsTheByteAtProgramCounter() {
      // GIVEN
      final int expectedValue = 0x13;
      bus.write(0, expectedValue);

      // WHEN
      final AddressingResult actual = AddressingMode.IMMEDIATE.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.IMMEDIATE.bytesToRead());
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
      final AddressingResult actual = AddressingMode.IMPLIED.fetch(null, null);

      // THEN
      assertThat(actual.value()).isEqualTo(AddressingMode.NOTHING_READ_VALUE);
      assertThat(actual.address()).isEqualTo(AddressingMode.IMPLIED_LOADED_ADDRESS);
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
      final int indirect = 0x6951;
      bus.writeAddressTo(0, indirect);
      final int address = 0x0012;
      bus.writeAddressTo(indirect, address);
      final int expectedValue = 0x13;
      bus.write(address, expectedValue);

      // WHEN
      final AddressingResult actual = AddressingMode.INDIRECT.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.INDIRECT.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("reproduces bug in 6502")
    void reproducesBug() {
      // GIVEN
      final int indirect = 0x69FF;
      bus.writeAddressTo(0, indirect);
      int nextIndirectDueToBug = 0x6900;
      final int address = 0x0012;
      bus.write(indirect, address);
      bus.write(nextIndirectDueToBug, address >> 8);
      final int expectedValue = 0x13;
      bus.write(address, expectedValue);

      // WHEN
      final AddressingResult actual = AddressingMode.INDIRECT.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.INDIRECT.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("INDIRECT_ZERO_PAGE_X addressing mode tests")
  class IndirectZeroPageOffsetXTests {
    @Test
    @DisplayName("loads the value at indirect address + X")
    void loadsValueAtIndirectAddressWithXOffset() {
      // GIVEN
      register.x(0x07);
      final int indirectZeroPageAddress = 0x0051;
      final int indirectZeroPageAddressPlusX = 0x0058;
      bus.write(0 ,indirectZeroPageAddress);
      final int zeroPageAddress = 0x0012;
      bus.write(indirectZeroPageAddressPlusX, zeroPageAddress);
      final int expectedValue = 0x13;
      bus.write(zeroPageAddress, expectedValue);

      // WHEN
      final AddressingResult actual = AddressingMode.INDIRECT_ZERO_PAGE_X.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(
          AddressingMode.INDIRECT_ZERO_PAGE_X.bytesToRead());
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
      bus.write(0, indirectZeroPageAddress);
      final int zeroPageAddress = 0x0012;
      bus.write(indirectZeroPAgeAddressPlusX, zeroPageAddress);
      final int expectedValue = 0x13;
      bus.write(zeroPageAddress, expectedValue);

      // WHEN
      final AddressingResult actual = AddressingMode.INDIRECT_ZERO_PAGE_X.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(
          AddressingMode.INDIRECT_ZERO_PAGE_X.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(zeroPageAddress);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("INDIRECT_ZERO_PAGE_Y addressing mode tests")
  class IndirectZeroPageOffsetYTests {
    @Test
    @DisplayName("loads the value at  indirect address + Y")
    void loadsValueAtIndirectAddressWithYOffset() {
      // GIVEN
      register.y(0x07);
      final int indirect = 0x0051;
      bus.write(0, indirect);
      final int address = 0x0012;
      final int addressPlusY = 0x0019;
      bus.write(indirect, address);
      final int expectedValue = 0x13;
      bus.write(addressPlusY, expectedValue);

      // WHEN
      AddressingResult actual = AddressingMode.INDIRECT_ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      assertThat(register.programCounter())
          .isEqualTo(AddressingMode.INDIRECT_ZERO_PAGE_Y.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(addressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("adds one cycle when indirect address + y resides in new memory page")
    void addsOneCycleWhenAddressPlusYIsOnNewPage() {
      // GIVEN
      register.y(0x07);
      final int indirect = 0x00FF;
      final int address = 0xFFFF;
      final int addressPlusY = 0x0006;
      bus.write(0, indirect);
      bus.write(indirect, address);
      bus.write((indirect + 1) & 0x00FF, address >> 8);
      final int expectedValue = 0x13;
      bus.write(addressPlusY, expectedValue);

      // WHEN
      AddressingResult actual = AddressingMode.INDIRECT_ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter()).isEqualTo(
          AddressingMode.INDIRECT_ZERO_PAGE_Y.bytesToRead());
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
      bus.write(0, relativeAddress);
      final int address = (relativeAddress + 1) & 0xFFFF;
      final int expectedValue = 0x13;
      bus.write(address, expectedValue);

      // WHEN
      final AddressingResult actual = AddressingMode.RELATIVE.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.RELATIVE.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(address);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }

    @Test
    @DisplayName("handlesNegativeAddress")
    void handlesNegativeRelativeAddress() {
      // GIVEN
      final int relativeAddress = -37;
      bus.write(0, relativeAddress);
      final int address = (relativeAddress + 1) & 0xFFFF;
      final int expectedValue = 0x13;
      bus.write(address, expectedValue);

      // WHEN
      AddressingResult actual = AddressingMode.RELATIVE.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.RELATIVE.bytesToRead());
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
      bus.write(0, zeroPageAddress);
      final int expectedValue = 0x13;
      bus.write(zeroPageAddress, expectedValue);

      // WHEN
      AddressingResult actual = AddressingMode.ZERO_PAGE.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.ZERO_PAGE.bytesToRead());
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
      bus.write(0, zeroPageAddress);
      final int expectedValue = 0x13;
      bus.write(zeroPageAddressPlusX, expectedValue);

      // WHEN
      AddressingResult actual = AddressingMode.ZERO_PAGE_X.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.INDIRECT_ZERO_PAGE_X.bytesToRead());
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
      bus.write(0, zeroPageAddress);
      final int expectedValue = 0x13;
      bus.write(zeroPageAddressPlusX, expectedValue);

      // WHEN
      AddressingResult actual = AddressingMode.ZERO_PAGE_X.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.INDIRECT_ZERO_PAGE_X.bytesToRead());
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
      bus.write(0, zeroPAgeAddress);
      final int expectedValue = 0x13;
      bus.write(zeroPageAddressPlusY, expectedValue);

      // WHEN
      AddressingResult actual = AddressingMode.ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.INDIRECT_ZERO_PAGE_Y.bytesToRead());
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
      bus.write(0, zeroPageAddress);
      final int expectedValue = 0x13;
      bus.write(zeroPageAddressPlusY, expectedValue);

      // WHEN
      AddressingResult actual = AddressingMode.ZERO_PAGE_Y.fetch(register, bus);

      // THEN
      assertThat(actual.register().programCounter())
          .isEqualTo(AddressingMode.INDIRECT_ZERO_PAGE_Y.bytesToRead());
      assertThat(actual.value()).isEqualTo(expectedValue);
      assertThat(actual.address()).isEqualTo(zeroPageAddressPlusY);
      assertThat(actual.additionalCyclesNeeded()).isEqualTo(0);
    }
  }
}