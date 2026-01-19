import gleam/dict.{type Dict}
import gleam/list
import gleam/option
import gleam/pair
import gleam/result
import trickster_studio/fragment

/// A semi-flattened Representation of a SpellPart.
/// Stored as a map of depth -> map of address -> glyph
/// 
/// The depth is the length of the address.
/// An address is a string of non-negative integers
/// with the node as the first element, then its parent, then its parent's parent...
/// 
/// A 0 represents that the node is an inner circle. 
/// Positive integers represent the node's index in its parent's list of children.
/// A glyph can be any fragment other than a SpellPart.
pub opaque type SpellTreeDepthMap {
  SpellTreeMap(data: MapData)
}

/// Glyph and number of siblings (including itself) per parent (ordered same as address)
pub type Node {
  Node(fragment: fragment.Fragment, sibling_count_stack: List(Int))
}

type MapData =
  Dict(Int, Dict(List(Int), Node))

pub fn entries(
  spell_tree_depth_map map: SpellTreeDepthMap,
) -> List(#(Int, Dict(List(Int), Node))) {
  dict.to_list(map.data)
}

pub fn to_spell_tree_depth_map(
  spell_part: fragment.SpellPart,
) -> SpellTreeDepthMap {
  to_spell_tree_depth_map_rec(spell_part, [], [], dict.from_list([]))
  |> SpellTreeMap
}

fn to_spell_tree_depth_map_rec(
  spell_part: fragment.SpellPart,
  address: List(Int),
  sibling_count_stack: List(Int),
  map: Dict(Int, Dict(List(Int), Node)),
) -> Dict(Int, Dict(List(Int), Node)) {
  let depth = list.length(address)
  let child_count = list.length(spell_part.children)

  let map_with_this_node = case spell_part.glyph {
    fragment.SpellPartFragment(inner_circle) ->
      to_spell_tree_depth_map_rec(
        inner_circle,
        [0, ..address],
        [0, ..sibling_count_stack],
        map,
      )

    glyph ->
      dict.upsert(map, depth, fn(val) {
        let node = Node(fragment: glyph, sibling_count_stack:)

        case val {
          option.Some(addr_map) -> dict.insert(addr_map, address, node)
          option.None -> dict.from_list([#(address, node)])
        }
      })
  }

  list.index_fold(
    spell_part.children,
    map_with_this_node,
    fn(entries_acc, child, i) {
      to_spell_tree_depth_map_rec(
        child,
        [i + 1, ..address],
        [child_count, ..sibling_count_stack],
        entries_acc,
      )
    },
  )
}

pub fn to_spell_part(
  spell_tree_depth_map map: SpellTreeDepthMap,
) -> Result(fragment.SpellPart, Nil) {
  to_spell_part_rec(map.data, [], 0)
}

fn to_spell_part_rec(
  map: MapData,
  address: List(Int),
  depth: Int,
) -> Result(fragment.SpellPart, Nil) {
  use glyph <- result.map(get_glyph(map, address, depth))

  let children =
    collect_children(map, address, depth, 1, [])
    |> list.reverse

  fragment.SpellPart(glyph, children)
}

fn get_glyph(
  map: MapData,
  address: List(Int),
  depth: Int,
) -> Result(fragment.Fragment, Nil) {
  use addr_map <- result.try(dict.get(map, depth))
  let potential_glyph = dict.get(addr_map, address)

  case potential_glyph {
    Ok(_) -> potential_glyph |> result.map(fn(x) { x.fragment })
    Error(_) ->
      to_spell_part_rec(map, [0, ..address], depth + 1)
      |> result.map(fragment.SpellPartFragment)
  }
}

fn collect_children(
  map: MapData,
  parent_address: List(Int),
  parent_depth: Int,
  child_index: Int,
  children: List(fragment.SpellPart),
) -> List(fragment.SpellPart) {
  let potential_child =
    to_spell_part_rec(map, [child_index, ..parent_address], parent_depth + 1)

  case potential_child {
    Ok(child) ->
      collect_children(map, parent_address, parent_depth, child_index + 1, [
        child,
        ..children
      ])
    Error(_) -> children
  }
}
