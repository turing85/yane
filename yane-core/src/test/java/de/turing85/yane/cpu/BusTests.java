package de.turing85.yane.cpu;

import static com.google.common.truth.Truth.*;
import static de.turing85.yane.cpu.Bus.*;

import org.junit.jupiter.api.*;

@DisplayName("Bus tests")
class BusTests {
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

      // WHEN
      final int actualValue = uut.write(address, expectedValue).read(address);

      // THEN
      assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("write masks address")
    void writeMasksAddress() {
      // GIVEN
      final int address = 0xFFFF_8817;
      final int sanitizedAddress = address & ADDRESS_MASK;
      final int expectedValue = 0x13;

      // WHEN
      final int actualValue = uut.write(address, expectedValue).read(sanitizedAddress);

      // THEN
      assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("read masks address")
    void readMasksAddress() {
      // GIVEN
      final int address = 0xFFFF_8817;
      final int sanitizedAddress = address & ADDRESS_MASK;
      final int expectedValue = 0x13;

      // WHEN
      final int actualValue = uut.write(sanitizedAddress, expectedValue).read(address);

      // THEN
      assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("write masks value")
    void writeMasksValue() {
      // GIVEN
      final int address = 0x8817;
      final int value = 0x113;
      final int expectedValue = value & VALUE_MASK;

      // WHEN
      final int actualValue = uut.write(address, value).read(address);

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

      // WHEN
      final int actualAddressValue = uut
          .writeAddressTo(address, expectedAddressValue)
          .readAddressFrom(address);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
      assertThat(uut.read(address)).isEqualTo(expectedAddressValueLow);
      assertThat(uut.read(address + 1)).isEqualTo(expectedAddressValueHigh);
    }

    @Test
    @DisplayName("write masks addresses")
    void writeMasksAddresses() {
      // GIVEN
      final int address = 0xFFFF_8817;
      final int sanitizedAddress = address & ADDRESS_MASK;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueLow = expectedAddressValue & 0xFF;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;

      // WHEN
      uut.writeAddressTo(address, expectedAddressValue);

      // THEN
      assertThat(uut.read(sanitizedAddress)).isEqualTo(expectedAddressValueLow);
      assertThat(uut.read(sanitizedAddress + 1)).isEqualTo(expectedAddressValueHigh);
    }

    @Test
    @DisplayName("read masks addresses")
    void readMasksAddresses() {
      // GIVEN
      final int address = 0xFFFF_8817;
      final int sanitizedAddress = address & ADDRESS_MASK;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueLow = expectedAddressValue & 0xFF;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;

      // WHEN
      final int actualAddressValue = uut
          .writeAddressTo(sanitizedAddress, expectedAddressValue)
          .readAddressFrom(address);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
      assertThat(uut.read(address)).isEqualTo(expectedAddressValueLow);
      assertThat(uut.read(address + 1)).isEqualTo(expectedAddressValueHigh);
    }

    @Test
    @DisplayName("write masks address value")
    void writeMasksAddressValue() {
      // GIVEN
      final int address = 0x8817;
      final int addressValue = 0xFFFF_1337;
      final int expectedAddressValue = addressValue & ADDRESS_MASK;
      final int expectedAddressValueLow = expectedAddressValue & 0xFF;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;

      // WHEN
      uut.writeAddressTo(address, addressValue).readAddressFrom(address);

      // THEN
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

      // WHEN
      final int actualAddressValue = uut
          .writeAddressTo(zeroPageAddress, expectedAddressValue)
          .readAddressFromZeroPage(zeroPageAddress);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
      assertThat(uut.read(zeroPageAddress)).isEqualTo(expectedAddressValueLow);
      assertThat(uut.read(zeroPageAddress + 1)).isEqualTo(expectedAddressValueHigh);
    }

    @Test
    @DisplayName("read masks address")
    void readMasksAddress() {
      // GIVEN
      final int zeroPageAddress = 0xFF12;
      final int sanitizedZeroPageAddress = zeroPageAddress & ZERO_PAGE_ADDRESS_MASK;
      final int expectedAddressValue = 0x1337;

      // WHEN
      final int actualAddressValue = uut
          .writeAddressTo(sanitizedZeroPageAddress, expectedAddressValue)
          .readAddressFromZeroPage(zeroPageAddress);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
    }

    @Test
    @DisplayName("read loops address")
    void readLoopsAddress() {
      // GIVEN
      final int zeroPageAddress = 0x00FF;
      final int nextZeroPageAddress = 0x0000;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueLow = expectedAddressValue & VALUE_MASK;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;

      // WHEN
      final int actualAddressValue = uut
          .write(zeroPageAddress, expectedAddressValueLow)
          .write(nextZeroPageAddress, expectedAddressValueHigh)
          .readAddressFromZeroPage(zeroPageAddress);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
    }
  }

  @Nested
  @DisplayName("Stack Write-Read tests")
  class StackWriteReadTests {
    @Test
    @DisplayName("write and then read")
    void writeRead() {
      // GIVEN
      final int stackOffset = 0x12;
      final int addressWrittenTo = stackOffset | STACK_START_ADDRESS;
      final int expectedValue = 0x13;

      // WHEN
      final int actualValue = uut.writeToStack(stackOffset, expectedValue).read(addressWrittenTo);

      // THEN
      assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("write masks stack offset")
    void writeMasksStackOffset() {
      // GIVEN
      final int stackOffset = 0xFF12;
      final int sanitizedStackOffset = stackOffset & STACK_OFFSET_MASK;
      final int addressWrittenTo = sanitizedStackOffset | STACK_START_ADDRESS;
      final int expectedValue = 0x13;

      // WHEN
      final int actualValue = uut.writeToStack(stackOffset, expectedValue).read(addressWrittenTo);

      // THEN
      assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("read masks stack offset")
    void readMasksStackOffset() {
      // GIVEN
      final int stackOffset = 0xFF12;
      final int sanitizedStackOffset = stackOffset & STACK_OFFSET_MASK;
      final int addressReadFrom = sanitizedStackOffset | STACK_START_ADDRESS;
      final int expectedValue = 0x13;

      // WHEN
      final int actualValue = uut.write(addressReadFrom, expectedValue).readFromStack(stackOffset);

      // THEN
      assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("write masks value")
    void writeMasksValue() {
      // GIVEN
      final int stackOffset = 0x12;
      final int addressWrittenTo = stackOffset | STACK_START_ADDRESS;
      final int value = 0xFF13;
      final int expectedValue = value & VALUE_MASK;

      // WHEN
      final int actualValue = uut.writeToStack(stackOffset, value).read(addressWrittenTo);

      // THEN
      assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("write masks address value")
    void writeMasksAddressValue() {
      // GIVEN
      final int stackOffset = 0xFF12;
      final int sanitizedStackOffset = stackOffset & STACK_OFFSET_MASK;
      final int value = 0xFF13;
      final int expectedValue = value & VALUE_MASK;

      // WHEN
      final int actualValue = uut
          .writeToStack(stackOffset, value)
          .readFromStack(sanitizedStackOffset);

      // THEN
      assertThat(actualValue).isEqualTo(expectedValue);
    }
  }

  @Nested
  @DisplayName("Stack address read write tests")
  class StackAddressWriteReadTests {
    @Test
    @DisplayName("write and then read")
    void writeRead() {
      // GIVEN
      final int stackOffsetWrite = 0x12;
      final int stackOffsetRead = stackOffsetWrite - 1;
      final int expectedAddressValue = 0x1337;

      // WHEN
      final int actualValue = uut
          .writeAddressToStack(stackOffsetWrite, expectedAddressValue)
          .readAddressFromStack(stackOffsetRead);

      // THEN
      assertThat(actualValue).isEqualTo(expectedAddressValue);
    }

    @Test
    @DisplayName("write masks offsets")
    void writeMasksOffsets() {
      // GIVEN
      final int stackOffsetWrite = 0xFF12;
      final int sanitizedStackOffset = stackOffsetWrite & STACK_OFFSET_MASK;
      final int stackAddressHigh = sanitizedStackOffset | STACK_START_ADDRESS;
      final int stackAddressLow = (sanitizedStackOffset - 1) | STACK_START_ADDRESS;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueLow = expectedAddressValue & VALUE_MASK;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;

      // WHEN
      uut.writeAddressToStack(stackOffsetWrite, expectedAddressValue);

      // THEN
      assertThat(uut.read(stackAddressLow)).isEqualTo(expectedAddressValueLow);
      assertThat(uut.read(stackAddressHigh)).isEqualTo(expectedAddressValueHigh);
    }

    @Test
    @DisplayName("read masks offsets")
    void readMasksOffsets() {
      // GIVEN
      final int stackOffsetWrite = 0x12;
      final int stackAddressWrite = stackOffsetWrite | STACK_START_ADDRESS;
      final int stackOffsetRead = 0xFF11;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;
      final int expectedAddressValueLow = expectedAddressValue & VALUE_MASK;

      // WHEN
      final int actualValue = uut
          .write(stackAddressWrite, expectedAddressValueHigh)
          .write(stackAddressWrite - 1, expectedAddressValueLow)
          .readAddressFromStack(stackOffsetRead);

      // THEN
      assertThat(actualValue).isEqualTo(expectedAddressValue);
    }

    @Test
    @DisplayName("write masks addressValue")
    void writeMasksAddressValue() {
      // GIVEN
      final int stackOffsetWrite = 0x12;
      final int stackOffsetRead = stackOffsetWrite - 1;
      final int addressValue = 0xFFFF_1337;
      final int expectedAddressValue = addressValue & ADDRESS_MASK;

      // WHEN
      final int actualValue = uut
          .writeAddressToStack(stackOffsetWrite, addressValue)
          .readAddressFromStack(stackOffsetRead);

      // THEN
      assertThat(actualValue).isEqualTo(expectedAddressValue);
    }

    @Test
    @DisplayName("Write loops address")
    void writeLoopsAddress() {
      // GIVEN
      final int stackOffsetWrite = 0x00;
      final int stackOffsetRead = 0xFF;
      final int stackAddressHigh = stackOffsetWrite | STACK_START_ADDRESS;
      final int stackAddressLow = stackOffsetRead | STACK_START_ADDRESS;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueLow = expectedAddressValue & VALUE_MASK;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;

      // WHEN
      uut.writeAddressToStack(stackOffsetWrite, expectedAddressValue);

      // THEN
      assertThat(uut.read(stackAddressLow)).isEqualTo(expectedAddressValueLow);
      assertThat(uut.read(stackAddressHigh)).isEqualTo(expectedAddressValueHigh);
    }

    @Test
    @DisplayName("Read loops address")
    void readLoopsAddress() {
      // GIVEN
      final int stackOffsetWrite = 0x00;
      final int stackOffsetRead = 0xFF;
      final int stackAddressHigh = stackOffsetWrite | STACK_START_ADDRESS;
      final int stackAddressLow = stackOffsetRead | STACK_START_ADDRESS;
      final int expectedAddressValue = 0x1337;
      final int expectedAddressValueLow = expectedAddressValue & VALUE_MASK;
      final int expectedAddressValueHigh = expectedAddressValue >> 8;

      // WHEN
      final int actualAddressValue = uut
          .write(stackAddressHigh, expectedAddressValueHigh)
          .write(stackAddressLow, expectedAddressValueLow)
          .readAddressFromStack(stackOffsetRead);

      // THEN
      assertThat(actualAddressValue).isEqualTo(expectedAddressValue);
    }
  }
}