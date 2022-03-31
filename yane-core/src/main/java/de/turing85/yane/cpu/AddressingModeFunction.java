package de.turing85.yane.cpu;

import de.turing85.yane.*;
import java.util.function.*;

/**
 * <p>Represents a function that fetches the data from the {@link Register} and/or the{@link
 * CpuBus}.</p>
 *
 * <p>Normally, data is fetched from the bus. There are some instructions, however, that require
 * data from the register, possibly not hitting the bus at all (e.g. {@link Command#TAX}, which
 * needs the value of {@link Register#a}.</p>
 *
 * <p>Furthermore, data fetches may occur on the {@link Register#programCounter}. In those cases,
 * the program counter must be incremented.</p>
 *
 * <p>The call only represents data <strong>fetching</strong>. If, for example, during the execution
 * of a command is then subsequently written back to the bus, then this is not represented by this
 * call.</p>
 */
interface AddressingModeFunction extends BiFunction<Register, CpuBus, AddressingResult> {

  /**
   * Reads the necessary data from the register and/or bus.
   *
   * @param register the register to fetch from
   * @param bus the bus to fetch from
   * @return the result of the fetch
   */
  default AddressingResult fetch(Register register, CpuBus bus) {
    return apply(register, bus);
  }
}