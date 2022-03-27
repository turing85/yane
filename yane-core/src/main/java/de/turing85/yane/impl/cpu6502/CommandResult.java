package de.turing85.yane.impl.cpu6502;

import lombok.*;

@Value(staticConstructor = "of")
class CommandResult {
  Register register;
  int additionalCyclesNeeded;
}