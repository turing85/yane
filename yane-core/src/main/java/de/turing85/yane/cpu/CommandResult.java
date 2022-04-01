package de.turing85.yane.cpu;

import lombok.*;

/**
 * <p>Represents the state change introduced by the execution of a {@link Command}.</p>
 *
 * <p>In particular, this class encapsulates:</p>
 *
 * <ul>
 *   <li>The {@link Register}-state after the command execution ({@link #register})
 *   <li>The {@link Bus}-state after the command execution ({@link #bus})
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
   * The {@link Bus} used by the command.
   */
  Bus bus;

  /**
   * The number of additional cycles needed by the {@link Command}.
   */
  int additionalCyclesNeeded;
}