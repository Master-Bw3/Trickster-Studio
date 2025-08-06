import gleam/result
import gleam/string

pub type Identifier {
  Identifier(namespace: String, path: String)
}

pub fn from_string(string: String) -> Result(Identifier, Nil) {
  use #(namespace, path) <- result.try(string.split_once(string, ":"))

  case
    string.lowercase(namespace) == namespace,
    string.lowercase(path) == path
  {
    True, True -> Ok(Identifier(namespace, path))
    _, _ -> Error(Nil)
  }
}

pub fn to_string(identifier: Identifier) -> String {
  identifier.namespace <> ":" <> identifier.path
}
