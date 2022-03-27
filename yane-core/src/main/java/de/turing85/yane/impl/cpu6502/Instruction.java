package de.turing85.yane.impl.cpu6502;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import lombok.*;

@Value
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Instruction implements de.turing85.yane.api.Instruction {
  static final Set<Instruction> INSTRUCTIONS;
  static final Map<Byte, Instruction> INSTRUCTIONS_BY_OPCODE;
  static final Map<String, Set<Instruction>> INSTRUCTIONS_BY_MNEMONIC;

  static {
    INSTRUCTIONS = Set.of(
        Instruction.of(Command.BRK, AddressingMode.IMPLIED, (byte) 0x00, 1, 7),
        Instruction.of(Command.ORA, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0x01, 2, 2),
        Instruction.unknownInstruction((byte) 0x02),
        Instruction.unknownInstruction((byte) 0x03),
        Instruction.unknownInstruction((byte) 0x04),
        Instruction.of(Command.ORA, AddressingMode.ZERO_PAGE, (byte) 0x05, 2, 0),
        Instruction.of(Command.ASL, AddressingMode.ZERO_PAGE, (byte) 0x06, 2, 5),
        Instruction.unknownInstruction((byte) 0x07),
        Instruction.of(Command.PHP, AddressingMode.IMPLIED, (byte) 0x08, 1, 3),
        Instruction.of(Command.ORA, AddressingMode.IMMEDIATE, (byte) 0x09, 2, 2),
        Instruction.of(Command.ASL, AddressingMode.ACCUMULATOR, (byte) 0x0A, 1, 2),
        Instruction.unknownInstruction((byte) 0x0B),
        Instruction.unknownInstruction((byte) 0x0C),
        Instruction.of(Command.ORA, AddressingMode.ABSOLUTE, (byte) 0x0D, 3, 4),
        Instruction.of(Command.ASL, AddressingMode.ABSOLUTE, (byte) 0x0E, 3, 6),
        Instruction.unknownInstruction((byte) 0x0F),
        Instruction.of(Command.BPL, AddressingMode.RELATIVE, (byte) 0x10, 2, 2),
        Instruction.of(Command.ORA, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0x11, 2, 5),
        Instruction.unknownInstruction((byte) 0x12),
        Instruction.unknownInstruction((byte) 0x13),
        Instruction.unknownInstruction((byte) 0x14),
        Instruction.of(Command.ORA, AddressingMode.ZERO_PAGE_X, (byte) 0x15, 2, 4),
        Instruction.of(Command.ASL, AddressingMode.ZERO_PAGE_X, (byte) 0x16, 2, 6),
        Instruction.unknownInstruction((byte) 0x17),
        Instruction.of(Command.CLC, AddressingMode.IMPLIED, (byte) 0x18, 1, 2),
        Instruction.of(Command.ORA, AddressingMode.ABSOLUTE_Y, (byte) 0x19, 3, 4),
        Instruction.unknownInstruction((byte) 0x1A),
        Instruction.unknownInstruction((byte) 0x1B),
        Instruction.unknownInstruction((byte) 0x1C),
        Instruction.of(Command.ORA, AddressingMode.ABSOLUTE_X, (byte) 0x1D, 3, 4),
        Instruction.of(Command.ASL, AddressingMode.ABSOLUTE_X, (byte) 0x1E, 3, 7),
        Instruction.of(Command.UNKNOWN, AddressingMode.ABSOLUTE_X, (byte) 0x1F, 1, 0),
        Instruction.of(Command.JSR, AddressingMode.ABSOLUTE, (byte) 0x20, 3, 6),
        Instruction.of(Command.AND, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0x21, 2, 6),
        Instruction.unknownInstruction((byte) 0x22),
        Instruction.unknownInstruction((byte) 0x23),
        Instruction.of(Command.BIT, AddressingMode.ZERO_PAGE, (byte) 0x24, 2, 3),
        Instruction.of(Command.AND, AddressingMode.ZERO_PAGE, (byte) 0x25, 1, 3),
        Instruction.of(Command.ROL, AddressingMode.ZERO_PAGE, (byte) 0x26, 2, 5),
        Instruction.unknownInstruction((byte) 0x27),
        Instruction.of(Command.PLP, AddressingMode.IMPLIED, (byte) 0x28, 1, 4),
        Instruction.of(Command.AND, AddressingMode.IMMEDIATE, (byte) 0x29, 2, 2),
        Instruction.of(Command.ROL, AddressingMode.ACCUMULATOR, (byte) 0x2A, 1, 2),
        Instruction.unknownInstruction((byte) 0x2B),
        Instruction.of(Command.BIT, AddressingMode.ABSOLUTE, (byte) 0x2C, 3, 4),
        Instruction.of(Command.AND, AddressingMode.ABSOLUTE, (byte) 0x2D, 3, 4),
        Instruction.of(Command.ROL, AddressingMode.ABSOLUTE, (byte) 0x2E, 3, 6),
        Instruction.unknownInstruction((byte) 0x2F),
        Instruction.of(Command.BMI, AddressingMode.RELATIVE, (byte) 0x30, 2, 2),
        Instruction.of(Command.AND, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0x31, 2, 5),
        Instruction.unknownInstruction((byte) 0x32),
        Instruction.unknownInstruction((byte) 0x33),
        Instruction.unknownInstruction((byte) 0x34),
        Instruction.of(Command.AND, AddressingMode.ZERO_PAGE_X, (byte) 0x35, 2, 4),
        Instruction.of(Command.ROL, AddressingMode.ZERO_PAGE_X, (byte) 0x36, 2, 6),
        Instruction.unknownInstruction((byte) 0x37),
        Instruction.of(Command.SEC, AddressingMode.IMPLIED, (byte) 0x38, 1, 2),
        Instruction.of(Command.AND, AddressingMode.ABSOLUTE_Y, (byte) 0x39, 3, 4),
        Instruction.unknownInstruction((byte) 0x3A),
        Instruction.unknownInstruction((byte) 0x3B),
        Instruction.unknownInstruction((byte) 0x3C),
        Instruction.of(Command.AND, AddressingMode.ABSOLUTE_X, (byte) 0x3D, 3, 4),
        Instruction.of(Command.ROL, AddressingMode.ABSOLUTE_X, (byte) 0x3E, 3, 7),
        Instruction.unknownInstruction((byte) 0x3F),
        Instruction.of(Command.RTI, AddressingMode.IMPLIED, (byte) 0x40, 1, 6),
        Instruction.of(Command.EOR, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0x41, 2, 6),
        Instruction.unknownInstruction((byte) 0x42),
        Instruction.unknownInstruction((byte) 0x43),
        Instruction.unknownInstruction((byte) 0x44),
        Instruction.of(Command.EOR, AddressingMode.ZERO_PAGE, (byte) 0x45, 2, 3),
        Instruction.of(Command.LSR, AddressingMode.ZERO_PAGE, (byte) 0x46, 2, 5),
        Instruction.unknownInstruction((byte) 0x47),
        Instruction.of(Command.PHA, AddressingMode.IMPLIED, (byte) 0x48, 1, 3),
        Instruction.of(Command.EOR, AddressingMode.IMMEDIATE, (byte) 0x49, 2, 2),
        Instruction.of(Command.LSR, AddressingMode.ACCUMULATOR, (byte) 0x4A, 1, 2),
        Instruction.unknownInstruction((byte) 0x4B),
        Instruction.of(Command.JMP, AddressingMode.ABSOLUTE, (byte) 0x4C, 3, 3),
        Instruction.of(Command.EOR, AddressingMode.ABSOLUTE, (byte) 0x4D, 3, 4),
        Instruction.of(Command.LSR, AddressingMode.ABSOLUTE, (byte) 0x4E, 3, 6),
        Instruction.unknownInstruction((byte) 0x4F),
        Instruction.of(Command.BVC, AddressingMode.RELATIVE, (byte) 0x50, 2, 2),
        Instruction.of(Command.EOR, AddressingMode.ZERO_PAGE_Y, (byte) 0x51, 2, 5),
        Instruction.unknownInstruction((byte) 0x52),
        Instruction.unknownInstruction((byte) 0x53),
        Instruction.unknownInstruction((byte) 0x54),
        Instruction.of(Command.EOR, AddressingMode.ZERO_PAGE_X, (byte) 0x55, 2, 4),
        Instruction.of(Command.LSR, AddressingMode.ZERO_PAGE_X, (byte) 0x56, 2, 6),
        Instruction.unknownInstruction((byte) 0x57),
        Instruction.of(Command.CLI, AddressingMode.IMPLIED, (byte) 0x58, 1, 2),
        Instruction.of(Command.EOR, AddressingMode.ABSOLUTE_Y, (byte) 0x59, 3, 4),
        Instruction.unknownInstruction((byte) 0x5A),
        Instruction.unknownInstruction((byte) 0x5B),
        Instruction.unknownInstruction((byte) 0x5C),
        Instruction.of(Command.EOR, AddressingMode.ABSOLUTE_X, (byte) 0x5D, 3, 4),
        Instruction.of(Command.LSR, AddressingMode.ABSOLUTE_X, (byte) 0x5E, 3, 7),
        Instruction.unknownInstruction((byte) 0x5F),
        Instruction.of(Command.RTS, AddressingMode.IMPLIED, (byte) 0x60, 1, 6),
        Instruction.of(Command.ADC, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0x61, 2, 6),
        Instruction.unknownInstruction((byte) 0x62),
        Instruction.unknownInstruction((byte) 0x63),
        Instruction.unknownInstruction((byte) 0x64),
        Instruction.of(Command.ADC, AddressingMode.ZERO_PAGE, (byte) 0x65, 2, 3),
        Instruction.of(Command.ROR, AddressingMode.ZERO_PAGE, (byte) 0x66, 2, 5),
        Instruction.unknownInstruction((byte) 0x67),
        Instruction.of(Command.PLA, AddressingMode.IMPLIED, (byte) 0x68, 1, 4),
        Instruction.of(Command.ADC, AddressingMode.IMMEDIATE, (byte) 0x69, 2, 2),
        Instruction.of(Command.ROR, AddressingMode.ACCUMULATOR, (byte) 0x6A, 1, 2),
        Instruction.unknownInstruction((byte) 0x6B),
        Instruction.of(Command.JMP, AddressingMode.INDIRECT, (byte) 0x6C, 3, 5),
        Instruction.of(Command.ADC, AddressingMode.ABSOLUTE, (byte) 0x6D, 3, 4),
        Instruction.of(Command.ROR, AddressingMode.ABSOLUTE, (byte) 0x6E, 3, 6),
        Instruction.unknownInstruction((byte) 0x6F),
        Instruction.of(Command.BVS, AddressingMode.RELATIVE, (byte) 0x70, 2, 2),
        Instruction.of(Command.ADC, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0x71, 2, 5),
        Instruction.unknownInstruction((byte) 0x72),
        Instruction.unknownInstruction((byte) 0x73),
        Instruction.unknownInstruction((byte) 0x74),
        Instruction.of(Command.ADC, AddressingMode.ZERO_PAGE_X, (byte) 0x75, 2, 4),
        Instruction.of(Command.ROR, AddressingMode.ZERO_PAGE_X, (byte) 0x76, 2, 6),
        Instruction.unknownInstruction((byte) 0x77),
        Instruction.of(Command.SEI, AddressingMode.IMPLIED, (byte) 0x78, 1, 2),
        Instruction.of(Command.ADC, AddressingMode.ABSOLUTE_Y, (byte) 0x79, 3, 4),
        Instruction.unknownInstruction((byte) 0x7A),
        Instruction.unknownInstruction((byte) 0x7B),
        Instruction.unknownInstruction((byte) 0x7C),
        Instruction.of(Command.ADC, AddressingMode.ABSOLUTE_X, (byte) 0x7D, 3, 4),
        Instruction.of(Command.ROR, AddressingMode.ABSOLUTE_X, (byte) 0x7E, 3, 7),
        Instruction.unknownInstruction((byte) 0x7F),
        Instruction.unknownInstruction((byte) 0x80),
        Instruction.of(Command.STA, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0x81, 2, 6),
        Instruction.unknownInstruction((byte) 0x82),
        Instruction.unknownInstruction((byte) 0x83),
        Instruction.of(Command.STY, AddressingMode.ZERO_PAGE, (byte) 0x84, 2, 3),
        Instruction.of(Command.STA, AddressingMode.ZERO_PAGE, (byte) 0x85, 2, 3),
        Instruction.of(Command.STX, AddressingMode.ZERO_PAGE, (byte) 0x86, 2, 3),
        Instruction.unknownInstruction((byte) 0x87),
        Instruction.of(Command.DEY, AddressingMode.IMPLIED, (byte) 0x88, 1, 2),
        Instruction.unknownInstruction((byte) 0x89),
        Instruction.of(Command.TXA, AddressingMode.IMPLIED, (byte) 0x8A, 1, 2),
        Instruction.unknownInstruction((byte) 0x8B),
        Instruction.of(Command.STY, AddressingMode.ABSOLUTE, (byte) 0x8C, 3, 4),
        Instruction.of(Command.STA, AddressingMode.ABSOLUTE, (byte) 0x8D, 3, 4),
        Instruction.of(Command.STX, AddressingMode.ABSOLUTE, (byte) 0x8E, 3, 4),
        Instruction.unknownInstruction((byte) 0x8F),
        Instruction.of(Command.BCC, AddressingMode.RELATIVE, (byte) 0x90, 2, 2),
        Instruction.of(Command.STA, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0x91, 2, 6),
        Instruction.unknownInstruction((byte) 0x92),
        Instruction.unknownInstruction((byte) 0x93),
        Instruction.of(Command.STY, AddressingMode.ZERO_PAGE_X, (byte) 0x94, 2, 4),
        Instruction.of(Command.STA, AddressingMode.ZERO_PAGE_X, (byte) 0x95, 2, 4),
        Instruction.of(Command.STX, AddressingMode.ZERO_PAGE_Y, (byte) 0x96, 2, 4),
        Instruction.unknownInstruction((byte) 0x97),
        Instruction.of(Command.TYA, AddressingMode.IMPLIED, (byte) 0x98, 1, 2),
        Instruction.of(Command.STA, AddressingMode.ABSOLUTE_Y, (byte) 0x99, 3, 5),
        Instruction.of(Command.TXS, AddressingMode.IMPLIED, (byte) 0x9A, 1, 2),
        Instruction.unknownInstruction((byte) 0x9B),
        Instruction.unknownInstruction((byte) 0x9C),
        Instruction.of(Command.STA, AddressingMode.ABSOLUTE_X, (byte) 0x9D, 3, 5),
        Instruction.unknownInstruction((byte) 0x9E),
        Instruction.unknownInstruction((byte) 0x9F),
        Instruction.of(Command.LDY, AddressingMode.IMMEDIATE, (byte) 0xA0, 2, 2),
        Instruction.of(Command.LDA, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0xA1, 2, 6),
        Instruction.of(Command.LDX, AddressingMode.IMMEDIATE, (byte) 0xA2, 2, 2),
        Instruction.unknownInstruction((byte) 0xA3),
        Instruction.of(Command.LDY, AddressingMode.ZERO_PAGE, (byte) 0xA4, 2, 3),
        Instruction.of(Command.LDA, AddressingMode.ZERO_PAGE, (byte) 0xA5, 2, 3),
        Instruction.of(Command.LDX, AddressingMode.ZERO_PAGE, (byte) 0xA6, 2, 3),
        Instruction.unknownInstruction((byte) 0xA7),
        Instruction.of(Command.TAY, AddressingMode.IMPLIED, (byte) 0xA8, 1, 2),
        Instruction.of(Command.LDA, AddressingMode.IMMEDIATE, (byte) 0xA9, 2, 2),
        Instruction.of(Command.TAX, AddressingMode.IMPLIED, (byte) 0xAA, 1, 2),
        Instruction.unknownInstruction((byte) 0xAB),
        Instruction.of(Command.LDY, AddressingMode.ABSOLUTE, (byte) 0xAC, 3, 4),
        Instruction.of(Command.LDA, AddressingMode.ABSOLUTE, (byte) 0xAD, 3, 4),
        Instruction.of(Command.LDX, AddressingMode.ABSOLUTE, (byte) 0xAE, 3, 4),
        Instruction.unknownInstruction((byte) 0xAF),
        Instruction.of(Command.BCS, AddressingMode.RELATIVE, (byte) 0xB0, 2, 2),
        Instruction.of(Command.LDA, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0xB1, 2, 5),
        Instruction.unknownInstruction((byte) 0xB2),
        Instruction.unknownInstruction((byte) 0xB3),
        Instruction.of(Command.LDY, AddressingMode.ZERO_PAGE_X, (byte) 0xB4, 2, 4),
        Instruction.of(Command.LDA, AddressingMode.ZERO_PAGE_X, (byte) 0xB5, 2, 4),
        Instruction.of(Command.LDX, AddressingMode.ZERO_PAGE_Y, (byte) 0xB6, 2, 4),
        Instruction.unknownInstruction((byte) 0xB7),
        Instruction.of(Command.CLV, AddressingMode.IMPLIED, (byte) 0xB8, 1, 2),
        Instruction.of(Command.LDA, AddressingMode.ABSOLUTE_Y, (byte) 0xB9, 3, 4),
        Instruction.of(Command.TSX, AddressingMode.IMPLIED, (byte) 0xBA, 1, 2),
        Instruction.unknownInstruction((byte) 0xBB),
        Instruction.of(Command.LDY, AddressingMode.ABSOLUTE_X, (byte) 0xBC, 3, 4),
        Instruction.of(Command.LDA, AddressingMode.ABSOLUTE_X, (byte) 0xBD, 3, 4),
        Instruction.of(Command.LDX, AddressingMode.ABSOLUTE_Y, (byte) 0xBE, 3, 4),
        Instruction.unknownInstruction((byte) 0xBF),
        Instruction.of(Command.CPY, AddressingMode.IMMEDIATE, (byte) 0xC0, 2, 2),
        Instruction.of(Command.CMP, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0xC1, 2, 6),
        Instruction.unknownInstruction((byte) 0xC2),
        Instruction.unknownInstruction((byte) 0xC3),
        Instruction.of(Command.CPY, AddressingMode.ZERO_PAGE, (byte) 0xC4, 2, 3),
        Instruction.of(Command.CMP, AddressingMode.ZERO_PAGE, (byte) 0xC5, 2, 3),
        Instruction.of(Command.DEC, AddressingMode.ZERO_PAGE, (byte) 0xC6, 2, 5),
        Instruction.unknownInstruction((byte) 0xC7),
        Instruction.of(Command.INY, AddressingMode.IMPLIED, (byte) 0xC8, 1, 2),
        Instruction.of(Command.CMP, AddressingMode.IMMEDIATE, (byte) 0xC9, 2, 2),
        Instruction.of(Command.DEX, AddressingMode.IMPLIED, (byte) 0xCA, 1, 2),
        Instruction.unknownInstruction((byte) 0xCB),
        Instruction.of(Command.CPY, AddressingMode.ABSOLUTE, (byte) 0xCC, 3, 4),
        Instruction.of(Command.CMP, AddressingMode.ABSOLUTE, (byte) 0xCD, 3, 4),
        Instruction.of(Command.DEC, AddressingMode.ABSOLUTE, (byte) 0xCE, 3, 6),
        Instruction.unknownInstruction((byte) 0xCF),
        Instruction.of(Command.BNE, AddressingMode.RELATIVE, (byte) 0xD0, 2, 2),
        Instruction.of(Command.CMP, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0xD1, 2, 5),
        Instruction.unknownInstruction((byte) 0xD2),
        Instruction.unknownInstruction((byte) 0xD3),
        Instruction.unknownInstruction((byte) 0xD4),
        Instruction.of(Command.CMP, AddressingMode.ZERO_PAGE_X, (byte) 0xD5, 2, 4),
        Instruction.of(Command.DEC, AddressingMode.ZERO_PAGE_X, (byte) 0xD6, 2, 6),
        Instruction.unknownInstruction((byte) 0xD7),
        Instruction.of(Command.CLD, AddressingMode.IMPLIED, (byte) 0xD8, 1, 2),
        Instruction.of(Command.CMP, AddressingMode.ABSOLUTE_Y, (byte) 0xD9, 3, 4),
        Instruction.unknownInstruction((byte) 0xDA),
        Instruction.unknownInstruction((byte) 0xDB),
        Instruction.unknownInstruction((byte) 0xDC),
        Instruction.of(Command.CMP, AddressingMode.ABSOLUTE_X, (byte) 0xDD, 3, 4),
        Instruction.of(Command.DEC, AddressingMode.ABSOLUTE_Y, (byte) 0xDE, 3, 7),
        Instruction.unknownInstruction((byte) 0xDF),
        Instruction.of(Command.CPX, AddressingMode.IMMEDIATE, (byte) 0xE0, 2, 2),
        Instruction.of(Command.SBC, AddressingMode.INDIRECT_ZERO_PAGE_X, (byte) 0xE1, 2, 6),
        Instruction.unknownInstruction((byte) 0xE2),
        Instruction.unknownInstruction((byte) 0xE3),
        Instruction.of(Command.CPX, AddressingMode.ZERO_PAGE, (byte) 0xE4, 2, 3),
        Instruction.of(Command.SBC, AddressingMode.ZERO_PAGE, (byte) 0xE5, 2, 3),
        Instruction.of(Command.INC, AddressingMode.ZERO_PAGE, (byte) 0xE6, 2, 5),
        Instruction.unknownInstruction((byte) 0xE7),
        Instruction.of(Command.INX, AddressingMode.IMPLIED, (byte) 0xE8, 1, 2),
        Instruction.of(Command.SBC, AddressingMode.IMMEDIATE, (byte) 0xE9, 2, 2),
        Instruction.of(Command.NOP, AddressingMode.IMPLIED, (byte) 0xEA, 1, 2),
        Instruction.unknownInstruction((byte) 0xEB),
        Instruction.of(Command.CPX, AddressingMode.ABSOLUTE, (byte) 0xEC, 3, 4),
        Instruction.of(Command.SBC, AddressingMode.ABSOLUTE, (byte) 0xED, 3, 4),
        Instruction.of(Command.INC, AddressingMode.ABSOLUTE, (byte) 0xEE, 3, 6),
        Instruction.unknownInstruction((byte) 0xEF),
        Instruction.of(Command.BEQ, AddressingMode.RELATIVE, (byte) 0xF0, 2, 2),
        Instruction.of(Command.SBC, AddressingMode.INDIRECT_ZERO_PAGE_Y, (byte) 0xF1, 2, 5),
        Instruction.unknownInstruction((byte) 0xF2),
        Instruction.unknownInstruction((byte) 0xF3),
        Instruction.unknownInstruction((byte) 0xF4),
        Instruction.of(Command.SBC, AddressingMode.ZERO_PAGE_X, (byte) 0xF5, 2, 4),
        Instruction.of(Command.INC, AddressingMode.ZERO_PAGE_X, (byte) 0xF6, 2, 6),
        Instruction.unknownInstruction((byte) 0xF7),
        Instruction.of(Command.SED, AddressingMode.IMPLIED, (byte) 0xF8, 1, 2),
        Instruction.of(Command.SBC, AddressingMode.ABSOLUTE_Y, (byte) 0xF9, 3, 4),
        Instruction.unknownInstruction((byte) 0xFA),
        Instruction.unknownInstruction((byte) 0xFB),
        Instruction.unknownInstruction((byte) 0xFC),
        Instruction.of(Command.SBC, AddressingMode.ABSOLUTE_X, (byte) 0xFD, 3, 4),
        Instruction.of(Command.INC, AddressingMode.ABSOLUTE_X, (byte) 0xFE, 3, 6),
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

  Command command;
  AddressingMode addressingMode;
  byte code;
  int bytesToRead;
  int cycles;


  private static Instruction of(
      Command command,
      AddressingMode addressingMode,
      byte code,
      int bytesToRead,
      int cycles) {
    return new Instruction(command, addressingMode, code, bytesToRead, cycles);
  }

  private static Instruction unknownInstruction(byte code) {
    return Instruction.of(Command.UNKNOWN, AddressingMode.UNKNOWN, code, 1, 1);
  }

  public String mnemonic() {
    return "%s %s".formatted(command().mnemonic(), addressingMode().mnemonic());
  }

  @Override public String toString() {
    return "[0x%08X] \"%s\"".formatted(code(), mnemonic());
  }
}