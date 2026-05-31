import gleam/int
import gleam/result
import gleam/string

pub type UUID {
  UUID(uuid_most_1: Int, uuid_most_2: Int, uuid_least_1: Int, uuid_least_2: Int)
}

pub fn from_string(string: String) -> Result(UUID, Nil) {
  todo
}

pub fn to_string(uuid: UUID) -> String {
  let UUID(m1, m2, l1, l2) = uuid

  let a = hex_pad(m1, 8)
  let b = hex_pad(m2, 8)
  let c = hex_pad(l1, 8)
  let d = hex_pad(l2, 8)

  string.join(
    [
      a,
      string.slice(b, 0, 4),
      string.slice(b, 4, 8),
      string.slice(c, 0, 4),
      string.slice(c, 4, 8) <> d,
    ],
    "-",
  )
}

fn hex_pad(n: Int, width: Int) -> String {
  int.to_base_string(n, 16)
  |> result.unwrap("")
  |> string.pad_start(to: width, with: "0")
}
