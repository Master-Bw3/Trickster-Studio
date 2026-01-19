import gleam/bool
import gleam/float
import gleam/int
import gleam/list
import gleam/option
import gleam/result
import gleam/set
import gleam/string
import gleam_community/maths
import ieee_float
import lustre/attribute
import lustre/element
import lustre/element/html
import tiramisu/geometry
import tiramisu/material
import tiramisu/scene
import tiramisu/transform
import trickster_studio/fragment
import trickster_studio/identifier
import trickster_studio/pattern
import vec/vec2
import vec/vec3

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
    fragment.PatternGlyphFragment(pattern) ->
      pattern_glyph(pattern, alpha_getter(size), id)
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

fn pattern_glyph(pattern: pattern.Pattern, alpha: Float, id: String) {
  let assert Ok(sprite_geom) = geometry.plane(size: vec2.Vec2(10.0, 10.0))
  let assert Ok(sprite_mat) =
    material.basic(
      color: 0xffffff,
      transparent: True,
      opacity: alpha,
      map: option.None,
      side: material.FrontSide,
      alpha_test: 0.0,
      depth_write: True,
    )

  let pattern_points =
    pattern.entries
    |> list.flat_map(fn(entry) { [entry.p1, entry.p2] })
    |> set.from_list

  let dots =
    list.filter_map(list.range(0, 8), fn(point) {
      case set.contains(pattern_points, point) {
        False -> Error(Nil)
        True -> {
          let position = get_pattern_dot_position(point)

          Ok(scene.mesh(
            id: id <> int.to_string(point),
            geometry: sprite_geom,
            material: sprite_mat,
            transform: transform.at(position),
            physics: option.None,
          ))
        }
      }
    })

  let lines =
    list.map(pattern.entries, fn(entry) {
      let pos_1 = get_pattern_dot_position(entry.p1)
      let pos_2 = get_pattern_dot_position(entry.p2)

      let dx = pos_2.x -. pos_1.x
      let dy = pos_2.y -. pos_1.y

      let angle = maths.atan2(dy, dx)
      let position =
        vec3.Vec3(
          { pos_1.x +. pos_2.x } /. 2.0,
          { pos_1.y +. pos_2.y } /. 2.0,
          0.0,
        )
      let length = distance(pos_1.x, pos_1.y, pos_2.x, pos_2.y) *. 0.6
      let assert Ok(line_geom) = geometry.plane(size: vec2.Vec2(length, 5.0))

      let transform =
        transform.at(position)
        |> transform.rotate_z(angle)

      scene.mesh(
        id: id <> int.to_string(entry.p1) <> int.to_string(entry.p2),
        geometry: line_geom,
        material: sprite_mat,
        transform:,
        physics: option.None,
      )
    })

  scene.empty(id, transform.identity, list.append(dots, lines))
}

fn distance(x1: Float, y1: Float, x2: Float, y2: Float) {
  float.square_root(
    { x2 -. x1 } *. { x2 -. x1 } +. { y2 -. y1 } *. { y2 -. y1 },
  )
  |> result.unwrap(0.0)
}

fn get_pattern_dot_position(i: Int) {
  let x_sign = int.to_float(i % 3 - 1)
  let y_sign = int.to_float(i / 3 - 1)

  let #(x_sign, y_sign) = case x_sign != 0.0 && y_sign != 0.0 {
    True -> #(x_sign *. 0.7, y_sign *. 0.7)
    False -> #(x_sign, y_sign)
  }

  vec3.Vec3(x_sign *. 100.0, y_sign *. -100.0, 0.0)
}
