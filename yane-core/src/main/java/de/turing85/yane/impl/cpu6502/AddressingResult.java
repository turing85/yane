package de.turing85.yane.impl.cpu6502;

import lombok.*;

@Value(staticConstructor = "of")
public class AddressingResult {
  Register register;
  int valueRead;
  int addresssLoaded;
  int additionalCyclesNeeded;
}