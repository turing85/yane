package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import lombok.*;

/**
 * <p>Represents the state change introduced by the execution of a {@link Command}.</p>
 *
 * <p>In particular, this class encapsulates:</p>
 *
 * <ul>
 *   <li>The {@link Register}-state after the command execution ({@link #register})
 *   <li>The {@link CpuBus}-state after the command execution ({@link #bus})
 *   <li>The number of additional cycles needed by the {@link CommandFunction}-execution and the
 *   {@link AddressingModeFunction}-fetching ({@link #additionalCyclesNeeded})
 * </ul>
 */
@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
class CommandResult {
  Register register;
  CpuBus bus;
  int additionalCyclesNeeded;
}