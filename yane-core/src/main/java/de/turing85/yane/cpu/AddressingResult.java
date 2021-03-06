package de.turing85.yane.cpu;

import lombok.*;

/**
 * <p>Represents the result of an {@link AddressingMode} execution.</p>
 *
 * <p>Instances of this class also function as input for the {@link CommandFunction} lambda.</p>
 *
 * <p>The result contains:</p>
 * <ul>
 *   <li> the updated {@link #register},
 *   <li> tue updated {@link #bus},
 *   <li> the {@link #value} at the calculated address (8 bit)
 *   <li> the calculated {@link #address} (16 bits)
 *   <li> the number of additional cycles needed by the addressing mode. This depends on the bytes
 *        read during the operation.
 * </ul>
 */
@Value
@AllArgsConstructor
public class AddressingResult {
  /**
   * The updated {@link Register}.
   */
  Register register;

  /**
   * The bus where {@link #address} and {@link #value} were read from.
   */
  Bus bus;

  /**
   * The address calculated by the {@link AddressingMode}.
   */
  int address;

  /**
   * The value read by the {@link AddressingMode}.
   */
  int value;

  /**
   * The number of additional cycles needed by the {@link AddressingMode}.
   */
  int additionalCyclesNeeded;

  /**
   * <p>Constructor.</p>
   *
   * <p>{@link #additionalCyclesNeeded} is set to {@code 0}.</p>
   *
   * @param register
   *     the {@link #register}
   * @param bus
   *     the {@link #bus}
   * @param address
   *     the {@link #address}
   * @param value
   *     {@link #value}
   */
  AddressingResult(Register register, Bus bus, int address, int value) {
    this(register, bus, address, value, 0);
  }
}