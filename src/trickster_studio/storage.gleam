import gleam/int
import gleam/result
import trickster_studio/blockpos.{type BlockPos}
import trickster_studio/error.{type TricksterStudioError}
import trickster_studio/serde

pub type Slot {
  Slot(index: Int, source: Source)
}

pub type Source {
  CasterSource
  BlockSource(BlockPos)
  EntitySource(String)
  SlotSource(Slot)
}

pub fn slot_to_string(slot: Slot) -> String {
  let source_string = source_to_string(slot.source)

  "slot " <> int.to_string(slot.index) <> " at " <> source_string
}

pub fn source_to_string(source: Source) -> String {
  case source {
    CasterSource -> "Caster"
    EntitySource(uuid) -> uuid
    SlotSource(slot) -> slot_to_string(slot)
    BlockSource(blockpos.BlockPos(x, y, z)) ->
      "("
      <> int.to_string(x)
      <> ", "
      <> int.to_string(y)
      <> ", "
      <> int.to_string(z)
      <> ")"
  }
}

pub fn encode_slot(slot: Slot) -> BitArray {
  let index_bit_array = serde.encode_int(slot.index)

  let source_bit_array = encode_source(slot.source)

  <<index_bit_array:bits, source_bit_array:bits>>
}

pub fn encode_source(source: Source) -> BitArray {
  let #(source_type_bit_array, source_bit_array) = case source {
    CasterSource -> #(serde.encode_string("caster"), <<>>)
    BlockSource(pos) -> #(serde.encode_string("block"), blockpos.encode(pos))
    EntitySource(uuid) -> #(
      serde.encode_string("entity"),
      serde.encode_string(uuid),
    )
    SlotSource(slot) -> #(serde.encode_string("slot"), encode_slot(slot))
  }
  <<source_type_bit_array:bits, source_bit_array:bits>>
}

pub fn decode_slot(
  bit_array: BitArray,
) -> Result(#(Slot, BitArray), TricksterStudioError) {
  use #(index, bit_array) <- result.try(serde.decode_int(bit_array))

  let source_result = decode_source(bit_array)

  use #(source, bit_array) <- result.map(source_result)

  #(Slot(index:, source:), bit_array)
}

pub fn decode_source(
  bit_array: BitArray,
) -> Result(#(Source, BitArray), TricksterStudioError) {
  use #(source_type, bit_array) <- result.try(serde.decode_string(bit_array))

  case source_type {
    "caster" -> Ok(#(CasterSource, bit_array))

    "block" -> decode_block_source(bit_array)

    "entity" -> decode_entity_source(bit_array)

    "slot" -> decode_slot_source(bit_array)

    _ -> Error(error.InvalidSlotType)
  }
}

fn decode_block_source(
  bit_array: BitArray,
) -> Result(#(Source, BitArray), TricksterStudioError) {
  use #(block_pos, bit_array) <- result.map(blockpos.decode(bit_array))

  #(BlockSource(block_pos), bit_array)
}

fn decode_entity_source(
  bit_array: BitArray,
) -> Result(#(Source, BitArray), TricksterStudioError) {
  use #(uuid, bit_array) <- result.map(serde.decode_string(bit_array))
  #(EntitySource(uuid), bit_array)
}

fn decode_slot_source(
  bit_array: BitArray,
) -> Result(#(Source, BitArray), TricksterStudioError) {
  use #(slot, bit_array) <- result.map(decode_slot(bit_array))

  #(SlotSource(slot), bit_array)
}
