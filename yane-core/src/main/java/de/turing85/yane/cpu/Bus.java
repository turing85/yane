package de.turing85.yane.cpu;

/**
 * <p>The bus used to read and write values.</p>
 *
 * <p>Addressing is done in the 16-bit domain. Each address holds a one byte value.</p>
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
   * Start address for the stack. The 1st memory page is used as stack memory.
   */
  static final int STACK_START_ADDRESS = 0x0100;

  /**
   * <p>Stack pointer mask.</p>
   *
   * <p>Write to the stack are writes to the 1st memory page, thus the stack pointer must be a
   * value between {@code 0} and {@code 255}.</p>
   */
  static final int STACK_OFFSET_MASK = 0xFF;

  /**
   * An {@code int[]}, representing the 64 kilobyte (2^16 byte) of memory, addressable through the
   * 16-bit memory addresses.
   */
  private final int[] memory = new int[ADDRESS_MASK + 1];

  /**
   * Writes a one byte value to an address.
   *
   * @param address
   *     the address to write to
   * @param value
   *     the value to write
   *
   * @return {@code this}, for method chaining
   */
  Bus write(int address, int value) {
    final int sanitizedAddress = address & ADDRESS_MASK;
    final int sanitizedValue = value & VALUE_MASK;
    memory[sanitizedAddress] = sanitizedValue;
    return this;
  }

  /**
   * Reads a one byte value from {@code address}.
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
   *
   * @return {@code this}, for method chaining
   */
  Bus writeAddressTo(int address, int addressValue) {
    return write(address + 1, addressValue >> 8)
        .write(address, addressValue & VALUE_MASK);
  }

  /**
   * <p>Reads an address value (16 bit) from the bus.</p>
   *
   * <p>The address value is read in little endianness, i.e. the lower 8 bits are read
   * from {@code address}, the higher 8 bits are read from {@code address - 1}.</p>
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
   * <p>Writes a one byte value to the stack memory.</p>
   *
   * @param stackOffset
   *     the stack offset to write to. The final address written to will be {@code (stackOffset &
   *     0xFF) | }{@link #STACK_START_ADDRESS}
   * @param value
   *     the value to write.
   *
   * @return {@code this}, for method chaining
   */
  Bus writeToStack(int stackOffset, int value) {
    final int sanitizedStackOffset = stackOffset & STACK_OFFSET_MASK;
    final int stackAddress = sanitizedStackOffset | STACK_START_ADDRESS;
    final int sanitizedValue = value & VALUE_MASK;
    return write(stackAddress, sanitizedValue);
  }

  /**
   * <p>Reads a one byte value to the stack memory.</p>
   *
   * @param stackOffset
   *     the stack offset to read from. The final address read from will be {@code (stackOffset &
   *     0xFF) | }{@link #STACK_START_ADDRESS}
   *
   * @return the value read
   */
  int readFromStack(int stackOffset) {
    final int sanitizedStackOffset = (stackOffset & STACK_OFFSET_MASK);
    final int stackAddress = sanitizedStackOffset | STACK_START_ADDRESS;
    return read(stackAddress);
  }

  /**
   * <p>Writes an address value (16 bit) to the stack.</p>
   *
   * <p>The address value is written in little endianness, i.e. the higher 8 bits are written
   * to {@code (stackOffset & 0xFF) | }{@link #STACK_START_ADDRESS}, the lower 8 bits are written to
   * {@code ((stackOffset - 1) & 0xFF) | }{@link #STACK_START_ADDRESS}.</p>
   *
   * @param stackOffset
   *     the stack offset to write to
   * @param addressValue
   *     the 16-bit value to write
   *
   * @return {@code this}, for method chaining
   */
  Bus writeAddressToStack(int stackOffset, int addressValue) {
    final int sanitizedStackOffset = stackOffset & STACK_OFFSET_MASK;
    final int stackAddress = sanitizedStackOffset | STACK_START_ADDRESS;
    final int sanitizedNextStackOffset = (sanitizedStackOffset - 1) & STACK_OFFSET_MASK;
    final int nextStackAddress = sanitizedNextStackOffset | STACK_START_ADDRESS;
    return write(stackAddress, addressValue >> 8)
        .write(nextStackAddress, addressValue & VALUE_MASK);
  }

  /**
   * <p>Read an address value (16 bit) to the stack.</p>
   *
   * <p>The address value is read in little endianness, i.e. the lower 8 bits are read
   * to {@code (stackOffset & 0xFF) | }{@link #STACK_START_ADDRESS}, the higher 8 bits are read from
   * {@code ((stackOffset + 1) & 0xFF) | }{@link #STACK_START_ADDRESS}.</p>
   *
   * @param stackOffset
   *     the stack offset to read from
   *
   * @return the 16-bit value read
   */
  int readAddressFromStack(int stackOffset) {
    final int sanitizedStackOffset = stackOffset & STACK_OFFSET_MASK;
    final int stackAddress = sanitizedStackOffset | STACK_START_ADDRESS;
    final int sanitizedPreviousStackOffset = (stackOffset + 1) & STACK_OFFSET_MASK;
    final int previousStackAddress = sanitizedPreviousStackOffset | STACK_START_ADDRESS;
    return read(stackAddress) | (read(previousStackAddress) << 8);
  }
}