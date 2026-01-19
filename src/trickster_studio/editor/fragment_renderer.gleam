import gleam/bool
import gleam/float
import gleam/int
import gleam/result
import gleam/string
import ieee_float
import lustre/attribute
import lustre/element
import lustre/element/html
import tiramisu/scene
import tiramisu/transform
import trickster_studio/fragment
import trickster_studio/identifier
import trickster_studio/pattern

type TextRenderer =
  fn(String, String) -> scene.Node

fn text(
  text: String,
  color: String,
  id: String,
  size: Float,
  alpha_getter: fn(Float) -> Float,
  text_size_getter: fn(Float) -> Float,
) -> scene.Node {
  let div =
    html.div(
      [
        attribute.style("color", color),
        attribute.style(
          "font-size",
          float.to_string(
            text_size_getter(size)
            /. { int.to_float(string.length(text)) /. 2.0 },
          )
            <> "px",
        ),
        attribute.style("opacity", float.to_string(alpha_getter(size) *. 2.0)),
        attribute.style("text-wrap", "nowrap"),
        attribute.style("font-family", "slkscr"),
      ],
      [
        html.text(text),
      ],
    )

  scene.css2d(
    id: id,
    html: element.to_string(div),
    transform: transform.identity,
  )
}

pub fn render_fragment(
  fragment: fragment.Fragment,
  id: String,
  size: Float,
  alpha_getter: fn(Float) -> Float,
  text_size_getter: fn(Float) -> Float,
) {
  let text_renderer = fn(string, color) {
    text(string, color, id, size, alpha_getter, text_size_getter)
  }

  case fragment {
    fragment.SpellPartFragment(_) -> scene.empty(id, transform.identity, [])
    fragment.PatternGlyphFragment(_) -> todo
    fragment.PatternLiteralFragment(_) -> todo
    fragment.NumberFragment(num) -> number(num, text_renderer)
    fragment.BlockTypeFragment(id) -> block_type(id, text_renderer)
    fragment.BooleanFragment(bool) -> boolean(bool, text_renderer)
    fragment.DimensionFragment(id) -> dimension(id, text_renderer)
    fragment.EntityFragment(uuid: _, name:) -> entity(name, text_renderer)
    fragment.EntityTypeFragment(_) -> todo
    fragment.ItemTypeFragment(_) -> todo
    fragment.ListFragment(_) -> todo
    fragment.MapFragment(_) -> todo
    fragment.SlotFragment(slot:, source:) -> todo
    fragment.StringFragment(_) -> todo
    fragment.TypeFragment(_) -> todo
    fragment.VectorFragment(x:, y:, z:) -> todo
    fragment.VoidFragment -> todo
    fragment.ZalgoFragment -> todo
  }
}

fn number(
  number: ieee_float.IEEEFloat,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#ddaa00"

  case ieee_float.to_finite(number) {
    Ok(num) ->
      num
      |> float.to_precision(2)
      |> float.to_string()
      |> text_renderer(color)

    Error(_) -> text_renderer("NaN", color)
  }
}

fn block_type(id: identifier.Identifier, text_renderer: TextRenderer) {
  let color = "#44aa33"

  identifier.to_string(id)
  |> text_renderer(color)
}

fn boolean(bool: Bool, text_renderer: TextRenderer) {
  let color = "#aa3355"

  bool.to_string(bool)
  |> text_renderer(color)
}

fn dimension(id: identifier.Identifier, text_renderer: TextRenderer) {
  let color = "#dd55bb"

  identifier.to_string(id)
  |> text_renderer(color)
}

fn entity(name: String, text_renderer: TextRenderer) {
  let color = "#8877bb"

  text_renderer(name, color)
}

fn pattern_glyph(pattern: pattern.Pattern) {
  todo
}
