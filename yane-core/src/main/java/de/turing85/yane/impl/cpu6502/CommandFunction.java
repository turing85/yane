package de.turing85.yane.impl.cpu6502;

interface CommandFunction {
  CommandResult execute(AddressingResult addressingResult);
}
