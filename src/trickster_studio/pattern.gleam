import gleam/bool
import gleam/dict.{type Dict}
import gleam/int
import gleam/list
import gleam/result
import trickster_studio/error.{type TricksterStudioError, Todo}

pub type Pattern {
  Pattern(entries: List(PatternEntry))
}

pub type PatternEntry {
  PatternEntry(p1: Int, p2: Int)
}

pub fn from_int(pattern_int: Int) -> Result(Pattern, TricksterStudioError) {
  list.range(0, 31)
  |> list.filter(fn(i) {
    int.bitwise_and(int.bitwise_shift_right(pattern_int, i), 1) == 1
  })
  |> list.map(lookup_entry)
  |> result.all
  |> result.map(Pattern)
}

pub fn to_int(pattern: Pattern) -> Int {
  echo possible_lines()

  let lines = possible_lines()
  list.index_map(lines, fn(line, i) {
    case list.contains(pattern.entries, line) {
      True -> int.bitwise_shift_left(1, i)
      False -> 0
    }
  })
  |> list.fold(0, fn(x, acc) { int.bitwise_or(x, acc) })
}

pub fn possible_lines() -> List(PatternEntry) {
  list.range(0, 31)
  |> list.filter_map(lookup_entry)
}

pub fn lookup_entry(i: Int) -> Result(PatternEntry, TricksterStudioError) {
  case i {
    00 -> Ok(PatternEntry(0, 1))
    01 -> Ok(PatternEntry(0, 2))
    02 -> Ok(PatternEntry(0, 3))
    03 -> Ok(PatternEntry(0, 4))
    04 -> Ok(PatternEntry(0, 5))
    05 -> Ok(PatternEntry(0, 6))
    06 -> Ok(PatternEntry(0, 7))
    07 -> Ok(PatternEntry(1, 2))
    08 -> Ok(PatternEntry(1, 3))
    09 -> Ok(PatternEntry(1, 4))
    10 -> Ok(PatternEntry(1, 5))
    11 -> Ok(PatternEntry(1, 6))
    12 -> Ok(PatternEntry(1, 8))
    13 -> Ok(PatternEntry(2, 3))
    14 -> Ok(PatternEntry(2, 4))
    15 -> Ok(PatternEntry(2, 5))
    16 -> Ok(PatternEntry(2, 7))
    17 -> Ok(PatternEntry(2, 8))
    18 -> Ok(PatternEntry(3, 4))
    19 -> Ok(PatternEntry(3, 6))
    20 -> Ok(PatternEntry(3, 7))
    21 -> Ok(PatternEntry(3, 8))
    22 -> Ok(PatternEntry(4, 5))
    23 -> Ok(PatternEntry(4, 6))
    24 -> Ok(PatternEntry(4, 7))
    25 -> Ok(PatternEntry(4, 8))
    26 -> Ok(PatternEntry(5, 6))
    27 -> Ok(PatternEntry(5, 7))
    28 -> Ok(PatternEntry(5, 8))
    29 -> Ok(PatternEntry(6, 7))
    30 -> Ok(PatternEntry(6, 8))
    31 -> Ok(PatternEntry(7, 8))
    __ -> {
      Error(Todo)
    }
  }
}
