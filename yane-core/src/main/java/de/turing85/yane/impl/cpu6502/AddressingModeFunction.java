package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import java.util.function.*;

public interface AddressingModeFunction extends BiFunction<Register, CpuBus, AddressingResult> {

  AddressingModeFunction ACCUMULATOR = (register, bus) ->
      new AddressingResult(register, register.a(), 0);

  AddressingModeFunction ABSOLUTE = (register, bus) -> {
    final short address = readAddressAtProgramPointerFromBus(register, bus);
    final byte value = bus.read(address);
    return new AddressingResult(register, value, 0);
  };

  private static short readAddressAtProgramPointerFromBus(Register register, CpuBus bus) {
    final byte addressLow = bus.read(register.getAndIncrementProgramCounter());
    final byte addressHigh = bus.read(register.getAndIncrementProgramCounter());
    return (short) (((addressHigh & 0xFF) << 8) | (addressLow & 0xFF));
  }

  AddressingModeFunction ABSOLUTE_X = (register, bus) -> {
    final short baseAddress = readAddressAtProgramPointerFromBus(register, bus);
    final byte baseAddressHigh = (byte) ((baseAddress & 0xFFFF) >> 8) ;
    final short address = (short) ((baseAddress + register.x()) & 0xFFFF);
    final byte addressHigh = (byte) ((address & 0xFFFF) >> 8);
    final int additionalCyclesNeeded;
    if (addressHigh != baseAddressHigh) {
      additionalCyclesNeeded = 1;
    } else {
      additionalCyclesNeeded = 0;
    }
    final byte value = bus.read(address);
    return new AddressingResult(register, value, additionalCyclesNeeded);
  };

  AddressingModeFunction ABSOLUTE_Y = (register, bus) -> {
    final short baseAddress = readAddressAtProgramPointerFromBus(register, bus);
    final byte baseAddressHigh = (byte) ((baseAddress & 0xFF00) >> 8);
    final short address = (short) ((baseAddress + register.y()) & 0xFFFF);
    final byte addressHigh = (byte) ((address & 0xFF00) >> 8);
    final int additionalCyclesNeeded;
    if (addressHigh != baseAddressHigh) {
      additionalCyclesNeeded = 1;
    } else {
      additionalCyclesNeeded = 0;
    }
    final byte value = bus.read(address);
    return new AddressingResult(register, value, additionalCyclesNeeded);
  };

  AddressingModeFunction IMMEDIATE = (register, bus) -> {
    final byte readValue = bus.read(register.getAndIncrementProgramCounter());
    return new AddressingResult(register, readValue, 0);
  };

  AddressingModeFunction IMPLIED = (register, bus) -> new AddressingResult(register, (byte) 0, 0);

  AddressingModeFunction INDIRECT = (register, bus) -> {
    final short indirect = readAddressAtProgramPointerFromBus(register, bus);
    final short indirectLow = (short) (indirect & 0xFF);
    final short absoluteAddressLow = readAddressAsShortFromBus(indirect, bus);
    final short absoluteAddressHigh;
    if (indirectLow == 0xFF) {
      // Hardware bug in 6502
      absoluteAddressHigh = (short) ((indirect & 0xFFFF) >> 8);
    } else {
      absoluteAddressHigh = readAddressAsShortFromBus((short) ((indirect + 1) & 0xFFFF), bus);
    }
    final short absoluteAddress =
        (short) ((short) ((absoluteAddressHigh & 0xFF) << 8) | absoluteAddressLow);
    final byte value = bus.read(absoluteAddress);
    return new AddressingResult(register, value, 0);
  };

  private static short readAddressAsShortFromBus(short address, CpuBus bus) {
    return (short) (bus.read(address) & 0xFF);
  }

  AddressingModeFunction INDIRECT_ZERO_PAGE_X = (register, bus) -> {
    final short zeroPageIndirectAddress =
        (short) (bus.read(register.getAndIncrementProgramCounter()) & 0xFF);
    final short zeroPagePlusXOffsetIndirectAddress =
        (short) (zeroPageIndirectAddress + register.x());
    final short absoluteAddress = readAddressFromBus(zeroPagePlusXOffsetIndirectAddress, bus);
    final byte value = bus.read(absoluteAddress);
    return new AddressingResult(register, value, 0);
  };

  private static short readAddressFromBus(short address, CpuBus bus) {
    final short addressLow = readAddressAsShortFromBus(address++, bus);
    final short addressHigh = readAddressAsShortFromBus(address, bus);
    return (short) (((addressHigh & 0xFF) << 8) | addressLow);
  }

  AddressingModeFunction INDIRECT_ZERO_PAGE_Y = (register, bus) -> {
    final short zeroPageIndirectAddress =
        readAddressAsShortFromBus(register.getAndIncrementProgramCounter(), bus);
    final short absoluteAddress = readAddressFromBus(zeroPageIndirectAddress, bus);
    final byte absoluteAddressHigh = (byte) ((absoluteAddress & 0xFF00) >> 8);
    final short absoluteAddressPlusY = (short) (absoluteAddress + register.y());
    final byte absoluteAddressPlusYHigh = (byte) ((absoluteAddressPlusY & 0xFF00) >> 8);
    final int additionalCyclesNeeded;
    if (absoluteAddressPlusYHigh != absoluteAddressHigh) {
      additionalCyclesNeeded = 1;
    } else {
      additionalCyclesNeeded = 0;
    }
    final byte value = bus.read(absoluteAddressPlusY);
    return new AddressingResult(register, value, additionalCyclesNeeded);
  };

  AddressingModeFunction REL = (register, bus) -> {
    final byte value = bus.read(register.getAndIncrementProgramCounter());
    return new AddressingResult(register, value, 0);
  };

  AddressingModeFunction ZERO_PAGE = (register, bus) -> {
    short zeroPageAddress = readAddressAttProgramPointerAsShortFromBus(register, bus);
    byte value = bus.read(zeroPageAddress);
    return new AddressingResult(register, value, 0);
  };

  private static short readAddressAttProgramPointerAsShortFromBus(Register register, CpuBus bus) {
    return (short) (bus.read(register.getAndIncrementProgramCounter()) & 0xFF);
  }

  AddressingModeFunction ZERO_PAGE_X = (register, bus) -> {
    short zeroPageAddress = readAddressAttProgramPointerAsShortFromBus(register, bus);
    short zeroPageAddressPlusX = (short) (zeroPageAddress + register.x());
    byte value = bus.read(zeroPageAddressPlusX);
    return new AddressingResult(register, value, 0);
  };

  AddressingModeFunction ZERO_PAGE_Y = (register, bus) -> {
    short zeroPageAddress = readAddressAttProgramPointerAsShortFromBus(register, bus);
    short zeroPageAddressPlusY = (short) (zeroPageAddress + register.y());
    byte value = bus.read(zeroPageAddressPlusY);
    return new AddressingResult(register, value, 0);
  };
}