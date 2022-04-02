package de.turing85.yane.cpu;

/**
 * <p>The bus used to read and write values.</p>
 *
 * <p>Addressing is done in the 16-bit domain. Each address holds one byte value.</p>
 *
 * <p>Even though addresses and values are {@code int}s, only the lower 16 bit and 8 bits
 * respectively are used.</p>
 */
public class Bus {
  /**
   * Bit-Mask to sanitize values (8-bit values) before writing to the bus.
   */
  static final int VALUE_MASK = 0xFF;

  /**
   * Bit-mask to sanitize addresses (16-bit addresses) before writing to the bus.
   */
  static final int ADDRESS_MASK = 0xFFFF;

  /**
   * Bit-mask to sanitize zero-page addresses (8-bit addresses) before writing to the bus.
   */
  static final int ZERO_PAGE_ADDRESS_MASK = 0xFF;

  /**
   * An {@code int[]}, representing the 64 kilobyte (2^16 byte) of memory, addressable through the
   * 16-bit memory addresses.
   */
  private final int[] memory = new int[ADDRESS_MASK + 1];

  /**
   * <p>Writes an address value (16 bit) to the bus.</p>
   *
   * <p>The address value is written in little endianness, i.e. the higher 8 bits are written
   * to {@code address}, the lower 8 bits are written to {@code address + 1}.</p>
   *
   * @param address
   *     the address to write to
   * @param addressValue
 *     the 16-bit value to write
   */
  void writeAddressToBus(int address, int addressValue) {
    write(address, addressValue);
    write(address + 1, addressValue >> 8);
  }

  /**
   * Writes one byte value to an address.
   *
   * @param address
   *     the address to write to
   * @param value
 *     the value to write
   */
  void write(int address, int value) {
    final int sanitizedAddress = address & ADDRESS_MASK;
    final int sanitizedValue = value & VALUE_MASK;
    memory[sanitizedAddress] = sanitizedValue;
  }

  /**
   * <p>Reads an address value (16 bit) from the bus.</p>
   *
   * <p>The address value is read in little endianness, i.e. the higher 8 bits are read
   * from {@code address}, the lower 8 bits are read from {@code address + 1}.</p>
   *
   * @param address
   *     the address to read from
   *
   * @return the address value read
   */
  int readAddressFrom(int address) {
    final int addressValueLow = read(address);
    final int addressValueHigh = read(address + 1);
    return addressValueLow | (addressValueHigh << 8);
  }

  /**
   * <p>Reads an address value (16 bit) from the bus on the zero memory page.</p>
   *
   * <p>The zeroPageAddress value is read in little endianness, i.e. the higher 8 bits are read
   * from {@code zeroPageAddress}, the lower 8 bits are read from {@code zeroPageAddress + 1}.</p>
   *
   * <p>If {@code zeroPageAddress} is {@code 0x00FF}, then the 2nd part of the zeroPageAddress is
   * read from {@code 0x0000}, not from {@code 0x0100}.</p>
   *
   * @param zeroPageAddress
   *     the zeroPageAddress to read from
   *
   * @return the zeroPageAddress value read
   */
  int readAddressFromZeroPage(int zeroPageAddress) {
    final int sanitizedZeroPageAddress = zeroPageAddress & ZERO_PAGE_ADDRESS_MASK;
    final int addressValueLow = read(sanitizedZeroPageAddress);
    final int addressValueHigh =
        read((sanitizedZeroPageAddress + 1) & ZERO_PAGE_ADDRESS_MASK);
    return addressValueLow | (addressValueHigh << 8);
  }

  /**
   * Reads one byte value from {@code address}.
   *
   * @param address
   *     the address to read from
   *
   * @return the value read
   */
  int read(int address) {
    final int sanitizedAddress = address & ADDRESS_MASK;
    return memory[sanitizedAddress];
  }
}