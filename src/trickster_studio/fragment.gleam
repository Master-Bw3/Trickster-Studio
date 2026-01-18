import gleam/bit_array
import gleam/dict.{type Dict}
import gleam/list
import gleam/option
import gleam/pair
import gleam/result.{map}
import ieee_float.{type IEEEFloat}
import trickster_studio/error.{type TricksterStudioError, Todo}
import trickster_studio/identifier.{type Identifier}
import trickster_studio/pattern.{type Pattern}
import trickster_studio/serde
import vec/vec3.{type Vec3}

pub type Fragment {
  SpellPartFragment(SpellPart)
  PatternGlyphFragment(Pattern)
  PatternLiteralFragment(Pattern)
  NumberFragment(IEEEFloat)
  BlockTypeFragment(Identifier)
  BooleanFragment(Bool)
  DimensionFragment(Identifier)
  EntityFragment(uuid: String, name: String)
  EntityTypeFragment(Identifier)
  ItemTypeFragment(Identifier)
  ListFragment(List(Fragment))
  MapFragment(Dict(Fragment, Fragment))
  SlotFragment(slot: Int, source: Source)
  StringFragment(String)
  TypeFragment(Identifier)
  VectorFragment(x: IEEEFloat, y: IEEEFloat, z: IEEEFloat)
  VoidFragment
  ZalgoFragment
}

pub type Source {
  Caster
  BlockPos(x: Int, y: Int, z: Int)
  UUID(String)
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

const entity_type_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "entity_type",
)

const slot_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "slot",
)

const string_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "string",
)

const item_type_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "item_type",
)

const entity_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "entity",
)

const type_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "type",
)

const zalgo_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "zalgo",
)

const vector_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "vector",
)

const block_type_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "block_type",
)

const map_id: identifier.Identifier = identifier.Identifier("trickster", "map")

const dimension_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "dimension",
)

const void_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "void",
)

const list_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "list",
)

const boolean_id: identifier.Identifier = identifier.Identifier(
  "trickster",
  "boolean",
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
      encode_fragment_struct(number_id, serde.encode_float(number))

    BlockTypeFragment(block_type) ->
      encode_fragment_struct(block_type_id, serde.encode_identifier(block_type))

    BooleanFragment(bool) ->
      encode_fragment_struct(boolean_id, serde.encode_boolean(bool))

    DimensionFragment(world) ->
      encode_fragment_struct(dimension_id, serde.encode_identifier(world))

    EntityFragment(uuid:, name:) ->
      encode_fragment_struct(
        entity_id,
        bit_array.append(serde.encode_string(uuid), serde.encode_string(name)),
      )

    EntityTypeFragment(entity_type) ->
      encode_fragment_struct(
        entity_type_id,
        serde.encode_identifier(entity_type),
      )

    ItemTypeFragment(item_type) ->
      encode_fragment_struct(item_type_id, serde.encode_identifier(item_type))

    ListFragment(list) ->
      encode_fragment_struct(
        list_id,
        serde.encode_list(list.map(list, encode_bytes)),
      )

    MapFragment(map) -> {
      let encoded_entries =
        dict.to_list(map)
        |> list.map(fn(entry) {
          bit_array.append(encode_bytes(entry.0), encode_bytes(entry.1))
        })

      encode_fragment_struct(map_id, serde.encode_list(encoded_entries))
    }

    SlotFragment(slot:, source:) ->
      case source {
        Caster -> serde.encode_boolean(False)

        BlockPos(x:, y:, z:) ->
          serde.encode_option(option.Some([x, y, z]), fn(blockpos) {
            bit_array.append(
              //is left
              serde.encode_boolean(True),
              serde.encode_list(list.map(blockpos, serde.encode_int)),
            )
          })

        UUID(uuid) ->
          serde.encode_option(option.Some(uuid), fn(uuid) {
            bit_array.append(
              //is not left
              serde.encode_boolean(False),
              serde.encode_string(uuid),
            )
          })
      }
      |> bit_array.append(serde.encode_int(slot), _)
      |> encode_fragment_struct(slot_id, _)

    StringFragment(string) ->
      encode_fragment_struct(string_id, serde.encode_string(string))

    TypeFragment(typetype) ->
      encode_fragment_struct(type_id, serde.encode_identifier(typetype))

    VectorFragment(x, y, z) ->
      encode_fragment_struct(
        vector_id,
        serde.encode_list(list.map([x, y, z], serde.encode_float)),
      )

    VoidFragment -> serde.encode_identifier(void_id)

    ZalgoFragment -> serde.encode_identifier(zalgo_id)

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
  let instructions = flatten_node(spell_part, []) |> list.reverse |> echo

  serde.encode_boolean(True)
  |> bit_array.append(
    serde.encode_list(list.map(instructions, encode_spell_instruction)),
  )
}

fn encode_fragment_struct(id: Identifier, value: BitArray) -> BitArray {
  serde.encode_identifier(id)
  |> bit_array.append(value)
}

fn flatten_node(
  node: SpellPart,
  instructions: List(SpellInstruction),
) -> List(SpellInstruction) {
  let instructions = [
    FragmentInstruction(node.glyph),
    ExitScopeInstruction,
    ..instructions
  ]

  let instructions_wtih_children =
    list.fold(list.reverse(node.children), instructions, fn(acc, child) {
      flatten_node(child, acc)
    })

  [EnterScopeInstruction, ..instructions_wtih_children]
}

fn encode_spell_instruction(spell_instruction: SpellInstruction) -> BitArray {
  case spell_instruction {
    FragmentInstruction(fragment) ->
      bit_array.concat([
        serde.encode_int(1),
        serde.encode_boolean(True),
        encode_bytes(fragment),
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

    _ -> {
      Error(Todo)
    }
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

    id if id == block_type_id ->
      serde.decode_identifier
      |> serde.map_decoder(BlockTypeFragment)
      |> serde.apply_decoder(bit_array)

    id if id == boolean_id ->
      serde.decode_boolean
      |> serde.map_decoder(BooleanFragment)
      |> serde.apply_decoder(bit_array)

    id if id == dimension_id ->
      serde.decode_identifier
      |> serde.map_decoder(DimensionFragment)
      |> serde.apply_decoder(bit_array)

    id if id == entity_id -> {
      use #(uuid, bit_array) <- result.try(serde.decode_string(bit_array))
      use #(name, bit_array) <- result.map(serde.decode_string(bit_array))

      #(EntityFragment(name: name, uuid: uuid), bit_array)
    }

    id if id == entity_type_id ->
      serde.decode_identifier
      |> serde.map_decoder(EntityTypeFragment)
      |> serde.apply_decoder(bit_array)

    id if id == item_type_id ->
      serde.decode_identifier
      |> serde.map_decoder(ItemTypeFragment)
      |> serde.apply_decoder(bit_array)

    id if id == list_id ->
      serde.list_of(decode_bytes)
      |> serde.map_decoder(ListFragment)
      |> serde.apply_decoder(bit_array)

    id if id == map_id -> {
      let entry_decoder = fn(bit_array) {
        use #(key, bit_array) <- result.try(decode_bytes(bit_array))
        use #(value, bit_array) <- result.map(decode_bytes(bit_array))

        #(#(key, value), bit_array)
      }

      serde.list_of(entry_decoder)
      |> serde.map_decoder(dict.from_list)
      |> serde.map_decoder(MapFragment)
      |> serde.apply_decoder(bit_array)
    }

    id if id == slot_id -> {
      use #(slot, bit_array) <- result.try(serde.decode_int(bit_array))
      use #(present, bit_array) <- result.try(serde.decode_boolean(bit_array))

      let uuid_source_decoder = fn(bit_array) {
        use #(uuid, bit_array) <- result.map(serde.decode_string(bit_array))

        #(SlotFragment(slot, UUID(uuid)), bit_array)
      }

      let blockpos_source_decoder = fn(bit_array) {
        use #(coordinates, bit_array) <- result.try(serde.list_of(
          serde.decode_int,
        )(bit_array))

        case coordinates {
          [x, y, z] -> Ok(#(SlotFragment(slot, BlockPos(x, y, z)), bit_array))
          _ -> {
            Error(Todo)
          }
        }
      }

      case present {
        False -> Ok(#(SlotFragment(slot: slot, source: Caster), bit_array))
        True -> {
          use #(is_left, bit_array) <- result.try(serde.decode_boolean(
            bit_array,
          ))
          case is_left {
            True -> blockpos_source_decoder(bit_array)
            False -> uuid_source_decoder(bit_array)
          }
        }
      }
    }

    id if id == string_id ->
      serde.decode_string
      |> serde.map_decoder(StringFragment)
      |> serde.apply_decoder(bit_array)

    id if id == type_id ->
      serde.decode_identifier
      |> serde.map_decoder(TypeFragment)
      |> serde.apply_decoder(bit_array)

    id if id == vector_id ->
      serde.list_of(serde.decode_float)
      |> serde.flatmap_decoder(fn(coords) {
        case coords {
          [x, y, z] -> Ok(VectorFragment(x, y, z))
          _ -> {
            Error(Todo)
          }
        }
      })
      |> serde.apply_decoder(bit_array)

    id if id == void_id -> Ok(#(VoidFragment, bit_array))

    id if id == zalgo_id -> Ok(#(ZalgoFragment, bit_array))

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
        fn(bit_array) {
          // withAlternative flag
          use #(_, bit_array) <- result.try(serde.decode_boolean(bit_array))
          serde.list_of(spell_instruction_from_bytes)(bit_array)
        }
        |> serde.map_decoder(unflatten)
        |> serde.list_of

      use #(glyph, bit_array) <- result.try(decode_bytes(bit_array))
      use #(children, bit_array) <- result.map(children_decoder(bit_array))

      #(SpellPartFragment(SpellPart(glyph, children)), bit_array)
    }

    _ -> {
      Error(Todo)
    }
  }
}

fn spell_instruction_from_bytes(
  bit_array: BitArray,
) -> Result(#(SpellInstruction, BitArray), TricksterStudioError) {
  use #(id, bit_array) <- result.try(serde.decode_int(bit_array))
  use #(present, bit_array) <- result.try(serde.decode_boolean(bit_array))

  case id, present {
    1, True ->
      result.map(decode_bytes(bit_array), pair.map_first(_, FragmentInstruction))
    2, False -> Ok(EnterScopeInstruction) |> result.map(pair.new(_, bit_array))
    3, False -> Ok(ExitScopeInstruction) |> result.map(pair.new(_, bit_array))
    _, _ -> {
      Error(Todo)
    }
  }
}

fn unflatten(instructions: List(SpellInstruction)) -> SpellPart {
  let stack = []
  let root = option.None

  let assert option.Some(result) =
    list.fold(list.reverse(instructions), #(stack, root), fn(args, instr) {
      let #(stack, root) = args

      case instr {
        EnterScopeInstruction ->
          // push a new, empty node frame
          #([#(option.None, []), ..stack], root)

        FragmentInstruction(glyph) ->
          // assign glyph to the top frame
          case stack {
            [#(_, children), ..rest] -> #(
              [#(option.Some(glyph), children), ..rest],
              root,
            )
            _ -> panic as "Fragment without scope"
          }

        ExitScopeInstruction ->
          case stack {
            [#(option.Some(glyph), children), ..rest] -> {
              let node = SpellPart(glyph, list.reverse(children))

              case rest {
                [] -> #([], option.Some(node))

                [#(g, parent_children), ..rest2] -> #(
                  [#(g, [node, ..parent_children]), ..rest2],
                  root,
                )
              }
            }

            _ -> panic as "Exit without complete node"
          }
      }
    })
    |> pair.second

  result
}
