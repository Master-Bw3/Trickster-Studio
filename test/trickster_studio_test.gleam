import gleam/bit_array
import gleam/string
import gleeunit
import ieee_float
import trickster_studio/fragment

pub fn main() -> Nil {
  gleeunit.main()
}

// gleeunit test functions end in `_test`
pub fn hello_world_test() {
  // echo {
  //   
  //   |> bit_array.append(bit_array.from_string("trickster:number"))
  // }
  let x =
    echo <<
      20, 116, 114, 105, 99, 107, 115, 116, 101, 114, 58, 115, 112, 101, 108,
      108, 95, 112, 97, 114, 116, 17, 116, 114, 105, 99, 107, 115, 116, 101, 114,
      58, 112, 97, 116, 116, 101, 114, 110, 0, 0, 0, -127, 0,
    >>

  // echo {
  //   serde.write_string(string.length(str))
  //   |> bit_array.append(bit_array.from_string(str))
  //   |> bit_array.append(ieee_float.to_bytes_64_be(ieee_float.finite(19.84)))
  // }

  fragment.from_bytes(x)
  |> echo
}
