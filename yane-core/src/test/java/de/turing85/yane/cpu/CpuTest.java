package de.turing85.yane.cpu;

import static com.google.common.truth.Truth.assertThat;

import de.turing85.yane.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CpuTest {

  @CsvSource({
      "5, 10, 62",
      "0, 10, 17",
      "10, 0, 27",
      "2, 100, 38",
      "100, 2, 822"})
  @ParameterizedTest(name = "{index} => lhs={0}, rhs={1}, expectedCycles={2}")
  @DisplayName("execute multiplication program")
  void executeMultiplicationProgram(int lhs, int rhs, int expectedCycles) {
    final int expectedProduct = lhs * rhs;

    final Bus bus = new Bus();

    // write operands to memory page 0, index 50 and 51
    bus.write(0x0050, lhs);
    bus.write(0x0051, rhs);

    // write memory addressees to rhs to page 0
    bus.write(lhs, 0x51);

    // start the program on memory page 6
    bus.writeAddressTo(Cpu.RESET_READ_ADDRESS, 0x0600);

    // load lhs into accumulator
    bus.write(0x0600, 0xA5);
    bus.write(0x0601, 0x50);

    // jump to address 0x06B0 to terminate program
    bus.write(0x0602, 0xF0);
    bus.write(0x0603, 0x1D);

    // transfer accumulator to x
    bus.write(0x0604, 0xAA);

    // load rhs into accumulator
    bus.write(0x0605, 0xA1);
    bus.write(0x0606, 0x00);

    // jump to address 0x0620 to terminate program
    bus.write(0x0607, 0xF0);
    bus.write(0x0608, 0x18);

    // Decrement x by one, accumulator holds lhs already once
    bus.write(0x0609, 0xCA);

    // Add rhs once more to a
    bus.write(0x060A, 0x65);
    bus.write(0x060B, 0x51);

    // Decrement x by 1
    bus.write(0x060C, 0xCA);

    // Jump back if x is not zero
    bus.write(0x060D, 0xD0);
    bus.write(0x060E, 0xFC);

    // Jump to address 0x0620 to terminate program
    bus.write(0x060F, 0x4C);
    bus.writeAddressTo(0x0610, 0x0620);

    // Write accumulator to stack
    bus.write(0x0620, 0x48);

    final Cpu cpu = new Cpu(bus, new Clock());

    boolean finished = false;
    while (cpu.register().programCounter() != 0x0621 || !finished) {
      finished = cpu.tick();
    }

    assertThat(bus.readFromStack(cpu.register().stackPointer() + 1))
        .isEqualTo(expectedProduct);
    assertThat(cpu.cycles()).isEqualTo(expectedCycles);
  }

}