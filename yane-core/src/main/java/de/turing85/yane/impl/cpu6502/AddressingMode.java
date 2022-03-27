package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import lombok.*;
import lombok.experimental.Delegate;

@Value
@EqualsAndHashCode
@Getter(AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class AddressingMode implements AddressingModeFunction {
  static int IMMEDIATE_LOADED_ADDRESS = Integer.MIN_VALUE;
  static int IMPLIED_LOADED_ADDRESS = Integer.MIN_VALUE + 1;
  static int UNKNOWN_LOADED_ADDRESS = Integer.MIN_VALUE + 2;

  private static final int IMPLIED_ADDRESSING_BYTES_TO_READ = 0;
  private static final int UNKNOWN_ADDRESSING_BYTES_TO_READ = 0;
  private static final int IMMEDIATE_ADDRESSING_BYTES_TO_READ = 1;
  private static final int INDIRECT_ADDRESSING_BYTES_TO_READ = 1;
  private static final int RELATIVE_ADDRESSING_BYTES_TO_READ = 1;
  private static final int ABSOLUTE_ADDRESSING_BYTES_TO_READ = 2;

  static final AddressingMode ACCUMULATOR = new AddressingMode(
      (register, bus) ->
          AddressingResult.of(register, bus, IMPLIED_LOADED_ADDRESS, 0),
      "A",
      IMPLIED_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode ABSOLUTE = new AddressingMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointerFromBus(register, bus);
        return AddressingResult.of(register, bus, address, 0);
      },
      "abs",
      ABSOLUTE_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode ABSOLUTE_X = new AddressingMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointerFromBus(register, bus);
        final int addressHigh = (address >> 8) & 0xFF;
        final int addressPlusX = (address + register.x()) & 0xFFFF;
        final int addressPlusXHigh = addressPlusX >> 8;
        final int additionalCyclesNeeded;
        if (addressPlusXHigh != addressHigh) {
          additionalCyclesNeeded = 1;
        } else {
          additionalCyclesNeeded = 0;
        }
        return AddressingResult.of(register, bus, addressPlusX, additionalCyclesNeeded);
      },
      "abs,X",
      ABSOLUTE_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode ABSOLUTE_Y = new AddressingMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointerFromBus(register, bus);
        final int addressHigh = address >> 8;
        final int addressPlusY = (address + register.y()) & 0xFFFF;
        final int addressPlusYHigh = addressPlusY >> 8;
        final int additionalCyclesNeeded;
        if (addressPlusYHigh != addressHigh) {
          additionalCyclesNeeded = 1;
        } else {
          additionalCyclesNeeded = 0;
        }
        return AddressingResult.of(register, bus, addressPlusY, additionalCyclesNeeded);
      },
      "abs,y",
      ABSOLUTE_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode IMMEDIATE = new AddressingMode(
      (register, bus) -> AddressingResult.of(register, bus, IMMEDIATE_LOADED_ADDRESS, 0),
      "#",
      IMMEDIATE_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode IMPLIED = new AddressingMode(
      (register, bus) -> AddressingResult.of(register, bus, IMPLIED_LOADED_ADDRESS, 0),
      "impl",
      IMPLIED_ADDRESSING_BYTES_TO_READ);

  static final AddressingMode INDIRECT = new AddressingMode(
      (register, bus) -> {
        final int indirect = readAddressAtProgramPointerFromBus(register, bus);
        final int indirectLow = indirect & 0xFF;
        final int absoluteAddressLow = bus.read(indirect);
        final int absoluteAddressHigh;
        if (indirectLow == 0xFF) {
          // Hardware bug in 6502
          absoluteAddressHigh = bus.read(indirect & 0xFF00);
        } else {
          absoluteAddressHigh = bus.read((indirect + 1) & 0xFFFF);
        }
        final int absoluteAddress = (absoluteAddressHigh << 8) | absoluteAddressLow;
        return AddressingResult.of(register, bus, absoluteAddress, 0);
      },
      "ind",
      2);

  static final AddressingMode INDIRECT_ZERO_PAGE_X = new AddressingMode(
      (register, bus) -> {
        final int zeroPageIndirectAddress =
            bus.read(register.getAndIncrementProgramCounter()) & 0xFF;
        final int zeroPagePlusXOffsetIndirectAddress =
            (zeroPageIndirectAddress + register.x()) & 0xFF;
        final int absoluteAddress = readAddressFromBus(
            zeroPagePlusXOffsetIndirectAddress,
            bus);
        return AddressingResult.of(register, bus, absoluteAddress, 0);
      },
      "X,ind",
      1);

  static final AddressingMode INDIRECT_ZERO_PAGE_Y = new AddressingMode(
      (register, bus) -> {
        final int zeroPageIndirectAddress = bus.read(register.getAndIncrementProgramCounter());
        final int absoluteAddress = readAddressFromBus(zeroPageIndirectAddress, bus);
        final int absoluteAddressHigh = (absoluteAddress >> 8) & 0xFF;
        final int absoluteAddressPlusY = (absoluteAddress + register.y()) & 0xFFFF;
        final int absoluteAddressPlusYHigh = absoluteAddressPlusY >> 8;
        final int additionalCyclesNeeded;
        if (absoluteAddressPlusYHigh != absoluteAddressHigh) {
          additionalCyclesNeeded = 1;
        } else {
          additionalCyclesNeeded = 0;
        }
        return AddressingResult.of(register, bus, absoluteAddressPlusY, additionalCyclesNeeded);
      },
      "ind,Y",
      1);

  static final AddressingMode RELATIVE = new AddressingMode(
      (register, bus) -> {
        final int programCounter = register.getAndIncrementProgramCounter();
        final int relativeAddress = bus.read(programCounter);
        final int signedRelativeAddress;
        if ((relativeAddress & 0x80) > 0) {
          signedRelativeAddress = relativeAddress | 0xFFFFFF00;
        } else {
          signedRelativeAddress = relativeAddress;
        }
        final int address = (programCounter + signedRelativeAddress) & 0xFFFF;
        return AddressingResult.of(register, bus, address, 0);
      },
      "rel",
      1);

  static final AddressingMode ZERO_PAGE = new AddressingMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        return AddressingResult.of(register, bus, zeroPageAddress, 0);
      },
      "zpg",
      1);

  static final AddressingMode ZERO_PAGE_X = new AddressingMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPageAddressPlusX = (zeroPageAddress + register.x()) & 0x00FF;
        return AddressingResult.of(register, bus, zeroPageAddressPlusX, 0);
      },
      "zpg,X",
      1);

  static final AddressingMode ZERO_PAGE_Y = new AddressingMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPageAddressPlusY = (zeroPageAddress + register.y()) & 0x00FF;
        return AddressingResult.of(register, bus, zeroPageAddressPlusY, 0);
      },
      "zpg,Y",
      1);

  static final AddressingMode UNKNOWN = new AddressingMode(
      (register, bus) ->
          AddressingResult.of(register, bus, UNKNOWN_LOADED_ADDRESS, 0),
      "???",
      UNKNOWN_ADDRESSING_BYTES_TO_READ);

  @Delegate
  AddressingModeFunction loadFunction;
  String mnemonic;
  int bytesToRead;

  @Override
  public String toString() {
    return mnemonic();
  }

  private static int readAddressAtProgramPointerFromBus(Register register, CpuBus bus) {
    final int addressLow = bus.read(register.getAndIncrementProgramCounter());
    final int addressHigh = bus.read(register.getAndIncrementProgramCounter());
    return (addressHigh << 8) | (addressLow & 0xFF);
  }

  private static int readAddressFromBus(int zeroPageAddress, CpuBus bus) {
    final int addressLow = bus.read(zeroPageAddress & 0x00FF);
    final int addressHigh = bus.read((zeroPageAddress + 1) & 0x00FF);
    return ((addressHigh << 8) | addressLow) & 0xFFFF;
  }
}