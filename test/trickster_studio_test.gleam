import gleam/dict
import gleam/list
import gleam/pair
import gleeunit
import ieee_float
import trickster_studio/fragment
import trickster_studio/spell_tree_map

pub fn main() -> Nil {
  gleeunit.main()
}

// gleeunit test functions end in `_test`
pub fn hello_world_test() {
  //  {
  //   
  //   |> bit_array.append(bit_array.from_string("trickster:number"))
  // }
  // let x =
  //    <<
  //     4,
  //     20,
  //     116,
  //     114,
  //     105,
  //     99,
  //     107,
  //     115,
  //     116,
  //     101,
  //     114,
  //     58,
  //     115,
  //     112,
  //     101,
  //     108,
  //     108,
  //     95,
  //     112,
  //     97,
  //     114,
  //     116,
  //     16,
  //     116,
  //     114,
  //     105,
  //     99,
  //     107,
  //     115,
  //     116,
  //     101,
  //     114,
  //     58,
  //     110,
  //     117,
  //     109,
  //     98,
  //     101,
  //     114,
  //     63,
  //     -16,
  //     0,
  //     0,
  //     0,
  //     0,
  //     0,
  //     0,
  //     0,
  //   >>

  // fragment.from_bytes(x)
  // |> 
  // |> result.map(fn(a) { fragment.to_bytes(a) })
  // |> 
  // |> result.try(fn(a) { fragment.from_bytes(a) })
  // |> 

  // let str = ""
  // assert str
  //   |> fragment.from_base64
  //   |> echo
  //   |> result.map(fn(x) { fragment.to_base64(x) })
  //   == Ok(str)
  Ok(Nil)
}

pub fn to_spell_tree_map_test() {
  let spell_part =
    fragment.SpellPart(fragment.NumberFragment(ieee_float.finite(2.0)), [
      fragment.SpellPart(
        fragment.SpellPartFragment(
          fragment.SpellPart(fragment.VoidFragment, [
            fragment.SpellPart(fragment.BooleanFragment(True), []),
          ]),
        ),
        [],
      ),
      fragment.SpellPart(fragment.ZalgoFragment, []),
    ])

  let expected_map =
    [
      #(0, [#([], fragment.NumberFragment(ieee_float.finite(2.0)))]),
      #(1, [#([2], fragment.ZalgoFragment)]),
      #(2, [#([0, 1], fragment.VoidFragment)]),
      #(3, [#([1, 0, 1], fragment.BooleanFragment(True))]),
    ]
    |> list.map(pair.map_second(_, dict.from_list))
    |> dict.from_list

  let result =
    spell_tree_map.to_spell_tree_depth_map(spell_part)
    |> spell_tree_map.entries
    |> dict.from_list

  assert result == expected_map
}

pub fn to_spell_part_test() {
  let expected_spell_part =
    fragment.SpellPart(fragment.NumberFragment(ieee_float.finite(2.0)), [
      fragment.SpellPart(
        fragment.SpellPartFragment(
          fragment.SpellPart(fragment.VoidFragment, [
            fragment.SpellPart(fragment.BooleanFragment(True), []),
          ]),
        ),
        [],
      ),
      fragment.SpellPart(fragment.ZalgoFragment, []),
    ])

  let result =
    spell_tree_map.to_spell_tree_depth_map(expected_spell_part)
    |> spell_tree_map.to_spell_part

  assert result == Ok(expected_spell_part)
}
