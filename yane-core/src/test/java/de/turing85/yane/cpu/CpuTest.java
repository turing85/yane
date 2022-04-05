package de.turing85.yane.cpu;

import static com.google.common.truth.Truth.assertThat;

import de.turing85.yane.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CpuTest {

  @CsvSource({
      "  5,  10,  50,  62",
      " 10,   5,  50, 102",
      "  0,  10,   0,  17",
      " 10,   0,   0,  27",
      "  2, 100, 200,  38",
      "100,   2, 200, 822"})
  @ParameterizedTest(name = "{0} * {1} = {2} is calculated in {3} cycles")
  @DisplayName("execute multiplication program")
  void executeMultiplicationProgram(int lhs, int rhs, int expectedProduct, int expectedCycles) {
    // GIVEN
    final Bus bus = new Bus();

    // write operands to memory page 0, index 50 and 51
    bus.write(0x0050, lhs);
    bus.write(0x0051, rhs);

    // write zero-page address of rhs (0x51) to page 0, byte lhs
    bus.write(lhs, 0x51);

    // start the program on memory page 6
    bus.writeAddressTo(Cpu.RESET_READ_ADDRESS, 0x0600);

    // load lhs into accumulator
    // Instruction: LDA zpg
    bus.write(0x0600, 0xA5);
    bus.write(0x0601, 0x50);

    // jump to address 0x06B0 to terminate program
    // Instruction: BEQ rel
    bus.write(0x0602, 0xF0);
    bus.write(0x0603, 0x1D);

    // transfer accumulator to x
    // Instruction: TAX impl
    bus.write(0x0604, 0xAA);

    // load rhs into accumulator
    // Instruction: LDA x,ind
    bus.write(0x0605, 0xA1);
    bus.write(0x0606, 0x00);

    // jump to address 0x0620 to terminate program
    // Instruction: BEQ rel
    bus.write(0x0607, 0xF0);
    bus.write(0x0608, 0x18);

    // Decrement x by one, accumulator holds lhs already once
    // Instruction: DEX impl
    bus.write(0x0609, 0xCA);

    // Add rhs once more to accumulator
    // Instruction: ADC zpg
    bus.write(0x060A, 0x65);
    bus.write(0x060B, 0x51);

    // Decrement x by 1
    // Instruction: DEX impl
    bus.write(0x060C, 0xCA);

    // Jump back if x is not zero
    // Instruction: BNE rel
    bus.write(0x060D, 0xD0);
    bus.write(0x060E, 0xFC);

    // Jump to address 0x0620 to terminate program
    // Instruction: JMP abs
    bus.write(0x060F, 0x4C);
    bus.writeAddressTo(0x0610, 0x0620);

    // Write accumulator to stack
    // Instruction: PHA impl
    bus.write(0x0620, 0x48);

    // WHEN
    final Cpu cpu = new Cpu(bus, new Clock());
    boolean finished = false;
    while (cpu.register().programCounter() != 0x0621 || !finished) {
      finished = cpu.tick();
    }

    // THEN
    assertThat(cpu.bus().readFromStack(cpu.register().stackPointer() + 1))
        .isEqualTo(expectedProduct);
    assertThat(cpu.cycles()).isEqualTo(expectedCycles);
  }

}