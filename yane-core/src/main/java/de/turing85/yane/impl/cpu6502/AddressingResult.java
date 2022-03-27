package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import lombok.*;

@Value(staticConstructor = "of")
class AddressingResult {
  Register register;
  CpuBus bus;
  int address;
  int additionalCyclesNeeded;

  int readValueFromAddress() {
    return bus().read(address());
  }
}