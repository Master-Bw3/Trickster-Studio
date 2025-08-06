import gleam/bit_array
import gleam/bool
import gleam/function
import gleam/list
import gleam/pair
import gleam/result.{map}
import identifier
import ieee_float.{type IEEEFloat}
import trickster_studio/error.{type TricksterStudioError, Todo}
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
const number_id = identifier.Identifier("trickster", "number")

const pattern_glyph_id = identifier.Identifier("trickster", "pattern")

const pattern_literal_id = identifier.Identifier("trickster", "pattern_literal")

const spell_part_id = identifier.Identifier("trickster", "spell_part")

// -----------

pub fn to_bytes(fragment: Fragment) -> BitArray {
  case fragment {
    NumberFragment(number) ->
      encode_fragment_struct(number_id, ieee_float.to_bytes_64_be(number))
    PatternGlyphFragment(_) -> todo
    PatternLiteralFragment(_) -> todo
    SpellPartFragment(SpellPart(glyph, children)) -> todo
  }
}

fn encode_fragment_struct(
  id: identifier.Identifier,
  value: BitArray,
) -> BitArray {
  serde.encode_identifier(id)
  |> bit_array.append(value)
}

pub fn from_bytes(bit_array: BitArray) -> Result(Fragment, TricksterStudioError) {
  decode_bytes(bit_array)
  |> result.map(pair.first)
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
