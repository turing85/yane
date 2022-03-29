package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
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
 *   <li> the {@link #value} at the calculated address (8-bit)
 *   <li> the calculated {@link #address} (16-bit)
 *   <li> the number of additional cycles needed by the addressing mode. This depends on the data
 *        read during the operation.
 * </ul>
 */
@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class AddressingResult {
  Register register;
  CpuBus bus;
  int address;
  int value;
  int additionalCyclesNeeded;

  AddressingResult(Register register, CpuBus bus, int address, int value) {
    this(register, bus, address, value, 0);
  }
}