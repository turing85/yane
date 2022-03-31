package de.turing85.yane.cpu;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import lombok.*;

/**
 * <p>Instruction of the 6502 processor.</p>
 *
 * <p>This class defines instructions for all 256 op codes ({@code 0x00} to {@code 0xFF}). Some of
 * these op codes represent illegal/undocumented op codes. For a list of all op codes, please
 * refer to https://www.masswerk.at/6502/6502_instruction_set.html.</p>
 */
@Value
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class Instruction {
  /**
   * <p>Set of all instructions.</p>
   *
   * <p>This set also includes unknown instructions.</p>
   */
  static final Set<Instruction> INSTRUCTIONS;

  /**
   * A map that maps op codes (represented as {code int}s) to the corresponding instruction.
   */
  static final Map<Byte, Instruction> INSTRUCTIONS_BY_OPCODE;

  /**
   * A map that maps mnemonics (represented as {@link String}s) to the corresponding instruction.
   */
  static final Map<String, Set<Instruction>> INSTRUCTIONS_BY_MNEMONIC;

  static {
    INSTRUCTIONS = Set.of(
        new Instruction(Command.BRK, AddressingMode.IMPLIED, (byte) 0x00, 7),
        new Instruction(Command.ORA, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0x01,  2),
        Instruction.unknownInstruction((byte) 0x02),
        Instruction.unknownInstruction((byte) 0x03),
        Instruction.unknownInstruction((byte) 0x04),
        new Instruction(Command.ORA, AddressingMode.ZERO_PAGE, (byte) 0x05, 3),
        new Instruction(Command.ASL, AddressingMode.ZERO_PAGE, (byte) 0x06, 5),
        Instruction.unknownInstruction((byte) 0x07),
        new Instruction(Command.PHP, AddressingMode.IMPLIED, (byte) 0x08, 3),
        new Instruction(Command.ORA, AddressingMode.IMMEDIATE, (byte) 0x09, 2),
        new Instruction(Command.ASL, AddressingMode.ACCUMULATOR, (byte) 0x0A, 2),
        Instruction.unknownInstruction((byte) 0x0B),
        Instruction.unknownInstruction((byte) 0x0C),
        new Instruction(Command.ORA, AddressingMode.ABSOLUTE, (byte) 0x0D, 4),
        new Instruction(Command.ASL, AddressingMode.ABSOLUTE, (byte) 0x0E, 6),
        Instruction.unknownInstruction((byte) 0x0F),
        new Instruction(Command.BPL, AddressingMode.RELATIVE, (byte) 0x10, 2),
        new Instruction(Command.ORA, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0x11, 5),
        Instruction.unknownInstruction((byte) 0x12),
        Instruction.unknownInstruction((byte) 0x13),
        Instruction.unknownInstruction((byte) 0x14),
        new Instruction(Command.ORA, AddressingMode.ZERO_PAGE_X, (byte) 0x15, 4),
        new Instruction(Command.ASL, AddressingMode.ZERO_PAGE_X, (byte) 0x16, 6),
        Instruction.unknownInstruction((byte) 0x17),
        new Instruction(Command.CLC, AddressingMode.IMPLIED, (byte) 0x18, 2),
        new Instruction(Command.ORA, AddressingMode.ABSOLUTE_Y, (byte) 0x19, 4),
        Instruction.unknownInstruction((byte) 0x1A),
        Instruction.unknownInstruction((byte) 0x1B),
        Instruction.unknownInstruction((byte) 0x1C),
        new Instruction(Command.ORA, AddressingMode.ABSOLUTE_X, (byte) 0x1D, 4),
        new Instruction(Command.ASL, AddressingMode.ABSOLUTE_X, (byte) 0x1E, 7),
        Instruction.unknownInstruction((byte) 0x1F),
        new Instruction(Command.JSR, AddressingMode.ABSOLUTE, (byte) 0x20, 6),
        new Instruction(Command.AND, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0x21, 6),
        Instruction.unknownInstruction((byte) 0x22),
        Instruction.unknownInstruction((byte) 0x23),
        new Instruction(Command.BIT, AddressingMode.ZERO_PAGE, (byte) 0x24, 3),
        new Instruction(Command.AND, AddressingMode.ZERO_PAGE, (byte) 0x25, 3),
        new Instruction(Command.ROL, AddressingMode.ZERO_PAGE, (byte) 0x26, 5),
        Instruction.unknownInstruction((byte) 0x27),
        new Instruction(Command.PLP, AddressingMode.IMPLIED, (byte) 0x28, 4),
        new Instruction(Command.AND, AddressingMode.IMMEDIATE, (byte) 0x29, 2),
        new Instruction(Command.ROL, AddressingMode.ACCUMULATOR, (byte) 0x2A, 2),
        Instruction.unknownInstruction((byte) 0x2B),
        new Instruction(Command.BIT, AddressingMode.ABSOLUTE, (byte) 0x2C, 4),
        new Instruction(Command.AND, AddressingMode.ABSOLUTE, (byte) 0x2D, 4),
        new Instruction(Command.ROL, AddressingMode.ABSOLUTE, (byte) 0x2E, 6),
        Instruction.unknownInstruction((byte) 0x2F),
        new Instruction(Command.BMI, AddressingMode.RELATIVE, (byte) 0x30, 2),
        new Instruction(Command.AND, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0x31, 5),
        Instruction.unknownInstruction((byte) 0x32),
        Instruction.unknownInstruction((byte) 0x33),
        Instruction.unknownInstruction((byte) 0x34),
        new Instruction(Command.AND, AddressingMode.ZERO_PAGE_X, (byte) 0x35, 4),
        new Instruction(Command.ROL, AddressingMode.ZERO_PAGE_X, (byte) 0x36, 6),
        Instruction.unknownInstruction((byte) 0x37),
        new Instruction(Command.SEC, AddressingMode.IMPLIED, (byte) 0x38, 2),
        new Instruction(Command.AND, AddressingMode.ABSOLUTE_Y, (byte) 0x39, 4),
        Instruction.unknownInstruction((byte) 0x3A),
        Instruction.unknownInstruction((byte) 0x3B),
        Instruction.unknownInstruction((byte) 0x3C),
        new Instruction(Command.AND, AddressingMode.ABSOLUTE_X, (byte) 0x3D, 4),
        new Instruction(Command.ROL, AddressingMode.ABSOLUTE_X, (byte) 0x3E, 7),
        Instruction.unknownInstruction((byte) 0x3F),
        new Instruction(Command.RTI, AddressingMode.IMPLIED, (byte) 0x40, 6),
        new Instruction(Command.EOR, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0x41, 6),
        Instruction.unknownInstruction((byte) 0x42),
        Instruction.unknownInstruction((byte) 0x43),
        Instruction.unknownInstruction((byte) 0x44),
        new Instruction(Command.EOR, AddressingMode.ZERO_PAGE, (byte) 0x45, 3),
        new Instruction(Command.LSR, AddressingMode.ZERO_PAGE, (byte) 0x46, 5),
        Instruction.unknownInstruction((byte) 0x47),
        new Instruction(Command.PHA, AddressingMode.IMPLIED, (byte) 0x48, 3),
        new Instruction(Command.EOR, AddressingMode.IMMEDIATE, (byte) 0x49, 2),
        new Instruction(Command.LSR, AddressingMode.ACCUMULATOR, (byte) 0x4A, 2),
        Instruction.unknownInstruction((byte) 0x4B),
        new Instruction(Command.JMP, AddressingMode.ABSOLUTE, (byte) 0x4C, 3),
        new Instruction(Command.EOR, AddressingMode.ABSOLUTE, (byte) 0x4D, 4),
        new Instruction(Command.LSR, AddressingMode.ABSOLUTE, (byte) 0x4E, 6),
        Instruction.unknownInstruction((byte) 0x4F),
        new Instruction(Command.BVC, AddressingMode.RELATIVE, (byte) 0x50, 2),
        new Instruction(Command.EOR, AddressingMode.ZERO_PAGE_Y, (byte) 0x51, 5),
        Instruction.unknownInstruction((byte) 0x52),
        Instruction.unknownInstruction((byte) 0x53),
        Instruction.unknownInstruction((byte) 0x54),
        new Instruction(Command.EOR, AddressingMode.ZERO_PAGE_X, (byte) 0x55, 4),
        new Instruction(Command.LSR, AddressingMode.ZERO_PAGE_X, (byte) 0x56, 6),
        Instruction.unknownInstruction((byte) 0x57),
        new Instruction(Command.CLI, AddressingMode.IMPLIED, (byte) 0x58, 2),
        new Instruction(Command.EOR, AddressingMode.ABSOLUTE_Y, (byte) 0x59, 4),
        Instruction.unknownInstruction((byte) 0x5A),
        Instruction.unknownInstruction((byte) 0x5B),
        Instruction.unknownInstruction((byte) 0x5C),
        new Instruction(Command.EOR, AddressingMode.ABSOLUTE_X, (byte) 0x5D, 4),
        new Instruction(Command.LSR, AddressingMode.ABSOLUTE_X, (byte) 0x5E, 7),
        Instruction.unknownInstruction((byte) 0x5F),
        new Instruction(Command.RTS, AddressingMode.IMPLIED, (byte) 0x60, 6),
        new Instruction(Command.ADC, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0x61, 6),
        Instruction.unknownInstruction((byte) 0x62),
        Instruction.unknownInstruction((byte) 0x63),
        Instruction.unknownInstruction((byte) 0x64),
        new Instruction(Command.ADC, AddressingMode.ZERO_PAGE, (byte) 0x65, 3),
        new Instruction(Command.ROR, AddressingMode.ZERO_PAGE, (byte) 0x66, 5),
        Instruction.unknownInstruction((byte) 0x67),
        new Instruction(Command.PLA, AddressingMode.IMPLIED, (byte) 0x68, 4),
        new Instruction(Command.ADC, AddressingMode.IMMEDIATE, (byte) 0x69, 2),
        new Instruction(Command.ROR, AddressingMode.ACCUMULATOR, (byte) 0x6A, 2),
        Instruction.unknownInstruction((byte) 0x6B),
        new Instruction(Command.JMP, AddressingMode.INDIRECT, (byte) 0x6C, 5),
        new Instruction(Command.ADC, AddressingMode.ABSOLUTE, (byte) 0x6D, 4),
        new Instruction(Command.ROR, AddressingMode.ABSOLUTE, (byte) 0x6E, 6),
        Instruction.unknownInstruction((byte) 0x6F),
        new Instruction(Command.BVS, AddressingMode.RELATIVE, (byte) 0x70, 2),
        new Instruction(Command.ADC, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0x71, 5),
        Instruction.unknownInstruction((byte) 0x72),
        Instruction.unknownInstruction((byte) 0x73),
        Instruction.unknownInstruction((byte) 0x74),
        new Instruction(Command.ADC, AddressingMode.ZERO_PAGE_X, (byte) 0x75, 4),
        new Instruction(Command.ROR, AddressingMode.ZERO_PAGE_X, (byte) 0x76, 6),
        Instruction.unknownInstruction((byte) 0x77),
        new Instruction(Command.SEI, AddressingMode.IMPLIED, (byte) 0x78, 2),
        new Instruction(Command.ADC, AddressingMode.ABSOLUTE_Y, (byte) 0x79, 4),
        Instruction.unknownInstruction((byte) 0x7A),
        Instruction.unknownInstruction((byte) 0x7B),
        Instruction.unknownInstruction((byte) 0x7C),
        new Instruction(Command.ADC, AddressingMode.ABSOLUTE_X, (byte) 0x7D, 4),
        new Instruction(Command.ROR, AddressingMode.ABSOLUTE_X, (byte) 0x7E, 7),
        Instruction.unknownInstruction((byte) 0x7F),
        Instruction.unknownInstruction((byte) 0x80),
        new Instruction(Command.STA, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0x81, 6),
        Instruction.unknownInstruction((byte) 0x82),
        Instruction.unknownInstruction((byte) 0x83),
        new Instruction(Command.STY, AddressingMode.ZERO_PAGE, (byte) 0x84, 3),
        new Instruction(Command.STA, AddressingMode.ZERO_PAGE, (byte) 0x85, 3),
        new Instruction(Command.STX, AddressingMode.ZERO_PAGE, (byte) 0x86, 3),
        Instruction.unknownInstruction((byte) 0x87),
        new Instruction(Command.DEY, AddressingMode.IMPLIED, (byte) 0x88, 2),
        Instruction.unknownInstruction((byte) 0x89),
        new Instruction(Command.TXA, AddressingMode.IMPLIED, (byte) 0x8A, 2),
        Instruction.unknownInstruction((byte) 0x8B),
        new Instruction(Command.STY, AddressingMode.ABSOLUTE, (byte) 0x8C, 4),
        new Instruction(Command.STA, AddressingMode.ABSOLUTE, (byte) 0x8D, 4),
        new Instruction(Command.STX, AddressingMode.ABSOLUTE, (byte) 0x8E, 4),
        Instruction.unknownInstruction((byte) 0x8F),
        new Instruction(Command.BCC, AddressingMode.RELATIVE, (byte) 0x90, 2),
        new Instruction(Command.STA, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0x91, 6),
        Instruction.unknownInstruction((byte) 0x92),
        Instruction.unknownInstruction((byte) 0x93),
        new Instruction(Command.STY, AddressingMode.ZERO_PAGE_X, (byte) 0x94, 4),
        new Instruction(Command.STA, AddressingMode.ZERO_PAGE_X, (byte) 0x95, 4),
        new Instruction(Command.STX, AddressingMode.ZERO_PAGE_Y, (byte) 0x96, 4),
        Instruction.unknownInstruction((byte) 0x97),
        new Instruction(Command.TYA, AddressingMode.IMPLIED, (byte) 0x98, 2),
        new Instruction(Command.STA, AddressingMode.ABSOLUTE_Y, (byte) 0x99, 5),
        new Instruction(Command.TXS, AddressingMode.IMPLIED, (byte) 0x9A, 2),
        Instruction.unknownInstruction((byte) 0x9B),
        Instruction.unknownInstruction((byte) 0x9C),
        new Instruction(Command.STA, AddressingMode.ABSOLUTE_X, (byte) 0x9D, 5),
        Instruction.unknownInstruction((byte) 0x9E),
        Instruction.unknownInstruction((byte) 0x9F),
        new Instruction(Command.LDY, AddressingMode.IMMEDIATE, (byte) 0xA0, 2),
        new Instruction(Command.LDA, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0xA1, 6),
        new Instruction(Command.LDX, AddressingMode.IMMEDIATE, (byte) 0xA2, 2),
        Instruction.unknownInstruction((byte) 0xA3),
        new Instruction(Command.LDY, AddressingMode.ZERO_PAGE, (byte) 0xA4, 3),
        new Instruction(Command.LDA, AddressingMode.ZERO_PAGE, (byte) 0xA5, 3),
        new Instruction(Command.LDX, AddressingMode.ZERO_PAGE, (byte) 0xA6, 3),
        Instruction.unknownInstruction((byte) 0xA7),
        new Instruction(Command.TAY, AddressingMode.IMPLIED, (byte) 0xA8, 2),
        new Instruction(Command.LDA, AddressingMode.IMMEDIATE, (byte) 0xA9, 2),
        new Instruction(Command.TAX, AddressingMode.IMPLIED, (byte) 0xAA, 2),
        Instruction.unknownInstruction((byte) 0xAB),
        new Instruction(Command.LDY, AddressingMode.ABSOLUTE, (byte) 0xAC, 4),
        new Instruction(Command.LDA, AddressingMode.ABSOLUTE, (byte) 0xAD, 4),
        new Instruction(Command.LDX, AddressingMode.ABSOLUTE, (byte) 0xAE, 4),
        Instruction.unknownInstruction((byte) 0xAF),
        new Instruction(Command.BCS, AddressingMode.RELATIVE, (byte) 0xB0, 2),
        new Instruction(Command.LDA, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0xB1, 5),
        Instruction.unknownInstruction((byte) 0xB2),
        Instruction.unknownInstruction((byte) 0xB3),
        new Instruction(Command.LDY, AddressingMode.ZERO_PAGE_X, (byte) 0xB4, 4),
        new Instruction(Command.LDA, AddressingMode.ZERO_PAGE_X, (byte) 0xB5, 4),
        new Instruction(Command.LDX, AddressingMode.ZERO_PAGE_Y, (byte) 0xB6, 4),
        Instruction.unknownInstruction((byte) 0xB7),
        new Instruction(Command.CLV, AddressingMode.IMPLIED, (byte) 0xB8, 2),
        new Instruction(Command.LDA, AddressingMode.ABSOLUTE_Y, (byte) 0xB9, 4),
        new Instruction(Command.TSX, AddressingMode.IMPLIED, (byte) 0xBA, 2),
        Instruction.unknownInstruction((byte) 0xBB),
        new Instruction(Command.LDY, AddressingMode.ABSOLUTE_X, (byte) 0xBC, 4),
        new Instruction(Command.LDA, AddressingMode.ABSOLUTE_X, (byte) 0xBD, 4),
        new Instruction(Command.LDX, AddressingMode.ABSOLUTE_Y, (byte) 0xBE, 4),
        Instruction.unknownInstruction((byte) 0xBF),
        new Instruction(Command.CPY, AddressingMode.IMMEDIATE, (byte) 0xC0, 2),
        new Instruction(Command.CMP, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0xC1, 6),
        Instruction.unknownInstruction((byte) 0xC2),
        Instruction.unknownInstruction((byte) 0xC3),
        new Instruction(Command.CPY, AddressingMode.ZERO_PAGE, (byte) 0xC4, 3),
        new Instruction(Command.CMP, AddressingMode.ZERO_PAGE, (byte) 0xC5, 3),
        new Instruction(Command.DEC, AddressingMode.ZERO_PAGE, (byte) 0xC6, 5),
        Instruction.unknownInstruction((byte) 0xC7),
        new Instruction(Command.INY, AddressingMode.IMPLIED, (byte) 0xC8, 2),
        new Instruction(Command.CMP, AddressingMode.IMMEDIATE, (byte) 0xC9, 2),
        new Instruction(Command.DEX, AddressingMode.IMPLIED, (byte) 0xCA, 2),
        Instruction.unknownInstruction((byte) 0xCB),
        new Instruction(Command.CPY, AddressingMode.ABSOLUTE, (byte) 0xCC, 4),
        new Instruction(Command.CMP, AddressingMode.ABSOLUTE, (byte) 0xCD, 4),
        new Instruction(Command.DEC, AddressingMode.ABSOLUTE, (byte) 0xCE, 6),
        Instruction.unknownInstruction((byte) 0xCF),
        new Instruction(Command.BNE, AddressingMode.RELATIVE, (byte) 0xD0, 2),
        new Instruction(Command.CMP, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0xD1, 5),
        Instruction.unknownInstruction((byte) 0xD2),
        Instruction.unknownInstruction((byte) 0xD3),
        Instruction.unknownInstruction((byte) 0xD4),
        new Instruction(Command.CMP, AddressingMode.ZERO_PAGE_X, (byte) 0xD5, 4),
        new Instruction(Command.DEC, AddressingMode.ZERO_PAGE_X, (byte) 0xD6, 6),
        Instruction.unknownInstruction((byte) 0xD7),
        new Instruction(Command.CLD, AddressingMode.IMPLIED, (byte) 0xD8, 2),
        new Instruction(Command.CMP, AddressingMode.ABSOLUTE_Y, (byte) 0xD9, 4),
        Instruction.unknownInstruction((byte) 0xDA),
        Instruction.unknownInstruction((byte) 0xDB),
        Instruction.unknownInstruction((byte) 0xDC),
        new Instruction(Command.CMP, AddressingMode.ABSOLUTE_X, (byte) 0xDD, 4),
        new Instruction(Command.DEC, AddressingMode.ABSOLUTE_X, (byte) 0xDE, 7),
        Instruction.unknownInstruction((byte) 0xDF),
        new Instruction(Command.CPX, AddressingMode.IMMEDIATE, (byte) 0xE0, 2),
        new Instruction(Command.SBC, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0xE1, 6),
        Instruction.unknownInstruction((byte) 0xE2),
        Instruction.unknownInstruction((byte) 0xE3),
        new Instruction(Command.CPX, AddressingMode.ZERO_PAGE, (byte) 0xE4, 3),
        new Instruction(Command.SBC, AddressingMode.ZERO_PAGE, (byte) 0xE5, 3),
        new Instruction(Command.INC, AddressingMode.ZERO_PAGE, (byte) 0xE6, 5),
        Instruction.unknownInstruction((byte) 0xE7),
        new Instruction(Command.INX, AddressingMode.IMPLIED, (byte) 0xE8, 2),
        new Instruction(Command.SBC, AddressingMode.IMMEDIATE, (byte) 0xE9, 2),
        new Instruction(Command.NOP, AddressingMode.IMPLIED, (byte) 0xEA, 2),
        Instruction.unknownInstruction((byte) 0xEB),
        new Instruction(Command.CPX, AddressingMode.ABSOLUTE, (byte) 0xEC, 4),
        new Instruction(Command.SBC, AddressingMode.ABSOLUTE, (byte) 0xED, 4),
        new Instruction(Command.INC, AddressingMode.ABSOLUTE, (byte) 0xEE, 6),
        Instruction.unknownInstruction((byte) 0xEF),
        new Instruction(Command.BEQ, AddressingMode.RELATIVE, (byte) 0xF0, 2),
        new Instruction(Command.SBC, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0xF1, 5),
        Instruction.unknownInstruction((byte) 0xF2),
        Instruction.unknownInstruction((byte) 0xF3),
        Instruction.unknownInstruction((byte) 0xF4),
        new Instruction(Command.SBC, AddressingMode.ZERO_PAGE_X, (byte) 0xF5, 4),
        new Instruction(Command.INC, AddressingMode.ZERO_PAGE_X, (byte) 0xF6, 6),
        Instruction.unknownInstruction((byte) 0xF7),
        new Instruction(Command.SED, AddressingMode.IMPLIED, (byte) 0xF8, 2),
        new Instruction(Command.SBC, AddressingMode.ABSOLUTE_Y, (byte) 0xF9, 4),
        Instruction.unknownInstruction((byte) 0xFA),
        Instruction.unknownInstruction((byte) 0xFB),
        Instruction.unknownInstruction((byte) 0xFC),
        new Instruction(Command.SBC, AddressingMode.ABSOLUTE_X, (byte) 0xFD, 4),
        new Instruction(Command.INC, AddressingMode.ABSOLUTE_X, (byte) 0xFE, 7),
        Instruction.unknownInstruction((byte) 0xFF));
    INSTRUCTIONS_BY_OPCODE = INSTRUCTIONS.stream()
        .collect(Collectors.toMap(
            Instruction::code,
            Function.identity()));
    INSTRUCTIONS_BY_MNEMONIC = INSTRUCTIONS.stream()
        .collect(Collectors.toMap(
            Instruction::mnemonic,
            Set::of,
            (lhs, rhs) -> Stream.concat(lhs.stream(), rhs.stream()).collect(Collectors.toSet())));
  }

  /**
   * The {@link Command} of this instruction.
   */
  Command command;

  /**
   * The {@link AddressingMode} of this instruction.
   */
  AddressingMode addressingMode;

  /**
   * the op code (8-bit value) that represents this instruction.
   */
  byte code;

  /**
   * <p>The number of cycles this instruction takes to execute.</p>
   *
   * <p>Notice that some {@link AddressingMode}s and {@link Command}s may add additional cycles to
   * the execution, depending on the data read during instruction execution.</p>
   */
  int cycles;

  /**
   * <p>The number of bytes read from the {@link Register#programCounter}.</p>
   *
   * <p>This value also specifies how often {@link Register#programCounter} is incremented during
   * the execution of this instruction.</p>
   *
   * @return The number of bytes read from the {@link Register#programCounter}
   */
  public int bytesToRead() {
    return 1 + addressingMode().bytesToRead();
  }

  /**
   * Static factory to build an instruction for an illegal op code.
   *
   * @param code illegal op code
   * @return the instruction, representing that illegal op code
   */
  private static Instruction unknownInstruction(byte code) {
    return new Instruction(Command.UNKNOWN, AddressingMode.UNKNOWN, code, 1);
  }

  /**
   * The mnemonic for this instruction.
   *
   * @return A short {@link String}-representation of the instruction.
   */
  public String mnemonic() {
    return "%s %s".formatted(command().mnemonic(), addressingMode().mnemonic());
  }

  /**
   * {@inheritDoc}
   *
   * @see #mnemonic()
   */
  @Override
  public String toString() {
    return "[0x%08X] \"%s\"".formatted(code(), mnemonic());
  }
}