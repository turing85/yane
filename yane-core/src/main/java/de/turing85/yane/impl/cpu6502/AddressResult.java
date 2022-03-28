package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import lombok.*;

@Value(staticConstructor = "of")
class AddressResult {
  Register register;
  CpuBus bus;
  int value;
  int address;
  int additionalCyclesNeeded;
}