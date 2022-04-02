package de.turing85.yane.cpu;

import static com.google.common.truth.Truth.*;
import static de.turing85.yane.cpu.Bus.*;

import org.junit.jupiter.api.*;

class BusTest {
  private final Bus uut = new Bus();

  @Nested
  @DisplayName("Write-Read tests")
  class WriteReadTests {
    @Test
    @DisplayName("write and then read")
    void writeRead() {
      // GIVEN
      final int address = 0x8817;
      final int expectedValue = 0x13;
      uut.write(address, expectedValue);

      // WHEN
      final int actualValue = uut.read(address);

      // THEN
      assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("write address is masked")
    void writeAddressIsMasked() {
      // GIVEN
      final int address = 0xFFFF_8817;
      final int maskedAddress = address & ADDRESS_MASK;
      final int expectedValue = 0x13;
      uut.write(address, expectedValue);

      // WHEN
      final int actualValue = uut.read(maskedAddress);

      // THEN
      assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("read address is masked")
    void readAddressIsMasked() {
      // GIVEN
      final int address = 0xFFFF_8817;
      final int maskedAddress = address & ADDRESS_MASK;
      final int expectedValue = 0x13;
      uut.write(maskedAddress, expectedValue);

      // WHEN
      final int actualValue = uut.read(address);

      // THEN
      assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("value is masked")
    void valueIsMasked() {
      // GIVEN
      final int address = 0x8817;
      final int value = 0x113;
      final int expectedValue = value & VALUE_MASK;
      uut.write(address, value);

      // WHEN
      final int actualValue = uut.read(address);

      // THEN
      assertThat(actualValue).isEqualTo(expectedValue);
    }
  }

  @Nested
  @DisplayName("Address Write-Read tests")
  class AddressWriteReadTests {
    @Test
    @DisplayName("write and then read")
    void writeRead() {
      // GIVEN
      final int address = 0x8817;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueLow = expectedAddressValue & 0xFF;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;
      uut.writeAddressToBus(address, expectedAddressValue);

      // WHEN
      final int actualAddressValue = uut.readAddressFrom(address);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
      assertThat(uut.read(address)).isEqualTo(expectedAddressValueLow);
      assertThat(uut.read(address + 1)).isEqualTo(expectedAddressValueHigh);
    }

    @Test
    @DisplayName("write address is masked")
    void writeAddressIsMasked() {
      // GIVEN
      final int address = 0xFFFF_8817;
      final int maskedAddress = address & ADDRESS_MASK;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueLow = expectedAddressValue & 0xFF;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;
      uut.writeAddressToBus(address, expectedAddressValue);

      // WHEN
      final int actualAddressValue = uut.readAddressFrom(maskedAddress);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
      assertThat(uut.read(maskedAddress)).isEqualTo(expectedAddressValueLow);
      assertThat(uut.read(maskedAddress + 1)).isEqualTo(expectedAddressValueHigh);
    }

    @Test
    @DisplayName("read address is masked")
    void readAddressIsMasked() {
      // GIVEN
      final int address = 0xFFFF_8817;
      final int maskedAddress = address & ADDRESS_MASK;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueLow = expectedAddressValue & 0xFF;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;
      uut.writeAddressToBus(maskedAddress, expectedAddressValue);

      // WHEN
      final int actualAddressValue = uut.readAddressFrom(address);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
      assertThat(uut.read(address)).isEqualTo(expectedAddressValueLow);
      assertThat(uut.read(address + 1)).isEqualTo(expectedAddressValueHigh);
    }

    @Test
    @DisplayName("address value is masked")
    void addressValueIsMasked() {
      // GIVEN
      final int address = 0x8817;
      final int addressValue = 0xFFFF_1337;
      final int expectedAddressValue = addressValue & ADDRESS_MASK;
      final int expectedAddressValueLow = expectedAddressValue & 0xFF;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;
      uut.writeAddressToBus(address, addressValue);

      // WHEN
      final int actualAddressValue = uut.readAddressFrom(address);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
      assertThat(uut.read(address)).isEqualTo(expectedAddressValueLow);
      assertThat(uut.read(address + 1)).isEqualTo(expectedAddressValueHigh);
    }
  }

  @Nested
  @DisplayName("Zero Page Address Write-Read tests")
  class ZeroPageAddressWriteReadTests {
    @Test
    @DisplayName("write and then read")
    void writeRead() {
      // GIVEN
      final int zeroPageAddress = 0x0012;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueLow = expectedAddressValue & 0xFF;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;
      uut.writeAddressToBus(zeroPageAddress, expectedAddressValue);

      // WHEN
      final int actualAddressValue = uut.readAddressFromZeroPage(zeroPageAddress);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
      assertThat(uut.read(zeroPageAddress)).isEqualTo(expectedAddressValueLow);
      assertThat(uut.read(zeroPageAddress + 1)).isEqualTo(expectedAddressValueHigh);
    }

    @Test
    @DisplayName("zero page read address is masked")
    void zeroPageReadAddressIsMasked() {
      // GIVEN
      final int zeroPageAddress = 0xFF12;
      final int maskedZeroPageAddress = zeroPageAddress & ZERO_PAGE_ADDRESS_MASK;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueLow = expectedAddressValue & 0xFF;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;
      uut.writeAddressToBus(maskedZeroPageAddress, expectedAddressValue);

      // WHEN
      final int actualAddressValue = uut.readAddressFromZeroPage(zeroPageAddress);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
      assertThat(uut.read(maskedZeroPageAddress)).isEqualTo(expectedAddressValueLow);
      assertThat(uut.read(maskedZeroPageAddress + 1)).isEqualTo(expectedAddressValueHigh);
    }

    @Test
    @DisplayName("zero page address loops back on zero page")
    void zeroPageAddressLoopsBackOnZeroPage() {
      // GIVEN
      final int zeroPageAddress = 0x00FF;
      final int nextZeroPageAddress = 0x0000;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueLow = expectedAddressValue & VALUE_MASK;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;
      uut.writeAddressToBus(zeroPageAddress, expectedAddressValueLow);
      uut.writeAddressToBus(nextZeroPageAddress, expectedAddressValueHigh);

      // WHEN
      final int actualAddressValue = uut.readAddressFromZeroPage(zeroPageAddress);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
    }
  }
}