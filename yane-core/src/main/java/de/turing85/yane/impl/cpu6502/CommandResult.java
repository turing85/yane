package de.turing85.yane.impl.cpu6502;

import lombok.*;

@Value(staticConstructor = "of")
public class CommandResult {
  Register register;
  int additionalCyclesNeeded;
}