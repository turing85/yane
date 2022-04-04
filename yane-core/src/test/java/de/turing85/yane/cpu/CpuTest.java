package de.turing85.yane.cpu;

import static com.google.common.truth.Truth.assertThat;

import de.turing85.yane.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CpuTest {

  @Test
  @DisplayName("execute multiplication program")
  void executeMultiplicationProgram() {
    int lhs = 5;
    int rhs = 10;
    final Bus bus = new Bus();

    // write operands to memory page 0, index 0 and 1
    bus.write(0x00, lhs);
    bus.write(0x01, rhs);

    // start the program on memory page 6
    bus.writeAddressTo(Cpu.RESET_READ_ADDRESS, 0x0600);

    // load lhs into accumulator
    bus.write(0x0600, 0xA5);
    bus.write(0x0601, 0x00);

    // jump to address 0x0700 to terminate program
    bus.write(0x0602, 0x30);
    bus.write(0x0603, 0xFC);

    // load lhs into x
    bus.write(0x0604, 0xA6);
    bus.write(0x0605, 0x01);

    // jump to address 0x0700 to terminate program
    bus.write(0x0606, 0x30);
    bus.write(0x0607, 0xF8);

    // Decrement x by one, accumulator holds a already once
    bus.write(0x0608, 0xCA);

    // Add lhs once more to a
    bus.write(0x0609, 0x65);
    bus.write(0x060A, 0x00);

    // Decrement x by 1
    bus.write(0x060B, 0xCA);

    // Jump back if x is not zero
    bus.write(0x060C, 0xD0);
    bus.write(0x060D, 0xFC);

    // Jump to address 0x0700 to terminate program
    bus.write(0x060E, 0x4C);
    bus.writeAddressTo(0x060F, 0x0700);

    final Cpu cpu = new Cpu(bus, new Clock());

    boolean finished = false;
    while (cpu.register().programCounter() != 0x0700 || !finished) {
      finished = cpu.tick();
    }

    assertThat(cpu.register().a()).isEqualTo(lhs * rhs);
    assertThat(cpu.register().x()).isEqualTo(0);
    assertThat(cpu.cycles()).isEqualTo(94);
  }

}