import gleam/bool
import gleam/float
import gleam/int
import gleam/list
import gleam/option
import gleam/pair
import gleam/result
import gleam/set
import gleam/string
import gleam_community/maths
import ieee_float
import lustre/attribute
import lustre/element
import lustre/element/html
import savoiardi
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
  fn(List(#(String, String))) -> scene.Node

fn text(
  lines: List(#(String, String)),
  id: String,
  size: Float,
  alpha_getter: fn(Float) -> Float,
  text_size_getter: fn(Float) -> Float,
) -> scene.Node {
  let div = fn(line) {
    let #(text, color) = line

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
  }

  let rendered_lines = list.map(lines, div)
  let container = html.div([], rendered_lines)

  scene.css2d(
    id: id <> " line ",
    html: element.to_string(container),
    transform: transform.identity,
  )
}

pub fn render_fragment(
  fragment: fragment.Fragment,
  id: String,
  size: Float,
  alpha_getter: fn(Float) -> Float,
  text_size_getter: fn(Float) -> Float,
  pattern_literal_texture: option.Option(savoiardi.Texture),
) -> scene.Node {
  let text_renderer = fn(lines) {
    text(lines, id, size, alpha_getter, text_size_getter)
  }

  case fragment {
    fragment.SpellPartFragment(_) -> scene.empty(id, transform.identity, [])
    fragment.PatternGlyphFragment(pattern) ->
      pattern_glyph(pattern, alpha_getter(size), id)
    fragment.PatternLiteralFragment(pattern) ->
      pattern_literal(pattern, alpha_getter(size), id, pattern_literal_texture)
    fragment.NumberFragment(num) -> number(num, text_renderer)
    fragment.BlockTypeFragment(id) -> block_type(id, text_renderer)
    fragment.BooleanFragment(bool) -> boolean(bool, text_renderer)
    fragment.DimensionFragment(id) -> dimension(id, text_renderer)
    fragment.EntityFragment(uuid: _, name:) -> entity(name, text_renderer)
    fragment.EntityTypeFragment(id) -> entity_type(id, text_renderer)
    fragment.ItemTypeFragment(id) -> item_type(id, text_renderer)
    fragment.MapFragment(_) -> todo
    fragment.SlotFragment(slot:, source:) -> todo
    fragment.StringFragment(string) -> string_fragment(string, text_renderer)
    fragment.TypeFragment(id) -> type_fragment(id, text_renderer)
    fragment.VectorFragment(x:, y:, z:) -> vector(x, y, z, text_renderer)
    fragment.VoidFragment -> void(text_renderer)
    fragment.ZalgoFragment -> zalgo(text_renderer)
    fragment.ListFragment(list) ->
      list_fragment(
        list,
        id,
        size,
        alpha_getter,
        text_size_getter,
        pattern_literal_texture,
      )
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
      |> pair.new(color)
      |> list.wrap
      |> text_renderer()

    Error(_) -> text_renderer([#("NaN", color)])
  }
}

fn block_type(
  id: identifier.Identifier,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#44aa33"

  identifier.to_string(id)
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn boolean(bool: Bool, text_renderer: TextRenderer) -> scene.Node {
  let color = "#aa3355"

  bool.to_string(bool)
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn dimension(
  id: identifier.Identifier,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#dd55bb"

  identifier.to_string(id)
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn entity(name: String, text_renderer: TextRenderer) -> scene.Node {
  let color = "#8877bb"

  text_renderer([#(name, color)])
}

fn entity_type(
  id: identifier.Identifier,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#8877bb"

  identifier.to_string(id)
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn item_type(
  id: identifier.Identifier,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#2266aa"

  identifier.to_string(id)
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn string_fragment(string: String, text_renderer: TextRenderer) -> scene.Node {
  let color = "#aabb77"

  text_renderer([#("\"" <> string <> "\"", color)])
}

fn type_fragment(
  id: identifier.Identifier,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#66cc00"

  identifier.to_string(id)
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn void(text_renderer: TextRenderer) -> scene.Node {
  let color = "#4400aa"

  text_renderer([#("void", color)])
}

fn zalgo(text_renderer: TextRenderer) -> scene.Node {
  let color = "#444444"

  text_renderer([#("zalgo", color)])
}

fn vector(
  x: ieee_float.IEEEFloat,
  y: ieee_float.IEEEFloat,
  z: ieee_float.IEEEFloat,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#aa7711"

  let to_str = fn(number) {
    case ieee_float.to_finite(number) {
      Ok(num) ->
        num
        |> float.to_precision(2)
        |> float.to_string()

      Error(_) -> "NaN"
    }
  }

  let str =
    "(" <> "," <> to_str(x) <> "," <> to_str(y) <> "," <> to_str(z) <> ")"

  text_renderer([#(str, color)])
}

fn list_fragment(
  fragments: List(fragment.Fragment),
  id: String,
  size: Float,
  alpha_getter: fn(Float) -> Float,
  text_size_getter: fn(Float) -> Float,
  pattern_literal_texture: option.Option(savoiardi.Texture),
) -> scene.Node {
  let spacing = 200.0
  let scale = float.min(1.0, 1.5 /. int.to_float(list.length(fragments)))
  let rendered_fragments =
    list.index_map(fragments, fn(fragment, i) {
      let rendered_fragment =
        render_fragment(
          fragment,
          id <> " index " <> int.to_string(i),
          size,
          alpha_getter,
          fn(size) { text_size_getter(size) *. scale },
          pattern_literal_texture,
        )

      scene.empty(
        id <> "LP" <> int.to_string(i),
        transform.at(vec3.Vec3(0.0, int.to_float(i) *. spacing, 0.0)),
        [rendered_fragment],
      )
    })

  let height = int.to_float(list.length(fragments)) *. spacing
  let centered = {
    0.5 *. { spacing -. height } *. scale
  }

  let transform =
    transform.at(vec3.Vec3(0.0, centered, 0.0))
    |> transform.scale_uniform(scale)

  let left_bracket =
    tall_bracket(
      height,
      id <> "left_b",
      alpha_getter(size),
      transform.at(vec3.Vec3(-150.0 *. scale, 0.0, 0.0))
        |> transform.scale_uniform(scale),
    )
  let right_bracket =
    tall_bracket(
      height,
      id <> "right_b",
      alpha_getter(size),
      transform.at(vec3.Vec3(150.0 *. scale, 0.0, 0.0))
        |> transform.scale_uniform(scale)
        |> transform.rotate_z(maths.pi()),
    )

  scene.empty(id, transform.identity, [
    left_bracket,
    scene.empty(id <> "inner", transform, rendered_fragments),
    right_bracket,
  ])
}

fn tall_bracket(
  height: Float,
  id: String,
  alpha: Float,
  transform: transform.Transform,
) {
  let leg_length = 40.0
  let line_width = 20.0
  let height = height -. 60.0

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
  let assert Ok(spine_geom) =
    geometry.plane(size: vec2.Vec2(line_width, height))
  let assert Ok(leg_geom) =
    geometry.plane(size: vec2.Vec2(leg_length, line_width))

  scene.empty(id, transform, [
    scene.mesh(
      id: id <> "leg",
      geometry: leg_geom,
      material: sprite_mat,
      transform: transform.at(vec3.Vec3(
        { leg_length -. line_width } /. 2.0,
        { height +. line_width } /. 2.0,
        0.0,
      )),
      physics: option.None,
    ),
    scene.mesh(
      id: id <> "spine",
      geometry: spine_geom,
      material: sprite_mat,
      transform: transform.identity,
      physics: option.None,
    ),
    scene.mesh(
      id: id <> "hat",
      geometry: leg_geom,
      material: sprite_mat,
      transform: transform.at(vec3.Vec3(
        { leg_length -. line_width } /. 2.0,
        { height +. line_width } /. -2.0,
        0.0,
      )),
      physics: option.None,
    ),
  ])
}

fn pattern_literal(
  pattern: pattern.Pattern,
  alpha: Float,
  id: String,
  pattern_literal_texture: option.Option(savoiardi.Texture),
) -> scene.Node {
  let assert Ok(sprite_geom) = geometry.plane(size: vec2.Vec2(500.0, 500.0))
  let assert Ok(sprite_mat) =
    material.basic(
      color: 0x888888,
      transparent: True,
      opacity: alpha,
      map: pattern_literal_texture,
      side: material.FrontSide,
      alpha_test: 0.0,
      depth_write: True,
    )

  let sprite =
    scene.mesh(
      id: id <> "literal",
      geometry: sprite_geom,
      material: sprite_mat,
      transform: transform.identity,
      physics: option.None,
    )

  scene.empty(id, transform.identity, [
    pattern_glyph(pattern, alpha, id <> "pattern"),
    sprite,
  ])
}

fn pattern_glyph(
  pattern: pattern.Pattern,
  alpha: Float,
  id: String,
) -> scene.Node {
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

fn distance(x1: Float, y1: Float, x2: Float, y2: Float) -> Float {
  float.square_root(
    { x2 -. x1 } *. { x2 -. x1 } +. { y2 -. y1 } *. { y2 -. y1 },
  )
  |> result.unwrap(0.0)
}

fn get_pattern_dot_position(i: Int) -> vec3.Vec3(Float) {
  let x_sign = int.to_float(i % 3 - 1)
  let y_sign = int.to_float(i / 3 - 1)

  let #(x_sign, y_sign) = case x_sign != 0.0 && y_sign != 0.0 {
    True -> #(x_sign *. 0.7, y_sign *. 0.7)
    False -> #(x_sign, y_sign)
  }

  vec3.Vec3(x_sign *. 100.0, y_sign *. -100.0, 0.0)
}
