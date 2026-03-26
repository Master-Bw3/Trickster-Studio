import gleam/bit_array
import trickster_studio/error.{Todo}

pub type BlockPos {
  BlockPos(x: Int, y: Int, z: Int)
}

pub fn decode(
  packed_long: BitArray,
) -> Result(#(BlockPos, BitArray), error.TricksterStudioError) {
  case packed_long {
    <<x:26-signed, z:26-signed, y:12-signed, bit_array:bits>> ->
      Ok(#(BlockPos(x, y, z), bit_array))
    _ -> Error(Todo)
  }
}

pub fn encode(block_pos: BlockPos) -> BitArray {
  let BlockPos(x:, y:, z:) = block_pos
  <<x:26, z:26, y:12>>
}
