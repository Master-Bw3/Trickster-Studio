import gleam/bool
import gleam/dict
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
import trickster_studio/spell_tree_map
import vec/vec2
import vec/vec3

import trickster_studio/fragment
import trickster_studio/identifier
import trickster_studio/pattern

pub const node_render_depth = 8

pub type Camera {
  Camera(zoom: Float, pan_x: Float, pan_y: Float)
}

pub fn spell_circle_widget(
  spell: spell_tree_map.SpellTreeDepthMap,
  editor_id: String,
  circle_texture: option.Option(savoiardi.Texture),
  pattern_literal_texture: option.Option(savoiardi.Texture),
  transform: transform.Transform,
  depth: Int,
  alpha_getter: fn(Float) -> Float,
  text_size_getter: fn(Float) -> Float,
) -> scene.Node {
  use <- bool.guard(
    option.is_none(circle_texture),
    scene.empty("spell circle", transform.identity, []),
  )

  spell
  |> spell_tree_map.entries
  |> list.filter(fn(entry) {
    entry.0 >= depth && entry.0 <= depth + node_render_depth
  })
  |> list.flat_map(fn(x) { pair.second(x) |> dict.to_list })
  |> place_circles(
    editor_id,
    circle_texture,
    pattern_literal_texture,
    alpha_getter,
    text_size_getter,
    depth,
  )
  |> scene.empty(editor_id, transform, _)
}

fn place_circles(
  nodes: List(#(List(Int), spell_tree_map.Node)),
  editor_id: String,
  circle_texture: option.Option(savoiardi.Texture),
  pattern_literal_texture: option.Option(savoiardi.Texture),
  alpha_getter: fn(Float) -> Float,
  text_size_getter: fn(Float) -> Float,
  view_depth: Int,
) -> List(scene.Node) {
  let assert Ok(sprite_geom) = geometry.plane(size: vec2.Vec2(500.0, 500.0))
  let sprite_mat = fn(opacity) {
    let assert Ok(mat) =
      material.basic(
        color: 0xffffff,
        transparent: True,
        opacity:,
        map: circle_texture,
        side: material.FrontSide,
        alpha_test: 0.0,
        depth_write: True,
      )

    mat
  }

  let assert Ok(divider_geom) = geometry.plane(size: vec2.Vec2(15.0, 80.0))
  let divider_mat = fn(opacity) {
    let assert Ok(mat) =
      material.basic(
        color: 0x7F7FFF,
        transparent: True,
        opacity:,
        map: option.None,
        side: material.FrontSide,
        alpha_test: 0.0,
        depth_write: True,
      )

    mat
  }

  let children =
    list.map(nodes, fn(node) {
      let #(
        address,
        spell_tree_map.Node(fragment:, sibling_count_stack:, child_count:),
      ) = node
      let #(transform, angle) =
        calculate_transform(address, sibling_count_stack)
      let size = transform.scale(transform).x
      let address_string =
        list.fold(address, "", fn(acc, i) { acc <> int.to_string(i) <> ", " })

      let glyph = case fragment {
        option.Some(fragment) -> [
          render_fragment(
            fragment,
            editor_id <> "glyph[" <> address_string <> "]",
            size,
            alpha_getter,
            text_size_getter,
            circle_texture,
            pattern_literal_texture,
            view_depth,
            list.length(address),
          ),
        ]
        option.None -> []
      }

      let divider = case child_count {
        0 -> []

        _ -> {
          let new_angle =
            { angle +. maths.pi() /. 2.0 }
            +. { 2.0 *. maths.pi() }
            /. int.to_float(child_count)
            *. 0.5
            -. { maths.pi() /. 2.0 }

          let x = maths.cos(new_angle) *. 235.0
          let y = maths.sin(new_angle) *. 235.0

          let divider_transform =
            transform.at(vec3.Vec3(x, y, 0.0))
            |> transform.rotate_z(new_angle -. maths.pi() /. 2.0)

          [
            scene.mesh(
              id: editor_id <> "divider[" <> address_string <> "]",
              geometry: divider_geom,
              material: divider_mat(alpha_getter(size) /. 4.0),
              transform: divider_transform,
              physics: option.None,
            ),
          ]
        }
      }

      let circle = [
        scene.mesh(
          id: editor_id <> "circle[" <> address_string <> "]",
          geometry: sprite_geom,
          material: sprite_mat(alpha_getter(size)),
          transform: transform.identity,
          physics: option.None,
        ),
      ]

      scene.empty(
        editor_id <> "container[" <> address_string <> "]",
        transform,
        [
          circle,
          glyph,
          divider,
        ]
          |> list.flatten,
      )
    })

  children
}

pub fn calculate_transform(
  address: List(Int),
  sibling_count_stack: List(Int),
) -> #(transform.Transform, Float) {
  calculate_transform_rec(
    list.reverse(address),
    list.reverse(sibling_count_stack),
    maths.pi() /. 2.0,
    transform.identity,
  )
}

fn calculate_transform_rec(
  address: List(Int),
  sibling_count_stack: List(Int),
  angle: Float,
  transform_acc: transform.Transform,
) -> #(transform.Transform, Float) {
  case address, sibling_count_stack {
    [index, ..rest_address], [sibling_count, ..rest_siblings] -> {
      let #(transform, new_angle) = case index {
        0 -> #(transform.scale_uniform(transform.identity, 0.4), angle)

        _ -> {
          let new_angle =
            angle
            -. { 2.0 *. maths.pi() }
            /. int.to_float(sibling_count)
            *. int.to_float(index - 1)

          let x = maths.cos(new_angle) *. 0.5
          let y = maths.sin(new_angle) *. 0.5
          let size =
            float.min(0.5, 1.0 /. { int.to_float(sibling_count + 1) /. 2.0 })

          let placement_scale = transform.scale(transform_acc).x *. 0.5

          let transform =
            transform.at(vec3.Vec3(
              1000.0 *. placement_scale *. x,
              1000.0 *. placement_scale *. y,
              0.0,
            ))
            |> transform.scale_uniform(size)

          #(transform, new_angle +. maths.pi() /. 2.0)
        }
      }

      calculate_transform_rec(
        rest_address,
        rest_siblings,
        new_angle,
        transform.compose(transform_acc, transform),
      )
    }

    // actually [], []
    _, _ -> #(transform_acc, angle)
  }
}

// ****************** //
// Fragment Renderers //
// ****************** //

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
  circle_texture: option.Option(savoiardi.Texture),
  pattern_literal_texture: option.Option(savoiardi.Texture),
  view_depth: Int,
  fragment_depth: Int,
) -> scene.Node {
  let text_renderer = fn(lines) {
    text(lines, id, size, alpha_getter, text_size_getter)
  }

  case fragment {
    fragment.PatternGlyphFragment(pattern) ->
      render_pattern_glyph(pattern, alpha_getter(size), id)
    fragment.PatternLiteralFragment(pattern) ->
      render_pattern_literal(
        pattern,
        alpha_getter(size),
        id,
        pattern_literal_texture,
      )
    fragment.NumberFragment(num) -> render_number(num, text_renderer)
    fragment.BlockTypeFragment(id) -> render_block_type(id, text_renderer)
    fragment.BooleanFragment(bool) -> render_boolean(bool, text_renderer)
    fragment.DimensionFragment(id) -> render_dimension(id, text_renderer)
    fragment.EntityFragment(uuid: _, name:) ->
      render_entity(name, text_renderer)
    fragment.EntityTypeFragment(id) -> render_entity_type(id, text_renderer)
    fragment.ItemTypeFragment(id) -> render_item_type(id, text_renderer)
    fragment.SlotFragment(slot:, source:) ->
      render_slot(slot, source, text_renderer)
    fragment.StringFragment(string) ->
      render_string_fragment(string, text_renderer)
    fragment.TypeFragment(id) -> render_type_fragment(id, text_renderer)
    fragment.VectorFragment(x:, y:, z:) -> render_vector(x, y, z, text_renderer)
    fragment.VoidFragment -> render_void(text_renderer)
    fragment.ZalgoFragment -> render_zalgo(text_renderer)
    fragment.ListFragment(list) ->
      render_list(
        list,
        id,
        size,
        alpha_getter,
        text_size_getter,
        circle_texture,
        pattern_literal_texture,
        view_depth,
        fragment_depth,
      )
    fragment.MapFragment(map) ->
      render_map(
        map,
        id,
        size,
        alpha_getter,
        text_size_getter,
        circle_texture,
        pattern_literal_texture,
        view_depth,
        fragment_depth,
      )
    fragment.SpellPartFragment(spell_part) ->
      spell_circle_widget(
        spell_tree_map.to_spell_tree_depth_map(spell_part),
        id,
        circle_texture,
        pattern_literal_texture,
        transform.identity |> transform.scale_uniform(0.2),
        int.min(0, view_depth - fragment_depth),
        fn(inner_size) {
          // todo: make circles get darker with depth
          float.min(alpha_getter(inner_size), alpha_getter(size))
        },
        fn(size) { text_size_getter(size) *. 0.2 },
      )
  }
}

fn render_number(
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

fn render_block_type(
  id: identifier.Identifier,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#44aa33"

  identifier.to_string(id)
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn render_boolean(bool: Bool, text_renderer: TextRenderer) -> scene.Node {
  let color = "#aa3355"

  bool.to_string(bool)
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn render_dimension(
  id: identifier.Identifier,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#dd55bb"

  identifier.to_string(id)
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn render_entity(name: String, text_renderer: TextRenderer) -> scene.Node {
  let color = "#8877bb"

  text_renderer([#(name, color)])
}

fn render_entity_type(
  id: identifier.Identifier,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#8877bb"

  identifier.to_string(id)
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn render_item_type(
  id: identifier.Identifier,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#2266aa"

  identifier.to_string(id)
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn render_string_fragment(
  string: String,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#aabb77"

  text_renderer([#("\"" <> string <> "\"", color)])
}

fn render_type_fragment(
  id: identifier.Identifier,
  text_renderer: TextRenderer,
) -> scene.Node {
  let color = "#66cc00"

  identifier.to_string(id)
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn render_void(text_renderer: TextRenderer) -> scene.Node {
  let color = "#4400aa"

  text_renderer([#("void", color)])
}

fn render_zalgo(text_renderer: TextRenderer) -> scene.Node {
  let color = "#444444"

  text_renderer([#("zalgo", color)])
}

fn render_vector(
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

  let str = "(" <> to_str(x) <> ", " <> to_str(y) <> ", " <> to_str(z) <> ")"

  text_renderer([#(str, color)])
}

fn render_slot(
  slot: Int,
  source: fragment.Source,
  text_renderer: fn(List(#(String, String))) -> scene.Node,
) -> scene.Node {
  let color = "#77aaee"

  let source_string = case source {
    fragment.Caster -> "caster"
    fragment.BlockPos(x:, y:, z:) -> {
      int.to_string(x)
      <> ", "
      <> int.to_string(y)
      <> ", "
      <> int.to_string(z)
      <> ")"
    }

    fragment.UUID(uuid) -> uuid
  }

  { "slot " <> int.to_string(slot) <> " at " <> source_string }
  |> pair.new(color)
  |> list.wrap
  |> text_renderer()
}

fn render_list(
  fragments: List(fragment.Fragment),
  id: String,
  size: Float,
  alpha_getter: fn(Float) -> Float,
  text_size_getter: fn(Float) -> Float,
  circle_texture: option.Option(savoiardi.Texture),
  pattern_literal_texture: option.Option(savoiardi.Texture),
  view_depth: Int,
  fragment_depth: Int,
) -> scene.Node {
  let spacing = 200.0
  let scale =
    float.divide(1.5, int.to_float(list.length(fragments)))
    |> result.unwrap(1.0)
    |> float.min(1.0)

  let rendered_fragments =
    list.index_map(fragments, fn(fragment, i) {
      let rendered_fragment =
        render_fragment(
          fragment,
          id <> " index " <> int.to_string(i),
          size,
          alpha_getter,
          fn(size) { text_size_getter(size) *. scale },
          circle_texture,
          pattern_literal_texture,
          view_depth,
          fragment_depth,
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
  let height = echo float.max(60.0, height -. 60.0)

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

fn render_map(
  fragment_map: dict.Dict(fragment.Fragment, fragment.Fragment),
  id: String,
  size: Float,
  alpha_getter: fn(Float) -> Float,
  text_size_getter: fn(Float) -> Float,
  circle_texture: option.Option(savoiardi.Texture),
  pattern_literal_texture: option.Option(savoiardi.Texture),
  view_depth: Int,
  fragment_depth: Int,
) -> scene.Node {
  let fragments = dict.to_list(fragment_map)

  let spacing = 200.0
  let scale = float.min(1.0, 0.8 /. int.to_float(list.length(fragments)))

  let arrow_renderer = fn(id) {
    text([#("->", "#ffffff")], id, size, alpha_getter, fn(size) {
      text_size_getter(size /. 5.0)
    })
  }

  let rendered_fragments =
    list.index_map(fragments, fn(entry, i) {
      let rendered_key =
        render_fragment(
          entry.0,
          id <> " key index " <> int.to_string(i),
          size,
          alpha_getter,
          fn(size) { text_size_getter(size) *. scale },
          circle_texture,
          pattern_literal_texture,
          view_depth,
          fragment_depth,
        )

      let rendered_value =
        render_fragment(
          entry.1,
          id <> " value index " <> int.to_string(i),
          size,
          alpha_getter,
          fn(size) { text_size_getter(size) *. scale },
          circle_texture,
          pattern_literal_texture,
          view_depth,
          fragment_depth,
        )

      scene.empty(
        id <> "LP" <> int.to_string(i),
        transform.at(vec3.Vec3(0.0, int.to_float(i) *. spacing, 0.0)),
        [
          scene.empty(
            id <> "KP" <> int.to_string(i),
            transform.at(vec3.Vec3(-1.0 *. spacing, 0.0, 0.0)),
            [rendered_key],
          ),
          arrow_renderer(id <> "arrow" <> int.to_string(i)),
          scene.empty(
            id <> "VP" <> int.to_string(i),
            transform.at(vec3.Vec3(1.0 *. spacing, 0.0, 0.0)),
            [rendered_value],
          ),
        ],
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
      transform.at(vec3.Vec3(-350.0 *. scale, 0.0, 0.0))
        |> transform.scale_uniform(scale),
    )
  let right_bracket =
    tall_bracket(
      height,
      id <> "right_b",
      alpha_getter(size),
      transform.at(vec3.Vec3(350.0 *. scale, 0.0, 0.0))
        |> transform.scale_uniform(scale)
        |> transform.rotate_z(maths.pi()),
    )

  scene.empty(id, transform.identity, [
    left_bracket,
    scene.empty(id <> "inner", transform, rendered_fragments),
    right_bracket,
  ])
}

fn render_pattern_literal(
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
    render_pattern_glyph(pattern, alpha, id <> "pattern"),
    sprite,
  ])
}

fn render_pattern_glyph(
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
      let length = distance(pos_1.x, pos_1.y, pos_2.x, pos_2.y) -. 30.0
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
