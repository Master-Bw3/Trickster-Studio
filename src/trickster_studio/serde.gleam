import gleam/bit_array
import gleam/int
import gleam/list
import gleam/pair
import gleam/result.{try}
import gleam/string
import ieee_float.{type IEEEFloat}
import trickster_studio/error.{type TricksterStudioError, Todo}
import trickster_studio/identifier.{type Identifier}

const segment_bits: Int = 127

const continue_bit: Int = 128

@external(javascript, "./js_utils_ffi.mjs", "ushr")
fn ushr(value: Int, by: Int) -> Int

@external(javascript, "./js_utils_ffi.mjs", "to_base64")
pub fn to_base64(bit_array: BitArray) -> String

@external(javascript, "./js_utils_ffi.mjs", "from_base64")
pub fn from_base64(base64: String) -> BitArray

@external(javascript, "./js_utils_ffi.mjs", "gzip")
pub fn gzip(bit_array: BitArray) -> bit_array

@external(javascript, "./js_utils_ffi.mjs", "ungzip")
pub fn ungzip(bit_array: BitArray) -> bit_array

pub fn encode_string(value: String) -> BitArray {
  encode_var_int(string.length(value))
  |> bit_array.append(bit_array.from_string(value))
}

fn encode_var_int(value: Int) -> BitArray {
  case
    int.bitwise_and(value, segment_bits) == 0,
    int.bitwise_and(value, int.bitwise_not(segment_bits)) == 0
  {
    True, _ -> <<0:size(8)>>
    False, True -> <<value:size(8)>>
    False, False ->
      bit_array.append(
        <<
          int.bitwise_or(int.bitwise_and(value, segment_bits), continue_bit):size(8),
        >>,
        encode_var_int(ushr(value, 7)),
      )
  }
}

pub fn encode_list(list: List(BitArray)) -> BitArray {
  bit_array.concat([encode_var_int(list.length(list)), ..list])
}

pub fn encode_identifier(value: Identifier) -> BitArray {
  identifier.to_string(value)
  |> encode_string
}

pub fn encode_int(value: Int) -> BitArray {
  <<value:size(32)>>
}

pub fn encode_boolean(bool: Bool) -> BitArray {
  case bool {
    True -> <<1:size(8)>>
    False -> <<0:size(8)>>
  }
}

pub type Decoder(a) =
  fn(BitArray) -> Result(#(a, BitArray), TricksterStudioError)

pub fn decode_string(
  bit_array: BitArray,
) -> Result(#(String, BitArray), TricksterStudioError) {
  use #(length, bits_after_size) <- try(decode_var_int(bit_array, 0, 0))

  case bits_after_size {
    <<string_bits:bytes-size(length), rest:bytes>> ->
      bit_array.to_string(string_bits)
      |> result.map(pair.new(_, rest))
      |> result.replace_error(Todo)

    _ -> Error(Todo)
  }
}

pub fn list_of(decoder: Decoder(a)) -> Decoder(List(a)) {
  fn(bit_array: BitArray) {
    use #(length, bits_after_size) <- try(decode_var_int(bit_array, 0, 0))

    decode_list_rec(decoder, bits_after_size, [], length)
  }
}

fn decode_list_rec(
  decoder: fn(BitArray) -> Result(#(a, BitArray), b),
  bit_array: BitArray,
  list: List(a),
  length: Int,
) -> Result(#(List(a), BitArray), TricksterStudioError) {
  case length {
    0 -> Ok(#(list.reverse(list), bit_array))
    _ -> {
      let decode_result = decoder(bit_array) |> result.replace_error(Todo)
      use #(value, rest) <- result.try(decode_result)

      decode_list_rec(decoder, rest, [value, ..list], length - 1)
    }
  }
}

fn decode_var_int(
  bit_array: BitArray,
  value: Int,
  position: Int,
) -> Result(#(Int, BitArray), TricksterStudioError) {
  case bit_array {
    <<first:size(8), rest:bytes>> -> {
      let new_value =
        int.bitwise_or(
          value,
          int.bitwise_shift_left(int.bitwise_and(first, segment_bits), position),
        )

      case int.bitwise_and(first, continue_bit) == 0 {
        True -> Ok(#(new_value, rest))
        False -> decode_var_int(rest, new_value, position + 7)
      }
    }

    _ -> Error(Todo)
  }
}

pub fn map_decoder(decoder: Decoder(a), with fun: fn(a) -> b) -> Decoder(b) {
  fn(bit_array) {
    decoder(bit_array)
    |> result.map(pair.map_first(_, fun))
  }
}

pub fn flatmap_decoder(
  decoder: Decoder(a),
  with fun: fn(a) -> Result(b, TricksterStudioError),
) -> Decoder(b) {
  fn(bit_array) {
    use #(result, bit_array) <- result.try(decoder(bit_array))

    result.map(fun(result), pair.new(_, bit_array))
  }
}

pub fn decode_identifier(
  bit_array: BitArray,
) -> Result(#(Identifier, BitArray), TricksterStudioError) {
  use #(string, rest) <- result.try(decode_string(bit_array))

  identifier.from_string(string)
  |> result.map(pair.new(_, rest))
  |> result.replace_error(Todo)
}

pub fn decode_int(
  bit_array: BitArray,
) -> Result(#(Int, BitArray), TricksterStudioError) {
  case bit_array {
    <<first:32, rest:bytes>> -> Ok(#(first, rest))
    _ -> Error(Todo)
  }
}

pub fn decode_float(
  bit_array: BitArray,
) -> Result(#(IEEEFloat, BitArray), TricksterStudioError) {
  case bit_array {
    <<first:64-bits, rest:bytes>> ->
      Ok(#(ieee_float.from_bytes_64_be(first), rest))
    _ -> Error(Todo)
  }
}

pub fn decode_boolean(
  bit_array: BitArray,
) -> Result(#(Bool, BitArray), TricksterStudioError) {
  case bit_array {
    <<first:size(8), rest:bytes>> ->
      case first {
        0 -> Ok(#(False, rest))
        1 -> Ok(#(True, rest))
        _ -> Error(Todo)
      }
    _ -> Error(Todo)
  }
}

pub fn apply_decoder(
  decoder: Decoder(a),
  bit_array: BitArray,
) -> Result(#(a, BitArray), TricksterStudioError) {
  decoder(bit_array)
}
