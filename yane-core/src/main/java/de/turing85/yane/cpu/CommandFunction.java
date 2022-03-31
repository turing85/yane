package de.turing85.yane.cpu;

/**
 * <p>Represents a function that operates on fetched data, possibly mutating the {@link Register}
 * and {@link CpuBus}.</p>
 *
 * <p>The data is represented as {@link AddressingResult}. This class also encapsulates the
 * {@link Register} and {@link CpuBus}. Mutation of the register is necessary to at least mutate the
 * {@link Register#programCounter}.</p>
 *
 * <p>The state changed introduced by the execution is reflected in the returned
 * {@link CommandResult}.</p>
 */
interface CommandFunction {
  /**
   * Execute on the given {@link AddressingResult}.
   *
   * @param addressingResult the {@link AddressingResult}, representing the data on which the
   *        command operates (including the {@link Register} and
   *        {@link CpuBus}).
   * @return the {@link CommandResult}, encapsulating the state after the command execution.
   */
  CommandResult execute(AddressingResult addressingResult);
}
