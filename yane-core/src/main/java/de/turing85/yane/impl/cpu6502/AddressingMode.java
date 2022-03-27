package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import lombok.*;
import lombok.experimental.Delegate;

@Value
@EqualsAndHashCode
@Getter(AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressingMode implements AddressingModeFunction {
  static int IMMEDIATE_LOADED_ADDRESS = Integer.MIN_VALUE;
  static int IMPLIED_LOADED_ADDRESS = Integer.MIN_VALUE + 1;
  static int UNKNOWN_LOADED_ADDRESS = Integer.MIN_VALUE + 2;

  static final AddressingMode ACCUMULATOR = new AddressingMode(
      (register, bus) ->
          AddressingResult.of(register, bus, IMPLIED_LOADED_ADDRESS, 0),
      "A");

  static final AddressingMode ABSOLUTE = new AddressingMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointerFromBus(register, bus);
        return AddressingResult.of(register, bus, address, 0);
      },
      "abs");

  private static int readAddressAtProgramPointerFromBus(Register register, CpuBus bus) {
    final int addressLow = bus.read(register.getAndIncrementProgramCounter());
    final int addressHigh = bus.read(register.getAndIncrementProgramCounter());
    return (addressHigh << 8) | (addressLow & 0xFF);
  }

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
      "abs,X");

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
      }, "abs,y");

  static final AddressingMode IMMEDIATE = new AddressingMode(
      (register, bus) -> AddressingResult.of(register, bus, IMMEDIATE_LOADED_ADDRESS, 0),
      "#");

  static final AddressingMode IMPLIED = new AddressingMode(
      (register, bus) -> AddressingResult.of(register, bus, IMPLIED_LOADED_ADDRESS, 0),
      "#");

  static final AddressingMode INDIRECT = new AddressingMode(
      (register, bus) -> {
        final int indirect = readAddressAtProgramPointerFromBus(register, bus);
        final int indirectLow = indirect & 0xFF;
        final int absoluteAddressLow = bus.read(indirect);
        final int absoluteAddressHigh;
        if (indirectLow == 0xFF) {
          // Hardware bug in 6502
          absoluteAddressHigh = indirect >> 8;
        } else {
          absoluteAddressHigh = bus.read((indirect + 1) & 0xFFFF);
        }
        final int absoluteAddress = (absoluteAddressHigh << 8) | absoluteAddressLow;
        return AddressingResult.of(register, bus, absoluteAddress, 0);
      },
      "ind");

  static final AddressingMode INDIRECT_ZERO_PAGE_X = new AddressingMode(
      (register, bus) -> {
        final int zeroPageIndirectAddress =
            bus.read(register.getAndIncrementProgramCounter()) & 0xFF;
        final int zeroPagePlusXOffsetIndirectAddress =
            (zeroPageIndirectAddress + register.x()) & 0xFF;
        final int absoluteAddress = readZeroPageAddressFromBus(
            zeroPagePlusXOffsetIndirectAddress,
            bus);
        return AddressingResult.of(register, bus, absoluteAddress, 0);
      },
      "X,ind");

  static int readZeroPageAddressFromBus(int zeroPageAddress, CpuBus bus) {
    final int addressLow = bus.read(zeroPageAddress & 0x00FF);
    final int addressHigh = bus.read((zeroPageAddress + 1) & 0x00FF);
    return ((addressHigh << 8) | addressLow) & 0xFFFF;
  }

  static final AddressingMode INDIRECT_ZERO_PAGE_Y = new AddressingMode(
      (register, bus) -> {
        final int zeroPageIndirectAddress = bus.read(register.getAndIncrementProgramCounter());
        final int absoluteAddress = readZeroPageAddressFromBus(zeroPageIndirectAddress, bus);
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
      "ind,Y");

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
      "REL");

  static final AddressingMode ZERO_PAGE = new AddressingMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        return AddressingResult.of(register, bus, zeroPageAddress, 0);
      },
      "zpg");

  static final AddressingMode ZERO_PAGE_X = new AddressingMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPageAddressPlusX = (zeroPageAddress + register.x()) & 0x00FF;
        return AddressingResult.of(register, bus, zeroPageAddressPlusX, 0);
      },
      "zpg,X");

  static final AddressingMode ZERO_PAGE_Y = new AddressingMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPageAddressPlusY = (zeroPageAddress + register.y()) & 0x00FF;
        return AddressingResult.of(register, bus, zeroPageAddressPlusY, 0);
      },
      "zpg,Y");

  static final AddressingMode UNKNOWN = new AddressingMode(
      (register, bus) ->
          AddressingResult.of(register, bus, UNKNOWN_LOADED_ADDRESS, 0),
      "???");

  @Delegate
  AddressingModeFunction loadFunction;
  String mnemonic;

  @Override
  public String toString() {
    return mnemonic();
  }
}