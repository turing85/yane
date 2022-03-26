package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import java.util.function.*;

public interface AddressingModeFunction extends BiFunction<Register, CpuBus, AddressingResult> {
  int IMMEDIATE_LOADED_ADDRESS = Integer.MIN_VALUE;
  int IMPLIED_LOADED_ADDRESS = Integer.MIN_VALUE + 1;

  AddressingModeFunction ACCUMULATOR = (register, bus) ->
      AddressingResult.of(register, register.a(), IMPLIED_LOADED_ADDRESS, 0);

  AddressingModeFunction ABSOLUTE = (register, bus) -> {
    final int address = readAddressAtProgramPointerFromBus(register, bus);
    final int value = bus.read(address);
    return AddressingResult.of(register, value, address, 0);
  };

  private static int readAddressAtProgramPointerFromBus(Register register, CpuBus bus) {
    final int addressLow = bus.read(register.getAndIncrementProgramCounter());
    final int addressHigh = bus.read(register.getAndIncrementProgramCounter());
    return (addressHigh << 8) | (addressLow & 0xFF);
  }

  AddressingModeFunction ABSOLUTE_X = (register, bus) -> {
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
    final int value = bus.read(addressPlusX);
    return AddressingResult.of(register, value, addressPlusX, additionalCyclesNeeded);
  };

  AddressingModeFunction ABSOLUTE_Y = (register, bus) -> {
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
    final int value = bus.read(addressPlusY);
    return AddressingResult.of(register, value, addressPlusY, additionalCyclesNeeded);
  };

  AddressingModeFunction IMMEDIATE = (register, bus) -> {
    final int readValue = bus.read(register.getAndIncrementProgramCounter());
    return AddressingResult.of(register, readValue, IMMEDIATE_LOADED_ADDRESS, 0);
  };

  AddressingModeFunction IMPLIED = (register, bus) ->
      AddressingResult.of(register, 0, IMPLIED_LOADED_ADDRESS, 0);

  AddressingModeFunction INDIRECT = (register, bus) -> {
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
    final int value = bus.read(absoluteAddress);
    return AddressingResult.of(register, value, absoluteAddress, 0);
  };

  AddressingModeFunction INDIRECT_ZERO_PAGE_X = (register, bus) -> {
    final int zeroPageIndirectAddress = bus.read(register.getAndIncrementProgramCounter()) & 0xFF;
    final int zeroPagePlusXOffsetIndirectAddress = (zeroPageIndirectAddress + register.x()) & 0xFF;
    final int absoluteAddress = readZeroPageAddressFromBus(zeroPagePlusXOffsetIndirectAddress, bus);
    final int value = bus.read(absoluteAddress);
    return AddressingResult.of(register, value, absoluteAddress, 0);
  };

  private static int readZeroPageAddressFromBus(int zeroPageAddress, CpuBus bus) {
    final int addressLow = bus.read(zeroPageAddress & 0x00FF);
    final int addressHigh = bus.read((zeroPageAddress + 1) & 0x00FF);
    return ((addressHigh << 8) | addressLow) & 0xFFFF;
  }

  AddressingModeFunction INDIRECT_ZERO_PAGE_Y = (register, bus) -> {
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
    final int value = bus.read(absoluteAddressPlusY);
    return AddressingResult.of(register, value, absoluteAddressPlusY, additionalCyclesNeeded);
  };

  AddressingModeFunction REL = (register, bus) -> {
    final int programCounter = register.getAndIncrementProgramCounter();
    final int relativeAddress = bus.read(programCounter);
    final int signedRelativeAddress;
    if ((relativeAddress & 0x80) > 0) {
      signedRelativeAddress = relativeAddress | 0xFFFFFF00;
    } else {
      signedRelativeAddress = relativeAddress;
    }
    final int address = (programCounter + signedRelativeAddress) & 0xFFFF;
    final int value = bus.read(address);
    return AddressingResult.of(register, value,  address, 0);
  };

  AddressingModeFunction ZERO_PAGE = (register, bus) -> {
    final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
    final int value = bus.read(zeroPageAddress);
    return AddressingResult.of(register, value, zeroPageAddress, 0);
  };

  AddressingModeFunction ZERO_PAGE_X = (register, bus) -> {
    final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
    final int zeroPageAddressPlusX = (zeroPageAddress + register.x()) & 0x00FF;
    final int value = bus.read(zeroPageAddressPlusX);
    return AddressingResult.of(register, value, zeroPageAddressPlusX, 0);
  };

  AddressingModeFunction ZERO_PAGE_Y = (register, bus) -> {
    final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
    final int zeroPageAddressPlusY = (zeroPageAddress + register.y()) & 0x00FF;
    final int value = bus.read(zeroPageAddressPlusY);
    return AddressingResult.of(register, value, zeroPageAddressPlusY, 0);
  };
}