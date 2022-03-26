package de.turing85.yane.api;

import java.util.*;

public interface Cpu<I extends Instruction> {
  Set<I> instructions();
}
