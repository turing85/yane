package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import java.util.function.*;

public interface AddressingModeFunction extends BiFunction<Register, CpuBus, AddressingResult> {
  AddressingModeFunction ACCUMULATOR = (register, bus) ->
      new AddressingResult(register, register.a(), 0);

  AddressingModeFunction ABSOLUTE = (register, bus) -> {
    final int address = readAddressAtProgramPointerFromBus(register, bus);
    final int value = bus.read(address);
    return new AddressingResult(register, value, 0);
  };

  private static int readAddressAtProgramPointerFromBus(Register register, CpuBus bus) {
    final int addressLow = bus.read(register.getAndIncrementProgramCounter());
    final int addressHigh = bus.read(register.getAndIncrementProgramCounter());
    return (addressHigh << 8) | (addressLow & 0xFF);
  }

  AddressingModeFunction ABSOLUTE_X = (register, bus) -> {
    final int baseAddress = readAddressAtProgramPointerFromBus(register, bus);
    final int baseAddressHigh = (baseAddress >> 8) & 0xFF;
    final int address = (baseAddress + register.x()) & 0xFFFF;
    final int addressHigh = address >> 8;
    final int additionalCyclesNeeded;
    if (addressHigh != baseAddressHigh) {
      additionalCyclesNeeded = 1;
    } else {
      additionalCyclesNeeded = 0;
    }
    final int value = bus.read(address);
    return new AddressingResult(register, value, additionalCyclesNeeded);
  };

  AddressingModeFunction ABSOLUTE_Y = (register, bus) -> {
    final int baseAddress = readAddressAtProgramPointerFromBus(register, bus);
    final int baseAddressHigh = baseAddress >> 8;
    final int address = (baseAddress + register.y()) & 0xFFFF;
    final int addressHigh = address >> 8;
    final int additionalCyclesNeeded;
    if (addressHigh != baseAddressHigh) {
      additionalCyclesNeeded = 1;
    } else {
      additionalCyclesNeeded = 0;
    }
    final int value = bus.read(address);
    return new AddressingResult(register, value, additionalCyclesNeeded);
  };

  AddressingModeFunction IMMEDIATE = (register, bus) -> {
    final int readValue = bus.read(register.getAndIncrementProgramCounter());
    return new AddressingResult(register, readValue, 0);
  };

  AddressingModeFunction IMPLIED = (register, bus) -> new AddressingResult(register, 0, 0);

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
    return new AddressingResult(register, value, 0);
  };

  AddressingModeFunction INDIRECT_ZERO_PAGE_X = (register, bus) -> {
    final int zeroPageIndirectAddress = bus.read(register.getAndIncrementProgramCounter()) & 0xFF;
    final int zeroPagePlusXOffsetIndirectAddress = (zeroPageIndirectAddress + register.x()) & 0xFF;
    final int absoluteAddress = readZeroPageAddressFromBus(zeroPagePlusXOffsetIndirectAddress, bus);
    final int value = bus.read(absoluteAddress);
    return new AddressingResult(register, value, 0);
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
    return new AddressingResult(register, value, additionalCyclesNeeded);
  };

  AddressingModeFunction REL = (register, bus) -> {
    final int value = bus.read(register.getAndIncrementProgramCounter());
    return new AddressingResult(register, value, 0);
  };

  AddressingModeFunction ZERO_PAGE = (register, bus) -> {
    final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
    final int value = bus.read(zeroPageAddress);
    return new AddressingResult(register, value, 0);
  };

  AddressingModeFunction ZERO_PAGE_X = (register, bus) -> {
    final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
    final int zeroPageAddressPlusX = (zeroPageAddress + register.x()) & 0x00FF;
    final int value = bus.read(zeroPageAddressPlusX);
    return new AddressingResult(register, value, 0);
  };

  AddressingModeFunction ZERO_PAGE_Y = (register, bus) -> {
    final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
    final int zeroPageAddressPlusY = (zeroPageAddress + register.y()) & 0x00FF;
    final int value = bus.read(zeroPageAddressPlusY);
    return new AddressingResult(register, value, 0);
  };
}