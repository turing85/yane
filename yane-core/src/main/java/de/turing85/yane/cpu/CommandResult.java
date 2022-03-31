package de.turing85.yane.cpu;

import de.turing85.yane.*;
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
  /**
   * The updated {@link Register}.
   */
  Register register;

  /**
   * The {@link CpuBus} used by the command.
   */
  CpuBus bus;

  /**
   * The number of additional cycles needed by the {@link Command}.
   */
  int additionalCyclesNeeded;
}