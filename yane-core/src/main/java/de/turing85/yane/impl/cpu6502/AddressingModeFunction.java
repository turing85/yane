package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import java.util.function.*;

interface AddressingModeFunction extends BiFunction<Register, CpuBus, AddressingResult> {
}