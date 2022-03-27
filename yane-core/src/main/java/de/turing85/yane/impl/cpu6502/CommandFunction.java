package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;

interface CommandFunction {
  CommandResult execute(Register register, CpuBus bus, AddressingMode addressingMode);
}
