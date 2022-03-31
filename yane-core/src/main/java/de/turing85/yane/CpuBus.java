package de.turing85.yane;

/**
 * <p>The bus used to read and write values.</p>
 *
 * <p>Addressing is done in the 16-bit domain. Each address holds one 8-bit value.</p>
 *
 * <p>Even though addresses and values are {@code int}s, only the lower 16 bit and 8 bits
 * respectively are used.</p>
 */
public interface CpuBus {

  /**
   * Writes one 8-bit value to an address.
   *
   * @param address the address to write to
   * @param value the value to write
   */
  void write(int address, int value);

  /**
   * Reads one 8-bit value from {@code address}.
   *
   * @param address the address to read from
   * @return the value read
   */
  int read(int address);
}