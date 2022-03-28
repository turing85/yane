package de.turing85.yane.impl.cpu6502;

import lombok.*;

@Value(staticConstructor = "of")
class AddressResult {
  Register register;
  int value;
  int address;
  int additionalCyclesNeeded;
}