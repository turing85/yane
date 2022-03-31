package de.turing85.yane.impl.cpu6502;

import de.turing85.yane.api.*;
import lombok.*;
import lombok.experimental.Delegate;

/**
 * <p>The addressing modes supported by the 6502 processor.</p>
 *
 * <p>When {@link #fetch(Register, CpuBus)} is called, data from the {@link CpuBus} and the
 * {@link Register} is read, and the {@link Register} is mutated. In particular, the {@link
 * Register#programCounter} is incremented. The result of a fetch is encapsulated in an {@link
 * AddressingResult}.</p>
 *
 * <p>Different modes read different number of bytes during execution, but the number of bytes
 * read for one mode is always the same, i.e. does not depend on the data read. The number of bytes 
 * read is equivalent to the number of times {@link Register#programCounter} is incremented.</p>
 */
@Value
@EqualsAndHashCode
@Getter(AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class AddressingMode implements AddressingModeFunction {
  /**
   * Since the {@link #IMPLIED} addressing mode does not read from any address (in fact, it does
   * not read anything since the data is implied in the command itself, e.g. {@link Command#TAX},
   * which transfers the data of {@link Register#a} into {@link Register#x}), this value is set to
   * {@link AddressingResult#address}. This indicates that the address should not be read.
   */
  static final int IMPLIED_LOADED_ADDRESS = Integer.MIN_VALUE;
  /**
   * Only used by pseudo-{@link Instruction}s with illegal opcodes.
   */
  static final int UNKNOWN_LOADED_ADDRESS = Integer.MIN_VALUE;

  /**
   * The {@link #IMPLIED} and {@link #UNKNOWN} addressing modes do not read any data, thus {@link
   * AddressingResult#value} is set to this value, indicating that no data has been read.
   */
  static final int NOTHING_READ_VALUE = Integer.MIN_VALUE;

  /**
   * Number of bytes read by the {@link #IMPLIED} addressing mode.
   */
  private static final int IMPLIED_ADDRESSING_BYTES_TO_READ = 0;

  /**
   * Number of bytes read by the {@link #UNKNOWN} addressing mode.
   */
  private static final int UNKNOWN_ADDRESSING_BYTES_TO_READ = 0;

  /**
   * Number of bytes read by the {@link #IMMEDIATE} addressing mode.
   */
  private static final int IMMEDIATE_ADDRESSING_BYTES_TO_READ = 1;

  /**
   * Number of bytes read by the {@link #INDIRECT} addressing mode.
   */
  private static final int INDIRECT_ADDRESSING_BYTES_TO_READ = 1;

  /**
   * Number of bytes read by the {@link #RELATIVE} addressing mode.
   */
  private static final int RELATIVE_ADDRESSING_BYTES_TO_READ = 1;

  /**
   * Number of bytes read by the {@link #ABSOLUTE}, {@link #ABSOLUTE_X} and {@link #ABSOLUTE_Y}
   * addressing modes.
   */
  private static final int ABSOLUTE_ADDRESSING_BYTES_TO_READ = 2;

  /**
   * Number of bytes read by the {@link #INDIRECT_ZERO_PAGE_Y}, {@link #INDIRECT_ZERO_PAGE_X},
   * {@link #ZERO_PAGE}, {@link #ZERO_PAGE_X} and {@link #ZERO_PAGE_Y} addressing modes. addressing
   * modes.
   */
  private static final int ZERO_PAGE_ADDRESSING_BYTES_TO_READ = 1;

  /**
   * The actual implementation of the accessing mode.
   */
  @Delegate
  AddressingModeFunction accessingModeFunction;

  /**
   * The mnemonic of the addressing mode.
   */
  String mnemonic;

  /**
   * How many bytes from the {@link Register#programCounter} are read and, in return, how often
   * the {@link Register#programCounter} is incremented.
   */
  int bytesToRead;

  /**
   * <p>Accumulator addressing mode.</p>
   *
   * <p>Loads the value of {@link Register#a}.</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code A}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@link #IMPLIED_LOADED_ADDRESS} (marker-value)</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>value of {@link Register#a}</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 0}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td> <td>never</td>
   *   </tr>
   * </table>
   */
  static final AddressingMode ACCUMULATOR = new AddressingMode(
      (register, bus) -> new AddressingResult(register, bus, IMPLIED_LOADED_ADDRESS, register.a()),
      "A",
      IMPLIED_ADDRESSING_BYTES_TO_READ);

  /**
   * <p>Absolute addressing mode.</p>
   *
   * <p>To construct the {@code address}, this mode reads two 8-bit values from the {@link CpuBus}
   * at addresses {@link Register#programCounter} ({@code PC_LOW}) and {@link
   * Register#programCounter}{@code + 1} ({@code PC_HIGH}). Then a 16-bit address is constructed by
   * using {@code PC_LOW} as the 8 lower bits and {@code PC_HIGH} as the 8 higher bits.</p>
   *
   * <p>The {@code value} of is then read from the {@code address}.</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code abs}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code address}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>value of {@code address}</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 2}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td> <td>never</td>
   *   </tr>
   * </table>
   */
  static final AddressingMode ABSOLUTE = new AddressingMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointer(register, bus);
        return new AddressingResult(register, bus, address, bus.read(address));
      },
      "abs",
      ABSOLUTE_ADDRESSING_BYTES_TO_READ);

  /**
   * <p>Absolute addressing mode with X register offset.</p>
   *
   * <p>To construct the {@code address}, this mode reads two 8-bit values from the {@link CpuBus}
   * at addresses {@link Register#programCounter} ({@code addressLow}) and {@link
   * Register#programCounter}{@code + 1} ({@code addressHigh}). Then a 16-bit address is constructed
   * by using {@code addressLow} as the 8 lower bits and {@code addressHigh} as the 8 higher bits.
   * We will call this address {@code address}. Finally, the value of {@link Register#x} is added
   * to {@code address}. We will call this address {@code addressPlusX}.</p>
   *
   * <p>The {@code value} of is then read from the {@code addressPlusX}.</p>
   *
   * <p>This addressing modes needs one additional cycle, if the high 8 bits of {@code address} and
   * {@code addressPlusX} differ, i.e. if a page boundary is crossed.</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code abs,x}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code address}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>value of {@code addressPlusX}</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 2}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td>
   *     <td>
   *       {@code 1}, if {@code address} and {@code addressPlusX} reside in different memory pages,
   *       i.e. if the high byte of {@code address} and the high byte of {@code addressPlusX} are
   *       not equal.
   *       <br>{@code 0} otherwise.
   *     </td>
   *   </tr>
   * </table>
   */
  static final AddressingMode ABSOLUTE_X = new AddressingMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointer(register, bus);
        final int addressPlusX = (address + register.x()) & 0xFFFF;
        return new AddressingResult(
            register,
            bus,
            addressPlusX,
            bus.read(addressPlusX),
            highBytesDiffers(address, addressPlusX) ? 1 : 0);
      },
      "abs,X",
      ABSOLUTE_ADDRESSING_BYTES_TO_READ);

  /**
   * <p>Absolute addressing mode with Y register offset.</p>
   *
   * <p>To construct the {@code address}, this mode reads two 8-bit values from the {@link CpuBus}
   * at addresses {@link Register#programCounter} ({@code addressLow}) and {@link
   * Register#programCounter}{@code + 1} ({@code addressHigh}). Then a 16-bit address is constructed
   * by using {@code addressLow} as the 8 lower bits and {@code addressHigh} as the 8 higher bits.
   * We will call this address {@code address}. Finally, the value of {@link Register#y} is added
   * to {@code address}. We will call this address {@code addressPlusY}.</p>
   *
   * <p>The {@code value} of is then read from the {@code addressPlusY}.</p>
   *
   * <p>This addressing modes needs one additional cycle, if the high 8 bits of {@code address} and
   * {@code addressPlusY} differ, i.e. if a page boundary is crossed.</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code abs,y}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code address}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>value of {@code addressPlusY}</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 2}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td>
   *     <td>
   *       {@code 1}, if {@code address} and {@code addressPlusY} reside in different memory pages,
   *       i.e. if the high byte of {@code address} and the high byte of {@code addressPlusY} are
   *       not equal.
   *       <br>{@code 0} otherwise.
   *     </td>
   *   </tr>
   * </table>
   */
  static final AddressingMode ABSOLUTE_Y = new AddressingMode(
      (register, bus) -> {
        final int address = readAddressAtProgramPointer(register, bus);
        final int addressPlusY = (address + register.y()) & 0xFFFF;
        return new AddressingResult(
            register,
            bus,
            addressPlusY,
            bus.read(addressPlusY),
            highBytesDiffers(address, addressPlusY) ? 1 : 0);
      },
      "abs,y",
      ABSOLUTE_ADDRESSING_BYTES_TO_READ);

  /**
   * <p>Immediate addressing mode.</p>
   *
   * <p>The next byte at {@link Register#programCounter} is read, and the
   * {@link Register#programCounter} is incremented by one.</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code #}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>the {@code programCounter}-value, before the increment</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>value, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 1}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td> <td>never</td>
   *   </tr>
   * </table>
   */
  static final AddressingMode IMMEDIATE = new AddressingMode(
      (register, bus) -> {
        final int address = register.getAndIncrementProgramCounter();
        return new AddressingResult(register, bus, address, bus.read(address));
      },
      "#",
      IMMEDIATE_ADDRESSING_BYTES_TO_READ);

  /**
   * <p>Implied addressing mode.</p>
   *
   * <p>Since the data is implied by the command, no data is read.</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code impl}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@link #IMPLIED_LOADED_ADDRESS}, marker value</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>{@link #NOTHING_READ_VALUE}, marker value</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 0}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td> <td>never</td>
   *   </tr>
   * </table>
   */
  static final AddressingMode IMPLIED = new AddressingMode(
      (register, bus) ->
          new AddressingResult(register, bus, IMPLIED_LOADED_ADDRESS, NOTHING_READ_VALUE),
      "impl",
      IMPLIED_ADDRESSING_BYTES_TO_READ);

  /**
   * <p>Indirect addressing mode.</p>
   *
   * <p>To construct the {@code address}, this mode reads two 8-bit values from the {@link CpuBus}
   * at addresses {@link Register#programCounter} ({@code indirectAddressLow}) and {@link
   * Register#programCounter}{@code + 1} ({@code indirectAddressHigh}). Then a 16-bit address is
   * constructed by using {@code addressLow} as the 8 lower bits and {@code addressHigh} as the 8
   * higher bits. We will call this address {@code indirectAddress}. A third and fourth read is made
   * to addresses {@code indirectAddress} and {@code indirectAddress + 1}, again interpreting the 
   * two 8-bit values as 16-bit address. We will call this address {@code address}. Finally, the 
   * value from {@code address} is read in a fifth read.</p>
   *
   * <p>There is a hardware bug in the 6502 processor: if the lower 8 bits of {@code
   * indirectAddress} are all set to {@code 1}, then, {@code addressHigh} is not read from
   * {@code indirectAddress + 1}, but from {@code ((indirectLow + 1) & 0x00FF) |
   * (indirectHigh << 8)}.So for example, when {@code indirectAddress} has value {@code 0x01FF},
   * then {@code addressLow} should be read from {@code 0x0200}. Instead, through this bus, it is
   * read from {@code 0x0100} (not changing the higher 8 bits).</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code ind}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code address}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>{@code value}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 2}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td> <td>never</td>
   *   </tr>
   * </table>
   */
  static final AddressingMode INDIRECT = new AddressingMode(
      (register, bus) -> {
        final int addressLow = readAddressAtProgramPointer(register, bus);
        final int addressHigh = lowestByteIsAllOnes(addressLow)
            // Hardware bug in 6502
            ? addressLow & 0xFF00
            // normal behaviour
            : (addressLow + 1) & 0xFFFF;
        final int address =
            bus.read(addressLow) | (bus.read(addressHigh) << 8);
        return new AddressingResult(register, bus, address, bus.read(address));
      },
      "ind",
      2);

  /**
   * <p>Indirect zero page addressing mode with X register offset.</p>
   *
   * <p>To construct the {@code address}, this mode reads one 8-bit values from the {@link CpuBus}
   * at addresses {@link Register#programCounter} ({@code zeroPageIndirectAddress}). Then, the value
   * of {@link Register#x} is added to {@code zeroPageAddress} ({@code
   * zeroPageIndirectAddressPlusX}). This addition is done in the 8-bit domain, i.e. the
   * result value is between {@code 0} and {@code 255}. The 8-bit value of {@code
   * zeroPageIndirectAddressPlusX} is interpreted as a 16-bit address by assuming that the 8 high
   * bit are all set to 0, effectively forming a zero page address (hence the name). A second read 
   * is made to address {@code zeroPageIndirectAddressPlusX}. We will call this value 
   * {@code address}. Again, {@code address} is interpreted as a zero page address. Finally, a
   * third read is made to {@code address}, to determine the {@code value}.</p>
   *
   * <p>Please note that the behaviour of this addressing mode differs from addressing mode
   * {@link #INDIRECT_ZERO_PAGE_Y}:</p>
   *
   * <ul>
   *   <li>This method adds {@link Register#x} to the <strong>indirect address</strong>, while</li>
   *   <li>
   *     {@link #INDIRECT_ZERO_PAGE_Y} adds {@link Register#y} to the <strong>address</strong>
   *   </li>
   * </ul>
   *
   * <p>The difference in behaviour is intended and not a bug.</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code X,ind}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code address}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>{@code value}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 1}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td> <td>never</td>
   *   </tr>
   * </table>
   */
  static final AddressingMode INDIRECT_ZERO_PAGE_X = new AddressingMode(
      (register, bus) -> {
        final int zeroPageIndirectAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPageIndirectAddressPlusX =
            (zeroPageIndirectAddress + register.x()) & 0xFF;
        final int address = bus.read(zeroPageIndirectAddressPlusX);
        return new AddressingResult(register, bus, address, bus.read(address));
      },
      "X,ind",
      1);

  /**
   * <p>Indirect zero page addressing mode with Y register offset.</p>
   *
   * <p>To construct the {@code address}, this mode reads one 8-bit values from the {@link CpuBus}
   * at addresses {@link Register#programCounter} ({@code zeroPageIndirectAddress}).The 8-bit value
   * of {@code zeroPageIndirectAddress} is interpreted as a 16-bit address by assuming that the 8
   * high bits are all set to 0, effectively forming a zero page address (hence the name). A second 
   * read is made to address {@code zeroPageIndirectAddress} ({@code address}). Then, the value of 
   * {@link Register#y} is added to {@code address} ({@code addressPlusY}). This addition is done in
   * the 8-bit domain, i.e. the result value is between {@code 0} and {@code 255}. Finally, a third 
   * read is made to {@code addressPlusY}, to determine the {@code value}.</p>
   *
   * <p>This addressing modes needs one additional cycle, if the high 8 bits of {@code address} and
   * {@code addressPlusY} differ, i.e. if a page boundary is crossed.</p>
   *
   * <p>Please note that this addressing mode behaves differently than
   * {@link #INDIRECT_ZERO_PAGE_X}:</p>
   *
   * <ul>
   *   <li>This method adds {@link Register#y} to the <strong>address</strong>, while</li>
   *   <li>
   *     {@link #INDIRECT_ZERO_PAGE_X} adds {@link Register#x} to the <strong>indirect
   *     address</strong>
   *   </li>
   * </ul>
   *
   * <p>The difference in behaviour is intended and not a bug.</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code ind,Y}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code addressPlusY}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>{@code value}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 1}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td>
   *     <td>
   *       {@code 1}, if {@code address} and {@code addressPlusY} reside in different memory pages,
   *       i.e. if the high byte of {@code address} and the high byte of {@code addressPlusY} are
   *       not equal.
   *       <br>{@code 0} otherwise.
   *     </td>
   *   </tr>
   * </table>
   */
  static final AddressingMode INDIRECT_ZERO_PAGE_Y = new AddressingMode(
      (register, bus) -> {
        final int zeroPageIndirectAddress = bus.read(register.getAndIncrementProgramCounter());
        final int address = readAddressFromBus(zeroPageIndirectAddress, bus);
        final int addressPlusY = (address + register.y()) & 0xFFFF;
        return new AddressingResult(
            register,
            bus,
            addressPlusY,
            bus.read(addressPlusY),
            highBytesDiffers(address, addressPlusY) ? 1 : 0);
      },
      "ind,Y",
      ZERO_PAGE_ADDRESSING_BYTES_TO_READ);

  /**
   * <p>Relative addressing mode.</p>
   *
   * <p>This addressing mode is only used by branching instructions to determine the jump target
   * address. To construct the {@code address}, the address at {@link Register#programCounter} is
   * read, the {@link Register#programCounter} is incremented by {@code 1}. We will call this
   * value {@code relativeAddress}. It is interpreted as 8-bit singed value, i.e. its value is
   * between {@code -128} and {@code 127}. Then, {@code address} is constructed by adding the
   * (incremented) value of {@link Register#programCounter} to {@code relativeAddress}. Finally,
   * the {@code value} is read from {@code address}.</p>
   *
   * <p>Notice that the addressing mode does not execute the jump. It only calculates the jump
   * address.</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code rel}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code address}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>{@code value}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 1}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td> <td>never</td>
   *   </tr>
   * </table>
   */
  static final AddressingMode RELATIVE = new AddressingMode(
      (register, bus) -> {
        final int relativeAddress = bus.read(register.getAndIncrementProgramCounter());
        final int signedRelativeAddress;
        if ((relativeAddress & 0x80) > 0) {
          signedRelativeAddress = relativeAddress | 0xFF00;
        } else {
          signedRelativeAddress = relativeAddress;
        }
        final int address = (register.programCounter() + signedRelativeAddress) & 0xFFFF;
        return new AddressingResult(register, bus, address, bus.read(address));
      },
      "rel",
      RELATIVE_ADDRESSING_BYTES_TO_READ);

  /**
   * <p>Zero page addressing mode.</p>
   *
   * <p>To construct the {@code address}, this mode reads one 8-bit values from the {@link CpuBus}
   * at addresses {@link Register#programCounter}.We will call this value {@code zeroPageAddress}. 
   * The 8-bit value of {@code zeroPageAddress} is interpreted as a 16-bit address by assuming that 
   * the 8 high bits are all set to 0, effectively forming a zero page address (hence the name). A 
   * second read is made to {@code zeroPageAddress}, to determine the {@code value}.</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code zpg}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code zeroPageAddress}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>{@code value}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 1}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td> <td>never</td>
   *   </tr>
   * </table>
   */
  static final AddressingMode ZERO_PAGE = new AddressingMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        return new AddressingResult(register, bus, zeroPageAddress, bus.read(zeroPageAddress));
      },
      "zpg",
      ZERO_PAGE_ADDRESSING_BYTES_TO_READ);

  /**
   * <p>Zero page addressing mode with X register offset.</p>
   *
   * <p>To construct the {@code address}, this mode reads one 8-bit values from the {@link CpuBus}
   * at addresses {@link Register#programCounter}.We will call this value {@code zeroPageAddress}.
   * Second, the value of {@link Register#x} is added to {@code address}. We will call this value
   * {@code zeroPageAddressPlusX}. This addition is done in the 8-bit domain, i.e. the result value 
   * is between {@code 0} and {@code 255}. The 8-bit value of {@code zeroPageAddressPlusX} is 
   * interpreted as a 16-bit address by assuming that the 8 high bits are all set to 0, effectively 
   * forming a zero page address (hence the name). A second read is made to 
   * {@code zeroPageAddressPlusX} to determine the {@code value}.</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code zpg,X}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code zeroPageAddressPlusX}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>{@code value}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 1}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td> <td>never</td>
   *   </tr>
   * </table>
   */
  static final AddressingMode ZERO_PAGE_X = new AddressingMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPageAddressPlusX = (zeroPageAddress + register.x()) & 0x00FF;
        return new AddressingResult(
            register,
            bus,
            zeroPageAddressPlusX,
            bus.read(zeroPageAddressPlusX));
      },
      "zpg,X",
      ZERO_PAGE_ADDRESSING_BYTES_TO_READ);

  /**
   * <p>Zero page addressing mode with Y register offset.</p>
   *
   * <p>To construct the {@code address}, this mode reads one 8-bit values from the {@link CpuBus}
   * at addresses {@link Register#programCounter}.We will call this value {@code zeroPageAddress}.
   * Second, the value of {@link Register#y} is added to {@code address}. We will call this value
   * {@code zeroPageAddressPlusY}. This addition is done in the 8-bit domain, i.e. the result value 
   * is between {@code 0} and {@code 255}. The 8-bit value of {@code zeroPageAddressPlusY} is 
   * interpreted as a 16-bit address by assuming that the 8 high bits are all set to 0, effectively 
   * forming a zero page address (hence the name). A second read is made to 
   * {@code zeroPageAddressPlusY} to determine the {@code value}.</p>
   *
   * <table border="1">
   *   <caption>behavioural summary</caption>
   *   <tr>
   *     <th>description</th> <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>mnemonic</td> <td>{@code zpg,Y}</td>
   *   </tr>
   *   <tr>
   *     <td>address</td> <td>{@code zeroPageAddressPlusY}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>value</td> <td>{@code value}, as described above</td>
   *   </tr>
   *   <tr>
   *     <td>program counter increased by</td> <td>{@code 1}</td>
   *   </tr>
   *   <tr>
   *     <td>additional cycle needed</td> <td>never</td>
   *   </tr>
   * </table>
   */  
  static final AddressingMode ZERO_PAGE_Y = new AddressingMode(
      (register, bus) -> {
        final int zeroPageAddress = bus.read(register.getAndIncrementProgramCounter());
        final int zeroPageAddressPlusY = (zeroPageAddress + register.y()) & 0x00FF;
        return new AddressingResult(
            register,
            bus,
            zeroPageAddressPlusY,
            bus.read(zeroPageAddressPlusY));
      },
      "zpg,Y",
      ZERO_PAGE_ADDRESSING_BYTES_TO_READ);

  /**
   * This addressing mode is used to represent unknown instructions.
   */
  static final AddressingMode UNKNOWN = new AddressingMode(
      (register, bus) ->
          new AddressingResult(register, bus, UNKNOWN_LOADED_ADDRESS, NOTHING_READ_VALUE),
      "???",
      UNKNOWN_ADDRESSING_BYTES_TO_READ);

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return mnemonic();
  }

  /**
   * <p>Reads two 8-bit values at {@link Register#programCounter} and
   * {@link Register#programCounter} {@code + 1}, interpreting them as 16-bit address.</p>
   *
   * <p>In the process, the {@link Register#programCounter} is incremented twice.</p>
   *
   * <p>The first read is interpreted as the lower 8 bits of the address, the second read is
   * interpreted as the higher 8 bits of the address.</p>
   *
   * @param register the register to get the program counter from. Notice that the register will be
   *        mutated, i.e. the program counter will be increased by 2
   * @param bus the bus to read the data from
   * @return the 16-bit address, as described above
   */
  private static int readAddressAtProgramPointer(Register register, CpuBus bus) {
    final int addressLow = bus.read(register.getAndIncrementProgramCounter());
    final int addressHigh = bus.read(register.getAndIncrementProgramCounter());
    return (addressHigh << 8) | addressLow;
  }

  /**
   * Checks whether the parameters differ in the higher 3 bytes.
   *
   * @param lhs the first parameter
   * @param rhs the second parameter
   * @return {@code true}, iff. {@code lhs} and {@code rhs} differ in the higher 3 bytes.
   */
  private static boolean highBytesDiffers(int lhs, int rhs) {
    final int addressHigh = lhs >> 8;
    final int addressPlusXHigh = rhs >> 8;
    return addressPlusXHigh != addressHigh;
  }

  /**
   * Checks whether the lowest byte contains only {@code 1}s.
   *
   * @param value the value to check
   * @return {@code true}, iff. the lower byte contains only {@code 1}s.
   */
  private static boolean lowestByteIsAllOnes(int value) {
    return (value & 0xFF) == 0xFF;
  }

  /**
   * <p>Given a {@code address}, reads two 8-bit values from addresses {@code address} and
   * {@code address + 1} and returns it as one 16-bit address.</p>
   *
   * <p>The 1st read is interpreted as the lower 8 bits of the 16-bit address, while the 2nd read is
   * interpreted as the higher 8 bits of the address.</p>
   *
   * @param address the address location
   * @param bus the bus to read from
   * @return the values of the two 8-bit reads, as 16-bit address
   */
  private static int readAddressFromBus(int address, CpuBus bus) {
    final int addressLow = bus.read(address);
    final int addressHigh = bus.read((address + 1) & 0x00FF);
    return (addressHigh << 8) | addressLow;
  }
}