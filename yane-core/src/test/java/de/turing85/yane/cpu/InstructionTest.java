package de.turing85.yane.cpu;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.junit.jupiter.api.*;

class InstructionTest {
  @Test
  void foo() {
    System.out.println(Instruction.INSTRUCTIONS.size());
    System.out.println(Instruction.INSTRUCTIONS_BY_MNEMONIC.entrySet().stream()
            .filter(Predicate.not(entry -> "??? ???".equals(entry.getKey())))
        .filter(entry -> entry.getValue().size() > 1)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }
}