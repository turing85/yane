package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import java.util.function.*;

interface AddressModeFunction extends BiFunction<Register, CpuBus, AddressingResult> {
  default AddressingResult fetch(Register register, CpuBus bus) {
    return apply(register, bus);
  }
}