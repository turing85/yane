package de.turing85.yane.impl.cpu6502;

import lombok.*;

@Value(staticConstructor = "of")
@AllArgsConstructor
public class AddressingResult {
  Register register;
  byte valueRead;
  int additionalCyclesNeeded;
}