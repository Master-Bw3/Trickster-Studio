import gleam/bit_array
import gleam/result
import gleam/string
import gleeunit
import ieee_float
import trickster_studio/fragment
import trickster_studio/pattern
import trickster_studio/serde

pub fn main() -> Nil {
  gleeunit.main()
}

// gleeunit test functions end in `_test`
pub fn hello_world_test() {
  // echo {
  //   
  //   |> bit_array.append(bit_array.from_string("trickster:number"))
  // }
  // let x =
  //   echo <<
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
  // |> echo
  // |> result.map(fn(a) { fragment.to_bytes(a) })
  // |> echo
  // |> result.try(fn(a) { fragment.from_bytes(a) })
  // |> echo

  fragment.SpellPartFragment(
    fragment.SpellPart(fragment.PatternGlyphFragment(pattern.Pattern([])), []),
  )
  |> fragment.to_base64
  |> echo
  |> fragment.from_base64
  |> echo
}
