import gleam/bit_array
import gleam/bool
import gleam/function
import gleam/list
import gleam/pair
import gleam/result.{map}
import ieee_float.{type IEEEFloat}
import trickster_studio/error.{type TricksterStudioError, Todo}
import trickster_studio/identifier
import trickster_studio/pattern.{type Pattern}
import trickster_studio/serde

pub type Fragment {
  SpellPartFragment(SpellPart)
  PatternGlyphFragment(Pattern)
  PatternLiteralFragment(Pattern)
  NumberFragment(IEEEFloat)
}

pub type SpellPart {
  SpellPart(glyph: Fragment, children: List(SpellPart))
}

type SpellInstruction {
  EnterScopeInstruction
  ExitScopeInstruction
  FragmentInstruction(fragment: Fragment)
}

// Identifiers
const number_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "number",
)

const pattern_glyph_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "pattern",
)

const pattern_literal_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "pattern_literal",
)

const spell_part_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "spell_part",
)

// -----------

const gzip_header: BitArray = <<31, -117, 8, 0, 0, 0, 0, 0, 0, -1>>

pub fn to_base64(fragment: Fragment) -> String {
  to_bytes(fragment)
  |> serde.to_base64
}

pub fn to_bytes(fragment: Fragment) -> BitArray {
  let bytes =
    encode_bytes(fragment)
    |> bit_array.append(<<4>>, _)
    |> serde.gzip

  case bytes {
    <<_:bytes-size(10), rest:bytes>> -> rest
    _ -> bytes
  }
}

fn encode_bytes(fragment: Fragment) -> BitArray {
  case fragment {
    NumberFragment(number) ->
      encode_fragment_struct(number_id, ieee_float.to_bytes_64_be(number))
    PatternGlyphFragment(pattern) ->
      encode_fragment_struct(
        pattern_glyph_id,
        serde.encode_int(pattern.to_int(pattern)),
      )
    PatternLiteralFragment(pattern) ->
      encode_fragment_struct(
        pattern_literal_id,
        serde.encode_int(pattern.to_int(pattern)),
      )
    SpellPartFragment(SpellPart(glyph, children)) ->
      encode_fragment_struct(
        spell_part_id,
        bit_array.concat([
          encode_bytes(glyph),
          serde.encode_list(list.map(children, encode_spell_part)),
        ]),
      )
  }
}

fn encode_spell_part(spell_part: SpellPart) -> BitArray {
  let instructions = flatten_node(spell_part, [])
  serde.encode_list(list.map(instructions, encode_spell_instruction))
}

fn encode_fragment_struct(
  id: identifier.Identifier,
  value: BitArray,
) -> BitArray {
  serde.encode_identifier(id)
  |> bit_array.append(value)
}

fn flatten_node(
  node: SpellPart,
  instructions: List(SpellInstruction),
) -> List(SpellInstruction) {
  let instructions = [
    ExitScopeInstruction,
    FragmentInstruction(node.glyph),
    ..instructions
  ]

  list.flat_map(node.children, fn(child) { flatten_node(child, instructions) })

  [EnterScopeInstruction, ..instructions]
}

fn encode_spell_instruction(spell_instruction: SpellInstruction) -> BitArray {
  case spell_instruction {
    FragmentInstruction(fragment) ->
      bit_array.concat([
        serde.encode_int(1),
        serde.encode_boolean(True),
        to_bytes(fragment),
      ])

    EnterScopeInstruction ->
      bit_array.concat([serde.encode_int(2), serde.encode_boolean(False)])

    ExitScopeInstruction ->
      bit_array.concat([serde.encode_int(3), serde.encode_boolean(False)])
  }
}

pub fn from_base64(base64: String) -> Result(Fragment, TricksterStudioError) {
  serde.from_base64(base64)
  |> from_bytes
}

pub fn from_bytes(bit_array: BitArray) -> Result(Fragment, TricksterStudioError) {
  let bytes = serde.ungzip(bit_array.append(gzip_header, bit_array))

  case bytes {
    <<4:size(8), rest:bytes>> ->
      decode_bytes(rest)
      |> result.map(pair.first)

    _ -> Error(Todo)
  }
}

fn decode_bytes(
  bit_array: BitArray,
) -> Result(#(Fragment, BitArray), TricksterStudioError) {
  use #(id, bit_array) <- result.try(serde.decode_identifier(bit_array))

  case id {
    id if id == number_id ->
      serde.decode_float
      |> serde.map_decoder(NumberFragment)
      |> serde.apply_decoder(bit_array)

    id if id == pattern_literal_id ->
      serde.decode_int
      |> serde.flatmap_decoder(pattern.from_int)
      |> serde.map_decoder(PatternLiteralFragment)
      |> serde.apply_decoder(bit_array)

    id if id == pattern_glyph_id ->
      serde.decode_int
      |> serde.flatmap_decoder(pattern.from_int)
      |> serde.map_decoder(PatternGlyphFragment)
      |> serde.apply_decoder(bit_array)

    id if id == spell_part_id -> {
      let children_decoder =
        serde.list_of(spell_instruction_from_bytes)
        |> serde.flatmap_decoder(decode_instructions(_, [], []))
        |> serde.list_of

      use #(glyph, bit_array) <- result.try(decode_bytes(bit_array))
      use #(children, bit_array) <- result.map(children_decoder(bit_array))

      #(SpellPartFragment(SpellPart(glyph, children)), bit_array)
    }

    _ -> Error(Todo)
  }
}

fn spell_instruction_from_bytes(
  bit_array: BitArray,
) -> Result(#(SpellInstruction, BitArray), TricksterStudioError) {
  use #(id, bit_array) <- result.try(serde.decode_int(bit_array))
  use #(present, bit_array) <- result.try(serde.decode_boolean(bit_array))

  case id, present {
    1, True -> result.map(from_bytes(bit_array), FragmentInstruction)
    2, False -> Ok(EnterScopeInstruction)
    3, False -> Ok(ExitScopeInstruction)
    _, _ -> Error(Todo)
  }
  |> result.map(pair.new(_, bit_array))
}

fn decode_instructions(
  instructions: List(SpellInstruction),
  scope: List(Int),
  children: List(SpellPart),
) -> Result(SpellPart, TricksterStudioError) {
  case instructions, children {
    [], [first_child] -> Ok(first_child)

    [first_instruction, ..remaining_instructions], _ -> {
      case first_instruction, scope {
        EnterScopeInstruction, _ ->
          decode_instructions(remaining_instructions, [0, ..scope], children)

        ExitScopeInstruction, [_] ->
          decode_instructions(remaining_instructions, [], children)

        ExitScopeInstruction, [_, next_scope, ..bit_array_scope] ->
          decode_instructions(
            remaining_instructions,
            [next_scope + 1, ..bit_array_scope],
            children,
          )

        FragmentInstruction(fragment), [first_scope, ..bit_array_scope] -> {
          let #(args, remaining_children) = list.split(children, first_scope)
          let args = list.reverse(args)

          decode_instructions(remaining_instructions, bit_array_scope, [
            SpellPart(fragment, args),
            ..remaining_children
          ])
        }
        _, _ -> Error(Todo)
      }
    }

    _, _ -> Error(Todo)
  }
}
